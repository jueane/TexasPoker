package com.lingzerg.poker.ui.manage.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.entity.SysUser;
import com.lingzerg.poker.service.SysUserService;


@Controller
@RequestMapping("manage/login")
public class LoginManageController {
	
	@Autowired
	SysUserService sysUserService;
	
	@RequestMapping(value="test")
	@ResponseBody
	public byte[] test() throws UnsupportedEncodingException {
		return "你好，测试成功！！".getBytes("utf-8");
	}
	
	@RequestMapping
	public String index(){
		return "manage/freemarker/login/index";
	}
	
	// status:1.成功,2.用户名或密码错误
	@RequestMapping(method=RequestMethod.POST)
	public String index(ModelMap modelMap,SysUser sysuser,HttpSession session) {
		System.out.println("index2");
		if (sysUserService.login(sysuser.getAdminname(),sysuser.getPassword())) {
			System.out.println("跳转");
			session.setAttribute("user", sysuser.getAdminname());
			return "redirect:/manage/member?pageIndex=1&pageSize=10";
		} else {
			System.out.println("密码错误,不跳转");
			return "redirect:/manage/login";
		}
	}
}
