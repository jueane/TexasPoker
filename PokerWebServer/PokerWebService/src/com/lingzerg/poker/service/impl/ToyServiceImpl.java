package com.lingzerg.poker.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.config.Config;
import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.dao.RechargeDao;
import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.entity.Recharge;
import com.lingzerg.poker.service.ToyService;
import com.lingzerg.poker.util.DateHelper;

@Service
@Transactional
public class ToyServiceImpl implements ToyService {

	private static double total = 0;
	
	@Autowired
	MemberDao memberDao;
	@Autowired
	RechargeDao rechargeDao;
	
	
	@Override
	public int playOneArmBandit(String token, String chip) {
		Member member = memberDao.getByToken(token);
		if (member != null && member.getGold() > 0) {
			double pArray[] = Config.getInstance().oneArmBandit_pArray;
			int reward[] = Config.getInstance().oneArmBandit_reward;
			
			double currP = (Math.random() * 100);
			int level = 0;
//			currP = 99.9;
			System.out.println("结果:"+currP);
			System.out.println("概率值:"+pArray[level]);
			while (currP > pArray[level]) {
				level++;
			}
			System.out.println("等级:"+level);
			int result = reward[level];
			
			if (result <= 0) {
				total += Integer.valueOf(chip);
				member.setGold(member.getGold()-Integer.valueOf(chip));
				Recharge recharge  = new Recharge();
				recharge.setMemberId(member.getId());
				recharge.setCreateDate(new Date());
				recharge.setCount(-1*Integer.valueOf(chip));
				recharge.setSourceType(4);
				rechargeDao.insert(recharge);
			} else {
				total -= Integer.valueOf(chip);
				member.setGold(member.getGold()+Integer.valueOf(chip)*result);
				
				Recharge recharge  = new Recharge();
				recharge.setMemberId(member.getId());
				recharge.setCreateDate(new Date());
				recharge.setCount(Integer.valueOf(chip));
				recharge.setSourceType(4);
				rechargeDao.insert(recharge);
			}
			System.out.println("投入筹码:"+chip);
			System.out.println("total:"+total);
			return result;
		}
		return 0;
	}

	//300德币（25%几率）、
	//500德币（25%几率）、
	//800德币（20%几率）、
	//1000德币（10%几率）、
	//2000德币（10%几率）、
	//5000德币（5%几率）、
	//道具（5%几率）、
	//兑换符文（兑换符文仅周日可中奖，几率100%，其他时间不中）
	@Override
	public int playWheel(String token) {
		Member member = memberDao.getByToken(token);
		if (member != null) {
			int pArray[] = Config.getInstance().wheel_pArray;
			int reward[] = Config.getInstance().wheel_reward;
			
			int currP = (int) (Math.random()*100);
			System.out.println(currP);
			int level = 0;
			while (currP > pArray[level]) {
				level++;
			}
			
			if (DateHelper.getWeekOfDate(new Date()) ==0) {
				//少符文字段
				return 9999;
			}
			member.setGold(member.getGold()+reward[level]);
			
			Recharge recharge  = new Recharge();
			recharge.setMemberId(member.getId());
			recharge.setCreateDate(new Date());
			recharge.setCount(reward[level]);
			recharge.setSourceType(4);
			rechargeDao.insert(recharge);
			
			return reward[level];
		}
		return 0;
	}

	@Override
	public double getTotal() {
		return total;
	}
}
