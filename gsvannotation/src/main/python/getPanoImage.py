from street_view import *

import sys

panoramaId = sys.argv[1]

pano = get_pano( panoramaId )
download_full_pano_image( pano, 3, None, 'javascript', 'external/pano_images')
