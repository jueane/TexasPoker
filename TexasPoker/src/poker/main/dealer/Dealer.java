package poker.main.dealer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.ByteString;

import poker.Hall;
import poker.data.MemberData;
import poker.main.exceptions.BetablePlayerNotEnoughException;
import poker.main.gamergroup.GamerGroupListener;
import poker.main.player.ActionProxy;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.main.room.RoomImpl;
import poker.main.room.RoomInstantConfig;
import poker.main.rule.CardRule;
import poker.main.rule.ScoreRule;
import poker.proto.ProtoFactory.CardsProto;
import poker.proto.ProtoFactory.PlayerJoinProto;
import poker.proto.ProtoFactory.PlayerLeaveProto;
import poker.proto.ProtoFactory.PlayerProto;
import poker.proto.ProtoFactory.ResultProto;
import poker.util.JLog;

public class Dealer implements GamerGroupListener {
	private JLog log;
	public RoomImpl room;
	public CardRule cardRule;
	public ScoreRule gameRule;
	public ActionProxy actionRule;
	public int holeCardsCount = 2;

	// 运行时区
	private Lock phaseLock = new ReentrantLock();
	private int phase = Phase.WAITING;
	private byte[] boardCards = new byte[5];

	// 下注期间的临时状态
	private Timer timer;
	private Boolean flowtaskLock = new Boolean(true);// 流程锁
	public TimerTask flowTask;// 流程任务

	public Dealer(RoomImpl room, Timer timer) {
		this.room = room;
		this.timer = timer;
		cardRule = new CardRule(log);
		gameRule = new ScoreRule();
		actionRule = new ActionProxy(this);
		// 监听游戏组人数变动
		room.gamerGroup.addGamerGroupListener(this);
	}

	public int getPhase() {
		return phase;
	}

	public byte[] getBoardcards() {
		return boardCards;
	}

	public void begin() {
		// 在begin中，不进行minPlayerForBegin的判断，因为淘汰赛中一大局中的第二小局会是2个人。
		phase = Phase.INITING;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		String strData = simpleDateFormat.format(new Date());
		log.testln("\r\n[Begin]" + strData + " Room " + room.property.id + " level " + room.property.level + ",blind" + room.property.smallBlind + "/" + room.property.bigBlind
				+ "-----------------------------------------------------");
		room.gamerGroup.cleanTimeoutPlayers();
		room.gamerGroup.checkBankroll();
		log.debugln("Room " + room.property.id + "，playerCount：" + room.gamerGroup.seats.getSittingCount() + "，player list：");
		int totalBankroll = 0;
		Iterator<Player> playerItr = room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				totalBankroll += curPlayer.bankroll;
				// 注释
				log.debugln("    Id:" + curPlayer.id + " in " + curPlayer.seat + ",gold:" + curPlayer.info.getGold() + ",bankroll:" + curPlayer.bankroll + ",status:" + curPlayer.status
						+ ",connection:" + curPlayer.connectionOk);
			}
		}
		log.debugln("Bankroll total:" + totalBankroll);
		log.debugln();
		// 检查完筹码之后，再次判断开始条件
		if (couldBegin() == false) {
			log.debugln("Sitting player not enough to PrepareAndBlind.");
			interrupt();
			return;
		}
		try {
			// 取桌面5张牌
			for (int i = 0; i < boardCards.length; i++) {
				boardCards[i] = cardRule.getCard();
			}
			log.debug("Board cards：");
			cardRule.showCards(boardCards);
			// 盲注
			actionRule.prepareAndBlind();
		} catch (BetablePlayerNotEnoughException e) {
			log.debugln("No player left in PrepareAndBlind.");
			interrupt();
			return;
		}
		room.pushRoomInfo();
		actionRule.dealHoleCards();
		phase = Phase.BETTING;
		actionRule.actionInTurn();
	}

	protected boolean couldBegin() {
		if (room.gamerGroup.seats.getSittingCount() >= room.minPlayerForBegin) {
			return true;
		}
		return false;
	}

	public void interrupt() {
		// 房间进入暂停中
		phase = Phase.WAITING;
		room.pushRoomInfo();
		log.testln("----------------------------------Interrupt----------------------------------------");
	}

	public void broadcastVictory(List<Score> scoreList) {
		ResultProto.Builder resultBuilder = ResultProto.newBuilder();
		int noFoldCountTmp = room.gamerGroup.seats.getNoFoldCount();
		for (int i = 0; i < scoreList.size(); i++) {
			// 广播……获胜结果
			ResultProto.Winner.Builder winnerBuilder = ResultProto.Winner.newBuilder();
			winnerBuilder.setPlayerId(scoreList.get(i).id);
			winnerBuilder.setSeat(scoreList.get(i).player.seat);
			// 可能只剩一位玩家未弃牌。此时不摊牌结束。
			if (noFoldCountTmp > 1) {
				winnerBuilder.setTitle(scoreList.get(i).title);
				winnerBuilder.setType(scoreList.get(i).type);
				winnerBuilder.setHolecards(ByteString.copyFrom(scoreList.get(i).player.holeCards));
				winnerBuilder.setMaxCards(ByteString.copyFrom(scoreList.get(i).maxCards));
			}
			winnerBuilder.setWon(scoreList.get(i).won);
			winnerBuilder.setBankroll(scoreList.get(i).player.bankroll);
			winnerBuilder.setGained(scoreList.get(i).player.gained);
			int actualGained = 0;
			actualGained = scoreList.get(i).player.gained - scoreList.get(i).player.anteTotal();
			// ----------------------------------改过，去除淘汰赛判断
			if (actualGained > 0) {
				actualGained -= scoreList.get(i).player.drawoff;
			}
			winnerBuilder.setActualGained(actualGained);
			resultBuilder.addWinnerList(winnerBuilder);
		}
		byte[] result = resultBuilder.build().toByteArray();
		// 广播……胜负结果
		room.gamerGroup.broadcast(712080, result);
		room.audienceGroup.broadcast(712080, result);
		if (JLog.debug) {
			log.debugln("\r\nResult：");
			Iterator<Player> playerItr = room.gamerGroup.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null) {
					log.debugln("	id：" + curPlayer.id + " in " + curPlayer.seat + "，bankroll：" + curPlayer.bankroll + "，gained：" + curPlayer.gained + ",drawoff:" + curPlayer.drawoff);
				}
			}
		}
	}

	public List<Score> computeVictory() {
		gameRule.reset();// 重置scoreList...
		// 将在座普通玩家加入计算
		Iterator<Player> playerItr = room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && (curPlayer.status == PlayerStatus.NORMAL || curPlayer.status == PlayerStatus.CHECK || curPlayer.status == PlayerStatus.FOLD)) {
				if (curPlayer.holeCards == null) {
					log.debugln("Player " + curPlayer.id + " holecards is null");
					continue;
				}
				byte[] allCards = new byte[5 + holeCardsCount];
				for (int j = 0; j < 5; j++) {
					allCards[j] = boardCards[j];
				}
				for (int j = 0; j < holeCardsCount; j++) {
					allCards[j + 5] = curPlayer.holeCards[j];
				}
				gameRule.add(curPlayer.id, curPlayer, allCards);
			}
		}

		List<Integer> winnerIdList = new ArrayList<>();
		List<Integer> loserIdList = new ArrayList<>();
		List<Score> playersCardsList = gameRule.compare();
		if (playersCardsList.size() >= 1) {
			log.debugln("Victory player id：" + playersCardsList.get(0).id);
			for (int i = 0; i < playersCardsList.size(); i++) {
				log.debugln("Id：" + playersCardsList.get(i).id + "，victory：" + playersCardsList.get(i).won + "，cards：" + playersCardsList.get(i).title);
				log.debug("    Origin cards：");
				cardRule.showCards(playersCardsList.get(i).cards);
				log.debug("    Max combined：");
				cardRule.showCards(playersCardsList.get(i).maxCards);
				log.debug("    Hole cards：");
				cardRule.showCards(playersCardsList.get(i).player.holeCards);

				// 更新胜负场次
				if (playersCardsList.get(i).won) {
					winnerIdList.add(playersCardsList.get(i).id);
				} else {
					loserIdList.add(playersCardsList.get(i).id);
				}
			}
			// 胜负场次持久化
			MemberData memberData = new MemberData();
			memberData.memberWinLostUpdate(winnerIdList, loserIdList);
		}
		return playersCardsList;
	}

	public void taskContinue(TimerTask task, long delay) {
		synchronized (this.flowtaskLock) {
			this.flowTask = task;
			timer.schedule(task, delay);
		}
	}

	public void taskPause() {
		synchronized (this.flowtaskLock) {
			if (flowTask != null) {
				flowTask.cancel();
				flowTask = null;
			}
		}
	}

	public void afterAction() {
		phase = Phase.DATA_PERSISTENT;
		log.debugln();
		// 计算胜出
		List<Score> scoreList = computeVictory();
		// 更新最大牌
		room.gamerGroup.updateMaxCards(scoreList);
		// 分配筹码
		actionRule.potPool.distributePots(scoreList);
		// 广播胜出
		broadcastVictory(scoreList);
		// 结算并持久化
		actionRule.potPool.dataPersistence();
		// 状态重置
		room.statusReset();

		log.debugln("\r\nPlayer list：");
		Iterator<Player> playerItr = room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				log.debugln("    Id:" + curPlayer.id + " in " + curPlayer.seat + ",gold:" + curPlayer.info.getGold() + ",bankroll:" + curPlayer.bankroll + ",status:" + curPlayer.status
						+ ",connection:" + curPlayer.connectionOk);
				log.debugln("conok:" + curPlayer.connectionOk);
				if (curPlayer.connectionOk == false) {
					curPlayer.sendDirect(3366, null);
				}
			}
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		String strData = simpleDateFormat.format(new Date());
		log.testln("\r\n[End]" + strData + "-------------------------------------------------------------------------------------------");

		phase = Phase.READY_FOR_NEXT;
		waitingForNextPlay();
	}

	protected void waitingForNextPlay() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// 清理无效玩家
				room.gamerGroup.cleanTimeoutPlayers();
				room.audienceGroup.cleanTimeoutPlayers();

				if (room.gamerGroup.seats.getSittingCount() >= room.minPlayerForBegin) {
					begin();
				} else {
					// 房间进入暂停中
					phase = Phase.WAITING;
					room.gamerGroup.broadcast(712100, null);
					room.audienceGroup.broadcast(712100, null);
					log.testln("Room " + room.property.id + " end,playing count:" + room.gamerGroup.seats.getPlayingCount() + ",sitting count:" + room.gamerGroup.seats.getSittingCount()
							+ ",phase:" + phase);
				}
			}
		};
		taskContinue(task, RoomInstantConfig.ROOM_PAUSE_TIME);
	}

	// 广播___翻开从第index张公牌开始，一共count张公牌
	public void broadcastBoardCards(int index, int count) {
		if (index < 1 || index > boardCards.length) {
			return;
		}
		byte[] cards = new byte[count];
		for (int i = 0; i < count; i++) {
			cards[i] = boardCards[(index - 1) + i];// index从1开始
		}
		log.debug("         Broadcast cards：");
		cardRule.showCards(cards);
		CardsProto.Builder cardsBuilder = CardsProto.newBuilder();
		cardsBuilder.setCards(ByteString.copyFrom(cards));
		byte[] cardsInfoBytes = cardsBuilder.build().toByteArray();
		room.gamerGroup.broadcast(712070, cardsInfoBytes);
		room.audienceGroup.broadcast(712070, cardsInfoBytes);
		actionRule.dealtBoardCardsCount += count;
	}

	@Override
	public void gamerGroupJoin(Player player) {
		// 另启动一线程启动游戏
		phaseLock.lock();
		if (phase == Phase.WAITING && room.gamerGroup.seats.getSittingCount() == room.minPlayerForBegin) {
			phase = Phase.INITING;
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 当房间玩家数量达到下限，则开始游戏
					log.debugln("\r\nCondition met,game begin.Player count:" + room.gamerGroup.seats.getPlayingCount() + "/" + room.gamerGroup.seats.getSittingCount() + "/"
							+ room.gamerGroup.seats.getSeatCount());
					begin();
				}
			}).start();
		}
		phaseLock.unlock();

	}

	@Override
	public void gamerGroupLeave(Player player) {
		// 原来为phase>=2
		if (phase == Phase.BETTING && player.status != PlayerStatus.READY) {
			// 若该玩家的离开导致可下注人数不足minPlayerCountForRun，则立即进入afterAction
			// 不用<minRun可以防止多线程多次执行
			if (room.gamerGroup.seats.getBetableCount() == room.minPlayerForRun - 1) {
				phaseLock.lock();
				if (phase == Phase.BETTING) {
					phase = Phase.DATA_PERSISTENT;
					// 判断可下注玩家数量是否减少
					new Thread(new Runnable() {
						@Override
						public void run() {
							taskPause();
							afterAction();
						}
					}).start();
				}
				phaseLock.unlock();
			} else if (room.gamerGroup.seats.getBettingPlayer() != null && room.gamerGroup.seats.getBettingPlayer().id == player.id) {
				final Player tempPlayer = player;
				new Thread(new Runnable() {
					@Override
					public void run() {
						taskPause();
						actionRule.actionHandler(tempPlayer, 722035, null);
					}
				}).start();
			}
		}

	}

	// 玩家无响应回调
	public void playerNoResponse(Player player) {
		if (getPhase() == Phase.BETTING && room.gamerGroup.seats.getBettingPlayer() != null && room.gamerGroup.seats.getBettingPlayer().id == player.id) {
			taskPause();
			actionRule.actionHandler(player, 722031, null);
		} else if (getPhase() == Phase.WAITING) {
			if (player.seat > -1) {
				room.gamerGroup.leaveGamersGroup(player);
			} else {
				room.audienceGroup.leaveSpectatorGroup(player);
			}
			log.debugln("Player " + player.id + " leave room.");
			Hall.uniqueHall().leaveServer(player);
		}

	}

	public boolean sitDown(Player player) {
		if (room.audienceGroup.spectatorMap.containsValue(player)) {
			log.debugln("[Player " + player.id + " exist in spectatorGroup.]");
			room.audienceGroup.leaveSpectatorGroup(player);
			room.gamerGroup.joinGamersGroup(player);
			// 广播坐下成功
			broadcastSatDown(player);
			return true;
		}
		return false;
	}

	public boolean standUp(Player player) {
		if (room.audienceGroup.joinSpectatorGroup(player)) {
			// 先离开游戏组再加入观众组的话，会导致收不到游戏结束的消息。
			room.gamerGroup.leaveGamersGroup(player);
			PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
			playerLeaveBuilder.setPlayerId(player.id);
			playerLeaveBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
			room.gamerGroup.broadcast(711075, playerLeaveBuilder.build().toByteArray());
			room.audienceGroup.broadcast(711075, playerLeaveBuilder.build().toByteArray());
			return true;
		}
		return false;
	}

	public boolean leave(Player player) {
		if (player.seat > -1) {
			room.gamerGroup.leaveGamersGroup(player);
			PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
			playerLeaveBuilder.setPlayerId(player.id);
			playerLeaveBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
			room.gamerGroup.broadcast(711095, playerLeaveBuilder.build().toByteArray());
			room.audienceGroup.broadcast(711095, playerLeaveBuilder.build().toByteArray());
			return true;
		} else {
			room.audienceGroup.leaveSpectatorGroup(player);
			return true;
		}
	}

	// 广播坐下成功
	private void broadcastSatDown(Player player) {
		PlayerJoinProto.Builder playerJoinBuilder = PlayerJoinProto.newBuilder();
		PlayerProto.Builder playerBuilder = PlayerProto.newBuilder();
		playerBuilder.setPlayerId(player.id);
		playerBuilder.setSeat(player.seat);
		playerBuilder.setNickname(player.info.getNickname() == null ? "" : player.info.getNickname());
		playerBuilder.setWinTimes(player.info.getWinTimes());
		playerBuilder.setLoseTimes(player.info.getLoseTimes());
		playerBuilder.setPortrait(player.info.getPortrait());
		playerBuilder.setPortraitBorder(0);
		playerBuilder.setMale(player.info.isMale());
		playerBuilder.setIsChallenger(false);
		playerBuilder.setBankRoll(player.bankroll);
		playerBuilder.setGold(player.info.getGold());
		playerBuilder.setMaxScore(player.info.getMaxScore());
		playerBuilder.setHeadImg(player.info.getHeadImg() == null ? "" : player.info.getHeadImg());
		playerJoinBuilder.setPlayer(playerBuilder);
		playerJoinBuilder.setPlayerId(player.id);
		playerJoinBuilder.setSeat(player.seat);
		playerJoinBuilder.setSeatsRemain(player.room.gamerGroup.seats.getSeatCount() - player.room.gamerGroup.seats.getSittingCount());
		player.room.gamerGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下成功
		player.room.audienceGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下成功
	}
}
