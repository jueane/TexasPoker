package com.lingzerg.poker.service;

import com.lingzerg.poker.entity.Report;

public interface ReportService extends BaseService<Report> {

	int insert(String token, String content);

}
