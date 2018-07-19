import os
import glob
import re



reqs = []
dir_list = next(os.walk('.'))[1]
for category in dir_list:
    file_list = glob.glob(os.path.join(category, './*.txt'))
    table = {}

    avg_redud = 0
    min_redud = 99999
    max_redud = 0
    has_req = 0
    
    for logfile in file_list:

        with open(logfile, 'r', encoding='utf-8', errors='replace') as f:
            redud = []
            try:
                for line in f:
                    if re.match("(.*);%;([0-9][0-9]*);%;(.*)", line):
                        a = line.split(sep=';%;')
                        if a[5] == "FALSE" or a[5] == 'GET' or a[5] == "false": #only GET requests
                            redud.append(a[4])
            except Exception as e:
                pass
            
            before = len(redud)
            redud = list(dict.fromkeys(redud))
            after = len(redud)

            if before != 0:
                has_req += 1
                file_avg = 100.0 * (before - after) / before
                avg_redud += file_avg

                if file_avg > max_redud:
                    max_redud = file_avg
                if file_avg < min_redud:
                    min_redud = file_avg

    avg_redud = 1.0 * avg_redud / has_req

    print(category, ",",
        min_redud, ",",
        max_redud, ",",
        avg_redud)