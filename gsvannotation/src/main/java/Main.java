import com.fasterxml.jackson.databind.ObjectMapper;
import gsvannotation.db.Model;
import gsvannotation.db.Panorama;
import gsvannotation.db.Species;
import gsvannotation.db.Sql2oModel;
import org.sql2o.Sql2o;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import static spark.Spark.*;

public class Main {
	
	public static String getPanoId(double lat, double lng) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/latlng2pano.py " +lat + " " + lng);
			p.waitFor();
			
			BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ));
			
			String panoId = stdInput.readLine();
			if(panoId.contains("Traceback")) {
				panoId = "";
			}
			return panoId;
		} catch( Exception e ) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void main(String[] args) {
		if( args.length != 4 ) {
			System.out.println("usage: java -jar gsvannotation.jar [dbhost] [dbusername] [dbpassword] [dbport]");
			return;
		}
		String dbhost = args[0];
		String dbusername = args[1];
		String dbpassword = args[2];
		String dbport = args[3];
		
	    Sql2o sql2o = new Sql2o("jdbc:mysql://" + dbhost + ":" + dbport + "/invasivespecies", 
	            dbusername, dbpassword);

		Model model = new Sql2oModel(sql2o);

		// for converting POJOs to json
		ObjectMapper jsonMapper = new ObjectMapper();
		
		externalStaticFileLocation("external");
		
		staticFileLocation("/public");
		
		get("/getPano", (request, response) -> {
			
			// assumes jar is run from a folder containing a panos subfolder
			// checks if panoid panoarama image exists under panos subfolder
			// if it doesn't exist, it downloads it using the python script
			String panoId = request.queryParams("panoId");
			Panorama pano = model.getPanorama(panoId);
			
			File panoImage = new File("external/pano_images/" + panoId + "_z3.jpg");
			if( !panoImage.exists() ) {
				Process p = Runtime.getRuntime().exec("python src/main/python/getPanoImage.py " + 
						pano.getLat() + " " + pano.getLng());
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
				if (s.getId() == id) {
					specie = s;
				}
			}

			return jsonMapper.writeValueAsString( specie );
		});

		get("/createPano", (request, response) -> {
			String panoId = request.queryParams("panoId");
			double lat = Double.parseDouble( request.queryParams("lat") );
			double lng = Double.parseDouble( request.queryParams("lng") );
			
			model.insertPanorama(panoId,  lat,  lng);
			return null;
		});
		
		get("/getBoundingBoxes/:panoid", (request, response) -> {
			// returns (json maybe) bounding boxes for requested panoid
			return null;
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
		
		get("/getAllSpecies", (request, response) -> {
			List<Species> species = model.getAllSpecies();
			return jsonMapper.writeValueAsString( species );
		});
		
		get("/latlng2pano", (request, response) -> {
			double lat = Double.parseDouble(request.queryParams("lat"));
			double lng = Double.parseDouble(request.queryParams("lng"));
			String panoId = getPanoId(lat,  lng);
			if( panoId != "" ) {
				Panorama pano = model.getPanorama(panoId);
				if( pano == null ) {
					model.insertPanorama(panoId,  lat,  lng);
				}
			}
			return panoId;
		});
	}
}
