package com.lingzerg.poker.ui.manage.controller;

import java.util.Date;
import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lingzerg.poker.entity.SysUser;
import com.lingzerg.poker.service.SysUserService;
import com.lingzerg.poker.util.Pager;


@Controller
@RequestMapping("manage/sysuser")
public class SysuserManageController {

	@Resource
	private SysUserService sysUserService;
	
	@RequestMapping
	public String index(ModelMap modelMap, 
			@RequestParam(required=false,defaultValue = "1") int pageIndex, 
			@RequestParam(required=false,defaultValue = "10") int pageSize,
			@RequestParam(required=false,defaultValue = "createDate") String orderBy, 
			@RequestParam(required=false,defaultValue = "false") Boolean asc,
			@RequestParam(required=false,defaultValue = "") String word) {
		Pager pager = sysUserService.getPagedList(pageIndex, pageSize, orderBy, asc, word);
		
		if (pager != null) { 
			modelMap.addAttribute("method","manage/sysuser");
			modelMap.addAttribute("pager",pager);
		} 
		
		return "manage/freemarker/sysuser/index";
	}
	
	@RequestMapping("create")
	public String create(){
		return "manage/freemarker/sysuser/create";
	}
	
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public String create(ModelMap modelMap, SysUser entity) {
		if (!entity.getPassword().equals("") && !entity.getAdminname().equals("") ) {
			System.out.println("insert");
			entity.setCreateDate(new Date());
			sysUserService.insert(entity);
			
		} else {
			System.out.println("带*号的值不为空！");
		}
		return "redirect:";
	}
	@RequestMapping("edit")
	public String edit(ModelMap modelMap, int id){
		System.out.println("id="+id);
		SysUser entity =  sysUserService.getById(id);
		modelMap.addAttribute("entity",entity);
//		return "redirect:";
		return "manage/freemarker/sysuser/edit";
	}
	
	@RequestMapping(value = "edit", method = RequestMethod.POST)
	public String edit(ModelMap modelMap, SysUser entity) {
		sysUserService.update(entity);
		return "redirect:";
	}
	
	
	@RequestMapping("delete")
	public String delete(int id) {
		System.out.println("delete id = " + id);
		SysUser entity = sysUserService.getById(id);
		entity.setDeleted(true);
		sysUserService.update(entity);
		return "redirect:";
	}
}
