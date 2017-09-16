package poker.entity;

public class RoomInfo extends BaseEntity {
	private int seatCount;
	private int smallBlind;
	private int bigBlind;
	private int minTake;
	private int maxTake;
	private int averageTake;
	private int type;

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public void setSmallBlind(int smallBlind) {
		this.smallBlind = smallBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public void setBigBlind(int bigBlind) {
		this.bigBlind = bigBlind;
	}

	public int getMinTake() {
		return minTake;
	}

	public void setMinTake(int minTake) {
		this.minTake = minTake;
	}

	public int getMaxTake() {
		return maxTake;
	}

	public void setMaxTake(int maxTake) {
		this.maxTake = maxTake;
	}

	public int getAverageTake() {
		return averageTake;
	}

	public void setAverageTake(int averageTake) {
		this.averageTake = averageTake;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
