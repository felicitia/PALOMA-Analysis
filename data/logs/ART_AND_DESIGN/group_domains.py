import csv
import pandas
import os
import requests
from requests_futures.sessions import FuturesSession
import time
import json
import argparse
from urllib.parse import urlsplit
from random import shuffle
from datetime import datetime
from datetime import timedelta
from concurrent.futures import ThreadPoolExecutor

#get file to load
parser = argparse.ArgumentParser(description='Get data from printed requests')
parser.add_argument('csv_file', metavar='C')
args = parser.parse_args()

#load csv file with requests
reqs = []
formatted_data = {}
total_num_urls = 0
total_expire_urls = 0
num_reliable_expire = 0
total_cachecontrol_urls = 0
num_reliable_cachecontrol = 0

mapping = {}
dir_list = next(os.walk('.'))[1]
for category in dir_list:
    os.chdir(category)
    mapping[category] = {}
    file_list = glob.glob('./*.txt')

    
    
    for logfile in file_list:
        app_req = 0
        app_req_get = 0
        mt = []
        with open(logfile[2:], 'r', encoding='utf-8', errors='replace') as f:
            try:
                for line in f:
                    if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                        a = line.split(sep=';%;')
                        app_req += 1
                        if a[5] == "FALSE" or a[5] == 'GET' or a[5] == "false": #only GET requests
                            reqs.append(a[4])
                            formatted_data[a[4]] = {}
                            formatted_data[a[4]]['expires'] = None
                            formatted_data[a[4]]['cacheControl'] = None
                            app_req_get += 1
                            mt.append(a[4])
            except Exception as e:
                pass
        if app_req > 3 and app_req_get > 0:
            mapping[category][logfile[2:]] = mt        

#print("sep=;")
#remove duplicates
reqs = list(dict.fromkeys(reqs))

#group urls by domains
shuffle(reqs)
times = {}

#send requests now, and in 15 seconds to measure times
session = FuturesSession(executor=ThreadPoolExecutor(max_workers=10))
url_data = {}
print("Sending all requests: " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
for url in reqs:
    url_data[url] = []
    formatted_data[url]["sent_at"] = datetime.now()
    response = session.get(url)
    url_data[url].append(response)
print("Sleeping for 10 seconds")
time.sleep(10) 

print("Sending round 2 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
for url in reqs:
    url_data[url].append(session.get(url))
print("Sleeping for 20 seconds")
time.sleep(20) 

print("Sending round 3 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
for url in reqs:
    url_data[url].append(session.get(url))
print("Sleeping for 30 seconds")
time.sleep(30) 

print("Sending round 4 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
for url in reqs:
    url_data[url].append(session.get(url))

for url in reqs:
    for rnd, fut in enumerate(url_data[url]):
        formatted_data[url][rnd] = {}
        try:
            resp = url_data[url][rnd].result()
            formatted_data[url][rnd]["scode"] = resp.status_code
            formatted_data[url][rnd]["content"] = resp.content

            #mark timestamp on rnd 1 for convenience
            if rnd == 0:
                times[url] = {}
                times[url]["random"] = resp.elapsed
                if 'expires' in resp.headers:
                    formatted_data[url]['expires'] = resp.headers['expires']
                if 'cache-control' in resp.headers:
                    formatted_data[url]['cacheControl'] = resp.headers['cache-control']

            if formatted_data[url][0]["scode"] != "failed":
                if formatted_data[url][rnd]["content"] == formatted_data[url][0]["content"]:
                    formatted_data[url][rnd]["changed"] = False
                else:
                    formatted_data[url][rnd]["changed"] = True
            else:
                formatted_data[url][rnd]["changed"] = "first request failed"
        except:
            #print(str(url) + "request failed")
            formatted_data[url][rnd]["scode"] = "failed"
            formatted_data[url][rnd]["changed"] = "failed"
        #print("Finished " + url + " round " + str(rnd))


print("cat,app,url,changed after 10s,changed after 30s,changed after 60s,expires reliable,cacheControl reliable")
for cat in mapping:
    for app in cat:
        for url in app:
            total_num_urls += 1
            expires_reliable = "N/A"
            cacheControl_reliable = "N/A"

            expires = formatted_data[url]["expires"]
            if expires != None:
                total_expire_urls += 1
                expires_reliable = True
                try:
                    d = datetime.strptime(expires, '%a, %d %b %Y %H:%M:%S GMT')
                    if formatted_data[url]["sent_at"] > d:
                        expires_reliable = False
                    if formatted_data[url]["sent_at"] + timedelta(seconds=10) > d and formatted_data[url][1]["changed"] == False:
                        expires_reliable = False
                    if formatted_data[url]["sent_at"] + timedelta(seconds=30) > d and formatted_data[url][1]["changed"] == False:
                        expires_reliable = False
                    if formatted_data[url]["sent_at"] + timedelta(seconds=60) > d and formatted_data[url][1]["changed"] == False:
                        expires_reliable = False
                    if expires_reliable == True:
                        num_reliable_expire += 1
                except:
                    expires_reliable = "Unknown Format"

            cacheControl = str(formatted_data[url]["cacheControl"]).split(",")
            for command in cacheControl:
                index = command.find("max-age")
                if index != -1:
                    try:
                        max_age = int(command[9:])
                        cacheControl_reliable = True
                        total_cachecontrol_urls += 1
                        if 10 > max_age and formatted_data[url][1]["changed"] == False:
                            cacheControl_reliable = False
                        if 30 > max_age and formatted_data[url][1]["changed"] == False:
                            cacheControl_reliable = False
                        if 60 > max_age and formatted_data[url][1]["changed"] == False:
                            cacheControl_reliable = False      
                        if cacheControl_reliable == True:
                            num_reliable_cachecontrol += 1
                    except:
                        cacheControl_reliable = "Unknown Format"
            if formatted_data[url][0]["scode"] == "failed":
                print(url + " failed")
            else:
                print(cat, ",", app, ",", url, "," + str(formatted_data[url][1]["changed"]) + "," + str(formatted_data[url][2]["changed"]) + "," + str(formatted_data[url][3]["changed"]) + "," + str(expires_reliable) + "," + str(cacheControl_reliable))
        print("Finished all processing all requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))

        print("total num urls: " + str(total_num_urls))
        if total_num_urls != 0:
            print("% expire urls: " + str(100.0 * total_expire_urls / total_num_urls))
        else:
            print("% expire urls: N/A")
        if total_expire_urls != 0:
            print("% reliable expire urls: " + str(100.0 * num_reliable_expire / total_expire_urls ))
        else:
            print("% reliable expire urls: N/A")
        if total_num_urls != 0:
            print("% cachecontrol urls: " + str(100.0 * total_cachecontrol_urls / total_num_urls))
        else:
            print("% cachecontrol urls: N/A")
        if total_cachecontrol_urls != 0:
            print("% reliable cachecontrol urls: " + str(100.0 * num_reliable_cachecontrol / total_cachecontrol_urls ))
        else:
            print("% reliable cachecontrol urls: N/A")