package gsvannotation.db;

import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sql2oModel implements Model {
	private Sql2o sql2o;
	
	public Sql2oModel(Sql2o sql2o) {
		this.sql2o = sql2o;
	}

	@Override
	public void insertPanorama(Panorama pano) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("insert into panorama ( panoramaId, lat, lng, description, region, country, gsvImageDate, imap_species_speciesId, noInvasives ) " + 
					" VALUES ( :panoramaId, :lat, :lng, :description, :region, :country, :gsvDate, :imap_speciesId, :noInvasives ) ")
				.addParameter("panoramaId",  pano.getPanoramaId())
				.addParameter("lat",  pano.getLat())
				.addParameter("lng",  pano.getLng())
				.addParameter("description",  pano.getDescription())
				.addParameter("region",  pano.getRegion())
				.addParameter("country",  pano.getCountry())
				.addParameter("gsvDate", pano.getGsvImageDate())
				.addParameter("imap_speciesId", pano.getImapSpeciesId())
				.addParameter("noInvasives",  pano.getNoInvasives() )
				.executeUpdate();
			conn.commit();
		}
	}

	@Override
	public List<Panorama> getAllPanos() {
		try( Connection conn = sql2o.open()) {
			List<Panorama> panos = conn.createQuery("select * from panorama " +
                          "where gsvImageDate > '2009-12-31' order by region, panoramaId")
					.executeAndFetch(Panorama.class);
			for( Panorama pano : panos ) {
				pano.setBoundingBoxes( getPanoBoundingBoxes(pano.getPanoramaId()));
			}
			return panos;
		}
	}

	@Override
	public List<Panorama> getPanosByIMapSpeciesId(int speciesId) {
		try(Connection conn = sql2o.open()) {
			List<Panorama> panoramas = conn.createQuery("select p.* from panorama p" +
					" where p.imap_species_speciesId = :speciesId and p.gsvImageDate > '2009-12-31' "+
                                        "order by p.region, p.panoramaId")
					.addParameter("speciesId", speciesId)
					.executeAndFetch(Panorama.class);
			for (Panorama panorama : panoramas) {
				panorama.setBoundingBoxes(getPanoBoundingBoxes(panorama.getPanoramaId()));
			}

			return panoramas;
		}
	}
	
	@Override
	public List<Panorama> getPanosByBoundingBoxSpeciesId( int speciesId ) {
		try(Connection conn = sql2o.open()) {
			List<Panorama> panoramas = conn.createQuery("select p.* from panorama p" +
					" where p.panoramaId in "+
					"(select panorama_panoramaId from bounding_box where species_speciesId = :speciesId) " +
					"and p.gsvImageDate > '2009-12-31' "+
                    "order by p.region, p.panoramaId")
					.addParameter("speciesId", speciesId)
					.executeAndFetch(Panorama.class);
			for (Panorama panorama : panoramas) {
				panorama.setBoundingBoxes(getPanoBoundingBoxes(panorama.getPanoramaId()));
			}

			return panoramas;
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
					.executeAndFetch(BoundingBox.class);
			return boundingBoxes;
		}
	}

	@Override
	public Map<Integer, Integer> getNumberOfBoundingBoxesPerSpecies() {
		Map<Integer, Integer> map = new HashMap<>();

		List<SpeciesAndBoundingBoxCount> speciesList = new ArrayList<>();

		try(Connection connection = sql2o.open() ) {
			 speciesList = connection.createQuery("select s.*, count(bb.species_speciesId) bb_count " +
					 "from species s " +
					 "left join bounding_box bb on s.speciesId = bb.species_speciesId " +
					 "group by s.speciesId")
					.executeAndFetch(new ResultSetHandler<SpeciesAndBoundingBoxCount>() {
						@Override
						public SpeciesAndBoundingBoxCount handle(ResultSet resultSet) throws SQLException {
							SpeciesAndBoundingBoxCount s = new SpeciesAndBoundingBoxCount();
							s.setSpeciesId(resultSet.getInt("speciesId"));
							s.setDescription(resultSet.getString("description"));
							s.setBoundingBoxCount(resultSet.getInt("bb_count"));
							return s;
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (SpeciesAndBoundingBoxCount s :
				speciesList) {
			map.put(s.getSpeciesId(), s.getBoundingBoxCount());
		}

		return map;
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
	public void updatePanorama(Panorama pano) {
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
			
			conn.createQuery("update panorama set noInvasives = :noInvasives where panoramaId= :panoramaId")
				.addParameter("panoramaId",  pano.getPanoramaId() )
				.addParameter("noInvasives",  pano.getNoInvasives() )
				.executeUpdate();
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

	//TODO: get rid of this class, just add bb count to Species
	public static class SpeciesAndBoundingBoxCount extends Species {
		private int boundingBoxCount;

		public SpeciesAndBoundingBoxCount() {
			super();
		}

		public int getBoundingBoxCount() {
			return boundingBoxCount;
		}

		public void setBoundingBoxCount(int boundingBoxCount) {
			this.boundingBoxCount = boundingBoxCount;
		}
	}

	@Override
	public void deletePanorama(String panoramaId) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("delete from bounding_box where panorama_panoramaId = :id")
				.addParameter("id",  panoramaId).executeUpdate();
			conn.createQuery("delete from panorama where panoramaId = :id")
				.addParameter("id",  panoramaId).executeUpdate();
			conn.commit();
		}		
	}
	
	@Override
	public String findNextPanorama( String currentPanoramaId ) {
		String panoramaId = "";
		try( Connection conn = sql2o.open() ) {
                        Panorama currentPanorama = getPanorama( currentPanoramaId );
                        // This should return things in the same order as the list in the index page
                        // It is meant to be called from the edit panorama page and return the next panorama
                        // We can't do a query that with both the region and panoramaId in the where clause
                        // if you're on the last panorama for the current region, you will get inconsistent results
                        // In practice, there are usually only around 10-20 panoramas per region, so looping over 
                        // these panoramas to find the next one is not terrible
			List<String> panoramaIds = conn.createQuery("select p.panoramaId from panorama p "+
					"where p.gsvImageDate > '2009-12-31' and p.region >= :currentRegion " +
					"order by region, panoramaId ")
                                .addParameter("currentRegion", currentPanorama.getRegion())
				.executeAndFetch(String.class);
			if( panoramaIds == null ) {
				panoramaId = "";
			} else { 
                                int indexOfCurrentPanoramaId = panoramaIds.indexOf( currentPanoramaId );
                                if( indexOfCurrentPanoramaId == -1 || indexOfCurrentPanoramaId == panoramaIds.size()-1 ) {
                                    panoramaId="";
                                } else {
                                  panoramaId = panoramaIds.get(indexOfCurrentPanoramaId+1);
                                }

                        }
		}
		return panoramaId;
	}
}
