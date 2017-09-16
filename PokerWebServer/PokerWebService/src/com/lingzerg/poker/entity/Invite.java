package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class Invite extends BaseEntity {
	private int memberId;
	private int parentId;

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

}
