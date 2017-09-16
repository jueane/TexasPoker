package com.lingzerg.poker.ui.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.config.Config;
import com.lingzerg.poker.service.GeneralService;
import com.lingzerg.poker.util.JsonHelper;

@Controller
@RequestMapping("")
public class GeneralController {

	@Autowired
	private GeneralService generalService;

	@RequestMapping("hidden")
	@ResponseBody
	public byte[] gethidden() {
		return JsonHelper.toJson(Config.getInstance().hiden);
	}
	
	@RequestMapping("getServerList")
	@ResponseBody
	public byte[] getServerList() {
		return JsonHelper.toJson(generalService.getServerList());
	}

	@RequestMapping("newestVersion")
	@ResponseBody
	public byte[] newestVersion() {
		return JsonHelper.toJson(generalService.newestVersion());
	}

	// status:1.成功,2.已领取
	@RequestMapping("getReward")
	@ResponseBody
	public byte[] getReward(String token, int rewardId) {
		return JsonHelper.toJson(generalService.getReward(token, rewardId));
	}

	// status:1.成功,2.参数错误
	@RequestMapping("getGem")
	@ResponseBody
	public byte[] getGem(String token, int gemCount) {
		return JsonHelper.toJson(generalService.getGem(token, gemCount));
	}

	@RequestMapping("getRankList")
	@ResponseBody
	public byte[] getRankList(String token, int type) {
		return JsonHelper.toJson(generalService.getRankList(token, type));
	}

}
