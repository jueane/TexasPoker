package com.lingzerg.poker.ui.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lingzerg.poker.service.RechargeService;
import com.lingzerg.poker.util.Pager;

@Controller
@RequestMapping("manage/recharge")
public class RechargeManageController {
	
	@Autowired
	RechargeService rechargeService;
	
	@RequestMapping
	public String index(ModelMap modelMap,  @RequestParam(required=false,defaultValue = "1") int pageIndex,    @RequestParam(required=false,defaultValue = "10") int pageSize) {
		if (pageIndex < 1) {
			pageIndex = 1;
		}
		
		if (pageSize < 1) {
			pageSize =10;
		}
		
		Pager pager = rechargeService.getPagedList(pageIndex, pageSize, "createDate", false, "");
		if (pager != null) {
			modelMap.addAttribute("method","manage/recharge");
			modelMap.addAttribute("pager",pager);
		} 
		return "manage/freemarker/recharge/index";
	}
}
