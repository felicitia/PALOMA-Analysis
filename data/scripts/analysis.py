import os, os.path
from os.path import join
from os import listdir, rmdir, remove
from shutil import move, copyfile
import json
import re

dir_list = next(os.walk('.'))[1]


for category in dir_list:
    print("Copying python scripts to subdirectory " + category)
    copyfile("gen_lines.py", join(category, "gen_lines.py"))
    os.chdir(category)
    os.system('py gen_lines.py')
    remove('gen_lines.py')  
    os.chdir("../")  

# print(json.dumps(num_lines_from_app, indent=4))
# histogram={}
# calls = 0
# while calls < 20:
#     histogram[calls] = 0
#     for app in num_lines_from_app:
#         if num_lines_from_app[app]["count"] == calls:
#             histogram[calls] += 1
#     calls += 1
# print(json.dumps(histogram, indent=4))

    

