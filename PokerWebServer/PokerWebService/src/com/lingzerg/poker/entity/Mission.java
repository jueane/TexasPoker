package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class Mission extends BaseEntity {
	private String title;
	private int target;
	private int reward;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

}
