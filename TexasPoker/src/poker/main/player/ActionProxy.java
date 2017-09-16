package poker.main.player;

import java.util.Iterator;
import java.util.TimerTask;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import poker.main.dealer.Dealer;
import poker.main.dealer.Phase;
import poker.main.dealer.Pot;
import poker.main.dealer.PotPool;
import poker.main.exceptions.BetablePlayerNotEnoughException;
import poker.main.room.RoomInstantConfig;
import poker.proto.ProtoFactory.ActionBroadcastProto;
import poker.proto.ProtoFactory.ActionNoticeProto;
import poker.proto.ProtoFactory.CardsProto;
import poker.proto.ProtoFactory.PotListProto;
import poker.proto.ProtoFactory.RaiseProto;
import poker.util.JLog;

public class ActionProxy {
	protected JLog log;

	private Dealer dealer;

	public boolean checkable = false;
	protected boolean bigBlindActed = false;

	public int bankerSeat = -1;
	public int smallBlindSeat = -1;
	public int bigBlindSeat = -1;
	public int smallBlindBettedCount = 0;
	public int bigBlindBettedCount = 0;
	public int dealtBoardCardsCount = 0;

	public int turn = 0;
	public int minCall = 0;// 当前跟注额，未减个人已下注
	public int minRaise = 0;

	public PotPool potPool;

	private int smallBlind;
	private int bigBlind;
	private int holeCardsCount;

	public ActionProxy(Dealer dealer) {
		this.dealer = dealer;
	}

	public void prepareAndBlind() throws BetablePlayerNotEnoughException {
		// 清空奖池
		potPool.potMap.clear();
		// 洗牌
		dealer.cardRule.shuffle();
		// 初始化这局的公牌
		dealtBoardCardsCount = 0;
		// 设置庄家和当前玩家
		try {
			smallBlindBettedCount = 0;
			bigBlindBettedCount = 0;
			dealer.room.gamerGroup.seats.reset(bankerSeat);// 取上一局庄家
			bankerSeat = dealer.room.gamerGroup.seats.nextBetable().seat;// 设置本局庄家
			// 进行盲注并设置大小盲注座号
			Player smallBlindPlayer = dealer.room.gamerGroup.seats.nextBetable();
			smallBlindBettedCount = smallBlindPlayer.blindBet(smallBlind);
			smallBlindSeat = smallBlindPlayer.seat;
			Player bigBlindPlayer = dealer.room.gamerGroup.seats.nextBetable();
			bigBlindBettedCount = bigBlindPlayer.blindBet(bigBlind);
			bigBlindSeat = bigBlindPlayer.seat;
			// 如果大盲筹码为0，则取消话语权。
			if (bigBlindPlayer.bankroll <= 0) {
				log.debugln("Big blind bankroll less than 0,cancel right.");
				bigBlindActed = true;
			} else {
				bigBlindActed = false;
			}
			// 打印
			log.debugln("Banker:" + +bankerSeat + ",player " + smallBlindPlayer.id + " blind:" + smallBlindPlayer.anteList[0] + ",player " + bigBlindPlayer.id + " blind:"
					+ bigBlindPlayer.anteList[0]);
		} catch (BetablePlayerNotEnoughException e) {
			log.debugln("All player disconnected！");
			throw e;
		}
		turn = 0;
		potPool.leftPlayerClear();
	}

	// 发牌，每个玩家2张牌，并通知观众正在发牌
	public void dealHoleCards() {
		Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player tempPlayer = playerItr.next();
			if (tempPlayer != null && tempPlayer.status == PlayerStatus.NORMAL) {
				tempPlayer.holeCards = dealer.cardRule.getCards(holeCardsCount);
				log.debug("显示手牌：");
				dealer.cardRule.showCards(tempPlayer.holeCards);

				CardsProto.Builder cardsBuilder = CardsProto.newBuilder();
				cardsBuilder.setCards(ByteString.copyFrom(tempPlayer.holeCards));
				tempPlayer.send(712010, cardsBuilder.build().toByteArray());
			}
		}
		Iterator<Player> spectItr = dealer.room.audienceGroup.spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			spectItr.next().send(712013, null);
		}
	}

	public void actionInTurn() {
		log.debugln("Turn：" + turn);
		try {
			minCall = 0;
			minRaise = 0;
			if (turn == 0) {
				minCall = bigBlind;// 第一轮下注下限
				minRaise = (bigBlind - smallBlind) * 2 + smallBlind;
				// 设置当前玩家
				dealer.room.gamerGroup.seats.reset(bigBlindSeat);
			} else {
				minRaise = bigBlind;
				dealer.room.gamerGroup.seats.reset(bankerSeat);
			}
			// 将上轮让牌状态的重置为正常
			Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && curPlayer.status == PlayerStatus.CHECK) {
					curPlayer.status = PlayerStatus.NORMAL;
				}
			}
			actionInRound();
		} catch (BetablePlayerNotEnoughException e) {
			afterOneTurn();
		}
	}

	public void actionInRound() throws BetablePlayerNotEnoughException {
		if (dealer.room.gamerGroup.seats.getNoFoldCount() < dealer.room.minPlayerForRun || (dealer.room.gamerGroup.seats.getBetableCount() < dealer.room.minPlayerForRun && minCall <= 0)) {

			afterOneTurn();
			return;
		}
		Player nextPlayer = dealer.room.gamerGroup.seats.nextBetable();
		// 本轮结束条件：1.全部让牌，注额全0
		if (minCall <= 0 && nextPlayer.status == PlayerStatus.CHECK) {
			boolean allCheck = true;
			Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				// 只要有bankroll>0的normal玩家，就不算all check.
				if (curPlayer != null && curPlayer.status == PlayerStatus.NORMAL && curPlayer.bankroll > 0) {
					allCheck = false;
					break;
				}
			}
			if (allCheck) {
				log.debugln("         All checked.");
				afterOneTurn();
				return;
			}
		}

		// 本轮结束条件：2.有弃有下，下注的全相等
		if ((nextPlayer.status == PlayerStatus.NORMAL || nextPlayer.status == PlayerStatus.CHECK) && nextPlayer.anteList[turn] > 0) {
			boolean allBetted = true;
			Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && (curPlayer.status == PlayerStatus.NORMAL || curPlayer.status == PlayerStatus.CHECK) && curPlayer.bankroll > 0 && curPlayer.anteList[turn] < minCall) {
					allBetted = false;
					break;
				}
			}
			// 如果大盲下注最多（可能是all in并小于bigBlind），并且其它人都已无筹码，则取消大盲的第一轮话语权。
			if (turn == 0 && dealer.room.gamerGroup.seats.getBetableCount() == 1 && dealer.room.gamerGroup.seats.getBettingPlayer() != null
					&& (dealer.room.gamerGroup.seats.getBettingPlayer().status == PlayerStatus.NORMAL || dealer.room.gamerGroup.seats.getBettingPlayer().status == PlayerStatus.CHECK)
					&& dealer.room.gamerGroup.seats.getBettingPlayer().bankroll > 0) {
				bigBlindActed = true;
				log.debugln("Cancel big blind right.");
			}
			// 要考虑到大盲第一轮下注前离开的情况.
			playerItr = dealer.room.gamerGroup.seats.iterator();
			boolean bigBlindPlayerExist = false;
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && curPlayer.seat == bigBlindSeat) {
					bigBlindPlayerExist = true;
					break;
				}
			}
			if (!bigBlindPlayerExist) {
				bigBlindActed = true;
			}
			// 判断是否全部已下注
			if (allBetted && (turn != 0 || bigBlindActed == true)) {
				log.debugln("         All betted.");
				afterOneTurn();
				return;
			}
		}

		// 如果玩家筹码为0时。则跳过该玩家。（nextPlayer在后边）
		if (nextPlayer.bankroll <= 0) {
			log.debugln("Skip one player.");
		} else if (nextPlayer.status == PlayerStatus.NORMAL || nextPlayer.status == PlayerStatus.CHECK) {
			// 设置能否让牌
			checkable = false;
			if (minCall == nextPlayer.anteList[turn]) {
				checkable = true;
			}
			log.debug("    Id:" + nextPlayer.id + " in " + dealer.room.gamerGroup.seats.getBettingPlayer().seat + ",status:" + nextPlayer.status + ",bankroll：" + nextPlayer.bankroll
					+ ",anted：" + nextPlayer.anteTotal() + "," + (checkable ? "checkable" : ("minCall：" + (minCall - nextPlayer.anteList[turn]))) + ",bigActed:" + bigBlindActed + ",minRaise:"
					+ minRaise + "...");

			// 通知客户端下注
			ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
			actionNoticeBuilder.setPlayerId(nextPlayer.id);
			actionNoticeBuilder.setSeat(dealer.room.gamerGroup.seats.getBettingPlayer().seat);
			actionNoticeBuilder.setRemainBankroll(nextPlayer.bankroll);
			actionNoticeBuilder.setMinCall(minCall - nextPlayer.anteList[turn]);
			actionNoticeBuilder.setMinRaise(minRaise);
			actionNoticeBuilder.setCheckable(checkable);
			byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
			dealer.room.gamerGroup.broadcast(712020, actionNoticeMsg);
			dealer.room.audienceGroup.broadcast(712020, actionNoticeMsg);

			final Player playerTemp = nextPlayer;
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					dealer.taskPause();
					playerTemp.proxy.noResponse();
					// 代替玩家尝试让牌
					log.debug("(Auto action 3)");
					actionHandler(playerTemp, 722031, null);
				}
			};
			int actionTimeountTemp = RoomInstantConfig.ACTION_TIMEOUT;
			if (playerTemp.connectionOk == false) {
				actionTimeountTemp = 3000;
			}
			dealer.taskContinue(task, actionTimeountTemp);

		}
	}

	public void actionHandler(Player player, int act, byte[] buff) {
		if (dealer.getPhase() != Phase.BETTING || dealer.room.gamerGroup.seats.getBettingPlayer() == null || dealer.room.gamerGroup.seats.getBettingPlayer().id != player.id) {
			log.debugln("Player " + player.id + " action in a wrong time.");
			return;
		}

		/// -----------------------------可能要干掉
		if (dealer.getPhase() == Phase.BETTING && dealer.room.gamerGroup.seats.getBettingPlayer() != null && dealer.room.gamerGroup.seats.getBettingPlayer().id == player.id) {
			dealer.taskPause();
		}
		/// -----------------------------可能要干掉

		ActionResult actionResult = new ActionResult();
		switch (act) {
		case 722031:
			// 让牌，若不可让，则自动转为弃牌
			if (checkable) {
				player.status = PlayerStatus.CHECK;
				actionResult.act = 1;
			} else {
				log.debug("Could not check.");
				player.status = PlayerStatus.FOLD;
				actionResult.act = 5;
			}
			break;
		case 722032:
			// 最小注额为0的情况下如果跟注，则视为弃牌
			if (checkable) {
				actionResult.act = 1;
				player.status = PlayerStatus.CHECK;
				break;
			}
			int myMinCall = minCall - player.anteList[turn];
			// 跟注，服务端计算出注额
			if (player.bankroll > 0 && player.bankroll < myMinCall) {
				// all in
				player.anteList[turn] = player.anteList[turn] + player.bankroll;
				actionResult.act = 4;
				actionResult.ante = player.bankroll;
				player.bankroll = 0;
			} else if (player.bankroll >= myMinCall) {
				// 正常跟注
				player.anteList[turn] = player.anteList[turn] + myMinCall;
				actionResult.act = 2;
				actionResult.ante = myMinCall;
				player.bankroll = player.bankroll - myMinCall;
			} else {
				// 不够则弃牌
				log.debugln("Bankroll is less than 0.(in call).");
				actionResult.act = 5;
				player.status = PlayerStatus.FOLD;
			}
			break;
		case 722033:
			// 加注
			RaiseProto raiseProto = null;
			try {
				if (buff == null) {
					throw new InvalidProtocolBufferException("Buff is null.");
				}
				raiseProto = RaiseProto.parseFrom(buff);
			} catch (InvalidProtocolBufferException e) {
				log.debug("Raise data format error.");
				actionResult.act = 5;
				player.status = PlayerStatus.FOLD;
				break;
			}
			int wannaRaise = raiseProto.getAnte();
			if (wannaRaise < minRaise) {
				log.errorln("Wanna raise is less than min raise.");
				wannaRaise = minRaise;
			}
			// 判断玩家能否加注
			if (player.bankroll >= minRaise) {
				if (player.bankroll < wannaRaise) {
					wannaRaise = player.bankroll;
				}
				player.anteList[turn] += wannaRaise;
				player.bankroll -= wannaRaise;
				if (player.bankroll > 0) {
					actionResult.act = 3;// 加注
				} else {
					actionResult.act = 4;// all in
				}
				actionResult.ante = wannaRaise;
			} else {
				log.debug("Bankroll is 0 (in raise).");
				actionResult.act = 5;
				player.status = PlayerStatus.FOLD;
			}
			break;
		case 722034:
			// 有人all in
			if (player.bankroll > 0) {
				player.anteList[turn] = player.anteList[turn] + player.bankroll;
				actionResult.act = 4;
				actionResult.ante = player.bankroll;
				player.bankroll = 0;
			} else {
				actionResult.act = 5;
				player.status = PlayerStatus.FOLD;
			}
			break;
		case 722035:
			// 弃牌
			actionResult.act = 5;
			player.status = PlayerStatus.FOLD;
			break;
		default:
			actionResult.act = 5;
			player.status = PlayerStatus.FOLD;
			break;
		}

		switch (actionResult.act) {
		case 1:// 让牌
			log.debugln("check");
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 1, actionResult.ante);
			break;
		case 2:// 跟注
			log.debugln("call " + actionResult.ante);
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 2, actionResult.ante);
			break;
		case 3:// 加注
			log.debugln("raise " + actionResult.ante);
			minRaise = (player.anteList[turn] - minCall) * 2 + minCall;// 下一次的最小加注值
			minCall = player.anteList[turn];// 改跟注下限
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 3, actionResult.ante);
			break;
		case 4:// all in
			log.debugln("All in " + actionResult.ante);
			if (player.anteList[turn] > minCall) {
				minRaise = (player.anteList[turn] - minCall) * 2 + minCall;// 下一次的最小加注值
				minCall = player.anteList[turn];// 改跟注下限
			}
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 4, actionResult.ante);
			break;
		case 5:// 弃牌
			log.debugln("fold");
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 5, actionResult.ante);
			break;
		default:
			log.debugln("exception fold");
			somebodyAction(player.id, dealer.room.gamerGroup.seats.getBettingPlayer().seat, 5, actionResult.ante);
			break;
		}

		if (dealer.room.gamerGroup.seats.getBettingPlayer().seat == bigBlindSeat) {
			bigBlindActed = true;
		}

		try {
			actionInRound();
		} catch (BetablePlayerNotEnoughException e) {
			afterOneTurn();
		}

	}

	protected void afterOneTurn() {
		// 计算本轮奖池
		if (turn <= 3) {
			potPool.computePots();
			log.debug("       Pot list：");
			PotListProto.Builder potListBuilder = PotListProto.newBuilder();
			for (int i = 0; i < potPool.anteLevelList.size(); i++) {
				Pot curPot = potPool.potMap.get(potPool.anteLevelList.get(i));
				potListBuilder.addPotList(curPot.total);
				log.debug(curPot.total + "(" + curPot.lowerLimit + ")；");
			}
			log.debugln();
			dealer.room.gamerGroup.broadcast(712050, potListBuilder.build().toByteArray());
			dealer.room.audienceGroup.broadcast(712050, potListBuilder.build().toByteArray());
		}
		// 发布公牌
		if (turn < 3 && dealer.room.gamerGroup.seats.getBetableCount() >= dealer.room.minPlayerForRun) {
			if (turn == 0) {
				dealer.broadcastBoardCards(dealtBoardCardsCount + 1, 3);
			} else {
				dealer.broadcastBoardCards(dealtBoardCardsCount + 1, 1);
			}
			turn++;
			// 下一轮前将让牌状态改为正常
			Iterator<Player> playerItr = dealer.room.gamerGroup.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && curPlayer.status == PlayerStatus.CHECK) {
					curPlayer.status = PlayerStatus.NORMAL;
				}
			}
			actionInTurn();// 进入下一轮
			return;
		}
		// 如若还有未公布的公牌，则在此全部公布。（前提是还有大于1个人未弃牌）
		if (dealer.room.gamerGroup.seats.getNoFoldCount() > 1) {
			dealer.broadcastBoardCards(dealtBoardCardsCount + 1, 5 - dealtBoardCardsCount);
		}
		dealer.afterAction();
	}

	// 广播___某人进行了某操作
	public void somebodyAction(int playerId, int seat, int act, int ante) {
		ActionBroadcastProto.Builder actionBroadcastBuilder = ActionBroadcastProto.newBuilder();
		actionBroadcastBuilder.setPlayerId(playerId);
		actionBroadcastBuilder.setSeat(seat);
		actionBroadcastBuilder.setAct(act);
		actionBroadcastBuilder.setAnte(ante);
		byte[] actionBroadcastBytes = actionBroadcastBuilder.build().toByteArray();
		dealer.room.gamerGroup.broadcast(712040, actionBroadcastBytes);
		dealer.room.audienceGroup.broadcast(712040, actionBroadcastBytes);
	}

}
