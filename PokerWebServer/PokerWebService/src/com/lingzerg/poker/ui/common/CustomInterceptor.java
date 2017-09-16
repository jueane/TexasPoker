package com.lingzerg.poker.ui.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class CustomInterceptor implements HandlerInterceptor{

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String controller = handler.getClass().getName();
		if(controller.contains("com.lingzerg.poker.ui.webservice")){
			return true;
		}
		if((!controller.equals("com.lingzerg.poker.ui.manage.controller.LoginManageController")) && request.getSession().getAttribute("user")==null){
			response.sendRedirect(request.getContextPath()+"/manage/login");
		}
		return true;
	}
	
	

}
