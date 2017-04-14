package gsvannotation.db;

import java.util.Date;
import java.util.List;

public class Panorama {
	private String panoramaId;
	private double lat;
	private double lng;
	private String image;
	List<BoundingBox> boundingBoxes;
	private String description;
	private String region;
	private String country;
	private Date gsvImageDate;
	private int imapSpeciesId;
	private boolean noInvasives; /// a flag indicating no invasive plants found in this panorama
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}
	public void setBoundingBoxes(List<BoundingBox> boundingBoxes) {
		this.boundingBoxes = boundingBoxes;
	}
	public String getPanoramaId() {
		return panoramaId;
	}
	public void setPanoramaId(String panoId) {
		this.panoramaId = panoId;
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
	public Date getGsvImageDate() {
		return gsvImageDate;
	}
	public void setGsvImageDate(Date gsvDate) {
		this.gsvImageDate = gsvDate;
	}
	public int getImapSpeciesId() {
		return imapSpeciesId;
	}
	public void setImapSpeciesId(int imapSpeciesId) {
		this.imapSpeciesId = imapSpeciesId;
	}
	public boolean getNoInvasives() {
		return noInvasives;
	}
	public void setNoInvasives(boolean noInvasives) {
		this.noInvasives = noInvasives;
	}
	
}

