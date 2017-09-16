package com.lingzerg.poker.util;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

public class CustomFreeMarkerView extends FreeMarkerView {
	private static final String CONTEXT_PATH = "basePath"; 
	@Override
	protected void exposeHelpers(Map<String, Object> model,
			HttpServletRequest request) throws Exception {
		model.put(CONTEXT_PATH, request.getContextPath());
		super.exposeHelpers(model, request);
	}

}
