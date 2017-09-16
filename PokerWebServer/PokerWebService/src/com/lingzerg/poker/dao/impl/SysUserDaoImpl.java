package com.lingzerg.poker.dao.impl;

import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.lingzerg.poker.dao.SysUserDao;
import com.lingzerg.poker.entity.SysUser;


@Repository
public class SysUserDaoImpl extends BaseDaoImpl<SysUser> implements SysUserDao {

	@Override
	public SysUser getUserByName(String adminname) {
		Session session = sessionFactory.getCurrentSession();
		return (SysUser) session.createCriteria(SysUser.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("adminname", adminname)).uniqueResult();
	}
	
}
