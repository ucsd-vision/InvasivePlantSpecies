from street_view import *

import sys

lat = sys.argv[1];
lng = sys.argv[2];

pano = get_nearest_pano(lat, lng)
download_full_pano_image( pano, 4, None, 'javascript', 'panos')
