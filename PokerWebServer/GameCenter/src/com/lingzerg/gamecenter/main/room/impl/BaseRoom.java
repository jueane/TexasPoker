package com.lingzerg.gamecenter.main.room.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;
import com.lingzerg.gamecenter.config.Config;
import com.lingzerg.gamecenter.data.MemberData;
import com.lingzerg.gamecenter.entity.MemberInfo;
import com.lingzerg.gamecenter.main.player.ActionResult;
import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.player.PlayerStatus;
import com.lingzerg.gamecenter.main.room.NoPlayerException;
import com.lingzerg.gamecenter.main.room.Room;
import com.lingzerg.gamecenter.main.room.impl.GameRule.PlayersCards;
import com.lingzerg.gamecenter.proto.ActionBroadcastPro.ActionBroadcastProto;
import com.lingzerg.gamecenter.proto.ActionNoticePro.ActionNoticeProto;
import com.lingzerg.gamecenter.proto.CardsPro.CardsProto;
import com.lingzerg.gamecenter.proto.EntereRoomResultPro.EntereRoomResultProto;
import com.lingzerg.gamecenter.proto.ResultPro.ResultProto;
import com.lingzerg.gamecenter.proto.RoomPro.RoomProto;
import com.lingzerg.gamecenter.util.JLog;
import com.lingzerg.gamecenter.util.OrderHelper;

public abstract class BaseRoom implements Room {
	// 配置区
	protected static final String WEB_SERVICE_URL = Config.getInstance().webServiceUrl;
	protected int minPlayerCountForBegin = 2;// 游戏开始的玩家数量下限
	protected int minPlayerCountForRun = 2;// 游戏运行的玩家数量下限
	// 基础属性区
	protected int id;
	protected int type;
	protected String title;
	protected int playingCount;// 正在进行游戏的玩家数量
	protected int maxPlayingCount;// 可以进行游戏的最大玩家数量
	protected int smallBlind;
	protected int bigBlind;
	protected int minTake;
	protected int maxTake;
	// 运行时区
	public Player[] playerList = null;
	public List<Player> spectatorList = new ArrayList<>();
	protected byte[] boardCards = new byte[5];
	protected List<Pot> potList;
	protected CardRule cardRule = new CardRule();
	protected int bettingSeat = -1;// 下一个可下注玩家座号
	protected int bankerSeat = -1;
	protected int smallBlindSeat = -1;
	protected int bigBlindSeat = -1;
	protected int dealtBoardCardsCount = 0;
	protected boolean roomIsRunning = false;// 房间运行状态
	// 数据持久化对象
	protected MemberData memberData = new MemberData();

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getPlayingCount() {
		return playingCount;
	}

	@Override
	public int getMaxPlayingCount() {
		return this.maxPlayingCount;
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
				playerList[i].setStatus(PlayerStatus.BEING_DELETE);
			}
		}

		clearConnectionInvalidPlayer();
		// 是否满，没满则加入
		EntereRoomResultProto.Builder enteredRoomBuilder = EntereRoomResultProto.newBuilder();
		enteredRoomBuilder.setRoomId(id);
		if (isSpectator == false && playingCount < maxPlayingCount) {
			int seatTmp = playerSitDown(player);
			enteredRoomBuilder.setSeat(seatTmp);
			try {
				player.send(20020, enteredRoomBuilder.build().toByteArray());
				playingCount++;
				JLog.debug("Done.");
			} catch (IOException e) {
				JLog.debug("Failed.");
				playerList[seatTmp] = null;
				return 0;
			}
		} else {
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

	protected int playerSitDown(Player player) {
		int emptySeat = -1;
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] == null) {
				player.setStatus(PlayerStatus.READY);
				playerList[i] = player;
				emptySeat = i;
				break;
			}
		}
		return emptySeat;
	}

	@Override
	public void run() {
		boolean first = true;
		while (true) {
			if (first) {
				first = false;
			} else {
				JLog.debugln("Game pause for 10 seconds.");
				try {
					Thread.sleep(Config.getInstance().nextGameWait);
				} catch (InterruptedException e) {
					JLog.debugln(e.getMessage());
				}
			}
			checkConnectionValidPlayer();
			updateRoomListInfo(playingCount);
			if (playingCount >= minPlayerCountForRun) {
				play();
			} else {
				break;
			}
			checkConnectionValidPlayer();
			if (playingCount < minPlayerCountForRun) {
				break;
			}
		}
		roomIsRunning = false;
		clearConnectionInvalidPlayer();
		updateRoomListInfo(playingCount);
		JLog.debugln("Room " + id + " be closed. PlayingCount:" + playingCount);
	}

	protected void init() {
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				playerList[i].init();
				if (playerList[i].getStatus() == PlayerStatus.READY) {
					playerList[i].setStatus(PlayerStatus.NORMAL);
				}
				// 检查筹码。
				if (playerList[i].getBankroll() <= 0) {
					if (playerList[i].getDetailInfo().getGold() > 0) {
						playerList[i].setBankroll(playerList[i].getDetailInfo().getGold() > minTake ? minTake : playerList[i].getDetailInfo().getGold());
					} else {
						// 金币不足无法补充，则移至观众席
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
			bettingSeat = bankerSeat;// 取上一局庄家
			nextPlayer();
			bankerSeat = bettingSeat;// 设置本局庄家
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
		JLog.debug("Board cards：");
		CardRule.showCards(boardCards);
	}

	// 游戏开始
	protected void play() {
		JLog.debugln("-----------------------------------Begin----------------------------------------");
		init();
		JLog.debugln("Room " + this.id + "，playingCount：" + playingCount + "，Player list：");
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				JLog.debugln("    Id:" + playerList[i].getId() + ",bankroll:" + playerList[i].getBankroll() + ",status:" + playerList[i].getStatus() + ",");
			}
		}
		JLog.debugln();
		broadcast(20030, roomInfoBytes());// 广播房间信息
		dealHoleCards();
		for (int i = 0; i < 4 && betableCount() > 1; i++) {
			try {
				JLog.debugln("Turn：" + (i + 1));
				actionByTurn(i);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			} catch (NoPlayerException e) {
				JLog.debugln("No player left.");
				return;
			}
			if (noFoldCount() == 1) {
				JLog.debugln("Only one player remain.");
				break;
			} else if (noFoldCount() < 1) {
				JLog.debugln("Room is empty,close!");
				return;
			}
			if (betableCount() < 2) {
				break;
			}
			// 公布公牌
			if (i == 0) {
				distributeBoardCards(1, 3);
			} else {
				distributeBoardCards(dealtBoardCardsCount + 1, 1);
			}
		}
		JLog.debugln();
		// 如若还有未公布的公牌，则在此全部公布。
		if (dealtBoardCardsCount < 5) {
			distributeBoardCards(dealtBoardCardsCount + 1, 5 - dealtBoardCardsCount);
		}
		// 计算胜出
		List<PlayersCards> playersCardsList = computeVictory();
		// 计算奖励
		computePotList();
		// 广播胜出
		broadcastVictory(playersCardsList);
		// 结算并持久化
		dataPersistence();

		JLog.debugln("\r\nPlayer list：");
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				JLog.debugln("    Id:" + playerList[i].getId() + ",bankroll:" + playerList[i].getBankroll() + ",status:" + playerList[i].getStatus() + ",");
			}
		}
		JLog.debugln("\r\n-------------------------------------End----------------------------------------");
	}

	protected void broadcast(int code, byte[] bytes) {
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getStatus() != PlayerStatus.BEING_DELETE) {
				try {
					playerList[i].send(code, bytes);
				} catch (IOException e) {
				}
			}
		}
		int spectorCount = spectatorList.size();
		for (int i = 0; i < spectorCount; i++) {
			if (spectatorList.get(i).getStatus() != PlayerStatus.BEING_DELETE) {
				try {
					spectatorList.get(i).send(code, bytes);
				} catch (IOException e) {
				}
			}
		}
	}

	protected List<PlayersCards> computeVictory() {
		GameRule gameRule = new GameRule();
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && (playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK || playerList[i].getStatus() == PlayerStatus.FOLD)) {
				byte[] cardsSeven = new byte[7];
				for (int j = 0; j < 5; j++) {
					cardsSeven[j] = boardCards[j];
				}
				byte[] playerHoleCards = playerList[i].getHoleCards();
				cardsSeven[5] = playerHoleCards[0];
				cardsSeven[6] = playerHoleCards[1];
				gameRule.add(playerList[i].getId(), playerList[i], cardsSeven);
			}
		}
		List<Integer> winnerIdList = new ArrayList<>();
		List<Integer> loserIdList = new ArrayList<>();
		List<PlayersCards> playersCardsList = gameRule.compare();
		if (playersCardsList.size() >= 1) {
			JLog.debugln("Victory player id：" + playersCardsList.get(0).id);
			for (int i = 0; i < playersCardsList.size(); i++) {
				JLog.debugln("Id：" + playersCardsList.get(i).id + "，victory：" + playersCardsList.get(i).won + "，cards：" + playersCardsList.get(i).title);
				JLog.debug("    Origin cards：");
				CardRule.showCards(playersCardsList.get(i).cards);
				JLog.debug("    Max combined：");
				CardRule.showCards(playersCardsList.get(i).maxCards);
				// 更新玩家史上最大牌型
				if (playersCardsList.get(i).player.getDetailInfo().getMaxCardsValue() < playersCardsList.get(i).value) {
					memberData.memberMaxCardsUpdate(playersCardsList.get(i).id, playersCardsList.get(i).maxCards, playersCardsList.get(i).value);
				}
				// 更新胜负场次
				if (playersCardsList.get(i).won) {
					winnerIdList.add(playersCardsList.get(i).id);
				} else {
					loserIdList.add(playersCardsList.get(i).id);
				}
			}
			// 胜负场次持久化
			memberData.memberWinLostUpdate(winnerIdList, loserIdList);
		}
		return playersCardsList;
	}

	protected void computePotList() {
		// 计算出边池等级
		List<Integer> anteTotalList = new ArrayList<>();
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				if (playerList[i].getStatus() == PlayerStatus.BEING_DELETE || playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK || playerList[i].getStatus() == PlayerStatus.FOLD) {
					if (!anteTotalList.contains(playerList[i].getAnteTotal())) {
						anteTotalList.add(playerList[i].getAnteTotal());
					}
				}
			}
		}
		OrderHelper.orderAsc(anteTotalList);// 正序排列
		// 正序计算所有边池金额
		int beforeBoundary = 0;
		for (int i = 0; i < anteTotalList.size(); i++) {
			Pot pot = new Pot();
			pot.lowerLimit = anteTotalList.get(i);
			for (int j = 0; j < playerList.length; j++) {
				if (playerList[j] != null && playerList[j].getAnteTotal() >= anteTotalList.get(i)) {
					pot.total += (anteTotalList.get(i) - beforeBoundary);
				}
			}
			beforeBoundary = anteTotalList.get(i);
			potList.add(pot);
		}

	}

	protected void broadcastVictory(List<PlayersCards> playersCardsList) {
		ResultProto.Builder resultBuilder = ResultProto.newBuilder();
		int computingSeat = 0;
		int noFoldCountTmp = noFoldCount();
		for (int i = 0; i < playersCardsList.size(); i++) {
			// 根据id取得玩家对象
			for (computingSeat = 0; computingSeat < maxPlayingCount; computingSeat++) {
				if (playerList[computingSeat] != null && playersCardsList.get(i).id == playerList[computingSeat].getId()) {
					break;
				}
			}
			int gainedTotal = 0;
			if (playersCardsList.get(i).player.getStatus() == PlayerStatus.NORMAL || playersCardsList.get(i).player.getStatus() == PlayerStatus.CHECK) {
				for (int j = 0; j < potList.size(); j++) {
					if (playerList[computingSeat].getAnteTotal() >= potList.get(j).lowerLimit) {
						gainedTotal += potList.get(j).total;
						potList.get(j).total = 0;
					}
				}
			}
			playersCardsList.get(i).player.setGained(gainedTotal);
			playersCardsList.get(i).player.setBankroll(playersCardsList.get(i).player.getBankroll() + playersCardsList.get(i).player.getGained());
			// 广播……获胜结果
			ResultProto.Winner.Builder winnerBuilder = ResultProto.Winner.newBuilder();
			winnerBuilder.setPlayerId(playersCardsList.get(i).id);
			winnerBuilder.setSeat(computingSeat);
			// 可能只剩一位玩家未弃牌。此时不摊牌结束。
			if (noFoldCountTmp > 1) {
				winnerBuilder.setTitle(playersCardsList.get(i).title);
				winnerBuilder.setType(playersCardsList.get(i).type);
				winnerBuilder.setHolecards(ByteString.copyFrom(playerList[computingSeat].getHoleCards()));
				winnerBuilder.setMaxCards(ByteString.copyFrom(playersCardsList.get(i).maxCards));
			}
			winnerBuilder.setWon(playersCardsList.get(i).won);
			winnerBuilder.setGainTotal(gainedTotal);
			resultBuilder.addWinnerList(winnerBuilder);
			// 持久化最大赢取
			if (gainedTotal > 0) {
				int actualGain = gainedTotal - playersCardsList.get(i).player.getAnteTotal();
				if (actualGain > playersCardsList.get(i).player.getDetailInfo().getMaxScore()) {
					memberData.memberMaxScoreUpdate(playersCardsList.get(i).id, actualGain);
				}
			}
		}

		byte[] result = resultBuilder.build().toByteArray();
		// 广播……胜负结果
		broadcast(20080, result);

		if (JLog.debug) {
			JLog.debugln("\r\nResult(not calculate)：");
			for (int i = 0; i < maxPlayingCount; i++) {
				if (playerList[i] != null) {
					JLog.debugln("	id：" + playerList[i].getId() + "，bankroll：" + playerList[i].getBankroll() + "，gained：" + playerList[i].getGained());
				}
			}
		}
	}

	protected void dataPersistence() {
		// 结算……按玩家获胜顺序结算……玩家状态重置
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				// 持久化……将玩家数据，如增加或扣除金币。实际盈亏=赢取-anteTotal。
				int actualGained = playerList[i].getGained() - playerList[i].getAnteTotal();
				if (actualGained > 0) {
					actualGained *= 0.95;
				}
				memberData.memberGoldAdd(playerList[i].getId(), actualGained);
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

	// 一轮下注
	protected void actionByTurn(int turn) throws NoPlayerException {
		int minCall = 0;
		Player player = null;
		if (turn == 0) {
			minCall = bigBlind;// 第一轮下注下限
			// 设置当前玩家
			bettingSeat = bigBlindSeat;
			player = nextPlayer();
		} else {
			bettingSeat = bankerSeat;
			player = nextPlayer();
		}
		// 将上轮让牌状态的重置为正常
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getStatus() == PlayerStatus.CHECK) {
				playerList[i].setStatus(PlayerStatus.NORMAL);
			}
		}
		while (noFoldCount() >= minPlayerCountForRun) {
			// 本轮结束条件：1.全部让牌，注额全0
			if (player.getStatus() == PlayerStatus.CHECK) {
				boolean allCheck = true;
				for (int i = 0; i < maxPlayingCount; i++) {
					if (playerList[i] != null && playerList[i].getStatus() == PlayerStatus.NORMAL) {
						allCheck = false;
						break;
					}
				}
				if (allCheck) {
					JLog.debugln("         All checked.");
					break;
				}
			}
			// 本轮结束条件：2.有弃有下，下注的全相等
			if (player.getStatus() == PlayerStatus.NORMAL && player.getAnte(turn) > 0) {
				boolean allBetted = true;
				for (int i = 0; i < maxPlayingCount; i++) {
					if (playerList[i] != null && (playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK) && playerList[i].getBankroll() > 0 && playerList[i].getAnte(turn) < minCall) {
						allBetted = false;
						break;
					}
				}
				if (allBetted) {
					JLog.debugln("         All betted.");
					break;
				}
			}
			if (player.getBankroll() <= 0) {
				System.out.println("Skip one player.");
				continue; // 如果玩家筹码为0时。则跳过该玩家
			} else if (player.getStatus() == PlayerStatus.NORMAL || player.getStatus() == PlayerStatus.CHECK) {
				JLog.debug("    Player " + player.getId() + " in " + bettingSeat + "，bankroll：" + player.getBankroll() + "，anted：" + player.getAnteTotal() + "，checkable：" + (minCall == 0) + "...");

				// 通知客户端下注
				ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
				actionNoticeBuilder.setPlayerId(player.getId());
				actionNoticeBuilder.setSeat(bettingSeat);
				actionNoticeBuilder.setRemainBankroll(player.getBankroll());
				actionNoticeBuilder.setMinCall(minCall);
				actionNoticeBuilder.setCheckable(minCall == 0);
				byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
				broadcast(20050, actionNoticeMsg);

				ActionResult actionResult = player.action(turn, bettingSeat, minCall - player.getAnte(turn), minCall == 0);
				switch (actionResult.act) {
				case 1:// 让牌
					JLog.debugln("check");
					somebodyAction(player.getId(), bettingSeat, 1, actionResult.ante);
					break;
				case 2:// 跟注
					JLog.debugln("call " + actionResult.ante);
					somebodyAction(player.getId(), bettingSeat, 2, actionResult.ante);
					break;
				case 3:// 加注
					JLog.debugln("raise " + actionResult.ante);
					minCall = player.getAnte(turn);// 改跟注下限
					somebodyAction(player.getId(), bettingSeat, 3, actionResult.ante);
					break;
				case 4:// all in
					JLog.debugln("All in " + actionResult.ante);
					if (player.getAnte(turn) > minCall) {
						minCall = player.getAnte(turn);// 改跟注下限
					}
					somebodyAction(player.getId(), bettingSeat, 4, actionResult.ante);
					break;
				case 5:// 弃牌
					JLog.debugln("fold");
					somebodyAction(player.getId(), bettingSeat, 5, actionResult.ante);
					break;
				default:
					JLog.debugln("exception fold");
					somebodyAction(player.getId(), bettingSeat, 5, actionResult.ante);
					break;
				}
			}
			player = nextPlayer();
		}
	}

	// 广播___房间信息
	protected byte[] roomInfoBytes() {
		RoomProto.Builder roomBuilder = RoomProto.newBuilder();
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				MemberInfo memberInfo = playerList[i].getDetailInfo();// 从数据库取最新数据
				RoomProto.Player.Builder playerBuilder = RoomProto.Player.newBuilder();
				playerBuilder.setPlayerId(playerList[i].getId());
				playerBuilder.setSeat(i);
				playerBuilder.setNickname(memberInfo.getNickname() == null ? "" : memberInfo.getNickname());
				playerBuilder.setWinTimes(memberInfo.getWinTimes());
				playerBuilder.setLoseTimes(memberInfo.getLoseTimes());
				playerBuilder.setPortrait(memberInfo.getPortrait());
				playerBuilder.setPortraitBorder(memberInfo.getPortraitBorder());
				playerBuilder.setMale(memberInfo.isMale());
				playerBuilder.setSign(memberInfo.getSign() == null ? "" : memberInfo.getSign());
				playerBuilder.setIsChallenger(false);
				playerBuilder.setBankRoll(playerList[i].getBankroll());
				playerBuilder.setGold(memberInfo.getGold());
				playerBuilder.setMaxScore(memberInfo.getMaxScore());
				// 加入列表
				roomBuilder.addPlayerList(playerBuilder);
			}
		}
		roomBuilder.setSmallBlind(smallBlind);
		roomBuilder.setBankerSeat(bankerSeat);
		roomBuilder.setSmallBlindSeat(smallBlindSeat);
		roomBuilder.setBigBlindSeat(bigBlindSeat);
		return roomBuilder.build().toByteArray();
	}

	// 发牌，每个玩家2张牌
	protected void dealHoleCards() {
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getStatus() == PlayerStatus.NORMAL) {
				playerList[i].dealHoleCards(cardRule.getCards(2));
			}
		}
	}

	// 获取可下注玩家数量
	protected int betableCount() {
		int count = 0;
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getBankroll() > 0 && (playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK)) {
				count++;
			}
		}
		return count;
	}

	// 获取未弃牌玩家数
	protected int noFoldCount() {
		int count = 0;
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && (playerList[i].getStatus() == PlayerStatus.NORMAL || playerList[i].getStatus() == PlayerStatus.CHECK)) {
				count++;
			}
		}
		return count;
	}

	// 下一个可下注玩家
	protected Player nextPlayer() throws NoPlayerException {
		int counter = 0;
		do {
			if (++bettingSeat >= maxPlayingCount) {
				bettingSeat = 0;
			}
			counter++;
			if (counter > 50) {
				throw new NoPlayerException();
			}
		} while (playerList[bettingSeat] == null || (playerList[bettingSeat].getStatus() != PlayerStatus.NORMAL && playerList[bettingSeat].getStatus() != PlayerStatus.CHECK));
		return playerList[bettingSeat];
	}

	// 广播___某人进行了某操作
	protected void somebodyAction(int playerId, int seat, int act, int ante) {
		ActionBroadcastProto.Builder actionBroadcastBuilder = ActionBroadcastProto.newBuilder();
		actionBroadcastBuilder.setPlayerId(playerId);
		actionBroadcastBuilder.setSeat(seat);
		actionBroadcastBuilder.setAct(act);
		actionBroadcastBuilder.setAnte(ante);
		byte[] actionBroadcastBytes = actionBroadcastBuilder.build().toByteArray();
		broadcast(20060, actionBroadcastBytes);
	}

	// 广播___翻开从第index张公牌开始，一共count张公牌
	protected void distributeBoardCards(int index, int count) {
		if (index < 1 || index > boardCards.length) {
			return;
		}
		byte[] cards = new byte[count];
		for (int i = 0; i < count; i++) {
			cards[i] = boardCards[(index - 1) + i];// index从1开始
		}
		JLog.debug("         Distribute cards：");
		CardRule.showCards(cards);
		CardsProto.Builder cardsBuilder = CardsProto.newBuilder();
		cardsBuilder.setCards(ByteString.copyFrom(cards));
		byte[] cardsInfoBytes = cardsBuilder.build().toByteArray();
		broadcast(20070, cardsInfoBytes);
		dealtBoardCardsCount += count;

	}

	protected void checkConnectionValidPlayer() {
		playingCount = 0;
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null && playerList[i].getStatus() != PlayerStatus.BEING_DELETE && playerList[i].ConnectionOk()) {
				playingCount++;
			}
		}
	}

	protected void clearConnectionInvalidPlayer() {
		playingCount = 0;
		for (int i = 0; i < maxPlayingCount; i++) {
			if (playerList[i] != null) {
				if (playerList[i].getStatus() != PlayerStatus.BEING_DELETE && playerList[i].ConnectionOk()) {
					playingCount++;
				} else {
					playerList[i] = null;
				}
			}
		}
	}

	// 游戏结束时清理无效观众
	protected void clearConnectionInvalidSpectator() {
		int spectatorCount = spectatorList.size();
		for (int i = 0; i < spectatorCount; i++) {
			if (spectatorList.get(i).getStatus() == PlayerStatus.BEING_DELETE || spectatorList.get(i).ConnectionOk() == false) {
				spectatorList.remove(i);
			}
		}
	}

	// 外部调用。参数大于0有效
	protected void updateRoomListInfo(int playingCount) {
		URL url = null;
		try {
			url = new URL(WEB_SERVICE_URL + "/room/modifyRoom?id=" + id + "&playingCount=" + playingCount);
			url.openStream();
		} catch (Exception e) {
			JLog.infoln("Connect web service error.");
		}
	}

}
