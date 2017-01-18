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
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("insert into gsv_pano ( panoId, lat, lng ) " + 
					" VALUES ( :panoId, :lat, :lng ) ")
				.addParameter("panoId",  panoId)
				.addParameter("lat",  lat)
				.addParameter("lng",  lng)
				.executeUpdate();
			conn.commit();
		}
	}

	@Override
	public List<Panorama> getAllPanos() {
		try( Connection conn = sql2o.open()) {
			List<Panorama> panos = conn.createQuery("select * from gsv_pano")
					.executeAndFetch(Panorama.class);
			for( Panorama pano : panos ) {
				pano.setBoundingBoxes( getPanoBoundingBoxes(pano.getPanoId()));
			}
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
			List<BoundingBox> boundingBoxes = conn.createQuery("select species_id, topleftX, topleftY,  bottomrightX, bottomrightY " + 
					" from bounding_box where gsv_pano_panoid = :panoid")
					.addParameter("panoid",  panoId)
					.addColumnMapping("species_id", "speciesId")
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
			if( pano != null ) {
				pano.setBoundingBoxes(getPanoBoundingBoxes(pano.getPanoId()));
			}
			return pano;
		}
	}

	@Override
	public void updatePanorama(Panorama pano) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("delete from bounding_box where gsv_pano_panoid = :panoId")
				.addParameter("panoId",  pano.getPanoId())
				.executeUpdate();
			for( BoundingBox bb : pano.getBoundingBoxes() ) {
				conn.createQuery("insert into bounding_box ( gsv_pano_panoid, species_id, topleftX, topleftY, bottomrightX, bottomrightY ) " + 
						" VALUES ( :panoId, :speciesId, :tlX, :tlY, :brX, :brY ) ")
					.addParameter("panoId",  pano.getPanoId())
					.addParameter("speciesId",  bb.getSpeciesId())
					.addParameter("tlX",  bb.getTopLeftX())
					.addParameter("tlY",  bb.getTopLeftY())
					.addParameter("brX",  bb.getBottomRightX())
					.addParameter("brY",  bb.getBottomRightY())
					.executeUpdate();
			}
			conn.commit();
		}
		
	}
	
}
