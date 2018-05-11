import psycopg2
import json

HOST = "myinstance.crim3pvzpnxc.us-east-1.rds.amazonaws.com"
PORT = 5432
DBNAME = "touchdown"
USER = "Admin2018"
PASSWORD = "Admin2018"

teamDict = {"Arizona Cardinals" : 1,
            "Atlanta Falcons" : 2,
            "Baltimore Ravens" : 3,
            "Buffalo Bills" : 4,
            "Carolina Panthers" : 5,
            "Chicago Bears" : 6,
            "Cincinnati Bengals" : 7,
            "Cleveland Browns" : 8,
            "Dallas Cowboys" : 9,
            "Denver Broncos" : 10,
            "Detroit Lions" : 11,
            "Green Bay Packers" : 12,
            "Houston Texans" : 13,
            "Indianapolis Colts" : 14,
            "Jacksonville Jaguars" : 15,
            "Kansas City Chiefs" : 16,
            "Miami Dolphins" : 17,
            "Minnesota Vikings" : 18,
            "New England Patriots" : 19,
            "New Orleans Saints" : 20,
            "New York Giants" : 21,
            "New York Jets" : 22,
            "Oakland Raiders" : 23,
            "Philadelphia Eagles" : 24,
            "Pittsburgh Steelers" : 25,
            "San Diego Chargers" : 26,
            "San Francisco 49ers" : 27,
            "Seattle Seahawks" : 28,
            "St. Louis Rams" : 29,
            "Tampa Bay Buccaneers" : 30,
            "Tennessee Titans" : 31,
            "Washington Redskins" :32
            }


def Connect():
    global conn, cursor
    conn = psycopg2.connect(host=HOST, user=USER, password=PASSWORD, dbname=DBNAME)
    cursor = conn.cursor()
    return conn, cursor


def CloseConnect(conn, cursor):
    cursor.close()
    conn.close()


def generateResponse():
    response = {
        'statusCode': 200,
        'headers': {'Content-Type': 'application/json'},
        'body': "success"
    }
    return response

def generateFailureResponse():
    response = {
        'statusCode': 400,
        'headers': {'Content-Type': 'application/json'},
        'body': "Team Name not found"
    }
    return response

def createUser(cursor, conn, userId):
    sql_command = "INSERT INTO Users(CognitoUserID) VALUES(\'" + userId + "\')"
    cursor.execute(sql_command)
    conn.commit()

def getUserId(cursor, userId):
    sql_command = "SELECT id FROM Users where CognitoUserID = \'" + userId + "\'"
    cursor.execute(sql_command)
    row = cursor.fetchone()
    return row[0]

def insertPreference(cursor, conn, userId, preference):
    sql_command = "SELECT count(*) FROM Users where CognitoUserID = \'" + userId + "\'"
    cursor.execute(sql_command)
    row = cursor.fetchone()
    if row[0] == 0:
        createUser(cursor, conn, userId)
    id = getUserId(cursor, userId)
    sql_command = "INSERT INTO UserPreferences VALUES (" + str(id) + ", " + str(teamDict[preference]) + ", 1)"
    cursor.execute(sql_command)
    conn.commit()

def createTable(cursor, conn):
    sql_command = """CREATE TABLE IF NOT EXISTS UserPreferences(
    UserId INT,
    PreferenceId INT,
    Value INT,
    PRIMARY KEY(UserId, PreferenceId)
    ) """
    cursor.execute(sql_command)
    conn.commit()

def lambda_handler(event, context):
    # Initiate Connection
    conn, cursor = Connect()

    createTable(cursor, conn)

    userId = event["queryStringParameters"]['userID']
    preference = event["queryStringParameters"]['preference']

    if preference not in teamDict.keys():
        return generateFailureResponse()

    insertPreference(cursor, conn, userId, preference)

    # Close Connection
    CloseConnect(conn, cursor)
    return generateResponse()
