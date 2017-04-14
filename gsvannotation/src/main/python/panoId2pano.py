from street_view import *

import sys

panoId = sys.argv[1]

pano = get_pano(panoId)

if len(pano.keys()) == 0:
  print "none"
else:
  jsonOut = {'panoramaId':pano['Location']['panoId'], 'lat': pano['Location']['lat'], 'lng': pano['Location']['lng'], 
    'description': pano['Location']['description'], 'region': pano['Location']['region'],
    'country': pano['Location']['country'], 'gsvImageDate': pano['Data']['image_date']+'-01'}

  print json.dumps(jsonOut)
