package com.lingzerg.poker.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.lingzerg.poker.dao.RechargeDao;
import com.lingzerg.poker.entity.Recharge;

@Repository
public class RechargeDaoImpl extends BaseDaoImpl<Recharge>implements RechargeDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Recharge> getPagedList(int memberId, int pageIndex, int pageSize) {
		
		Session session = sessionFactory.getCurrentSession();
		// 查询列表
		Criteria crt = session.createCriteria(Recharge.class);
		crt.add(Restrictions.eq("deleted", false));
		crt.add(Restrictions.eq("memberId",memberId));
		
		crt.addOrder(Order.desc("createDate"));
		
		if (pageIndex > 0 && pageSize > 0) {
			crt.setFirstResult((pageIndex - 1) * pageSize);
			crt.setMaxResults(pageSize);
		}
		crt.setMaxResults(pageSize);
		return crt.list();
	}
	
}
