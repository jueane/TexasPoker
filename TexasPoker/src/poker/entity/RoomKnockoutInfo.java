package poker.entity;

public class RoomKnockoutInfo extends BaseEntity {
	private int seatCount;
	private int smallBlind;
	private int bigBlind;
	private int type;

	private int entryFee;
	private int initBankroll;
	private int[] reward;

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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getEntryFee() {
		return entryFee;
	}

	public void setEntryFee(int entryFee) {
		this.entryFee = entryFee;
	}

	public int getInitBankroll() {
		return initBankroll;
	}

	public void setInitBankroll(int initBankroll) {
		this.initBankroll = initBankroll;
	}

	public int[] getReward() {
		return reward;
	}

	public void setReward(int[] reward) {
		this.reward = reward;
	}

}
