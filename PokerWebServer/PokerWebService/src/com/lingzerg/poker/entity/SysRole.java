package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class SysRole extends BaseEntity {
	
	private String display;

	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
}
