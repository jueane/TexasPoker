package com.lingzerg.poker.service;

import java.util.List;

import com.lingzerg.poker.ui.webservice.viewmodel.ServerVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RankVm;
import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.ui.webservice.viewmodel.VersionVm;

public interface GeneralService {
	
	List<ServerVm> getServerList();

	VersionVm newestVersion();

	ResultVm getReward(String token, int rewardId);

	ResultVm getGem(String token, int gemCount);

	List<RankVm> getRankList(String token, int type);

}
