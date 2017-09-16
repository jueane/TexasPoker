package com.lingzerg.poker.service;

import com.lingzerg.poker.util.Pager;


public interface BaseService<T> {
	public T insert(T obj);
	
	public T update(T obj);
	
	public int delete(int id);
	
	public T getById(int id);
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc,String word);
	
}
