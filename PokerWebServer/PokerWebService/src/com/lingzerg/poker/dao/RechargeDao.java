package com.lingzerg.poker.dao;

import java.util.List;

import com.lingzerg.poker.entity.Recharge;

public interface RechargeDao extends BaseDao<Recharge>{

	@Override
	public Recharge getById(int id);
	
	public List<Recharge> getPagedList(int memberId,int pageIndex,int pageSize);
	
	@Override
	public Recharge insert(Recharge recharge);
	
}
