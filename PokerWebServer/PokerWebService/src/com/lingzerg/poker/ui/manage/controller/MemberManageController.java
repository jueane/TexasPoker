package com.lingzerg.poker.ui.manage.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.service.MemberService;
import com.lingzerg.poker.util.Pager;


@Controller
@RequestMapping("manage/member")
public class MemberManageController {
	
	@Autowired
	MemberService memberService;
	
	@RequestMapping("test")
	@ResponseBody 
	public byte[] test() throws UnsupportedEncodingException {
		return "你好，测试成功！！".getBytes("utf-8");
	}
	
	@RequestMapping
	public String index(ModelMap modelMap,  @RequestParam(required=false,defaultValue = "1") int pageIndex,    @RequestParam(required=false,defaultValue = "10") int pageSize) {
		if (pageIndex < 1) {
			pageIndex = 1;
		}
		
		if (pageSize < 1) {
			pageSize =10;
		}
		
		Pager pager = memberService.getPagedList(pageIndex, pageSize, "createDate", false, "");
		
		if (pager != null) { 
			modelMap.addAttribute("method","manage/member");
			modelMap.addAttribute("pager",pager);
		} 
		return "manage/freemarker/member/index";
	}
	
	@RequestMapping("create")
	public String create(){
		return "manage/freemarker/member/create";
	}
	
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public String create(ModelMap modelMap, Member entity) {
		if (entity.getUsername() != null && entity.getPassword() != null) {
			entity.setCreateDate(new Date());
			
			if (memberService.insert(entity) != null) {
				System.out.println("创建成功");
				modelMap.addAttribute("notice", "创建成功！");
				return "manage/freemarker/member/create";
			} else {
				System.out.println("用户名,邮箱,手机号三者必须是唯一的！");
				modelMap.addAttribute("notice", "用户名,邮箱,手机号三者必须是唯一的！");
				return "manage/freemarker/member/create";
			}
		} else {
			modelMap.addAttribute("notice", "创建失败！");
		}
		return "manage/freemarker/member/create";
	}
	
	@RequestMapping("edit")
	public String edit(ModelMap modelMap, int id){
		System.out.println("id="+id);
		Member entity =  memberService.getById(id);
		if (entity != null) {
			modelMap.addAttribute("notice", "编辑成功！");
		} else {
			modelMap.addAttribute("notice", "用户名,邮箱,手机号三者必须是唯一的！");
		}
		modelMap.addAttribute("entity",entity);
//		return "redirect:";
		return "manage/freemarker/member/edit";
	}
	
	@RequestMapping(value = "edit", method = RequestMethod.POST)
	public String edit(ModelMap modelMap, Member entity) {
		System.out.println("carete date:" + entity.getCreateDate());
		memberService.update(entity);
		return "redirect:";
	}
	
	
	@RequestMapping("delete")
	public String delete(int id) {
		Member entity = memberService.getById(id);
		entity.setDeleted(true);
		memberService.update(entity);
		return "redirect:/manage/member";
	}
	
}
