package com.lingzerg.poker.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.entity.Report;
import com.lingzerg.poker.service.ReportService;
import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.dao.ReportDao;
import com.lingzerg.poker.util.Pager;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

	@Autowired
	ReportDao ReportDao;
	
	@Autowired
	MemberDao memberDao;
	
	@Override
	public Report insert(Report obj) {
		return ReportDao.insert(obj);
	}

	@Override
	public Report update(Report obj) {
		return ReportDao.update(obj);
	}

	@Override
	public int delete(int id) {
		return ReportDao.delete(id);
	}

	@Override
	public Report getById(int id) {
		return ReportDao.getById(id);
	}

	@Override
	public Pager getPagedList(int pageIndex, int pageSize, String orderBy,
			boolean asc, String word) {
		return ReportDao.getPagedList(pageIndex, pageSize, orderBy, asc, word);
	}

	@Override
	public int insert(String token, String content) {
		Member member = memberDao.getByToken(token);
		if (member != null) {
			Report report = new Report();
			report.setMemberId(member.getId());
			report.setContent(content);
			ReportDao.insert(report);
			return 1;
		}
		return 0;
	}

}
