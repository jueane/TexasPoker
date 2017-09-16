package com.lingzerg.poker.entity;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class SysUser extends BaseEntity {
	private String adminname;
	private String password;
	private Date lastLoginDate;
	private String roleIdList;
	private boolean nonremovable;
	
	public String getAdminname() {
		return adminname;
	}
	public void setAdminname(String adminname) {
		this.adminname = adminname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getLastLoginDate() {
		return lastLoginDate;
	}
	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	public String getRoleIdList() {
		return roleIdList;
	}
	public void setRoleIdList(String roleIdList) {
		this.roleIdList = roleIdList;
	}
	public boolean isNonremovable() {
		return nonremovable;
	}
	public void setNonremovable(boolean nonremovable) {
		this.nonremovable = nonremovable;
	}
}
