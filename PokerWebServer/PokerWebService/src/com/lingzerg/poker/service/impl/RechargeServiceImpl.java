package com.lingzerg.poker.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.dao.RechargeDao;
import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.entity.Recharge;
import com.lingzerg.poker.service.RechargeService;
import com.lingzerg.poker.ui.webservice.viewmodel.RechargeVm;
import com.lingzerg.poker.util.Pager;

@Service
@Transactional
public class RechargeServiceImpl implements RechargeService {

	@Autowired
	private RechargeDao rechargeDao;
	
	@Autowired
	private MemberDao memberDao;
	
	@Override
	public Recharge getById(int id) {
		return rechargeDao.getById(id);
	}
	
	@Override
	public List<RechargeVm> getList(String token,int pageIndex, int pageSize) {
		Member member = memberDao.getByToken(token);
		if (member != null) {
			System.out.println("member id :"+member.getId());
			List<Recharge> RechargeList = rechargeDao.getPagedList(member.getId(),pageIndex, pageSize);
			List<RechargeVm> RechargeVmList = new ArrayList<RechargeVm>();
			for (int i = 0; i < RechargeList.size(); i++) {
				RechargeVm rechargeVm = new RechargeVm();
				rechargeVm.count = RechargeList.get(0).getCount();
				rechargeVm.sourceType = RechargeList.get(0).getSourceType();
				rechargeVm.createDate = RechargeList.get(0).getCreateDate();
				RechargeVmList.add(rechargeVm);
			}
			return RechargeVmList;
		} else {
			return null;
		}
	}

	@Override
	public Recharge insert(Recharge recharge) {
		return rechargeDao.insert(recharge);
	}
	
	@Override
	public String purchase(String token, int count, int sourceType) {
		Recharge recharge = new Recharge();
		Member member = memberDao.getByToken(token);
		if (member != null) {
			member.setGold(count);
			recharge.setMemberId(memberDao.getByToken(token).getId());
			recharge.setCount(count);
			recharge.setSourceType(sourceType);
			recharge.setCreateDate(new Date());
			recharge.setDeleted(false);
			rechargeDao.insert(recharge);
			if (recharge.getId() > 0) {
				return "true";
			} else {
				return "false";
			}
		} else {
			return "请登录";
		}
		
	}

	@Override
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc, String word) {
		return rechargeDao.getPagedList(pageIndex, pageSize, orderBy, asc, word);
	}
	
	
	
	
}
