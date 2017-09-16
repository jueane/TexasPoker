package com.lingzerg.poker.service;

import java.util.List;




import com.lingzerg.poker.entity.Recharge;
import com.lingzerg.poker.ui.webservice.viewmodel.RechargeVm;
import com.lingzerg.poker.util.Pager;


public interface RechargeService {
	public Recharge getById(int id);
	public List<RechargeVm> getList(String token,int pageIndex,int pageSize);
	public Recharge insert(Recharge recharge);
	
	public String purchase(String token, int count, int sourceType);
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc,String word);
}
