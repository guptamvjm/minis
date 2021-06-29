import json, requests
import os, subprocess
from pathlib import Path
from random import *

def get_pics(query):
    """Retrieve pictures using query from Unsplash API"""
    url = 'https://api.unsplash.com/photos/random'
    ps = {'count': 5, 'query': query, 'client_id': 'OjXIqPrz-SZJV4riyq5T_e4wMn4xVw9DF4F9OuyKeJw'}
    if not ps['query']:
        ps.pop('query')
    response = requests.get(url, params=ps)
    pictures = json.loads(response.text)
    #f = open('/home/guptamvjm/Desktop/personal/noname.txt', 'w')
    #f.write(str(pictures))
    #f.close()
    return pictures

def download_photo(photos, directory):
    """Given image dictionary and target directory, download photo"""
    url = photos[randint(0, len(photos) - 1)]['urls']['raw'] + '&crop=entropy&fit=crop&w=1920&h=1080'
    name = 'current_photo'
    subprocess.run(['wget', '-O', name, url])
    cwd = os.getcwd() + '/' + name
    subprocess.run(['mv', cwd, directory])
    return directory + '/' + name

def change_background(query, directory):
    """Given a query and directory to download photo, change the background"""
    subprocess.run(['mkdir', directory])
    p = get_pics(query)
    file_loc = download_photo(p, directory)
    full_path = 'file://' + file_loc
    subprocess.run(['gsettings', 'set', 'org.gnome.desktop.background', 'picture-uri', full_path])

def file_crontab(query, fname):
    """Make a copy of current crontab, update with new command"""
    s = '0 * * * * python3 ' + str(Path(__file__).absolute()) + ' ' + query + '\n'
    prev_cron = subprocess.run('crontab -l', shell=True, capture_output=True)
    prev_cron = str(prev_cron.stdout)[1:]
    total = check_crontab(s, eval(prev_cron))
    f = open(fname, 'w')
    f.write(total)
    f.close()
    return fname

def crontab_command(query):
    """Alternate crontab command"""
    base = '"0 * * * * python3 ' + str(Path(__file__).absolute()) + ' ' + query + '"'
    s = 'crontab -l 2>/dev/null; echo ' + s + ' | crontab -'
    return s

def check_crontab(new, old):
    """Checks if new command is in the previous crontab file. If so, deletes appropriate old entry"""
    path = str(Path(__file__).absolute())
    old = old.split('\n')
    for command in old[:]:
        if new[:9] == command[:9]:
            if path[path.rindex('/') + 1:] in command:
                old.remove(command)
    sep = '\n'
    return sep.join(old) + new
    
def run():
    """Parse and use command-line arguments"""
    import argparse
    parser = argparse.ArgumentParser(description="Desktop Background Changer For UNIX Devices")
    parser.add_argument('--init', help="Will update crontab, automated for every hour", action='store_true')
    parser.add_argument('query', nargs='?', default='', help="Search query (Suggestions: city, nature, minimal)")
    args = parser.parse_args()
    fname = '/home/guptamvjm/Desktop/temp_cron'
    if args.init:
        s = file_crontab(args.query, fname)
        subprocess.run('crontab '+ s, shell=True)
        os.remove(s)
        #s = crontab_command(args.query)
        #completed = subprocess.run('crontab '+ s, shell=True, capture_output=True)
        #prev_crontab = completed.stdout
    change_background(args.query, '/home/guptamvjm/Desktop')

run()

"""
crontab -l 2>/dev/null; echo "* * * * * python3 /home/guptamvjm/Desktop/personal/CaesarCipher/cipher.py 0 
/home/guptamvjm/Desktop/personal/CaesarCipher/caesar.txt" | crontab -
"""
#p = read_file("noname.txt")
#p = eval(p)


