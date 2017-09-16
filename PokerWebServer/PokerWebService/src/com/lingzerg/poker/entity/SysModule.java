package com.lingzerg.poker.entity;

import javax.persistence.Entity;

@Entity
public class SysModule extends BaseEntity {
	private int parentId;
	private String Name;
	private String Url;
	private String icon;
	private int squence;
	private boolean nonremovable;
	
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getSquence() {
		return squence;
	}
	public void setSquence(int squence) {
		this.squence = squence;
	}
	public boolean isNonremovable() {
		return nonremovable;
	}
	public void setNonremovable(boolean nonremovable) {
		this.nonremovable = nonremovable;
	}

}
