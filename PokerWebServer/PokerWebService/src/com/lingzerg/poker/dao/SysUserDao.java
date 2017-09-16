package com.lingzerg.poker.dao;

import com.lingzerg.poker.entity.SysUser;

public interface SysUserDao extends BaseDao<SysUser> {
	public SysUser getUserByName(String uname);
}
