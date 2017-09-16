package com.lingzerg.gamecenter.main.room.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lingzerg.gamecenter.config.Config;
import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.player.PlayerStatus;
import com.lingzerg.gamecenter.main.room.NoPlayerException;
import com.lingzerg.gamecenter.main.room.Room;
import com.lingzerg.gamecenter.main.room.RoomKnockoutVm;
import com.lingzerg.gamecenter.proto.EntereRoomResultPro.EntereRoomResultProto;
import com.lingzerg.gamecenter.proto.KnockoutBroadcastPro.KnockoutBroadcastProto;
import com.lingzerg.gamecenter.proto.KnockoutResultPro.KnockoutResultProto;
import com.lingzerg.gamecenter.util.JLog;

public class RoomKnockoutImpl extends BaseRoom implements Room {

	private int entryFee;
	private int serviceFee;

	private boolean newGame = true;// 是否第一局

	private int paidPlayerCount = 0;
	private List<Player> knockoutSequence;// 淘汰顺序

	public RoomKnockoutImpl(RoomKnockoutVm rv) {
		if (rv.maxPlayingCount <= 2) {
			rv.maxPlayingCount = 6;
		}
		this.minPlayerCountForRun = 2;
		this.minPlayerCountForBegin = rv.maxPlayingCount;
		this.id = rv.id;
		this.type = rv.type;
		this.title = rv.title;
		this.maxPlayingCount = rv.maxPlayingCount;
		this.smallBlind = rv.smallBlind;
		this.bigBlind = rv.bigBlind;
		this.minTake = rv.entryFee;
		this.playerList = new Player[maxPlayingCount];
		// 初始化底池
		this.potList = new ArrayList<>();
		this.entryFee = rv.entryFee;
		this.serviceFee = rv.serviceFee;
	}

	@Override
	public int playerIn(Player player, boolean isSpectator) {
		JLog.debug("Player " + player.getId() + " enter room " + this.id + " ...");
		// 检查是否已加入过此房间
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getId() == player.getId()) {
				playerList[i].setStatus(PlayerStatus.BEING_DELETE);
			}
		}
		for (int i = 0; i < spectatorList.size(); i++) {
			if (spectatorList.get(i) != null && spectatorList.get(i).getId() == player.getId()) {
				spectatorList.get(i).setStatus(PlayerStatus.BEING_DELETE);
			}
		}

		clearConnectionInvalidPlayer();
		EntereRoomResultProto.Builder enteredRoomBuilder = EntereRoomResultProto.newBuilder();
		enteredRoomBuilder.setRoomId(id);
		boolean hasSat = false;
		if (isSpectator == false && roomIsRunning == false && playingCount < maxPlayingCount) {
			// 上一行roomIsRunning == false为淘汰赛特殊代码
			// 判断金币是否足够
			player.init();
			if (player.getDetailInfo() != null && player.getDetailInfo().getGold() >= entryFee + serviceFee) {
				int seatTmp = playerSitDown(player);
				enteredRoomBuilder.setSeat(seatTmp);
				try {
					player.send(20020, enteredRoomBuilder.build().toByteArray());
					playingCount++;
					hasSat = true;
					JLog.debug("Done.");
				} catch (IOException e) {
					JLog.debug("Failed.");
					playerList[seatTmp] = null;
					return 0;
				}
			}
		}
		if (hasSat == false) {
			enteredRoomBuilder.setSeat(-1);
			try {
				player.send(20020, enteredRoomBuilder.build().toByteArray());
				JLog.debug("Watching.");
			} catch (IOException e) {
				JLog.debug("Failed.");
				return 0;
			}
			spectatorList.add(player);
		}

		JLog.debugln(" PlayingCount： " + playingCount);
		// 当房间玩家数量达到下限，则开始游戏
		if (roomIsRunning == false && playingCount >= minPlayerCountForBegin) {
			JLog.debugln("\r\nCondition met in room " + this.id + ",game beginning.");
			new Thread(this).start();
			// 设置房间运行状态
			roomIsRunning = true;
		} else {
			if (roomIsRunning) {
				try {
					player.send(20030, roomInfoBytes());
				} catch (IOException e) {
				}
			} else {
				broadcast(20030, roomInfoBytes());
			}
		}
		updateRoomListInfo(playingCount);
		return id;
	}

	protected void init() {
		if (newGame) {
			paidPlayerCount = 0;
			knockoutSequence = new ArrayList<>();
		}
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				playerList[i].init();
				if (playerList[i].getStatus() == PlayerStatus.READY) {
					playerList[i].setStatus(PlayerStatus.NORMAL);
				}
				// 淘汰赛代码区。。。
				if (newGame && (playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK || playerList[i].getStatus() == PlayerStatus.FOLD || playerList[i].getStatus() == PlayerStatus.KNOCKED_OUT)) {
					// 在此扣除所有玩家的参赛费和服务费
					if (playerList[i].getDetailInfo().getGold() >= entryFee + serviceFee) {
						memberData.memberGoldAdd(playerList[i].getId(), -(entryFee + serviceFee));
						paidPlayerCount++;
						// 更新玩家信息。
						playerList[i].getDetailInfo().setGold(playerList[i].getDetailInfo().getGold() - (entryFee + serviceFee));
						playerList[i].setStatus(PlayerStatus.NORMAL);
						playerList[i].setBankroll(1000);
					} else {
						// 金币不足，只能站起。若观众席也满。（改为无限观众）
						spectatorList.add(playerList[i]);
						playerList[i] = null;
						playingCount--;
					}
				}
			}
		}
		// 清空奖池
		for (int i = 0; i < potList.size(); i++) {
			potList.get(i).lowerLimit = 0;
			potList.get(i).total = 0;
		}
		// 设置庄家和当前玩家
		try {
			nextPlayer();
			bankerSeat = bettingSeat;
			// 进行盲注并设置大小盲注座号
			nextPlayer().blindBet(smallBlind);
			smallBlindSeat = bettingSeat;
			nextPlayer().blindBet(bigBlind);
			bigBlindSeat = bettingSeat;
			bettingSeat = bankerSeat;// 恢复当前玩家
		} catch (NoPlayerException e) {
			JLog.debugln("All player disconnected！");
		}

		JLog.debugln("Thread id：" + Thread.currentThread().getId() + "，banker seat：" + bankerSeat + "，banker Id：" + playerList[bankerSeat].getId());
		// 洗牌
		cardRule.shuffle();
		// 初始化这局的公牌
		dealtBoardCardsCount = 0;
		for (int i = 0; i < boardCards.length; i++) {
			boardCards[i] = cardRule.getCard();
		}
		// 初始化完毕，状态恢复
		if (newGame) {
			newGame = false;
		}
	}

	protected void dataPersistence() {
		// 淘汰筹码为0的玩家
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				// 检查筹码。
				if ((playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK || playerList[i].getStatus() == PlayerStatus.FOLD) && playerList[i].getBankroll() <= 0) {
					// 淘汰赛代码
					playerList[i].setStatus(PlayerStatus.KNOCKED_OUT);
					knockoutSequence.add(0, playerList[i]);// 从头插入
					// 广播出局
					KnockoutBroadcastProto.Builder kbpBuilder = KnockoutBroadcastProto.newBuilder();
					kbpBuilder.setPlayerId(playerList[i].getId());
					kbpBuilder.setSeat(i);
					broadcast(20065, kbpBuilder.build().toByteArray());
				}
			}
		}
		// 计算未出局玩家数量
		int betableCountTmp = betableCount();
		if (betableCountTmp <= 1) {
			if (betableCountTmp == 1) {
				try {
					knockoutSequence.add(0, nextPlayer());// 将第一名加入淘汰队列
				} catch (NoPlayerException e) {
					JLog.debugln("Get last player error in knockout");
				}
			}
			int bankrollTotal = entryFee * paidPlayerCount;// 所有参赛费总额。

			KnockoutResultProto.Builder knockoutResultBuilder = KnockoutResultProto.newBuilder();
			for (int i = 1; i <= 3 && i <= knockoutSequence.size(); i++) {
				Player playerTemp = knockoutSequence.get(i - 1);
				System.out.println("第" + i + "名：" + playerTemp.getId());
				double rewardRate = 0;
				if (i == 1) {
					rewardRate = 0.55;
				} else if (i == 2) {
					rewardRate = 0.25;
				} else {
					rewardRate = 0.15;
				}
				memberData.memberGoldAdd(playerTemp.getId(), (int) (bankrollTotal * rewardRate));
				KnockoutResultProto.KnockoutWinner.Builder knockoutWinnerBuilder = KnockoutResultProto.KnockoutWinner.newBuilder();
				knockoutWinnerBuilder.setPlayerId(playerTemp.getId());
				if (playerTemp.getDetailInfo().getNickname() != null) {
					knockoutWinnerBuilder.setNickname(playerTemp.getDetailInfo().getNickname());
				}
				knockoutWinnerBuilder.setRanking(i);
				knockoutWinnerBuilder.setGainedTotal((int) (bankrollTotal * rewardRate));
				// 加至列表
				knockoutResultBuilder.addKnockoutWinnerList(knockoutWinnerBuilder);
			}
			// 广播淘汰赛结果
			byte[] knockoutResultBytes = knockoutResultBuilder.build().toByteArray();
			broadcast(20085, knockoutResultBytes);
			// 淘汰赛额外等待时间
			try {
				Thread.sleep(Config.getInstance().nextGameWaitInKnockout);
			} catch (InterruptedException e) {
			}
			newGame = true;
		}

		// 结算……按玩家获胜顺序结算……玩家状态重置
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				// 注释掉的是普通赛代码，以下
				// 持久化……将玩家数据，如增加或扣除金币。实际盈亏=赢取-anteTotal。
				// memberData.memberGoldAdd(playerList[i].getId(),
				// playerList[i].getGained() - playerList[i].getAnteTotal());
				playerList[i].setGained(0);// 清0
				if (playerList[i].getStatus() == PlayerStatus.BEING_DELETE) {
					JLog.debugln("Remove player " + playerList[i].getId());
					playerList[i] = null;
				} else {
					playerList[i].resetStatus();
					playerList[i].resetAnteList();
				}
			}
		}

		clearConnectionInvalidSpectator();
	}
}
