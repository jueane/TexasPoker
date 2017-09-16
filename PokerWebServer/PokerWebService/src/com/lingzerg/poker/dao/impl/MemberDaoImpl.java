package com.lingzerg.poker.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.entity.*;

@Repository
public class MemberDaoImpl extends BaseDaoImpl<Member> implements MemberDao {

	@Override
	public Member getByUsername(String username) {
		Session session = sessionFactory.getCurrentSession();
		return (Member) session.createCriteria(Member.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("username", username)).uniqueResult();
	}

	@Override
	public Member getByEmail(String email) {
		Session session = sessionFactory.getCurrentSession();
		return (Member) session.createCriteria(Member.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("email", email)).uniqueResult();
	}

	@Override
	public Member getByPhone(String phone) {
		Session session = sessionFactory.getCurrentSession();
		return (Member) session.createCriteria(Member.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("phone", phone)).uniqueResult();
	}

	@Override
	public Member getByToken(String token) {
		Session session = sessionFactory.getCurrentSession();
		return (Member) session.createCriteria(Member.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("token", token)).uniqueResult();
	}

	@Override
	public Member getByOpenId(String openId) {
		Session session = sessionFactory.getCurrentSession();
		return (Member) session.createCriteria(Member.class).add(Restrictions.eq("deleted", false)).add(Restrictions.eq("openId", openId)).uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Member> getListByMemberIdList(List<Integer> idList) {
		return sessionFactory.getCurrentSession().createCriteria(Member.class).add(Restrictions.in("id", idList)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Member> getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc) {
		Session session = sessionFactory.getCurrentSession();
		// 查询列表
		Criteria crt = session.createCriteria(Member.class);
		crt.add(Restrictions.eq("deleted", false));
		if (asc) {
			crt.addOrder(Order.asc(orderBy));
		} else {
			crt.addOrder(Order.desc(orderBy));
		}
		crt.setFirstResult((pageIndex - 1) * pageSize);
		crt.setMaxResults(pageSize);
		return crt.list();
	}

	@Override
	public List<Integer> challengeIdList() {
		Session session = sessionFactory.getCurrentSession();
		Criteria crt = session.createCriteria(Member.class);
		crt.add(Restrictions.eq("deleted", false));
		crt.addOrder(Order.desc("gold"));
		crt.setMaxResults(50);

		ProjectionList pList = Projections.projectionList();
		pList.add(Projections.property("id"));
		crt.setProjection(pList);
		List<Integer> idList = null;
		try {
			idList = crt.list();
		} catch (Exception e) {
			System.err.println("列表查询错误!");
		}
		if (idList == null) {
			idList = new ArrayList<>();
		}
		return idList;
	}

}
