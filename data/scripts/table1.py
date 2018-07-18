import os, os.path
from os.path import join
from os import listdir, rmdir, remove
from shutil import move, copyfile
import json
import re
import glob
import ast

dir_list = next(os.walk('.'))[1]
num_zero_req = 0

print("category,#apps,min #req, max #req, avg #req, #app >=4 req (%)")
for category in dir_list:
    os.chdir(category)
    num_apps = 0
    min_req = 99999
    max_req = 0
    total_req = 0
    num_threshold_apps = 0

    file_list = glob.glob('./*.txt')
    for logfile in file_list:
        cnt_req = 0
        num_apps += 1

        with open(logfile[2:], 'r', encoding='utf-8', errors='replace') as f:
            try:
                for line in f:
                    if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                        cnt_req += 1
                        total_req += 1
            except Exception as e:
                pass
            if cnt_req > max_req:
                max_req = cnt_req
            if cnt_req < min_req:
                min_req = cnt_req
            if cnt_req >= 4:
                num_threshold_apps += 1
            if cnt_req == 0:
                num_zero_req += 1

    avg = 1.0 * total_req / num_apps

    print(category, ",", num_apps, ",", min_req, ",", max_req, ",", avg, ",", num_threshold_apps)
    os.chdir("../")  
print(num_zero_req)