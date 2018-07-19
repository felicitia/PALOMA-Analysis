import os, os.path
from os.path import join
from os import listdir, rmdir, remove
from shutil import move, copyfile
import json
import re
import glob
import csv

file_list = glob.glob(os.path.join('./*.csv'))
 
for file in file_list:
    with open(file, newline='') as csvfile:
        spamreader = csv.reader(csvfile, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL, skipinitialspace=True)

        #cat specific stats
        avg_exp = 0
        avg_exp_pc = 0
        avg_tr_exp_pc = 0
        avg_cc = 0
        avg_cc_pc = 0
        avg_tr_cc_pc = 0
        min_nec_pc = 99999
        max_nec_pc = 0
        avg_nec_pc = 0
        num_req = 0
        denom_nec = 0

        app_cnt = {}
        app_ex_cnt = {}
        app_tr_ex_cnt = {}
        app_cc_cnt = {}
        app_tr_cc_cnt = {}
        app_nec_cnt = {}

        for row in spamreader:
            num_req += 1
            if row[1].strip() not in app_cnt:
                app_cnt[row[1].strip()] = 0
                app_ex_cnt[row[1].strip()] = 0
                app_tr_ex_cnt[row[1].strip()] = 0
                app_cc_cnt[row[1].strip()] = 0
                app_tr_cc_cnt[row[1].strip()] = 0
                app_nec_cnt[row[1].strip()] = 0           
            app_cnt[row[1].strip()] += 1 

            #file specific stats
            exp = row[3].strip()
            tr_exp = row[4].strip()
            cc = row[5].strip()
            tr_cc = row[6].strip()
            necc = row[7].strip()

            if exp == "True":
                avg_exp += 1
                app_ex_cnt[row[1].strip()] += 1

            if tr_exp == "True":
                app_tr_ex_cnt[row[1].strip()] += 1

            if cc == "True":
                avg_cc += 1
                app_cc_cnt[row[1].strip()] += 1
            
            if tr_cc == "True":
                app_tr_cc_cnt[row[1].strip()] += 1

            if necc != "999":
                denom_nec += 1
                if necc == "10":
                    avg_nec_pc += 10
                    if 10 < min_nec_pc:
                        min_nec_pc = 10
                    if 10 > max_nec_pc:
                        max_nec_pc = 10
                elif necc == "30":
                    avg_nec_pc += 30
                    if 30 < min_nec_pc:
                        min_nec_pc = 30
                    if 30 > max_nec_pc:
                        max_nec_pc = 30
                else:
                    avg_nec_pc += 60
                    if 60 < min_nec_pc:
                        min_nec_pc = 60
                    if 60 > max_nec_pc:
                        max_nec_pc = 60
        
        # final stats
        if denom_nec != 0:
            avg_nec_pc = 1.0 * avg_nec_pc / denom_nec
        else:
            avg_nec_pc = "NO REQ CHANGED"
        if min_nec_pc == 99999:
            min_nec_pc = "NO REQ CHANGED"
        if max_nec_pc == 0:
            max_nec_pc = "NO REQ CHANGED"
            
        avg_exp = 1.0 * avg_exp / len(app_cnt)
        avg_cc = 1.0 * avg_cc / len(app_cnt)

        denom_ex = 0
        denom_cc = 0
        denom_app = 0
        for app in app_cnt:
            denom_app += 1
            avg_exp_pc += 100.0 * app_ex_cnt[app] / app_cnt[app]
            avg_cc_pc += 100.0 * app_cc_cnt[app] / app_cnt[app]
            # avg_nec_pc += 100.0 * app_nec_cnt / app_cnt[app]

            if app_ex_cnt[app] != 0:
                avg_tr_exp_pc += 100.0 * app_tr_ex_cnt[app] / app_ex_cnt[app]
                denom_ex += 1
            if app_cc_cnt[app] != 0:
                avg_tr_cc_pc += 100.0 * app_tr_cc_cnt[app] / app_cc_cnt[app]
                denom_cc += 1

        avg_exp_pc = 1.0 * avg_exp_pc / denom_app
        avg_cc_pc = 1.0 * avg_cc_pc / denom_app

        if denom_ex != 0:
            avg_tr_exp_pc = 1.0 * avg_tr_exp_pc / denom_ex
        else:
            avg_tr_exp_pc = "N/A"

        if denom_cc != 0:
            avg_tr_cc_pc = 1.0 * avg_tr_cc_pc / denom_cc
        else:
            avg_tr_cc_pc = "N/A"            

        print(file, ",",
            avg_exp, ",",
            avg_exp_pc, ",",
            avg_tr_exp_pc, ",",
            avg_cc, ",",
            avg_cc_pc, ",",
            avg_tr_cc_pc, ",",
            max_nec_pc, ",",
            min_nec_pc, ",",
            avg_nec_pc)

