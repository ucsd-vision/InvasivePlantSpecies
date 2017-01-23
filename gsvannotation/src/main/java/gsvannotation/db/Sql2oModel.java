package gsvannotation.db;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

public class Sql2oModel implements Model {
	private Sql2o sql2o;
	
	public Sql2oModel(Sql2o sql2o) {
		this.sql2o = sql2o;
	}

	@Override
	public void insertPanorama(Panorama pano) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("insert into panorama ( panoramaId, lat, lng, description, region, country ) " + 
					" VALUES ( :panoramaId, :lat, :lng, :description, :region, :country ) ")
				.addParameter("panoramaId",  pano.getPanoramaId())
				.addParameter("lat",  pano.getLat())
				.addParameter("lng",  pano.getLng())
				.addParameter("description",  pano.getDescription())
				.addParameter("region",  pano.getRegion())
				.addParameter("country",  pano.getCountry())
				.executeUpdate();
			conn.commit();
		}
	}

	@Override
	public List<Panorama> getAllPanos() {
		try( Connection conn = sql2o.open()) {
			List<Panorama> panos = conn.createQuery("select * from panorama")
					.executeAndFetch(Panorama.class);
			for( Panorama pano : panos ) {
				pano.setBoundingBoxes( getPanoBoundingBoxes(pano.getPanoramaId()));
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
			List<BoundingBox> boundingBoxes = conn.createQuery("select species_speciesId, topleftX, topleftY,  bottomrightX, bottomrightY " + 
					" from bounding_box where panorama_panoramaId = :panoid")
					.addParameter("panoid",  panoId)
					.addColumnMapping("species_speciesId", "speciesId")
					.executeAndFetch(BoundingBox.class);
			return boundingBoxes;
		}
	}

	@Override
	public Panorama getPanorama(String panoId) {
		try( Connection conn = sql2o.open() ) {
			Panorama pano = conn.createQuery("select * from panorama where panoramaId = :panoid")
					.addParameter("panoid",  panoId)
					.executeAndFetchFirst(Panorama.class);
			if( pano != null ) {
				pano.setBoundingBoxes(getPanoBoundingBoxes(pano.getPanoramaId()));
			}
			return pano;
		}
	}

	@Override
	public void updatePanoramaBoundingBoxes(Panorama pano) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("delete from bounding_box where panorama_panoramaId = :panoId")
				.addParameter("panoId",  pano.getPanoramaId())
				.executeUpdate();
			for( BoundingBox bb : pano.getBoundingBoxes() ) {
				conn.createQuery("insert into bounding_box ( panorama_panoramaId, species_speciesId, topleftX, topleftY, bottomrightX, bottomrightY ) " + 
						" VALUES ( :panoId, :speciesId, :tlX, :tlY, :brX, :brY ) ")
					.addParameter("panoId",  pano.getPanoramaId())
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

	@Override
	public void updateSpecies(Species species) {

		try ( Connection conn = sql2o.beginTransaction() ) {
			if (species.getSpeciesId() == -1) {
				conn.createQuery("insert into species ( description ) " +
							" VALUES ( :description ) ")
						.addParameter("description", species.getDescription())
						.executeUpdate();
				conn.commit();
			} else {
				conn.createQuery("update species set description = :description where speciesId = :id")
						.addParameter("description", species.getDescription())
						.addParameter("id", species.getSpeciesId())
						.executeUpdate();
				conn.commit();
			}
		}
	}

}
