package com.lingzerg.poker.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.dao.impl.MissionDaoImpl;
import com.lingzerg.poker.entity.Mission;
import com.lingzerg.poker.service.MissionService;

@Service
@Transactional
public class MissionServiceImpl implements MissionService {

	@Autowired
	private MissionDaoImpl missionDao;
	
	@Override
	public Mission getById(int id) {
		return missionDao.getById(id);
	}

	@Override
	public List<Mission> getList(int pageIndex, int pageSize) {
		return missionDao.getPagedList(pageIndex, pageSize);
	}

}
