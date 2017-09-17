package gsvannotation.db;

import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
			List<Panorama> panos = conn.createQuery(
					"select panoramaId, lat, lng, image, description, country, region, gsvImageDAte, imap_species_speciesId, noInvasives "+
					"from panorama " +
                    "where gsvImageDate > '2009-12-31' order by region, panoramaId")
					.executeAndFetch(Panorama.class);
			for( Panorama pano : panos ) {
				pano.setBoundingBoxes( getPanoBoundingBoxes(pano.getPanoramaId()));
                                pano.setCandidateBoxes( getPanoCandidateBoxes( pano.getPanoramaId() ) );
			}
			return panos;
		}
	}

	@Override
	public List<Panorama> getPanosByIMapSpeciesId(int speciesId) {
		try(Connection conn = sql2o.open()) {
			List<Panorama> panoramas = conn.createQuery(
					"select panoramaId, lat, lng, image, description, country, region, gsvImageDAte, imap_species_speciesId, noInvasives "+
					"from panorama " +
					" where imap_species_speciesId = :speciesId and gsvImageDate > '2009-12-31' "+
                    "order by region, panoramaId")
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
			List<Panorama> panoramas = conn.createQuery(
					"select panoramaId, lat, lng, image, description, country, region, gsvImageDAte, imap_species_speciesId, noInvasives "+
					"from panorama " +
					" where panoramaId in "+
					"(select panorama_panoramaId from bounding_box where species_speciesId = :speciesId) " +
					"and gsvImageDate > '2009-12-31' "+
                    "order by region, panoramaId")
					.addParameter("speciesId", speciesId)
					.executeAndFetch(Panorama.class);
			for (Panorama panorama : panoramas) {
				panorama.setBoundingBoxes(getPanoBoundingBoxes(panorama.getPanoramaId()));
			}

			return panoramas;
		}
		
	}

        private int getNumberOfBoxes(int speciesId, int statusId) {
          try( Connection conn = sql2o.open() ) {
            return conn.createQuery("select count(b.species_speciesId) from bounding_box b " +
              "where species_speciesId = :speciesId and " +
              "bounding_box_status_statusId = :statusId ")
              .addParameter("speciesId", speciesId)
              .addParameter("statusId", statusId)
              .executeScalar(Integer.class);
          }
        }


	@Override
	public List<Species> getAllSpecies() {
		try( Connection conn = sql2o.open() ) {
			List<Species> species = conn.createQuery("select * from species")
					.executeAndFetch(Species.class);
                        for( Species sp : species ) {
                          sp.setGroundTruthBoxes(getNumberOfBoxes(sp.getSpeciesId(), Species.GROUND_TRUTH_STATUS));
                          sp.setCandidateBoxes(getNumberOfBoxes(sp.getSpeciesId(), Species.CANDIDATE_STATUS));
                          sp.setConfirmedBoxes(getNumberOfBoxes(sp.getSpeciesId(), Species.CONFIRMED_STATUS));
                          sp.setRejectedBoxes(getNumberOfBoxes(sp.getSpeciesId(), Species.REJECTED_STATUS));
                        }
			return species;
		}
	}

	@Override
	public List<BoundingBox> getPanoBoundingBoxes(String panoId) {
		try( Connection conn = sql2o.open() ) {
			List<BoundingBox> boundingBoxes = conn.createQuery("select boxId, species_speciesId, topleftX, topleftY,  bottomrightX, bottomrightY " + 
					" from bounding_box where panorama_panoramaId = :panoid and "+
					" bounding_box_status_statusid in ( 1, 3) ")
					.addParameter("panoid",  panoId)
					.executeAndFetch(BoundingBox.class);
			return boundingBoxes;
		}
	}
	
	@Override
	public List<BoundingBox> getPanoCandidateBoxes(String panoId) {
		try( Connection conn = sql2o.open() ) {
			List<BoundingBox> boundingBoxes = conn.createQuery("select boxId, species_speciesId, topleftX, topleftY,  bottomrightX, bottomrightY " + 
					" from bounding_box where panorama_panoramaId = :panoid and "+
					" bounding_box_status_statusid = 2 ")
					.addParameter("panoid",  panoId)
					.executeAndFetch(BoundingBox.class);
			return boundingBoxes;
		}		
	}
	
	public List<Panorama> getCandidates() {
		try( Connection conn = sql2o.beginTransaction() ) {
			List<Panorama> panoramas = conn.createQuery(
					"select p.panoramaId, p.lat, p.lng, p.image, p.description, p.country, p.region, p.gsvImageDAte, p.imap_species_speciesId, p.noInvasives "+
					"from panorama p, bounding_box bb " +
					"where bb.bounding_box_status_statusid = 2 "+
					" and p.panoramaId = bb.panorama_panoramaId " +
					"order by rand() limit 10")
					.executeAndFetch( Panorama.class );
			
			for(Panorama panorama : panoramas ) { 
				panorama.setBoundingBoxes(
						conn.createQuery("select boxId, species_speciesId, topleftX, topleftY,  bottomrightX, bottomrightY " + 
								" from bounding_box where panorama_panoramaId = :panoid and "+
								" bounding_box_status_statusid = 2 limit 1")
								.addParameter("panoid",  panorama.getPanoramaId())
								.executeAndFetch( BoundingBox.class ) );
//				for( BoundingBox box : panorama.getBoundingBoxes() ) {
//					conn.createQuery("update bounding_box set bounding_box_status_statusid = 5 " +
//							" where panorama_panoramaId = :panoramaId and " +
//							" topLeftX = :topLeftX and topLeftY = :topLeftY "  +
//							" and bottomRightX = :bottomRightX and bottomRightY = :bottomRightY")
//					.addParameter("panoramaId",  panorama.getPanoramaId())
//					.addParameter("topLeftX", box.getTopLeftX())
//					.addParameter("topLeftY",  box.getTopLeftY())
//					.addParameter("bottomRightX",  box.getBottomRightX())
//					.addParameter("bottomRightY",  box.getBottomRightY())
//					.executeUpdate();
//				}
			}		
			conn.commit();
			return panoramas;
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
			Panorama pano = conn.createQuery(
					"select panoramaId, lat, lng, image, description, country, region, gsvImageDAte, imap_species_speciesId, noInvasives "+
					"from panorama " +
					"where panoramaId = :panoid")
					.addParameter("panoid",  panoId)
					.executeAndFetchFirst(Panorama.class);
			if( pano != null ) {
				pano.setBoundingBoxes(getPanoBoundingBoxes(pano.getPanoramaId()));
				pano.setCandidateBoxes(getPanoCandidateBoxes(pano.getPanoramaId()));
			}
			return pano;
		}
	}

	@Override
	public void updatePanorama(Panorama pano) {
		try ( Connection conn = sql2o.beginTransaction() ) {
			Panorama panoBeforeUpdate = getPanorama(pano.getPanoramaId());
			for( BoundingBox bb : pano.getBoundingBoxes() ) {
				if( bb.getBoxId() == -1 ) {
					conn.createQuery("insert into bounding_box ( panorama_panoramaId, species_speciesId, topleftX, topleftY, bottomrightX, bottomrightY, "+
							"bounding_box_status_statusid, modifiedDate, createdDate) " + 
							" VALUES ( :panoId, :speciesId, :tlX, :tlY, :brX, :brY, 1, :modifiedDate, :createdDate ) ")
						.addParameter("panoId",  pano.getPanoramaId())
						.addParameter("speciesId",  bb.getSpeciesId())
						.addParameter("tlX",  bb.getTopLeftX())
						.addParameter("tlY",  bb.getTopLeftY())
						.addParameter("brX",  bb.getBottomRightX())
						.addParameter("brY",  bb.getBottomRightY())
						.addParameter("modifiedDate", new Date())
						.addParameter("createdDate",  new Date())
						.executeUpdate();
				} else {
					conn.createQuery("update bounding_box set "+
							"species_speciesId = :speciesId, " +
							"topleftX = :tlX, " +
							"topleftY = :tlY, " +
							"bottomRightX = :brX, " +
							"bottomRightY = :brY, " +
							"modifiedDate = :modifiedDate "+
							"where boxId = :boxId")
					.addParameter("boxId",  bb.getBoxId())
					.addParameter("speciesId",  bb.getSpeciesId())
					.addParameter("tlX",  bb.getTopLeftX())
					.addParameter("tlY",  bb.getTopLeftY())
					.addParameter("brX",  bb.getBottomRightX())
					.addParameter("brY",  bb.getBottomRightY())
					.addParameter("modifiedDate",  new Date())
					.executeUpdate();
				}
			}
			// figure out which boxes need to be deleted
			HashSet<Integer> oldBoxIds = new HashSet<Integer>();
			for( BoundingBox box : panoBeforeUpdate.getBoundingBoxes() ) {
				oldBoxIds.add(box.getBoxId());
			}
			HashSet<Integer> newBoxIds = new HashSet<Integer>();
			for( BoundingBox box : pano.getBoundingBoxes() ) {
				newBoxIds.add(box.getBoxId());
			}
			
			oldBoxIds.removeAll(newBoxIds);
			for(Integer boxIdToDelete : oldBoxIds ) {
				conn.createQuery("delete from bounding_box where boxId = :boxId")
					.addParameter("boxId",  boxIdToDelete.toString())
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
			// We can't do a query with both the region and panoramaId in the where clause
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

	@Override
	public void confirmCandidate(String boxId) {
		try( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("update bounding_box set bounding_box_status_statusid = 3, " +
                          "modifiedDate = :modifiedDate where boxId = :boxId")
				.addParameter("boxId", boxId)
				.addParameter("modifiedDate", new Date())
				.executeUpdate();
			conn.commit();
		}
	}

	@Override
	public void rejectCandidate(String boxId) {
		try( Connection conn = sql2o.beginTransaction() ) {
			conn.createQuery("update bounding_box set bounding_box_status_statusid = 4, " +
                          "modifiedDate = :modifiedDate where boxId = :boxId")
				.addParameter("boxId", boxId)
				.addParameter("modifiedDate", new Date())
				.executeUpdate();
			conn.commit();
		}		
	}
	

}
