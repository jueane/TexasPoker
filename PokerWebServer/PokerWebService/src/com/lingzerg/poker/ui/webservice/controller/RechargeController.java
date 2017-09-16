package com.lingzerg.poker.ui.webservice.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.service.RechargeService;
import com.lingzerg.poker.util.JsonHelper;

//int Status,
//String msg
//58f04c0404bcab8151c126d9623fe055
@Controller  
@RequestMapping("recharge")
public class RechargeController {
	
	@Autowired
	private RechargeService service;
	
	@RequestMapping("getById")
	@ResponseBody
	public byte[] getById(int id) {
		return JsonHelper.toJson(service.getById(id));
	}

	@RequestMapping("getList")
	@ResponseBody
	public byte[] getList(String token,int pageIndex, int pageSize) {
		return JsonHelper.toJson(service.getList(token,pageIndex, pageSize));
	}
	//1 充值, 2 游戏内, 3 活动, 4 苹果机, 5 发牌女郎, 6 表情, 7 评论奖励, 8 首日登陆奖励, 9 邀请好友
	@RequestMapping("purchase")
	@ResponseBody
	public byte[] purchase(String token, int count, int sourceType) {
		return JsonHelper.toJson(service.purchase(token, count, sourceType));
	}
}
