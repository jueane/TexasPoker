package com.lingzerg.poker.ui.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.service.ToyService;
import com.lingzerg.poker.util.JsonHelper;


@Controller  
@RequestMapping("toy")
public class ToyController {
	
	@Autowired
	ToyService toyService;
	
	@RequestMapping("playOneArmBandit")
	@ResponseBody
	public byte[] playOneArmBandit(String token, String chip) {
		System.out.println("playOneArmBandit");
		if (Integer.valueOf(chip)> 0) {
			return JsonHelper.toJson(toyService.playOneArmBandit(token,chip));
		}
		System.out.println("chip < 0");
		return JsonHelper.toJson(0);
	}
	
	@RequestMapping("playWheel")
	@ResponseBody
	public byte[] playWheel(String token){
		return JsonHelper.toJson(toyService.playWheel(token));
	}
	
	@RequestMapping("getTotal")
	@ResponseBody
	public byte[] getTotal(){
		return JsonHelper.toJson(toyService.getTotal());
	}
}