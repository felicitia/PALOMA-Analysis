import os
import glob
import re
import ast
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

file_list = glob.glob('./*.txt')
session = FuturesSession(executor=ThreadPoolExecutor(max_workers=50))

exceptions = 0
redud = []
min_redud_pc = 99999
max_redud_pc = 0
total_redud_pc = 0
num_apps = 0
cat_summary = []

avg_expires = 0
avg_expires_pc = 0
avg_tr_exp_pc = 0
avg_cc = 0
avg_cc_pc = 0
avg_tr_cc_pc = 0
denom_exp = 0
denom_cc = 0

num_lines_from_app = {}
for logfile in file_list:
    with open(logfile[2:], 'r', encoding='utf-8', errors='replace') as f:
        lines = []
        redud = []
        formatted_data = {}
        try:
            for line in f:
                if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                    a = line.split(sep=';%;')
                    lines.append(line)
                    if a[5] == "FALSE" or a[5] == 'GET' or a[5] == "false": #only GET requests
                        redud.append(a[4])
                        formatted_data[a[4]] = {}
                        formatted_data[a[4]]['expires'] = None
                        formatted_data[a[4]]['cacheControl'] = None
        except Exception as e:
            exceptions += 1
        
        #calculate redundant% for each app
        if len(lines) <= 3: #neglect apps with less than 4 calls
            continue
        before = len(redud)
        if before == 0:
            continue
        num_apps += 1
        redud = list(dict.fromkeys(redud))
        after = len(redud)
        redud_pc = 100.0 * (before - after) / before     
        total_redud_pc += redud_pc
        if redud_pc > max_redud_pc:
            max_redud_pc = redud_pc
        if redud_pc < min_redud_pc:
            min_redud_pc = redud_pc   

        #requests analysis
        url_data = {}
        times = {}
        total_num_urls = 0
        total_expire_urls = 0
        num_reliable_expire = 0
        total_cachecontrol_urls = 0
        num_reliable_cachecontrol = 0
        #print("Sending all requests: " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
        for url in redud:
            url_data[url] = []
            formatted_data[url]["sent_at"] = datetime.now()
            response = session.get(url)
            url_data[url].append(response)
        #print("Sleeping for 10 seconds")
        time.sleep(10) 

        #print("Sending round 2 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
        for url in redud:
            url_data[url].append(session.get(url))
        #print("Sleeping for 20 seconds")
        time.sleep(20) 

        #print("Sending round 3 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
        for url in redud:
            url_data[url].append(session.get(url))
        #print("Sleeping for 30 seconds")
        time.sleep(30) 

        #print("Sending round 4 of requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))
        for url in redud:
            url_data[url].append(session.get(url))

        for url in redud:
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


        #print("url;changed after 10s;changed after 30s;changed after 60s;response time;expires;cacheControl;expires reliable;cacheControl reliable")
        for url in redud:
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
            #if formatted_data[url][0]["scode"] == "failed":
             #   print(url + " failed")
            #else:
             #   print(url + ";" + str(formatted_data[url][1]["changed"]) + ";" + str(formatted_data[url][2]["changed"]) + ";" + str(formatted_data[url][3]["changed"]) + ";" + str(times[url]["random"].microseconds) + ";" + str(expires) + ";" + str(cacheControl) + ";" + str(expires_reliable) + ";" + str(cacheControl_reliable))
        #print("Finished all processing all requests at " + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()))

        if total_num_urls != 0:
            avg_expires += total_expire_urls
            avg_expires_pc += (100.0 * total_expire_urls / total_num_urls)
            avg_cc += total_cachecontrol_urls
            avg_cc_pc += (100.0 * total_cachecontrol_urls / total_num_urls)

            if total_expire_urls != 0:
                avg_tr_exp_pc += (100.0 * num_reliable_expire / total_expire_urls)
                denom_exp += 1
            if total_cachecontrol_urls != 0:                
                avg_tr_cc_pc += (100.0 * num_reliable_cachecontrol / total_cachecontrol_urls) 
                denom_cc += 1

        # print("total num urls: " + str(total_num_urls))
        # if total_num_urls != 0:
        #     print("% expire urls: " + str(100.0 * total_expire_urls / total_num_urls))
        # else:
        #     print("% expire urls: N/A")
        # if total_expire_urls != 0:
        #     print("% reliable expire urls: " + str(100.0 * num_reliable_expire / total_expire_urls ))
        # else:
        #     print("% reliable expire urls: N/A")
        # if total_num_urls != 0:
        #     print("% cachecontrol urls: " + str(100.0 * total_cachecontrol_urls / total_num_urls))
        # else:
        #     print("% cachecontrol urls: N/A")
        # if total_cachecontrol_urls != 0:
        #     print("% reliable cachecontrol urls: " + str(100.0 * num_reliable_cachecontrol / total_cachecontrol_urls ))
        # else:
        #     print("% reliable cachecontrol urls: N/A")

if num_apps != 0:
    avg_redud_pc = 1.0 * total_redud_pc / num_apps
    avg_expires = 1.0 * avg_expires / num_apps
    avg_expires_pc = 1.0 * avg_expires_pc / num_apps
    if denom_exp != 0:
        avg_tr_exp_pc = 1.0 * avg_tr_exp_pc / denom_exp
    else:
        avg_tr_exp_pc = "N/A"
    avg_cc = 1.0 * avg_cc / num_apps
    avg_cc_pc = 1.0 * avg_cc_pc / num_apps
    if denom_cc != 0:
        avg_tr_cc_pc = 1.0 * avg_tr_cc_pc / denom_cc
    else:
        avg_tr_cc_pc = "N/A"


    print(avg_expires, ",", 
        avg_expires_pc, ",", 
        avg_tr_exp_pc, ",",
        avg_cc, ",",
        avg_cc_pc, ",",
        avg_tr_cc_pc, ",",
        min_redud_pc, ",", max_redud_pc, ",", avg_redud_pc, ",", num_apps)


# print("prefix,id,body,stmt,urlString,isDoOutput,length,cacheControl,expires,age,setCookie,,,")
# for line in lines:
#     line = line.replace(";%;", '","')
#     line = line.replace("\n", "")
#     line = '"' + line + '"'
#     try:
#         print(line.encode().decode())
#     except Exception as e:
#         print(line.encode())

