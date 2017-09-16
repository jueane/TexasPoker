package com.lingzerg.poker.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class SysRolePermission extends BaseEntity {
	
	@ManyToOne(targetEntity = SysRole.class)
	@JoinColumn(name = "sysRoleId", insertable = false, updatable = false, nullable = false)
	private SysRole sysRole;
	
	private int sysRoleId;
	
	@ManyToOne(targetEntity = SysModule.class)
	@JoinColumn(name = "sysModuleId", insertable = false, updatable = false, nullable = false)
	private SysModule module;
	
	private int sysModuleId;
	private int permission;
	
	public int getSysRoleId() {
		return sysRoleId;
	}
	public void setSysRoleId(int sysRoleId) {
		this.sysRoleId = sysRoleId;
	}
	public int getSysModuleId() {
		return sysModuleId;
	}
	public void setSysModuleId(int sysModuleId) {
		this.sysModuleId = sysModuleId;
	}
	public int getPermission() {
		return permission;
	}
	public void setPermission(int permission) {
		this.permission = permission;
	}

}
