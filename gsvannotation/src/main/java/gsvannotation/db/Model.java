package gsvannotation.db;

import java.util.List;
import java.util.Map;

public interface Model {
	Panorama getPanorama(String panoId);
	void insertPanorama(Panorama pano);
	void updatePanoramaBoundingBoxes(Panorama pano);
	List<Panorama> getAllPanos();
	List<Panorama> getPanosBySpeciesId(int speciesId);


	List<Species> getAllSpecies();
	void updateSpecies(Species species);
	List<BoundingBox> getPanoBoundingBoxes(String panoId);

	Map<Integer, Integer> getNumberOfBoundingBoxesPerSpecies();

}
