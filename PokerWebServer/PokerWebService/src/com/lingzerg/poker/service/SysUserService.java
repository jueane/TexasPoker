package com.lingzerg.poker.service;

import com.lingzerg.poker.entity.SysUser;


public interface SysUserService extends BaseService<SysUser> {
	public boolean login(String adminname, String password);
	
}
