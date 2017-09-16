package com.lingzerg.gamecenter.entity;

public class RoomInfo extends BaseEntity {
	private int maxCount;
	private int playingCount;
	private int bigBlind;
	private int smallBlind;
	private int bigTake;
	private int smallTake;

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public int getPlayingCount() {
		return playingCount;
	}

	public void setPlayingCount(int playingCount) {
		this.playingCount = playingCount;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public void setBigBlind(int bigBlind) {
		this.bigBlind = bigBlind;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public void setSmallBlind(int smallBlind) {
		this.smallBlind = smallBlind;
	}

	public int getBigTake() {
		return bigTake;
	}

	public void setBigTake(int bigTake) {
		this.bigTake = bigTake;
	}

	public int getSmallTake() {
		return smallTake;
	}

	public void setSmallTake(int smallTake) {
		this.smallTake = smallTake;
	}

}
