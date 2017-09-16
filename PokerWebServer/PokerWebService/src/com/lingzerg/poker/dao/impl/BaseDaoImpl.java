package com.lingzerg.poker.dao.impl;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.lingzerg.poker.dao.BaseDao;
import com.lingzerg.poker.util.Pager;

public abstract class BaseDaoImpl<T> implements BaseDao<T> {

	@Autowired
	protected SessionFactory sessionFactory;

	private final Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public BaseDaoImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Override
	public Class<T> getEntityClass() {
		return persistentClass;
	}

	@Override
	public T insert(T obj) {
		sessionFactory.getCurrentSession().save(obj);
		return obj;
	}

	@Override
	public T update(T obj) {
		sessionFactory.getCurrentSession().update(obj);
		return obj;
	}

	@Override
	public int delete(int id) {
		String sql = "update " + persistentClass.getSimpleName() + " set deleted=1 where id=" + id;
		return sessionFactory.getCurrentSession().createQuery(sql).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getById(int id) {
		return (T) sessionFactory.getCurrentSession().get(persistentClass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getPagedList(int pageIndex, int pageSize) {
		Criteria crt = sessionFactory.getCurrentSession().createCriteria(persistentClass);
		crt.add(Restrictions.eq("deleted", false));
		if (pageIndex > 0 && pageSize > 0) {
			crt.setFirstResult((pageIndex - 1) * pageSize);
			crt.setMaxResults(pageSize);
		}
		return crt.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc) {
		Criteria crt = sessionFactory.getCurrentSession().createCriteria(persistentClass);
		crt.add(Restrictions.eq("deleted", false));
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
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc, String word) {
		Criteria crtTotal = sessionFactory.getCurrentSession().createCriteria(persistentClass);
		crtTotal.add(Restrictions.eq("deleted", false));
		
		Criteria crt = sessionFactory.getCurrentSession().createCriteria(persistentClass);
		crt.add(Restrictions.eq("deleted", false));
		if (word != null) {
			if (word.length() > 0) {
				crt.add(Restrictions.like("username", word));
			}
		}
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
		
		
		Pager pager = new Pager();
		pager.list = crt.list();
		pager.pageIndex = pageIndex;
		pager.pageSize = pageSize;
		pager.total = Integer.parseInt(crtTotal.setProjection(Projections.rowCount()).uniqueResult().toString());
		return pager;
	}
}
