import os, os.path
from os.path import join
from os import listdir, rmdir, remove
from shutil import move, copyfile
import json
import re

dir_list = next(os.walk('.'))[1]

# for category in dir_list:
#     if os.path.isdir(os.path.join(category, "NewApp")) == True:
#         for filename in listdir(os.path.join(category, "NewApp")):
#             move(os.path.join(category, "NewApp", filename), join(category, filename))
#         rmdir(os.path.join(category, "NewApp"))
num_lines_from_app = {}
total_apps = 0
exceptions = 0
for category in dir_list:
    # print("Copying python scripts to subdirectory " + category)
    # copyfile("normalize_logs.py", join(category, "normalize.py"))
    # copyfile("group_domains.py", join(category, "group_domains.py"))
    os.chdir(category)
    # print("Executing normalize logs")
    # os.system('py normalize.py > lines.csv')
    # print("Executing group script")
    # os.system('py group_domains.py lines.csv > groups.csv')
    # remove('normalize.py')
    # remove('group_domains.py')  
    file_list = next(os.walk('.'))[2]
    for logfile in file_list:
        total_apps += 1        
        with open(logfile, 'r', encoding='utf-8', errors='replace') as f:
            num_lines_from_app[logfile] = {}
            num_lines_from_app[logfile]["count"] = 0
            num_lines_from_app[logfile]["loc"] = category
            try:
                for line in f:
                    if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                        num_lines_from_app[logfile]["count"] += 1
            except Exception as e:
                exceptions += 1
    os.chdir("../")  

print("total:", total_apps)
print(json.dumps(num_lines_from_app, indent=4))
histogram={}
calls = 0
while calls < 20:
    histogram[calls] = 0
    for app in num_lines_from_app:
        if num_lines_from_app[app]["count"] == calls:
            histogram[calls] += 1
    calls += 1
print(json.dumps(histogram, indent=4))

    

