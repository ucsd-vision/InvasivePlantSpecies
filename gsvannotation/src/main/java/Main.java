import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Main {
	
	public static String getPanoId(double lat, double lng) {
		try {
			Process p = Runtime.getRuntime().exec("python src/main/python/latlng2pano.py " +lat + " " + lng);
			p.waitFor();
			
			BufferedReader stdInput = new BufferedReader( new InputStreamReader( p.getInputStream() ));
			
			String panoId = stdInput.readLine();
			return panoId;
		} catch( Exception e ) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void main(String[] args) {
		staticFileLocation("/public");
		get("/hello", (req, res) -> "Hello World");
		
		get("/getpano", (request, response) -> {
			
			// assumes jar is run from a folder containing a panos subfolder
			// checks if panoid panoarama image exists under panos subfolder
			// if it doesn't exist, it downloads it using the python script
			
			double lat = Double.parseDouble(request.queryParams("lat"));
			double lng = Double.parseDouble(request.queryParams("lng"));
			String panoId = getPanoId(lat, lng);
			File panoImage = new File("panos/" + panoId + "_z4.jpg");
			if( panoImage.exists() ) {
				return "pano exists";
			} else {
				Process p = Runtime.getRuntime().exec("python src/main/python/getPanoImage.py " + lat + " " + lng);
				p.waitFor();
				return "pano did not exist";
			}
			
		});
		
		get("/getBoundingBoxes/:panoid", (request, response) -> {
			// returns (json maybe) bounding boxes for requested panoid
			return null;
		});
		
		post("/saveBoundingBox", (request, response) -> {
		
			return null;
		});

		get("/getAllPanos", (request, response) -> {
			return null;
		});
		
		get("/latlng2pano", (request, response) -> {
			double lat = Double.parseDouble(request.queryParams("lat"));
			double lng = Double.parseDouble(request.queryParams("lng"));
			String panoId = getPanoId(lat,  lng);
			return panoId;
		});
	}
}
