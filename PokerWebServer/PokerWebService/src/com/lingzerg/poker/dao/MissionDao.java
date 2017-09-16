package com.lingzerg.poker.dao;

import com.lingzerg.poker.entity.Mission;

public interface MissionDao extends BaseDao<Mission> {

	@Override
	public Mission getById(int id);

}
