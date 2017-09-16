package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class Reward extends BaseEntity {
	private int memberId;
	private int count;
	private boolean got;
	private int source;

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isGot() {
		return got;
	}

	public void setGot(boolean got) {
		this.got = got;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

}
