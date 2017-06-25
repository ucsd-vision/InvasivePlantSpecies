import com.fasterxml.jackson.databind.ObjectMapper;
import gsvannotation.db.*;
import org.sql2o.Sql2o;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Main {
	
	public static Panorama latlng2pano(double lat, double lng) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/latlng2pano.py " +lat + " " + lng);
			p.waitFor();
			
			BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ));
			
			// for converting POJOs to json
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
			String json = stdInput.lines().collect(Collectors.joining());
			if( json.contains("none") ) {
				return null;
			} else {
				Panorama pano = jsonMapper.readValue(json,  Panorama.class);
				return pano;
			}
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Panorama panoId2pano(String panoId) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/panoId2pano.py " +panoId);
			p.waitFor();
			
			BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ));
			
			// for converting POJOs to json
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
			String json = stdInput.lines().collect(Collectors.joining());
			if( json.contains("none") ) {
				return null;
			} else {
				Panorama pano = jsonMapper.readValue(json,  Panorama.class);
				return pano;
			}
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		if( args.length != 5 ) {
			System.out.println("usage: java -jar gsvannotation.jar [dbhost] [dbusername] [dbpassword] [dbport] [webport]");
			return;
		}
		String dbhost = args[0];
		String dbusername = args[1];
		String dbpassword = args[2];
		String dbport = args[3];
		String webport = args[4];
		
	    Sql2o sql2o = new Sql2o("jdbc:mysql://" + dbhost + ":" + dbport + "/invasivespecies?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", 
	            dbusername, dbpassword);
	    
	    // these column mappings get applied to all queries
	    Map<String,String> columnMaps = new HashMap<String,String>();
	    columnMaps.put("imap_species_speciesId", "imapSpeciesId");
	    columnMaps.put("species_speciesId", "speciesId");
	    sql2o.setDefaultColumnMappings(columnMaps);

		Model model = new Sql2oModel(sql2o);

		// for converting POJOs to json
		ObjectMapper jsonMapper = new ObjectMapper();
		
		// panorama images will be saved here
		// TODO: possibly make this a command line argument
		externalStaticFileLocation("external");
		
		// for static html/css/js files
		staticFileLocation("/public");
		
		port(Integer.parseInt( webport ) );
		
		get("/getPano", (request, response) -> {
			
			// assumes jar is run from a folder containing a panos subfolder
			// checks if panoid panoarama image exists under panos subfolder
			// if it doesn't exist, it downloads it using the python script
			String panoramaId = request.queryParams("panoramaId");
			Panorama pano = model.getPanorama(panoramaId);
			
			File panoImage = new File("external/pano_images/" + panoramaId + "_z3.jpg");
			if( !panoImage.exists() ) {
				Process p = Runtime.getRuntime().exec("python src/main/python/getPanoImage.py " + 
						pano.getPanoramaId() );
				p.waitFor();
			}
			String panoJson = jsonMapper.writeValueAsString(pano);
			return panoJson;
		});

		get("/getSpecies", (request, response) -> {
			String idString = request.queryParams("speciesId");
			int id = Integer.parseInt(idString);

			List<Species> species = model.getAllSpecies();

			Species specie = null;
			for (Species s :
					species) {
				if (s.getSpeciesId() == id) {
					specie = s;
				}
			}

			return jsonMapper.writeValueAsString( specie );
		});
		
		post("/savePano", (request, response) -> {
			String body = request.body();
			Panorama pano = jsonMapper.readValue(body,  Panorama.class);
			model.updatePanorama( pano );
			response.status(200);
			return "ok";
		});

		post("/saveSpecies", (request, response) -> {
			String body = request.body();
			Species species = jsonMapper.readValue(body,  Species.class);
			model.updateSpecies(species);
			response.status(200);
			return "ok";
		});

		get("/getAllPanos", (request, response) -> {
			List<Panorama> panos = model.getAllPanos();
			
			return jsonMapper.writeValueAsString( panos );
		});

		get("/getHeatmap", ((request, response) -> {
			List<Panorama> panoramas = model.getAllPanos();
			Image heatmap = new BufferedImage(3328, 1664, BufferedImage.TYPE_INT_RGB);

			long[][] heatmapArray = getHeatmapArray(panoramas);


			return null;
		}));
		
		get("/getAllSpecies", (request, response) -> {
			List<Species> species = model.getAllSpecies();
			return jsonMapper.writeValueAsString( species );
		});
		
		get("panoId2pano", (request, response) -> {
			String panoId = request.queryParams("panoId");
			Panorama pano = panoId2pano(panoId);
			if( pano!= null ) {
				if( request.queryParams().contains("imapSpeciesId") ) {
					pano.setImapSpeciesId( Integer.parseInt( request.queryParams("imapSpeciesId") ));
				} else {
					pano.setImapSpeciesId(1); // default to phrag
				}
				// Checks to see if panorama is already in database
                if( model.getPanorama(pano.getPanoramaId()) == null ) {
					model.insertPanorama(pano);
				}
				return pano.getPanoramaId();
			} else {
				return "none";
			}
		});
		
		get("/latlng2pano", (request, response) -> {
			double lat = Double.parseDouble(request.queryParams("lat"));
			double lng = Double.parseDouble(request.queryParams("lng"));
			Panorama pano = latlng2pano(lat,  lng);
                        // Check to see if a panorama exists for this lat/lng

			if( pano != null ) {
				if( request.queryParams().contains("imapSpeciesId") ) {
					pano.setImapSpeciesId( Integer.parseInt( request.queryParams("imapSpeciesId") ));
				} else {
					pano.setImapSpeciesId(1); // default to phrag
				}
				// Checks to see if panorama is already in database
                if( model.getPanorama(pano.getPanoramaId()) == null ) {
					model.insertPanorama(pano);
				}
				return pano.getPanoramaId();
			} else {
				return "none";
			}
		});

		get("getPanosByIMapSpeciesId", (request, response) -> {
			String idString = request.queryParams("speciesId");
			int id = Integer.parseInt(idString);

			List<Panorama> panoramas;

			if (id == -1) {
				panoramas = model.getAllPanos();
			} else {
				panoramas = model.getPanosByIMapSpeciesId(id);
			}

			return jsonMapper.writeValueAsString(panoramas);
		});
		
		get("getPanosByBoundingBoxSpeciesId", (request, response) -> {
			List<Panorama> panoramas;
			
			String speciesId = request.queryParams("speciesId");
			int id = Integer.parseInt( speciesId );
			
			if( id == -1 ) {
				panoramas = model.getAllPanos();
			} else {
				panoramas = model.getPanosByBoundingBoxSpeciesId( id );
			}
			
			return jsonMapper.writeValueAsString(panoramas);
		});

		get("getNumberOfBoundingBoxesPerSpecies", (request, response)
				-> jsonMapper.writeValueAsString(model.getNumberOfBoundingBoxesPerSpecies()));
		
		get("findNextPanorama", (request, response) -> {
                        String currentPanoramaId = request.queryParams("currentPanoramaId");
			return model.findNextPanorama( currentPanoramaId );
		});
		
		delete("deletePanorama", (request, response) -> {
			String panoramaId = request.queryParams("panoramaId");
			model.deletePanorama(panoramaId);
			return "";
		});

        long[][] heatmap = getHeatmapArray(model.getAllPanos());
		writeArrayOut(heatmap);
		createImageFromArray(heatmap);
	}

	public static long[][] getHeatmapArray(List<Panorama> panoramas) {
		Image heatmap = new BufferedImage(3328, 1664, BufferedImage.TYPE_INT_RGB);

		long[][] heatmapArray = new long[3328][1664];
		long largestNumber = -1;
		int counter = 0;

		for (Panorama p :
				panoramas) {
			for (BoundingBox bb :
					p.getBoundingBoxes()) {
				// adds a standard panorama, which does not cross borders
				if (bb.getTopLeftX() < bb.getBottomRightX() && bb.getTopLeftX() >= 0 && bb.getTopLeftY() >=0) {
					counter++;
					for (int i = bb.getTopLeftX(); i < bb.getBottomRightX(); i++) {
						for (int j = bb.getTopLeftY(); j < bb.getBottomRightY(); j++) {
							heatmapArray[i][j]++;
							if (heatmapArray[i][j] > largestNumber) {
								largestNumber = heatmapArray[i][j];
							}
						}
					}
				} else if (bb.getTopLeftX() >= bb.getBottomRightX()) {
					counter++;
					for (int i = 0; i < bb.getBottomRightX(); i++) {
						for (int j = bb.getTopLeftY(); j < bb.getBottomRightY(); j++) {
							heatmapArray[i][j]++;
							if (heatmapArray[i][j] > largestNumber) {
								largestNumber = heatmapArray[i][j];
							}
						}
					}

					for (int i = bb.getTopLeftX(); i < heatmapArray.length; i++) {
						for (int j = bb.getTopLeftY(); j < bb.getBottomRightY(); j++) {
							heatmapArray[i][j]++;
							if (heatmapArray[i][j] > largestNumber) {
								largestNumber = heatmapArray[i][j];
							}
						}
					}
				} else {
					counter++;
					for (int i = 0; i < bb.getBottomRightX(); i++) {
						for (int j = bb.getTopLeftY(); j < bb.getBottomRightY(); j++) {
							heatmapArray[i][j]++;
							if (heatmapArray[i][j] > largestNumber) {
								largestNumber = heatmapArray[i][j];
							}
						}
					}

					for (int i = heatmapArray.length+bb.getTopLeftX(); i < heatmapArray.length; i++) {
						for (int j = bb.getTopLeftY(); j < bb.getBottomRightY(); j++) {
							heatmapArray[i][j]++;
							if (heatmapArray[i][j] > largestNumber) {
								largestNumber = heatmapArray[i][j];
							}
						}
					}
				}


			}
		}
		System.out.println(counter);

		double ratio = 255.0/largestNumber; // this scales the heat map. otherwise just use 1.0/largestNumber to normalize if necessary.
		for (int i = 0; i < heatmapArray.length; i++) {
			for (int j = 0; j < heatmapArray[0].length; j++) {
				heatmapArray[i][j] = 255-(int) (heatmapArray[i][j]*ratio);
			}
		}

		System.out.println(ratio);
		System.out.println(largestNumber);
		return heatmapArray;
	}

	public static void writeArrayOut(long[][] array) {
		try {
			PrintWriter writer = new PrintWriter("heatmap.txt");

			writer.print(array.length);
			writer.print(' ');
			writer.println(array[0].length);

			for (int i = 0; i < array.length; i++) {
				writer.print(array[i][0]);
				for (int j = 1; j < array[0].length; j++) {
					writer.print(' ');
					writer.print(array[i][j]);
				}
				writer.println();
			}

			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Image createImageFromArray(long[][] array) {
        BufferedImage image = new BufferedImage(array.length, array[0].length, BufferedImage.TYPE_INT_RGB);

        Graphics g = image.getGraphics();

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                g.setColor(new Color((int)array[i][j],(int)array[i][j],(int)array[i][j]));
                g.drawLine(i,j,i,j);
            }
        }

        try {
            ImageIO.write(image, "png", new File("heatmap.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return image;
	}
}
