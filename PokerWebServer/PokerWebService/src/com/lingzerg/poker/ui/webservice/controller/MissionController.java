package com.lingzerg.poker.ui.webservice.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.service.MissionService;
import com.lingzerg.poker.util.JsonHelper;

@Controller
@RequestMapping("mission")
public class MissionController {
	
	@Autowired
	private MissionService service;
	
	@RequestMapping("getById")
	@ResponseBody
	public byte[] getById(int id) {
		return JsonHelper.toJson(service.getById(id));
	}
	
	@RequestMapping("getList")
	@ResponseBody
	public byte[] getList(int pageIndex, int pageSize){
		
		return JsonHelper.toJson(service.getList(pageIndex, pageSize));
	}
}
