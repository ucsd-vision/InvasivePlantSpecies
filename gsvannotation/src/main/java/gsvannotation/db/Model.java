package gsvannotation.db;

import java.util.List;
import java.util.Map;

public interface Model {
	Panorama getPanorama(String panoId);
	void insertPanorama(Panorama pano);
	void updatePanorama(Panorama pano);
	
	List<Panorama> getAllPanos();
	List<Panorama> getPanosByIMapSpeciesId(int speciesId);
	void deletePanorama(String panoramaId);
	String findUnannotatedPanorama();

	List<Species> getAllSpecies();
	void updateSpecies(Species species);
	List<BoundingBox> getPanoBoundingBoxes(String panoId);

	Map<Integer, Integer> getNumberOfBoundingBoxesPerSpecies();

}
