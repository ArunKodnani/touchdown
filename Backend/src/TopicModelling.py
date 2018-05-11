import json

import psycopg2
import pandas as pd
import numpy as np
import boto3
from scipy import sparse
import time

s3 = boto3.resource('s3')

HOST = "myinstance.crim3pvzpnxc.us-east-1.rds.amazonaws.com"
PORT = 5432
DBNAME = "touchdown"
USER = "Admin2018"
PASSWORD = "Admin2018"

conn = psycopg2.connect(host = HOST, user = USER, password = PASSWORD, dbname = DBNAME)
cursor = conn.cursor()

sql_command = "SELECT * FROM Articles ORDER BY Id"
cursor.execute(sql_command)

articleList = []
while True:
	row = cursor.fetchone()
	if row == None:
		break
	articleList.append(row[1])

articleString = "\n".join(articleList)
s3.Object('touchdown-comprehend-bucket',"Article_List").put(Body=articleString)

time.sleep(60)

comprehend = boto3.client(service_name='comprehend', region_name='us-east-1')

input_s3_url = "s3://touchdown-comprehend-bucket/Article_List"
input_doc_format = "ONE_DOC_PER_LINE"
output_s3_url = "s3://touchdown-comprehend-bucket/Article_Topic_Models"
data_access_role_arn = "arn:aws:iam::968468597232:role/service-role/AmazonComprehendServiceRole-ComprehendS3Access"
number_of_topics = 10

input_data_config = {"S3Uri": input_s3_url, "InputFormat": input_doc_format}
output_data_config = {"S3Uri": output_s3_url}

start_topics_detection_job_result = comprehend.start_topics_detection_job(NumberOfTopics=number_of_topics,
                                                                          InputDataConfig=input_data_config,
                                                                          OutputDataConfig=output_data_config,
                                                                          DataAccessRoleArn=data_access_role_arn)

print('start_topics_detection_job_result: ' + json.dumps(start_topics_detection_job_result))

job_id = start_topics_detection_job_result["JobId"]

print('job_id: ' + job_id)
