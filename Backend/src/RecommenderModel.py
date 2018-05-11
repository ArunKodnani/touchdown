import psycopg2
import pandas as pd
import numpy as np
from scipy import sparse
import tarfile
import boto3
from lightfm import LightFM
import json
from boto3.dynamodb.conditions import Key
from decimal import Decimal
import pickle
import os

HOST = "myinstance.crim3pvzpnxc.us-east-1.rds.amazonaws.com"
PORT = 5432
DBNAME = "touchdown"
USER = "Admin2018"
PASSWORD = "Admin2018"
NUM_THREADS = 2
NUM_COMPONENTS = 30
NUM_EPOCHS = 3
ITEM_ALPHA = 1e-6


def Connect():
    global conn, cursor
    conn = psycopg2.connect(host=HOST, user=USER, password=PASSWORD, dbname=DBNAME)
    cursor = conn.cursor()
    return conn, cursor


def CloseConnect(conn, cursor):
    cursor.close()
    conn.close()


def load_dataframe(filename):
    df = pd.read_csv(filename)
    return df


def GetUserPreferencesMatrix(cursor):
    sql_command = "SELECT * FROM UserPreferences"
    cursor.execute(sql_command)
    userPreferencesDf = pd.DataFrame()
    j = 0
    while True:
        row = cursor.fetchone()
        if row == None:
            break

        tempDf = pd.DataFrame({'UserID': row[0], 'PreferenceId': row[1], 'Value': row[2]}, index=[j])
        userPreferencesDf = userPreferencesDf.append(tempDf)
        j = j + 1
    columnTitles = ['UserID', 'PreferenceId', 'Value']
    userPreferencesDf = userPreferencesDf.reindex(columns=columnTitles)
    matrixRow = np.array(userPreferencesDf['UserID'])
    matrixColumn = np.array(userPreferencesDf['PreferenceId'])
    matrixData = np.array(userPreferencesDf['Value'])
    matrix = sparse.coo_matrix((matrixData, (matrixRow, matrixColumn)),
                               shape=(userPreferencesDf['UserID'].max() + 1, userPreferencesDf['PreferenceId'].max() + 1))
    return matrix

def GetUserClickMatrix(cursor):
    sql_command = "SELECT * FROM UserClicks"
    cursor.execute(sql_command)
    userClickDf = pd.DataFrame()
    j = 0
    while True:
        row = cursor.fetchone()
        if row == None:
            break

        tempDf = pd.DataFrame({'UserID': row[0], 'ArticleID': row[1], 'Clicks': row[2]}, index=[j])
        userClickDf = userClickDf.append(tempDf)
        j = j + 1
    columnTitles = ['UserID', 'ArticleID', 'Clicks']
    userClickDf = userClickDf.reindex(columns=columnTitles)
    matrixRow = np.array(userClickDf['UserID'])
    matrixColumn = np.array(userClickDf['ArticleID'])
    matrixData = np.array(userClickDf['Clicks'])
    matrix = sparse.coo_matrix((matrixData, (matrixRow, matrixColumn)),
                               shape=(userClickDf['UserID'].max() + 1, userClickDf['ArticleID'].max() + 1))
    return matrix


def GetDocTopicsMatrix():
    ExtractTopicModelTar()
    data = load_dataframe("doc-topics.csv")
    data.docname = data.docname.map(lambda x: x.replace("Article_List:", ""))
    dataShape = data.shape
    topicMatrixRow = np.array(data['docname'])
    topicMatrixColumn = np.array(data['topic'])
    topicMatrixData = np.array(data['proportion'])
    topicMatrix = sparse.csr_matrix((topicMatrixData, (topicMatrixRow, topicMatrixColumn)),
                                    shape=(dataShape[0], dataShape[0]))
    return topicMatrix


def ExtractTopicModelTar():
    s3 = boto3.client('s3')
    s3.download_file(
        'touchdown-comprehend-bucket',
        'Article_Topic_Models/968468597232-TopicsDetection-42f20eff5d01277938fcbd71b594d25f/output/output.tar.gz', 'output.tar.gz')
    tar = tarfile.open("output.tar.gz")
    tar.extractall()
    tar.close()


def TrainModel(userClickMatrix, topicMatrix, userPreferencesMatrix):
    # Define a new model instance
    model = LightFM(loss='warp',
                    item_alpha=ITEM_ALPHA,
                    no_components=NUM_COMPONENTS)

    # Fit the hybrid model.
    model = model.fit(userClickMatrix,
                      user_features=userPreferencesMatrix,
                      item_features=topicMatrix,
                      epochs=NUM_EPOCHS,
                      num_threads=NUM_THREADS)

    return model


def getUserList(cursor):
    users = []
    sql_command = "SELECT DISTINCT UserId FROM UserClicks"
    cursor.execute(sql_command)
    while True:
        row = cursor.fetchone()
        if row == None:
            break
        users.append(row[0])

    return users


def makeRecommendations(model, userList, topicMatrix, item_ids, userPreferencesMatrix):
    recommendations = {}
    for user in userList:
        recommendation = model.predict(user_ids=user, item_ids=item_ids, item_features=topicMatrix, user_features=userPreferencesMatrix)
        recommendations[user] = recommendation.argsort()[-20:]

    return recommendations


def getItemIds(cursor):
    itemIds = []
    sql_command = "SELECT DISTINCT ArticleId FROM UserClicks"
    cursor.execute(sql_command)
    while True:
        row = cursor.fetchone()
        if row == None:
            break
        itemIds.append(row[0])

    return np.array(itemIds)


def generateResponse(recommendation, table):
    response = {}
    for articleId in recommendation:
        articleDetails = table.query(
            KeyConditionExpression=Key('id').eq(int(str(articleId)))

        )
        response[str(articleId)] = articleDetails['Items']
    return response


def generateResponses(recommendations, table):
    responses = {}
    for user, recommendation in recommendations.items():
        responses[user] = generateResponse(recommendation, table)

    return responses


def storeInDynamo(userRecommendations, userRecommendationTable):
    for user, userRecommedation in userRecommendations.items():
        # print(userRecommedation)
        userRecommendationTable.put_item(
            Item={
                "userId": str(user),
                "data": userRecommedation
            }
        )

def SetPreferences(model, cursor, topicMatrix, userPreferencesMatrix):
    dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
    articleTable = dynamodb.Table('articles')
    userList = getUserList(cursor)
    item_ids = getItemIds(cursor)
    recommendations = makeRecommendations(model, userList, topicMatrix, item_ids, userPreferencesMatrix)
    responses = generateResponses(recommendations, articleTable)
    userRecommendationTable = dynamodb.Table('UserRecommendation')
    storeInDynamo(responses, userRecommendationTable)

def saveToS3(model, topicMatrix):
    # Saving Model to S3
    pickle.dump(model, open("model.pkl", 'wb'))
    s3 = boto3.resource('s3')
    s3.meta.client.upload_file('model.pkl', 'touchdown-comprehend-bucket', 'model.pkl')
    os.remove("model.pkl")

    # Saving topicMatrix to S3
    pickle.dump(topicMatrix, open("topicMatrix.pkl", 'wb'))
    s3.meta.client.upload_file('topicMatrix.pkl', 'touchdown-comprehend-bucket', 'topicMatrix.pkl')
    os.remove("topicMatrix.pkl")

if __name__ == '__main__':
    # Initiate Connection
    conn, cursor = Connect()

    # Get User Matrix
    userPreferencesMatrix = GetUserPreferencesMatrix(cursor)

    # Get User Click Data
    userClickMatrix = GetUserClickMatrix(cursor)

    # Get Topic Modelling Data
    topicMatrix = GetDocTopicsMatrix()

    # Train the Model
    model = TrainModel(userClickMatrix, topicMatrix, userPreferencesMatrix)

    # Saving file to S3
    saveToS3(model, topicMatrix)

    # Set Preferences
    SetPreferences(model, cursor, topicMatrix, userPreferencesMatrix)

    # Close Connection
    CloseConnect(conn, cursor)
