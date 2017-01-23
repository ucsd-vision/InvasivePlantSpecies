from street_view import *

import sys

lat = sys.argv[1]
lng = sys.argv[2]

pano = get_nearest_pano(lat, lng)

jsonOut = {'panoramaId':pano['Location']['panoId'], 'lat': lat, 'lng': lng, 
  'description': pano['Location']['description'], 'region': pano['Location']['region'],
  'country': pano['Location']['country']}

print json.dumps(jsonOut)
