package gsvannotation.db;

import java.util.List;

public class Panorama {
	private String panoId;
	private double lat;
	private double lng;
	private String image;
	List<BoundingBox> boundingBoxes;
	
	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}
	public void setBoundingBoxes(List<BoundingBox> boundingBoxes) {
		this.boundingBoxes = boundingBoxes;
	}
	public String getPanoId() {
		return panoId;
	}
	public void setPanoId(String panoId) {
		this.panoId = panoId;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
}

