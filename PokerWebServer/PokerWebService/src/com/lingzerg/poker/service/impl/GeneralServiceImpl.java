package com.lingzerg.poker.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.dao.MemberDao;
import com.lingzerg.poker.dao.RewardDao;
import com.lingzerg.poker.dao.ServerDao;
import com.lingzerg.poker.entity.Member;
import com.lingzerg.poker.entity.Reward;
import com.lingzerg.poker.entity.Server;
import com.lingzerg.poker.service.GeneralService;
import com.lingzerg.poker.ui.webservice.viewmodel.ServerVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RankVm;
import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.ui.webservice.viewmodel.VersionVm;
import com.lingzerg.poker.util.StringHelper;

@Service
@Transactional
public class GeneralServiceImpl implements GeneralService {

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private RewardDao rewardDao;
	@Autowired
	private ServerDao serverDao;

	@Override
	public List<ServerVm> getServerList() {
		List<Server> serverList = serverDao.getPagedList(0, 0);
		List<ServerVm> svList = new ArrayList<>();
		if (serverList != null) {
			int count = serverList.size();
			for (int i = 0; i < count; i++) {
				ServerVm sv = new ServerVm();
				sv.type = serverList.get(i).getType();
				sv.ip = serverList.get(i).getIp();
				sv.port = serverList.get(i).getPort();
				svList.add(sv);
			}
		}
		return svList;
	}

	@Override
	public VersionVm newestVersion() {
		VersionVm versionVm = new VersionVm();
		versionVm.version = "0.1.3";
		versionVm.url = "http://www.google.com/poker.app";
		return versionVm;
	}

	@Override
	public ResultVm getReward(String token, int rewardId) {
		ResultVm resultVm = new ResultVm();
		Member member = memberDao.getByToken(token);
		Reward reward = rewardDao.getById(rewardId);
		// 判断该奖励是否属于该玩家
		if (member.getId() == reward.getMemberId() && !reward.isGot()) {
			member.setGold(member.getGold() + reward.getCount());
			reward.setGot(true);
			resultVm.status = 1;
			resultVm.msg = "获取奖励成功";
		} else {
			resultVm.status = 2;
			resultVm.msg = "已领取";
		}
		return resultVm;
	}

	@Override
	public ResultVm getGem(String token, int gemCount) {
		ResultVm resultVm = new ResultVm();
		// ------------------------------一个宝石换piece个金币
		int gemToGold = 1100000;
		int goldToGem = 900000;
		int swapTotal = 0;
		if (gemCount < 0) {
			swapTotal = gemCount * gemToGold;
		} else {
			swapTotal = gemCount * goldToGem;
		}

		Member member = memberDao.getByToken(token);
		if (gemCount == 0) {
			resultVm.status = 2;
			resultVm.msg = "参数错误";
			return resultVm;
		}
		if (gemCount > 0) {
			if (member.getGold() >= swapTotal) {
				System.err.println("gold:" + member.getGold());
				System.err.println("swapTotal:" + swapTotal);
				member.setGold(member.getGold() - swapTotal);
				member.setGem(member.getGem() + gemCount);
			} else {
				resultVm.status = 3;
				resultVm.msg = "金币不足";
				return resultVm;
			}
		} else if (gemCount < 0) {
			if (member.getGem() >= -gemCount) {
				member.setGold(member.getGold() - swapTotal);
				member.setGem(member.getGem() + gemCount);
			} else {
				resultVm.status = 4;
				resultVm.msg = "宝石不足";
				return resultVm;
			}
		}
		resultVm.status = 1;
		resultVm.msg = "兑换成功";
		return resultVm;
	}

	@Override
	public List<RankVm> getRankList(String token, int type) {
		// 1：总资产，2：好友总资产，3：当日赢取
		int size = 30;// 排行榜长度
		List<Integer> challengeIdList = memberDao.challengeIdList();// 最强王者Id列表
		switch (type) {
		case 1: {
			List<Member> memberList = memberDao.getPagedList(1, size, "gold", false);
			List<RankVm> rankVmList = null;
			if (memberList != null && memberList.size() > 0) {
				rankVmList = new ArrayList<>();
				for (int i = 0; i < memberList.size(); i++) {
					RankVm rankVm = new RankVm();
					rankVm.id = memberList.get(i).getId();
					rankVm.index = i + 1;
					rankVm.username = memberList.get(i).getUsername();
					rankVm.nickname = memberList.get(i).getNickname();
					rankVm.portrait = memberList.get(i).getPortrait();
					rankVm.portraitBorder = memberList.get(i).getPortraitBorder();
					rankVm.male = memberList.get(i).isMale();
					rankVm.todayScore = 0;
					rankVm.goldTotal = memberList.get(i).getGold();
					rankVm.isChallenger = (memberList.get(i).getGold() >= 10000001) & challengeIdList.contains(memberList.get(i).getId());
					rankVmList.add(rankVm);
				}
			}
			return rankVmList;
		}
		case 2: {
			Member member = memberDao.getByToken(token);
			if (member == null || member.getFriendIdList().isEmpty()) {
				return null;
			}
			List<Member> memberList = memberDao.getListByMemberIdList(StringHelper.stringToListInt(member.getFriendIdList()));
			List<RankVm> rankVmList = null;
			if (memberList != null && memberList.size() > 0) {
				rankVmList = new ArrayList<>();
				for (int i = 0; i < memberList.size(); i++) {
					RankVm rankVm = new RankVm();
					rankVm.id = memberList.get(i).getId();
					rankVm.index = i + 1;
					rankVm.username = memberList.get(i).getUsername();
					rankVm.nickname = memberList.get(i).getNickname();
					rankVm.portrait = memberList.get(i).getPortrait();
					rankVm.portraitBorder = memberList.get(i).getPortraitBorder();
					rankVm.male = memberList.get(i).isMale();
					rankVm.todayScore = 0;
					rankVm.goldTotal = memberList.get(i).getGold();
					rankVm.isChallenger = (memberList.get(i).getGold() >= 10000001) & challengeIdList.contains(memberList.get(i).getId());
					rankVmList.add(rankVm);
				}
			}
			return rankVmList;
		}
		case 3: {

			break;
		}
		default:
			break;
		}
		return null;
	}
}
