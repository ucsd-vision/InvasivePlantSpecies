from subprocess import call
import urllib2

# this text file contains lat/lngs for invasive species found in gsv images
# by folks at the DEC
# this script prepopulates the database
f = open("species_latlngs.txt", "r")
lines = f.readlines()
f.close()

for line in lines:
  latlng = line[0:-1].split(',')
  url = 'http://localhost:4567/latlng2pano?lat='+latlng[0]+'&lng='+latlng[1]
  print url
  #call(["wget", url])
  f = urllib2.urlopen( url )
  print f.read()

