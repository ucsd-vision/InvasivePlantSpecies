package gsvannotation.db;

import java.util.List;
import java.util.Map;

public interface Model {
	Panorama getPanorama(String panoId);
	void insertPanorama(Panorama pano);
	void updatePanorama(Panorama pano);
	void confirmCandidate(String boxId);
	void rejectCandidate(String boxId);
	
	List<Panorama> getAllPanos();
	List<Panorama> getPanosByIMapSpeciesId(int speciesId);
	List<Panorama> getPanosByBoundingBoxSpeciesId( int speciesId );
	void deletePanorama(String panoramaId);
	String findNextPanorama( String currentPanoramaId );

	List<Species> getAllSpecies();
	void updateSpecies(Species species);
	List<BoundingBox> getPanoBoundingBoxes(String panoId);
	List<BoundingBox> getPanoCandidateBoxes(String panoId);
	List<Panorama> getCandidates();
	Map<Integer, Integer> getNumberOfBoundingBoxesPerSpecies();

}
