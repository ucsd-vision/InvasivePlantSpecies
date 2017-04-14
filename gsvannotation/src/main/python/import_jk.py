#import japanese knotweet (jk)
from street_view import *
from subprocess import call
import urllib2
import utm

# this text file contains lat/lngs for invasive species found in gsv images
# by folks at the DEC
# this script prepopulates the database
#file is comma separated with columns:
# status: 1000, 3 are good, 25 means skip
# utm n
# utm w

f = open("jk_utm.csv", "r")
lines = f.readlines()
f.close()

for line in lines:
  vals = line[0:-1].split(',')
  status = vals[0]
  utm_easting = float(vals[1])
  utm_northing = float(vals[2])
  
  if status == "25":
    continue
  (lat,lng) = utm.to_latlon(utm_easting, utm_northing, 18, 'N')
  url = 'http://localhost:4567/latlng2pano?lat='+str(lat)+'&lng='+str(lng)+'&imapSpeciesId=2'
  print url
  #call(["wget", url])
  f = urllib2.urlopen( url )
  panoId = f.read()
  print panoId
  if panoId=='none':
    continue  
  pano = get_pano( panoId )
  if pano.has_key('Links'):
    linksRetrieved = 0
    for panoLink in pano['Links']:
	  url = 'http://localhost:4567/panoId2pano?panoId='+panoLink['panoId']+'&imapSpeciesId=2'
	  print url
	  f = urllib2.urlopen( url )
	  print f.read()
	  

