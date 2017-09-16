package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class Room extends BaseEntity {
	private int type; // 1 是初级 2 是中级 3 是高级
	private String title;
	private int maxPlayingCount;
	private int smallBlind;
	private int bigBlind;
	private int smallTake;
	private int bigTake;
	private int count;//该类房间要创建的数量

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMaxPlayingCount() {
		return maxPlayingCount;
	}

	public void setMaxPlayingCount(int maxPlayingCount) {
		this.maxPlayingCount = maxPlayingCount;
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

	public int getSmallTake() {
		return smallTake;
	}

	public void setSmallTake(int smallTake) {
		this.smallTake = smallTake;
	}

	public int getBigTake() {
		return bigTake;
	}

	public void setBigTake(int bigTake) {
		this.bigTake = bigTake;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
