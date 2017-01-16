package gsvannotation.db;

import java.util.List;

import org.sql2o.Sql2o;

public class Sql2oModel implements Model {
	private Sql2o sql2o;

	@Override
	public void insertPanorama(String panoId, double lat, double lng) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Panorama> getAllPanos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Species> getAllSpecies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BoundingBox> getPanoBoundingBoxes(String panoId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Panorama getPanorama(String panoId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
