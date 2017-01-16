package gsvannotation.db;

import java.util.List;

public interface Model {
	void insertPanorama(String panoId, double lat, double lng);
	
	void addPanoBoundingBox(String panoId, int speciesId, 
			int topLeftX, int topLeftY, 
			int bottomRightX, int bottomRightY);
	
	List<Panorama> getAllPanos();

	List<Species> getAllSpecies();

	List<BoundingBox> getPanoBoundingBoxes(String panoId);

	Panorama getPanorama(String panoId);
}
