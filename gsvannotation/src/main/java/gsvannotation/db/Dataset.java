package gsvannotation.db;

public class Dataset {
  private int datasetId;
  private String description;
  private String subfolder;

  
  public int getDatasetId() {
    return datasetId;
  }

  public void setDatsetId(int id) {
    this.datasetId = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSubfolder() {
    return subfolder;
  }

  public void setSubfolder(String subfolder) {
    this.subfolder = subfolder;
  }
}

