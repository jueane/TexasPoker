package com.lingzerg.poker.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.dao.SysUserDao;
import com.lingzerg.poker.entity.SysUser;
import com.lingzerg.poker.service.SysUserService;
import com.lingzerg.poker.util.Pager;


@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {
	
	@Autowired
	SysUserDao sysUserDao;
	
	@Override
	public boolean login(String uname, String pwd) {
		SysUser user = sysUserDao.getUserByName(uname);
		if (user != null) {
			if (user.getPassword().equals(pwd)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	@Override
	public SysUser getById(int id) {
		return sysUserDao.getById(id);
	}

	@Override
	public SysUser update(SysUser entity) {
		return sysUserDao.update(entity);
	}

	@Override
	public SysUser insert(SysUser entity) {
		return sysUserDao.insert(entity);
	}

	@Override
	public int delete(int id) {
		return sysUserDao.delete(id);
	}

	@Override
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc,String word) {
		return sysUserDao.getPagedList(pageIndex, pageSize, orderBy, asc, word);
	}

}
