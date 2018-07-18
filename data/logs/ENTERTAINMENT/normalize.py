import os
import glob
import re
import ast

file_list = glob.glob('./*.txt')

lines = []
total_num_requests = 0
total_get_requests = 0
total_data_length = 0
total_apps_with_found_data_length = 0
total_expire_requests = 0
total_apps = 0
exceptions = 0

num_lines_from_app = {}
for logfile in file_list:
    with open(logfile[2:], 'r', encoding='utf-8', errors='replace') as f:
        num_lines_from_app[logfile[2:]] = 0
        try:
            for line in f:
                if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                    num_lines_from_app[logfile[2:]] += 1
                    lines.append(line)
                    a = line.split(sep=';%;')
                    total_num_requests += 1
                    if a[5] == 'false' or a[5] == 'GET': #get request
                        total_get_requests += 1
                    if a[6] != None and a[6] != '' and a[6] != 'Not found' and a[6] != '[]':
                        #unbox string
                        length = ast.literal_eval(a[6])
                        if isinstance(length, (list, tuple)):
                            total_data_length += length[0]
                        else:
                            total_data_length += int(a[6])
                        total_apps_with_found_data_length += 1
                    if a[8] != None and a[8] != '' and a[8] != 'Not found' and a[8] != '[]':
                        total_expire_requests += 1
            total_apps += 1
        except Exception as e:
            exceptions += 1
            print("exception: " + logfile[2:] + ",,,,,,,,,,")
            print(str(e) + ",,,,,,,,,,")

print("total apps: " + str(total_apps) + ",,,,,,,,,,")
print("Total number of requests: " + str(total_num_requests) + ",,,,,,,,,,")
print("Total number of GET requests: " + str(total_get_requests) + ",,,,,,,,,,")
print("Percentage of GET requests: " + str(100.0 * total_get_requests / total_num_requests) + "%" + ",,,,,,,,,,")
print("Average number of requests per app: " + str(1.0 * total_num_requests / total_apps) + ",,,,,,,,,,")
print("Average number of GET requests per app: " + str(1.0 * total_get_requests / total_apps) + ",,,,,,,,,,")
print("Average number of expire requests: " + str(total_expire_requests) + ",,,,,,,,,,")
print("Percentage of expire requests: " + str(100.0 * total_expire_requests / total_num_requests) + "%" + ",,,,,,,,,,")
print("Average number of expire requests per app: " + str(1.0 * total_expire_requests / total_apps) + ",,,,,,,,,,")
print("Average data length per request: " + str(1.0 * total_data_length / total_apps_with_found_data_length) + ",,,,,,,,,,")
print("Exceptions: " + str(exceptions) + ",,,,,,,,,,")

print(",,,,,,,,,,")
print("prefix,id,body,stmt,urlString,isDoOutput,length,cacheControl,expires,age,setCookie,,,")
for line in lines:
    line = line.replace(";%;", '","')
    line = line.replace("\n", "")
    line = '"' + line + '"'
    try:
        print(line.encode().decode())
    except Exception as e:
        print(line.encode())

print(",,,,,,,,,,")
print("package,numRequests" + ",,,,,,,,,,")
for line in num_lines_from_app:
    print(line + "," + str(num_lines_from_app[line]) + ",,,,,,,,,,")
