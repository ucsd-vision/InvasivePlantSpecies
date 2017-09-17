package gsvannotation.db;

import java.util.Date;

public class BoundingBox {
	private int boxId;
	private int speciesId;
	private int topLeftX;
	private int topLeftY;
	private int bottomRightX;
	private int bottomRightY;
	private int userId;
	private Date createTime;
	private boolean groundTruth = false;
	private boolean candidate = false;
	private boolean confirmed = false;
	private boolean rejected = false;
	
	private static final int STATUS_GROUND_TRUTH=1;
	private static final int STATUS_CANDIDATE=2;
	private static final int STATUS_CANDIDATE_CONFIRMED=3;
	private static final int STATUS_CANDIDATE_REJECTED=1;
	
	
	private int statusId;
	
	public int getBoxId() {
		return boxId;
	}
	
	public void setBoxId(int boxId) {
		this.boxId = boxId;
	}

	public int getSpeciesId() {
		return speciesId;
	}

	public void setSpeciesId(int speciesId) {
		this.speciesId = speciesId;
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public void setTopLeftX(int topLeftX) {
		this.topLeftX = topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public void setTopLeftY(int topLeftY) {
		this.topLeftY = topLeftY;
	}

	public int getBottomRightX() {
		return bottomRightX;
	}

	public void setBottomRightX(int bottomRightX) {
		this.bottomRightX = bottomRightX;
	}

	public int getBottomRightY() {
		return bottomRightY;
	}

	public void setBottomRightY(int bottomRightY) {
		this.bottomRightY = bottomRightY;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public void setStatusId(int statusId) {
		this.statusId = statusId;
		if( statusId == STATUS_GROUND_TRUTH ) {
			this.groundTruth = true;
			this.candidate = false;
			this.confirmed = false;
			this.rejected = false;
		} else if( statusId == STATUS_CANDIDATE ) {
			this.groundTruth = false;
			this.candidate = true;
			this.confirmed = false;
			this.rejected = false;
		} else if( statusId == STATUS_CANDIDATE_CONFIRMED ) {
			this.groundTruth = false;
			this.candidate = true;
			this.confirmed = true;
			this.rejected = false;
		} else if( statusId == STATUS_CANDIDATE_REJECTED ) {
			this.groundTruth = false;
			this.candidate = true;
			this.confirmed = false;
			this.rejected = true;
		}
	}

	public boolean isGroundTruth() {
		return groundTruth;
	}

	public boolean isCandidate() {
		return candidate;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public boolean isRejected() {
		return rejected;
	}

	public int getStatusId() {
		return statusId;
	}

}
