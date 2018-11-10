import json
import pprint
import datetime
import urllib2
import sys

# RETURNS:
# dhm            <- PMD
# kerstbomen
# restgft

if len(sys.argv) != 3:
        print ("Usage: afval.py postalcode housenumber")
        sys.exit(1)

url="http://json.mijnafvalwijzer.nl/?method=postcodecheck&postcode=" + sys.argv[1] + "&street=&huisnummer=" + sys.argv[2] + "&toevoeging=&platform=phone&langs=nl"

contents = urllib2.urlopen(url).read()

data = json.loads(contents)

#print(data['data']['ophaaldagen']['data'])

ophaaldagen = data['data']['ophaaldagen']['data']

type='none'

now = datetime.datetime.now()
datetoday="%d-%d-%d" % (now.year, now.month, now.day)
#datetoday='2018-11-14'

for ophaaldag in ophaaldagen:
        if ophaaldag['date'] == datetoday:
                type=ophaaldag['type']
                break

print(type)
