package poker.main.dealer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import poker.data.MemberData;
import poker.data.ScoreData;
import poker.main.gamergroup.GamerGroupListener;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.main.room.RoomInstantConfig;
import poker.util.JLog;
import poker.util.OrderHelper;

public class PotPool implements GamerGroupListener {

	public List<Integer> anteLevelList;// 边池等级列表
	public int maxAnteLevelLowerLimit;
	public ConcurrentSkipListMap<Integer, Pot> potMap = new ConcurrentSkipListMap<>();

	private ConcurrentHashMap<Integer, Player> leftPlayerMap = new ConcurrentHashMap<>();// 中途离开的玩家暂存，以持久化资产

	protected ScoreData scoreData = new ScoreData();
	protected MemberData memberData = new MemberData();
	protected JLog log;

	Dealer dealer;

	public PotPool(Dealer dealer) {
		this.dealer = dealer;
		//监听游戏组人数变动
		dealer.room.gamerGroup.addGamerGroupListener(this);
	}

	public void leftPlayerClear() {
		leftPlayerMap.clear();
	}

	// 计算目前为止所有的奖池
	public void computePots() {
		potMap.clear();
		// 计算出边池等级
		anteLevelList = new ArrayList<>();
		Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && (curPlayer.status == PlayerStatus.NORMAL || curPlayer.status == PlayerStatus.CHECK) && curPlayer.anteTotal() > 0) {
				if (anteLevelList.contains(curPlayer.anteTotal()) == false) {
					anteLevelList.add(curPlayer.anteTotal());
				}
			}
		}
		if (anteLevelList.size() == 0) {
			anteLevelList.add(0);
		} else {
			OrderHelper.orderAsc(anteLevelList);// 正序排列
		}

		maxAnteLevelLowerLimit = 0;
		if (anteLevelList.isEmpty() == false) {
			maxAnteLevelLowerLimit = anteLevelList.get(anteLevelList.size() - 1);
		}

		// 正序计算所有边池金额
		int beforeBoundary = 0;
		for (int i = 0; i < anteLevelList.size(); i++) {
			Pot pot = new Pot();
			pot.lowerLimit = anteLevelList.get(i);
			Iterator<Player> playerCompItr = dealer.room.gamerGroup.seats.iterator();
			while (playerCompItr.hasNext()) {
				Player curCompPlayer = playerCompItr.next();
				if (curCompPlayer != null && curCompPlayer.anteTotal() >= anteLevelList.get(i)) {
					pot.total += (anteLevelList.get(i) - beforeBoundary);
					log.debugln("      Player " + curCompPlayer.id + " put in " + pot.lowerLimit + ":" + (anteLevelList.get(i) - beforeBoundary) + ".");
				}
			}
			// // 加上离开的玩家
			Iterator<Player> leftPlayerItr = leftPlayerMap.values().iterator();
			while (leftPlayerItr.hasNext()) {
				Player curPlayer = leftPlayerItr.next();
				if (curPlayer != null && curPlayer.anteTotal() >= anteLevelList.get(i)) {
					pot.total += (anteLevelList.get(i) - beforeBoundary);
					log.debugln("      Player " + curPlayer.id + " put in " + pot.lowerLimit + ":" + (anteLevelList.get(i) - beforeBoundary) + ".");
				}
			}
			beforeBoundary = anteLevelList.get(i);
			potMap.put(pot.lowerLimit, pot);
		}

		// _________________________弃牌玩家多余筹码入池

		// 加上弃牌的玩家
		Iterator<Player> folderItr = dealer.room.gamerGroup.seats.iterator();// 弃牌玩家
		while (folderItr.hasNext()) {
			Player curPlayer = folderItr.next();
			if (curPlayer != null && curPlayer.status == PlayerStatus.FOLD && curPlayer.anteTotal() > 0 && anteLevelList.contains(curPlayer.anteTotal()) == false
					&& curPlayer.anteTotal() < maxAnteLevelLowerLimit) {
				// 定位界限
				int anteLevelBelong = 0;// 属于某个池级
				int lowerLimit = 0;
				for (int i = 0; i < anteLevelList.size(); i++) {
					if (curPlayer.anteTotal() > anteLevelList.get(i)) {
						anteLevelBelong = anteLevelList.get(i);
						lowerLimit = anteLevelList.get(i);
					} else {
						break;
					}
				}
				if (anteLevelBelong == 0) {
					anteLevelBelong = anteLevelList.get(0);
				}
				// 多余筹码入池
				log.debugln("      Player " + curPlayer.id + " overage put in " + anteLevelBelong + ":" + (curPlayer.anteTotal() - lowerLimit) + ".");
				potMap.get(anteLevelBelong).total += curPlayer.anteTotal() - lowerLimit;
			}
		}
		// 加上中途离开的玩家
		folderItr = leftPlayerMap.values().iterator();
		while (folderItr.hasNext()) {
			Player curPlayer = folderItr.next();
			if (curPlayer != null && curPlayer.anteTotal() > 0 && anteLevelList.contains(curPlayer.anteTotal()) == false && curPlayer.anteTotal() < maxAnteLevelLowerLimit) {
				curPlayer.status = PlayerStatus.FOLD;
				// 定位界限
				int anteLevelBelong = 0;// 属于某个池级
				int lowerLimit = 0;
				for (int i = 0; i < anteLevelList.size(); i++) {
					if (curPlayer.anteTotal() > anteLevelList.get(i)) {
						anteLevelBelong = anteLevelList.get(i);
						lowerLimit = anteLevelList.get(i);
					} else {
						break;
					}
				}
				if (anteLevelBelong == 0) {
					anteLevelBelong = anteLevelList.get(0);
				}
				// 多余筹码入池
				log.debugln("      Player " + curPlayer.id + " overage put in " + anteLevelBelong + ":" + (curPlayer.anteTotal() - lowerLimit) + ".");
				potMap.get(anteLevelBelong).total += curPlayer.anteTotal() - lowerLimit;
			}
		}

		// _________________________end__________________________

	}

	public void distributePots(List<Score> scoreList) {
		computePots();
		log.debug("       Pot list：");
		for (int i = 0; i < anteLevelList.size(); i++) {
			Pot curPot = potMap.get(anteLevelList.get(i));
			log.debug(curPot.total + "(" + curPot.lowerLimit + ")；");
		}
		log.debugln();
		// 计算每个池的共享人数
		Iterator<Pot> potItr = potMap.values().iterator();
		while (potItr.hasNext()) {
			Pot curPot = potItr.next();
			for (int j = 0; j < scoreList.size(); j++) {
				if (scoreList.get(j).player.status == PlayerStatus.NORMAL || scoreList.get(j).player.status == PlayerStatus.CHECK) {
					if (scoreList.get(j).player.anteTotal() >= curPot.lowerLimit && scoreList.get(j).value >= curPot.cardsValue) {
						if (curPot.cardsValue <= 0) {
							curPot.cardsValue = scoreList.get(j).value;
						}
						curPot.shareCount++;
					}
				}
			}
		}
		// 分配奖池
		for (int i = 0; i < scoreList.size(); i++) {
			int gainedTotal = 0;
			if (scoreList.get(i).player.status == PlayerStatus.NORMAL || scoreList.get(i).player.status == PlayerStatus.CHECK) {
				potItr = potMap.values().iterator();
				while (potItr.hasNext()) {
					Pot curPot = potItr.next();
					if (scoreList.get(i).player.anteTotal() >= curPot.lowerLimit && scoreList.get(i).value >= curPot.cardsValue && curPot.total > 0) {
						gainedTotal += curPot.total / curPot.shareCount;
						curPot.total -= curPot.total / curPot.shareCount;
						curPot.shareCount--;
						if (curPot.shareCount <= 0) {
							curPot.total = 0;
						}
					}
				}
			}
			scoreList.get(i).player.gained = gainedTotal;
			// ---------------------160627有删除
			if (gainedTotal - scoreList.get(i).player.anteTotal() > 0) {
				scoreList.get(i).player.drawoff = (int) ((gainedTotal - scoreList.get(i).player.anteTotal()) * RoomInstantConfig.DRAWOFF_RATE);
			}
			scoreList.get(i).player.bankroll += scoreList.get(i).player.gained - scoreList.get(i).player.drawoff;
		}

	}

	public void dataPersistence() {
		// 结算……按玩家获胜顺序结算……玩家状态重置
		Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				// 持久化……将玩家数据，如增加或扣除金币。实际盈亏=赢取-anteTotal。
				int actualGained = curPlayer.gained - curPlayer.anteTotal();
				// 更新最大赢取
				if (actualGained > 0 && actualGained > curPlayer.info.getMaxScore()) {
					memberData.memberMaxScoreUpdate(curPlayer.id, actualGained);
				}
				scoreData.insertGoldGained(curPlayer.id, actualGained - curPlayer.drawoff, curPlayer.drawoff);
				log.debugln("Player " + curPlayer.id + " actualGained：" + actualGained + ",drawoff：" + curPlayer.drawoff);
			}
		}

		// 返还因离开而弃牌的玩家多下的筹码
		Iterator<Player> leftPlayerItr = leftPlayerMap.values().iterator();
		while (leftPlayerItr.hasNext()) {
			Player curPlayer = leftPlayerItr.next();
			int returnBack = curPlayer.anteTotal() - dealer.actionRule.potPool.maxAnteLevelLowerLimit;
			if (returnBack > 0) {
				memberData.memberGoldAdd(curPlayer.id, returnBack);
				log.debugln("Player " + curPlayer.id + " get back gold：" + returnBack);
			}
		}
	}

	@Override
	public void gamerGroupJoin(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gamerGroupLeave(Player player) {
		// 加入离开组
		if (player.anteTotal() > 0) {
			leftPlayerMap.put(player.id, player);
		}

	}
}
