package gsvannotation.db;

import java.util.List;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oModel implements Model {
	private Sql2o sql2o;
	
	public Sql2oModel(Sql2o sql2o) {
		this.sql2o = sql2o;
	}

	@Override
	public void insertPanorama(String panoId, double lat, double lng) {
		try ( Connection conn = sql2o.beginTransaction()) {
			conn.createQuery("insert into gsv_panorama ( panoId, lat, lng ) " + 
					" VALUES ( :panoId, :lat, :lng ) ")
				.addParameter("panoId",  panoId)
				.addParameter("lat",  lat)
				.addParameter("lng",  lng)
				.executeUpdate();
		}
	}

	@Override
	public List<Panorama> getAllPanos() {
		try( Connection conn = sql2o.open()) {
			List<Panorama> panos = conn.createQuery("select * from gsv_panorama")
					.executeAndFetch(Panorama.class);
			return panos;
		}
	}

	@Override
	public List<Species> getAllSpecies() {
		try( Connection conn = sql2o.open() ) {
			List<Species> species = conn.createQuery("select * from species")
					.executeAndFetch(Species.class);
			return species;
		}
	}

	@Override
	public List<BoundingBox> getPanoBoundingBoxes(String panoId) {
		try( Connection conn = sql2o.open() ) {
			List<BoundingBox> boundingBoxes = conn.createQuery("select * from bounding_box where gsv_pano_panoid = :panoid")
					.addParameter("panoid",  panoId)
					.executeAndFetch(BoundingBox.class);
			return boundingBoxes;
		}
	}

	@Override
	public Panorama getPanorama(String panoId) {
		try( Connection conn = sql2o.open() ) {
			Panorama pano = conn.createQuery("select * from gsv_pano where panoId = :panoid")
					.addParameter("panoid",  panoId)
					.executeAndFetchFirst(Panorama.class);
			return pano;
		}
	}
	
}
