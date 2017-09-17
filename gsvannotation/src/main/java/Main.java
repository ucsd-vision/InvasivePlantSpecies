import com.fasterxml.jackson.databind.ObjectMapper;
import gsvannotation.db.*;
import org.sql2o.Sql2o;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Main {

	public static Panorama latlng2pano(double lat, double lng) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/latlng2pano.py " + lat + " " + lng);
			p.waitFor();

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// for converting POJOs to json
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
			String json = stdInput.lines().collect(Collectors.joining());
			if (json.contains("none")) {
				return null;
			} else {
				Panorama pano = jsonMapper.readValue(json, Panorama.class);
				return pano;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Panorama panoId2pano(String panoId) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/panoId2pano.py " + panoId);
			p.waitFor();

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// for converting POJOs to json
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
			String json = stdInput.lines().collect(Collectors.joining());
			if (json.contains("none")) {
				return null;
			} else {
				Panorama pano = jsonMapper.readValue(json, Panorama.class);
				return pano;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		if (args.length != 6) {
			System.out.println(
					"usage: java -jar gsvannotation.jar [dbhost] [dbname] [dbusername] [dbpassword] [dbport] [webport]");
			return;
		}
		String dbhost = args[0];
		String dbName = args[1];
		String dbusername = args[2];
		String dbpassword = args[3];
		String dbport = args[4];
		String webport = args[5];

		Sql2o sql2o = new Sql2o(
				"jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbName
						+ "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
				dbusername, dbpassword);

		// these column mappings get applied to all queries
		Map<String, String> columnMaps = new HashMap<String, String>();
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

		port(Integer.parseInt(webport));

		get("/getPano", (request, response) -> {

			// assumes jar is run from a folder containing a panos subfolder
			// checks if panoid panoarama image exists under panos subfolder
			// if it doesn't exist, it downloads it using the python script
			String panoramaId = request.queryParams("panoramaId");
			Panorama pano = model.getPanorama(panoramaId);

			File panoImage = new File("external/pano_images/" + panoramaId + "_z3.jpg");
			if (!panoImage.exists()) {
				Process p = Runtime.getRuntime().exec("python src/main/python/getPanoImage.py " + pano.getPanoramaId());
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
			for (Species s : species) {
				if (s.getSpeciesId() == id) {
					specie = s;
				}
			}

			return jsonMapper.writeValueAsString(specie);
		});

		post("/savePano", (request, response) -> {
			String body = request.body();
			Panorama pano = jsonMapper.readValue(body, Panorama.class);
			model.updatePanorama(pano);
			response.status(200);
			return "ok";
		});

		post("/saveSpecies", (request, response) -> {
			String body = request.body();
			Species species = jsonMapper.readValue(body, Species.class);
			model.updateSpecies(species);
			response.status(200);
			return "ok";
		});

		post("confirmCandidate", (request, response) -> {
			String boxId = request.queryParams("boxId");
			model.confirmCandidate(boxId);
			return "ok";
		});
		
		post("rejectCandidate", (request, response) -> {
			String boxId = request.queryParams("boxId");
			model.rejectCandidate(boxId);
			return "ok";
		});

		get("/getAllPanos", (request, response) -> {
			List<Panorama> panos = model.getAllPanos();

			return jsonMapper.writeValueAsString(panos);
		});

		get("/getAllSpecies", (request, response) -> {
			List<Species> species = model.getAllSpecies();
			return jsonMapper.writeValueAsString(species);
		});

		get("panoId2pano", (request, response) -> {
			String panoId = request.queryParams("panoId");
			Panorama pano = panoId2pano(panoId);
			if (pano != null) {
				if (request.queryParams().contains("imapSpeciesId")) {
					pano.setImapSpeciesId(Integer.parseInt(request.queryParams("imapSpeciesId")));
				} else {
					pano.setImapSpeciesId(1); // default to phrag
				}
				// Checks to see if panorama is already in database
				if (model.getPanorama(pano.getPanoramaId()) == null) {
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
			Panorama pano = latlng2pano(lat, lng);
			// Check to see if a panorama exists for this lat/lng

			if (pano != null) {
				if (request.queryParams().contains("imapSpeciesId")) {
					pano.setImapSpeciesId(Integer.parseInt(request.queryParams("imapSpeciesId")));
				} else {
					pano.setImapSpeciesId(1); // default to phrag
				}
				// Checks to see if panorama is already in database
				if (model.getPanorama(pano.getPanoramaId()) == null) {
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
			int id = Integer.parseInt(speciesId);

			if (id == -1) {
				panoramas = model.getAllPanos();
			} else {
				panoramas = model.getPanosByBoundingBoxSpeciesId(id);
			}

			return jsonMapper.writeValueAsString(panoramas);
		});

		get("getNumberOfBoundingBoxesPerSpecies",
				(request, response) -> jsonMapper.writeValueAsString(model.getNumberOfBoundingBoxesPerSpecies()));

		get("findNextPanorama", (request, response) -> {
			String currentPanoramaId = request.queryParams("currentPanoramaId");
			return model.findNextPanorama(currentPanoramaId);
		});

		delete("deletePanorama", (request, response) -> {
			String panoramaId = request.queryParams("panoramaId");
			model.deletePanorama(panoramaId);
			return "";
		});
		
		get("getCandidates", (request, response) -> {
			List<Panorama> panoramas;
			if( request.queryParams().contains("panoramaId") ) {
				String panoramaId = request.queryParams("panoramaId");
				Panorama pano = model.getPanorama( panoramaId );
				panoramas = new ArrayList<Panorama>();
				panoramas.add( pano );
			} else {
				panoramas = model.getCandidates();
			}
			return jsonMapper.writeValueAsString(panoramas);
		});
		

	}

}
