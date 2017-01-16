from street_view import *

import sys

lat = sys.argv[1]
lng = sys.argv[2]

pano = get_nearest_pano(lat, lng)

print pano['Location']['panoId']
