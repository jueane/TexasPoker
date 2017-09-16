package com.lingzerg.poker.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.lingzerg.poker.dao.MessageDao;
import com.lingzerg.poker.entity.Message;

@Repository
public class MessageDaoImpl extends BaseDaoImpl<Message>implements MessageDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Message> getByReceiveId(int receiveId,int pageIndex,int pageSize,String orderBy, boolean asc) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crt= session.createCriteria(Message.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("receiveId", receiveId));
		if (!orderBy.isEmpty()) {
			if (asc) {
				crt.addOrder(Order.asc(orderBy));
			} else {
				crt.addOrder(Order.desc(orderBy));
			}
		}
		if (pageIndex > 0 && pageSize > 0) {
			crt.setFirstResult((pageIndex - 1) * pageSize);
			crt.setMaxResults(pageSize);
		}
		return crt.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Message> getBySendId(int sendId,int pageIndex,int pageSize,String orderBy, boolean asc) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crt= session.createCriteria(Message.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("sendId", sendId));
		if (!orderBy.isEmpty()) {
			if (asc) {
				crt.addOrder(Order.asc(orderBy));
			} else {
				crt.addOrder(Order.desc(orderBy));
			}
		}
		if (pageIndex > 0 && pageSize > 0) {
			crt.setFirstResult((pageIndex - 1) * pageSize);
			crt.setMaxResults(pageSize);
		}
		return crt.list();
	}

}
