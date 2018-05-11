from newspaper import Article
import requests
import datetime
import boto3
import json
from newsapi import NewsApiClient
import psycopg2
from boto3.dynamodb.conditions import Key


HOST = "myinstance.crim3pvzpnxc.us-east-1.rds.amazonaws.com"
PORT = 5432
DBNAME = "touchdown"
USER = "Admin2018"
PASSWORD = "Admin2018"


API_KEY = '02533076a8ed437aafa9f3c83434d423'

API_HOST = 'https://newsapi.org'
PATH = '/v2/everything'
KEYWORD = 'American Football OR NFL OR AFL'
SORTBY = 'relevancy'
PAGESIZE = 100
FROMDATE = '2018-04-23'
TODATE = '2018-05-01'
LANGUAGE = 'en'



def getArticleText(url):
    article = Article(url)
    # Download the article.
    article.download()
    try:
        # Parse the article.
        article.parse()
    except:
        return
    # Fetch the article text.
    text = article.text
    # Remove the new line character.
    return text.replace('\n', ' ')

def getArticleSummary(url):
    article = Article(url)
    # Download the article.
    article.download()
    try:
        # Parse the article.
        article.parse()
    except:
        return

    article.nlp()
    # Fetch the article text.
    summary = article.summary
    # Remove the new line character.
    return summary


def getArticle(newsapi, cursor, conn, table):

    itr = 1
    flag = 0
    print("Firing a Query! for page = " + str(itr) + "\n")
    articles = newsapi.get_everything(q=KEYWORD, from_param=FROMDATE, to=TODATE, language=LANGUAGE, sort_by=SORTBY,
                                          page_size=PAGESIZE, page=itr)
    articles = json.loads(json.dumps(articles))
    while(articles['status'] == 'ok' and itr < 40 ):
        for article in articles['articles']:
            text = getArticleText(article['url'])
            if text:
                x = {}

                x['URL'] = article['url']
                x['ImageURL'] = article['urlToImage']
                x['Summary'] = getArticleSummary(article['url'])
                x['Title'] = article['title']
                text = text.replace("'","")
                sql_command = "SELECT Id FROM Articles where Content = \'" + text + "\'"
                cursor.execute(sql_command)
                while True:
                    row = cursor.fetchone()
                    if row == None:
                        break
                    else:
                        flag = 1

                if flag == 0 and x['ImageURL'] and x['Summary'] and x['Title']:
                    sql_command = "INSERT INTO Articles(Content) VALUES(\'" + text + "\')"
                    cursor.execute(sql_command)
                    conn.commit()
                    sql_command = "SELECT Id FROM Articles where Content = \'" + text +"\'"
                    cursor.execute(sql_command)
                    row = cursor.fetchone()
                    x['id'] = row[0]
                    table.put_item(
                        Item=x
                    )
                else:
                    flag=0

        itr = itr + 1
        articles = newsapi.get_everything(q=KEYWORD, from_param=FROMDATE, to=TODATE, language=LANGUAGE, sort_by=SORTBY,
                                          page_size=PAGESIZE, page=itr)
        print("Firing a Query! for page = " + str(itr) + "\n")
        articles = json.loads(json.dumps(articles))



if __name__ == '__main__':
    conn = psycopg2.connect(host=HOST, user=USER, password=PASSWORD, dbname=DBNAME)
    cursor = conn.cursor()
    sql_command = """CREATE TABLE IF NOT EXISTS Articles(
    Id SERIAL PRIMARY KEY,
    Content TEXT
    ) """
    cursor.execute(sql_command)
    conn.commit()

    dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
    table = dynamodb.Table('articles')

    newsapi = NewsApiClient(api_key=API_KEY)
    getArticle(newsapi, cursor, conn, table)





