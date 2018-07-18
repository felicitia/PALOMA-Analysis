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
total_apps = 0
num_get_only = 0
print("category,min #req, max #req, avg #req, min #GET, max #GET, avg. #GET, min GET%, max GET%, avg GET%")
for category in dir_list:
    os.chdir(category)
    num_apps = 0
    min_req = 99999
    max_req = 0
    total_req = 0
    num_threshold_apps = 0

    min_get = 99999
    max_get = 0
    total_get = 0

    min_get_pc = 99999
    max_get_pc = 0
    total_get_pc = 0

    file_list = glob.glob('./*.txt')
    for logfile in file_list:
        cnt_req = 0
        cnt_get = 0
        with open(logfile[2:], 'r', encoding='utf-8', errors='replace') as f:
            try:
                for line in f:
                    if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                        cnt_req += 1
                        
                        a = line.split(sep=';%;')
                        if a[5] == 'false' or a[5] == 'GET': #get request
                            cnt_get += 1
            except Exception as e:
                pass

            if cnt_req >= 4:
                num_apps += 1
                total_req += cnt_req
                total_get += cnt_get
                num_threshold_apps += 1
                if cnt_req > max_req:
                    max_req = cnt_req
                if cnt_req < min_req:
                    min_req = cnt_req
                if cnt_get > max_get:
                    max_get = cnt_get
                if cnt_get < min_get:
                    min_get = cnt_get

                get_pc = 100.0 * cnt_get / cnt_req
                if get_pc = 100:
                    num_get_only += 1

                if get_pc > max_get_pc:
                    max_get_pc = get_pc
                if get_pc < min_get_pc:
                    min_get_pc = get_pc
                total_get_pc += get_pc
    total_apps += num_apps
    if num_apps != 0:

        avg = 1.0 * total_req / num_apps
        avg_get = 1.0 * total_get / num_apps
        avg_get_pc = 1.0 * total_get_pc / num_apps

        print(category, ",", min_req, ",", max_req, ",", avg, ",", min_get, ",", max_get, ",", avg_get, ",", min_get_pc, ",", max_get_pc, ",", avg_get_pc)

    os.chdir("../")  
print(total_apps)
print(num_get_only)
