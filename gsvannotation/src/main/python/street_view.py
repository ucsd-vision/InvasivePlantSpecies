import urllib2
import json
import math
import os
import numpy as np
from PIL import ImageDraw
from cStringIO import StringIO
from PIL import Image
from Queue import PriorityQueue
import xml.etree.ElementTree as ET
import base64
import zlib
import struct
import time
import csv

EARTH_RADIUS = 6371000  # Radius in meters of Earth
GOOGLE_CAR_CAMERA_HEIGHT = 3 # ballpark estimate of the number of meters that camera is off the ground

# What is the correct latitude/longitude of the camera for a panorama?  I'm not sure!  Averaging 'lat' and 'original_lat' seems to work best!
def get_pano_lat_lng(pano, method=None):
  if 'CalibratedLocation' in pano: return float(pano['CalibratedLocation']['lat']), float(pano['CalibratedLocation']['lng'])
  if method is None: method = 'original'
  if method=='normal': return float(pano['Location']['lat']), float(pano['Location']['lng'])
  elif method == 'original': return float(pano['Location']['original_lat']), float(pano['Location']['original_lng'])
  elif method == 'average': return (float(pano['Location']['lat'])+float(pano['Location']['original_lat']))/2.0, (float(pano['Location']['lng'])+float(pano['Location']['original_lng']))/2.0

# Find the closest streetview panorama to a user specified latitude and longitude.  Returns various info
# about this panorama, such as the camera latitude/longitude, the panorama id, etc.
def get_nearest_pano(latitude, longitude, radius=50, rank='closest', key=None, get_depth_map=False, get_pano_map=False, panos=None, method=None):
  if panos is None:  # Query google for the nearest pano
    url = 'https://cbks0.googleapis.com/cbk?output=json&oe=utf-8&it=all&dm='+str(int(get_depth_map))+'&pm='+str(int(get_pano_map))+'&rank=' + str(rank) + '&ll=' + str(latitude) + ',' + str(longitude) + '&radius=' + str(radius) + '&cb_client=apiv3&v=4&hl=en-US&gl=US'
    if not key is None: url = url + '&key=' + key
    response = urllib2.urlopen(url)
    data = response.read()
    return json.loads(data)
  else: # if we have pre-downloaded a set of panos, search though the dictionary of panos for the closest one
    best_d = 10000000
    pano = None
    for pid in outdoor_panos(panos):
      lat1, lng1 = get_pano_lat_lng(panos[pid], method=method)
      dist = haversine_distance(latitude, longitude, lat1, lng1)
      if dist < best_d:
        pano = panos[pid]
        best_d = dist
    return pano

# Download the full 360 degree panorama image for a streetview location 'pano', which is the return result of  
# get_nearest_pano(...)
def download_full_pano_image(pano, zoom=None, key=None, api='javascript', out_dir=None, tiles=None, max_retries=0, prepend=''):
  max_zoom = int(pano['Location']['zoomLevels'])
  if zoom is None: zoom = max_zoom  # default to highest resolution image
  down = int(math.pow(2,max_zoom-zoom))  # downsample amount
  image_width, image_height = int(pano['Data']['image_width'])/down, int(pano['Data']['image_height'])/down
  tile_width, tile_height = int(pano['Data']['tile_width']), int(pano['Data']['tile_height'])
  full_pano_image = Image.new("RGB", (image_width, image_height), "black")
  num_x, num_y = int(math.ceil(image_width / float(tile_width))), int(math.ceil(image_height / float(tile_height)))
  if not out_dir is None: 
    full_name = out_dir + '/' + prepend +  pano['Location']['panoId'] + '_z' + str(zoom) + '.jpg'
    if os.path.isfile(full_name):
      try:
        im = Image.open(full_name)
        return im
      except IOError:
        print "IOError reading image " + full_name + " in download_full_pano_image()"
        os.rename(full_name, full_name + '.bad')
    print 'Downloading ' + full_name
  for tile_y in range(num_y):
    for tile_x in range(num_x):
      download = tiles is None or tiles[tile_x,tile_y]
      tile = None
      if not out_dir is None and download: 
        fname = out_dir + '/' + pano['Location']['panoId'] + '_x' + str(tile_x) + '_y' + str(tile_y) + '_z' + str(zoom) + '.jpg'
        if os.path.isfile(fname):
          try:
            tile = Image.open(fname)
            download = False
          except IOError:
            print "IOError reading image " + fname + " in download_full_pano_image()"
            os.rename(fname, fname + '.bad')
      if download:
        if api=='javascript':  # javascript api
          url = 'http://cbk0.google.com/cbk?output=tile&panoid='+pano['Location']['panoId']+'&zoom='+str(zoom)+'&x='+str(tile_x)+'&y='+str(tile_y)
        else:  # static street view api, doesn't work currently, not implemented correctly
          url = 'https://maps.googleapis.com/maps/api/streetview?size='+pano['Data']['tile_width']+'x'+pano['Data']['tile_height']+'&pano='+pano['Location']['panoId']+'&fov='+str(360.0/num_x)+'&heading='+str((tile_x+.5)*360.0/num_x)+'&pitch='+str((num_y/2.0-tile_y+.5)*180.0/num_y)
        if not key is None: url = url + '&key=' + key
        for num_retries in range(1+max_retries):
          try:
            response = urllib2.urlopen(url)
            data = response.read()
          except urllib2.HTTPError, err:
            print 'Error '+str(err.code)+' downloading image ' + url + ' ' + fname
            if err.code == 400:
              print 'aborting ' + url + ' ' + fname
              open(fname+'.bad', 'a').close()
              return None
            if num_retries < max_retries:
              time.sleep(math.pow(2, num_retries+1))
              continue
          except:
            print 'Unknown Error downloading image ' + url + ' ' + fname
            if num_retries < max_retries:
              time.sleep(math.pow(2, num_retries+1))
              continue
        try:
          tile = Image.open(StringIO(data))
          #if not out_dir is None: tile.save(fname)
        except:
          print "IOError reading image " + fname + " in download_full_pano_image()"
          open(full_name+'.bad', 'a').close()
          return None
      if not tile is None:
        full_pano_image.paste(tile, (tile_x*tile_width, tile_y*tile_height))
  if not out_dir is None and tiles is None: 
    full_pano_image.save(full_name)
         
  return full_pano_image

