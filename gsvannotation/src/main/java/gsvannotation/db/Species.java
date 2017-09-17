package gsvannotation.db;

public class Species {
	private int speciesId;
	private String description;
        private int groundTruthBoxes;
        private int candidateBoxes;
        private int confirmedBoxes;
        private int rejectedBoxes;

        public static int GROUND_TRUTH_STATUS = 1;
        public static int CANDIDATE_STATUS = 2;
        public static int CONFIRMED_STATUS = 3;
        public static int REJECTED_STATUS = 4;
	
	public int getSpeciesId() {
		return speciesId;
	}
	public void setSpeciesId(int id) {
		this.speciesId = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
        public void setGroundTruthBoxes(int groundTruthBoxes) {
          this.groundTruthBoxes=groundTruthBoxes;
        }
        public int getGroundTruthBoxes() {
          return groundTruthBoxes;
        }
        public void setCandidateBoxes(int candidateBoxes) {
          this.candidateBoxes = candidateBoxes;
        }
        public int getCandidateBoxes() {
          return candidateBoxes;
        }
        public void setConfirmedBoxes(int confirmedBoxes) {
          this.confirmedBoxes = confirmedBoxes;
        }
        public int getConfirmedBoxes() {
          return confirmedBoxes;
        }
        public void setRejectedBoxes(int rejectedBoxes) {
          this.rejectedBoxes = rejectedBoxes;
        }
        public int getRejectedBoxes() {
          return rejectedBoxes;
        }
}
