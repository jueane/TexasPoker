package com.lingzerg.poker.dao;

import java.util.List;

import com.lingzerg.poker.util.Pager;


public interface BaseDao<T> {
	
	public Class<T> getEntityClass();
	
	public T insert(T obj);
	
	public T update(T obj);
	
	public int delete(int id);
	
	public T getById(int id);
	
	public List<T> getPagedList(int pageIndex, int pageSize);
	
	public List<T> getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc);

	public Pager getPagedList(int pageIndex, int pageSize, String orderBy, boolean asc, String word);
	
	
}
