package gsvannotation.db;

import java.util.List;

public interface Model {
	Panorama getPanorama(String panoId);
	void insertPanorama(Panorama pano);
	void updatePanoramaBoundingBoxes(Panorama pano);
	List<Panorama> getAllPanos();


	List<Species> getAllSpecies();
	void updateSpecies(Species species);
	List<BoundingBox> getPanoBoundingBoxes(String panoId);

}
