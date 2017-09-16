package com.lingzerg.poker.service;

import java.util.List;

import com.lingzerg.poker.entity.Mission;

public interface MissionService {
	public Mission getById(int id);
	public List<Mission> getList(int pageIndex,int pageSize);
}
