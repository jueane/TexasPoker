package com.lingzerg.poker.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.lingzerg.poker.dao.PasswordResetDao;
import com.lingzerg.poker.entity.PasswordReset;

@Repository
public class PasswordResetDaoImpl extends BaseDaoImpl<PasswordReset> implements PasswordResetDao{

	@Override
	public PasswordReset getLastByMemberId(int memberId) {
		Session session=sessionFactory.getCurrentSession();
		Criteria crt=session.createCriteria(PasswordReset.class);
		crt.add(Restrictions.eq("deleted", false));
		crt.add(Restrictions.eq("memberId", memberId));
		crt.addOrder(Order.desc("id"));
		crt.setMaxResults(1);
		return (PasswordReset) crt.uniqueResult();
	}
	

}
