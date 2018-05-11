import psycopg2
import json


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


def generateResponse(value):
    response = {
        'statusCode': 200,
        'headers': {'Content-Type': 'application/json'},
        'body': json.dumps(value)
    }
    return response

def checkUser(cursor, userId):
    value = None
    sql_command = "SELECT count(*) FROM Users where CognitoUserID = \'" + userId + "\'"
    cursor.execute(sql_command)
    row = cursor.fetchone()
    if row[0] == 0:
        value = "no"
    else:
        value = "yes"

    return value

def createTable(cursor, conn):
    sql_command = """CREATE TABLE IF NOT EXISTS Users(
        Id SERIAL PRIMARY KEY,
        CognitoUserID TEXT
        ) """
    cursor.execute(sql_command)
    conn.commit()


def lambda_handler(event, context):
    # Initiate Connection
    conn, cursor = Connect()

    createTable(cursor, conn)

    userId = event["queryStringParameters"]['userID']

    value = checkUser(cursor, userId)

    # Close Connection
    CloseConnect(conn, cursor)
    return generateResponse(value)
