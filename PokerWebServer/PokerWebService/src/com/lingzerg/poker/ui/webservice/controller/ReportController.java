package com.lingzerg.poker.ui.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.service.ReportService;
import com.lingzerg.poker.util.JsonHelper;

@Controller
@RequestMapping("report")
public class ReportController {
	
	@Autowired
	ReportService reportService;
	
	@RequestMapping(value="send",method=RequestMethod.POST)
	@ResponseBody
	public byte[] sendMsg(String token,String content) {
		return JsonHelper.toJson(reportService.insert(token,content));
	}
}