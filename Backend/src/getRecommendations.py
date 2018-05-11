import json
import boto3
import botocore
from boto3.dynamodb.conditions import Key
from decimal import Decimal
import psycopg2
import random
import pickle
import numpy as np
from scipy import sparse

HOST = "myinstance.crim3pvzpnxc.us-east-1.rds.amazonaws.com"
PORT = 5432
DBNAME = "touchdown"
USER = "Admin2018"
PASSWORD = "Admin2018"


def Connect():
    global conn, cursor
    conn = psycopg2.connect(host=HOST, user=USER, password=PASSWORD, dbname=DBNAME)
    cursor = conn.cursor()
    return conn, cursor


def CloseConnect(conn, cursor):
    cursor.close()
    conn.close()


def generateLambdaResponse(value):
    response = {
        'statusCode': 200,
        'headers': {'Content-Type': 'application/json'},
        'body': value
    }
    return response


def getUserId(cursor, userId):
    sql_command = "SELECT id FROM Users where CognitoUserID = \'" + userId + "\'"
    cursor.execute(sql_command)
    row = cursor.fetchone()
    if row:
        return row[0]
    else:
        return None


def generateFailureResponse():
    response = {
        'statusCode': 400,
        'headers': {'Content-Type': 'application/json'},
        'body': "Team Name not found"
    }
    return response


def generateResponse(recommendation, table):
    response = {}
    for articleId in recommendation:
        articleDetails = table.query(
            KeyConditionExpression=Key('id').eq(int(str(articleId)))

        )
        response[str(articleId)] = articleDetails['Items']
    return response


class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, Decimal):
            return float(o)
        return super(DecimalEncoder, self).default(o)


def getItemIds(cursor):
    itemIds = []
    sql_command = "SELECT DISTINCT ArticleId FROM UserClicks"
    cursor.execute(sql_command)
    while True:
        row = cursor.fetchone()
        if row == None:
            break
        itemIds.append(row[0] + 1)

    return np.array(itemIds)


def GetDocTopicsMatrix():
    s3 = boto3.client('s3')
    s3.download_file(
        'touchdown-bucket',
        'topicMatrix.pkl',
        'topicMatrix.pkl')
    return pickle.load(open("topicMatrix.pkl", 'rb'))


def checkFileExists():
    result = True
    s3 = boto3.resource('s3')
    try:
        s3.Object('touchdown-bucket', 'model.pkl').load()
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == "404":
            result = False
    return result


def getTheModel():
    s3 = boto3.client('s3')
    s3.download_file(
        'touchdown-bucket',
        'model.pkl',
        'model.pkl')
    return pickle.load(open('model.pkl', 'rb'))


def getUserPreferences(Id):
    sql_command = "SELECT * FROM UserPreferences where UserId =  \'" + str(Id) + "\'"
    cursor.execute(sql_command)
    UserID = np.array([])
    PreferenceId = np.array([])
    Value = np.array([])
    maxUserID = 0
    maxPreferenceID = 0
    while True:
        row = cursor.fetchone()
        if row == None:
            break
        if row[0] > maxUserID:
            maxUserID = row[0]
        if row[1] > maxPreferenceID:
            maxPreferenceID = row[1]

        UserID = np.append(UserID, row[0])
        PreferenceId = np.append(PreferenceId, row[1])
        Value = np.append(Value, row[2])

    matrix = sparse.coo_matrix((Value, (UserID, PreferenceId)),
                               shape=(
                                   maxUserID + 1, maxPreferenceID + 1))
    return matrix


def storeInDynamo(userId, userRecommendation, userRecommendationTable):
    userRecommendationTable.put_item(
        Item={
            "userId": str(userId),
            "data": userRecommendation
        }
    )

def getFromDynamoDB(Id):
    dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
    table = dynamodb.Table('UserRecommendation')
    articleDetails = table.query(KeyConditionExpression=Key('userId').eq(str(Id)))
    return articleDetails


def predictRecommendation(Id, cursor, articleTable, table):
    model = getTheModel()
    userPreferenceMatrix = getUserPreferences(Id)
    topicMatrix = GetDocTopicsMatrix()
    item_ids = getItemIds(cursor)
    print(type(topicMatrix))
    recommendation = model.predict(user_ids=0, item_ids=item_ids, item_features=topicMatrix,
                                   user_features=userPreferenceMatrix)
    recommendations = recommendation.argsort()[-20:]
    response = generateResponse(recommendations, articleTable)
    storeInDynamo(Id, response, table)
    result = getFromDynamoDB(Id)
    delete_item(table, 'userId', Id)
    return result

def delete_item(table, pk_name, pk_value):
    response = table.delete_item(Key={pk_name: pk_value})

    return

def getRandomRecommendations(randomArticleIds, articleTable, Id, table):
    response = generateResponse(randomArticleIds, articleTable)
    storeInDynamo(Id, response, table)
    result = getFromDynamoDB(Id)
    delete_item(table, 'userId', Id)
    return result

def lambda_handler(event, context):
    # Initiate Connection
    conn, cursor = Connect()

    userId = event["queryStringParameters"]['userID']

    # Fetch System User ID
    Id = getUserId(cursor, userId)

    # Check User in DynamoDB
    dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
    table = dynamodb.Table('UserRecommendation')
    articleTable = dynamodb.Table('articles')
    articleDetails = table.query(KeyConditionExpression=Key('userId').eq(str(Id)))
    print(checkFileExists())

    if articleDetails['Count'] != 0:

        responseBody = {'messages': 'String'}
        responseBody['messages'] = articleDetails

        # Close Connection
        CloseConnect(conn, cursor)

        print("Printing at line 176")
        return generateLambdaResponse(json.dumps(responseBody, cls=DecimalEncoder))
    elif checkFileExists():
        responseBody = {'messages': 'String'}
        responseBody['messages'] = predictRecommendation(Id, cursor, articleTable, table)

        # Close Connection
        CloseConnect(conn, cursor)

        print("Printing at line 185")
        return generateLambdaResponse(json.dumps(responseBody, cls=DecimalEncoder))
    else:
        randomArticleIds = []
        while len(randomArticleIds) != 20:
            num = random.randint(1, 1000)
            randomArticleIds.append(num)
        responseBody = {'messages': 'String'}
        responseBody['messages'] = getRandomRecommendations(randomArticleIds,articleTable, Id, table)

        # Close Connection
        CloseConnect(conn, cursor)

        print("Printing at line 198")
        return generateLambdaResponse(json.dumps(responseBody, cls=DecimalEncoder))


