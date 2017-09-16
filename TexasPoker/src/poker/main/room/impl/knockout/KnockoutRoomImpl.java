package poker.main.room.impl.knockout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import poker.data.KnockoutProfitData;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.main.room.Phase;
import poker.main.room.RoomType;
import poker.main.room.impl.CardRule;
import poker.main.room.impl.GameRule;
import poker.main.room.impl.RoomImpl;
import poker.proto.ProtoFactory.KnockoutBroadcastProto;
import poker.util.JLog;

public class KnockoutRoomImpl extends RoomImpl {
	public static final double DRAWOFF_RATE = 0;

	protected final int initBankroll;
	protected int entryFee;
	protected int[] reward;
	protected boolean newKnockoutPlay = true;// 是否第一局（一大局的第一小局）
	protected List<Player> knockoutSequence;// 淘汰顺序

	// 运行时区
	protected long beginTimeOnePlay;// 一大局开始的时间

	// 持久化对象
	protected KnockoutProfitData kProfitData = new KnockoutProfitData();

	public KnockoutRoomImpl(KnockoutRoomVm rv) {
		super(rv);
		this.entryFee = rv.entryFee;
		this.minTake = rv.entryFee;
		this.reward = rv.reward;
		this.initBankroll = rv.initBankroll;
	}

	@Override
	protected void init() {
		this.roomType = RoomType.KNOCKOUT;
		this.minPlayerForBegin = this.seats.getSeatCount();
		this.minPlayerForRun = 2;
		log = new JLog("room/Knockout_" + this.id);
		cardRule = new CardRule(this.log);
		gameRule = new GameRule();
	}

	@Override
	public boolean playerEnter(Player player) {
		if (phase == Phase.WAITING) {
			// 更新玩家最新信息
			player.seat = -1;
			player.info = memberData.getById(player.id);
			player.bankroll = initBankroll;
			if (player.info.getGold() >= entryFee) {
				super.playerEnter(player);
				if (player.seat > -1) {
					// 进入成功，扣除金币
					memberData.memberGoldAdd(player.id, -entryFee);
					kProfitData.insert(player.id, roomLevel, entryFee, 0);
					player.info.setGold(player.info.getGold() - entryFee);
					log.debugln("Knockout " + this.id + " is NewKnockoutPlay:" + newKnockoutPlay);
					return true;
				}
			} else {
				log.debugln("Player " + player.id + " enter room " + this.id + " failed.Gold is not enough.");
			}
		}
		return false;
	};

	@Override
	protected boolean leaveGamersGroup(Player player) {
		// 淘汰赛中，离开自动托管
		Iterator<Player> playerItr = seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.id == player.id) {
				seats.leave(curPlayer);
				// 如果写成phase>=2，将会出现allStandup时，多次执行以下条件中的语句
				if (phase == Phase.BETTING && player.status != PlayerStatus.READY) {
					log.debugln("Player count:" + seats.getBetableCount() + ",minPlayerCountForRun:" + minPlayerForRun);
					if (seats.getBetableCount() == minPlayerForRun - 1) {
						log.debugln("Betable count is not enough.");
						phaseLock.lock();
						if (phase == Phase.BETTING) {
							phase = Phase.DATA_PERSISTENT;
							new Thread(new Runnable() {
								@Override
								public void run() {
									taskPause();
									afterAction();
								}
							}).start();
						}
						phaseLock.unlock();
					} else if (seats.getBettingPlayer() != null && seats.getBettingPlayer().id == player.id) {
						log.debugln("Betable count is enough.But betting player is me.");
						// 是自己正在下注
						final Player tempPlayer = player;
						new Thread(new Runnable() {
							@Override
							public void run() {
								taskPause();
								actionHandler(tempPlayer, 722035, null);
							}
						}).start();
					} else if (seats.getBettingPlayer() != null && seats.getBettingPlayer().id != player.id) {
						log.debugln("Betable count is enough.But betting player is not me.");
						// 不是自己正在下注
					}
				}
				player.seat = -1;
				log.debugln("Player " + player.id + " leaveGamersGroup.");
				player.connectionOk = false;
				player.handler.channelCtx.close();
				if (phase > Phase.WAITING) {
					knockoutSequence.add(player);
					player.status = PlayerStatus.KNOCKED_OUT;
					player.bankroll = 0;
				}
				return true;
			}
		}
		return true;
	}

	@Override
	protected boolean joinSpectatorGroup(Player player) {
		return false;
	}

	@Override
	protected boolean leaveSpectatorGroup(Player player) {
		return false;
	}

	@Override
	public void checkBankroll() {
		if (newKnockoutPlay) {
			log.debugln("[Knockout begin]InitBankroll:" + initBankroll + ",entryFee:" + entryFee + ",reward:" + reward + ",blind:" + smallBlind + "/" + bigBlind);
			beginTimeOnePlay = new Date().getTime();// 截取开始时间
			knockoutSequence = new ArrayList<>();
			Iterator<Player> playerItr = seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null) {
					knockoutSequence.add(curPlayer);
					if (curPlayer.status == PlayerStatus.READY) {
						curPlayer.status = PlayerStatus.NORMAL;
					}
				}
			}
		}
		this.smallBlind = BlindIncreaseRule.getBlind(seats.getSeatCount(), new Date().getTime() - beginTimeOnePlay);
		this.bigBlind = this.smallBlind * 2;
	}

	@Override
	protected boolean couldBegin() {
		if (newKnockoutPlay) {
			if (this.seats.getSittingCount() >= minPlayerForBegin) {
				return true;
			}
		} else {
			if (this.seats.getSittingCount() >= minPlayerForRun) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void interrupt() {
		int validCount = 0;
		Iterator<Player> playerItr = seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.status != PlayerStatus.KNOCKED_OUT) {
				validCount++;
			}
		}
		if (validCount <= 1) {
			newKnockoutPlay = true;
		}
		super.interrupt();
	}

	@Override
	public void dataPersistence() {
		// 按筹码排序
		for (int i = 0; i < knockoutSequence.size() - 1; i++) {
			for (int j = i; j < knockoutSequence.size(); j++) {
				if (knockoutSequence.get(i).bankroll > knockoutSequence.get(j).bankroll) {
					Player tempPlayer = knockoutSequence.get(i);
					knockoutSequence.set(i, knockoutSequence.get(j));
					knockoutSequence.set(j, tempPlayer);
				}
			}
		}
		for (int i = 0; i < knockoutSequence.size(); i++) {
			if ((knockoutSequence.get(i).status == PlayerStatus.NORMAL || knockoutSequence.get(i).status == PlayerStatus.CHECK || knockoutSequence.get(i).status == PlayerStatus.FOLD)
					&& knockoutSequence.get(i).bankroll <= 0) {
				knockoutSequence.get(i).status = PlayerStatus.KNOCKED_OUT;
				// 广播出局
				KnockoutBroadcastProto.Builder kbpBuilder = KnockoutBroadcastProto.newBuilder();
				kbpBuilder.setPlayerId(knockoutSequence.get(i).id);
				kbpBuilder.setSeat(knockoutSequence.get(i).seat);
				int rank = 1;
				Iterator<Player> playerItr = seats.iterator();
				while (playerItr.hasNext()) {
					Player curPlayer = playerItr.next();
					if (curPlayer != null && curPlayer.status != PlayerStatus.KNOCKED_OUT) {
						rank++;
					}
				}
				kbpBuilder.setRanking(rank);
				if (rank >= 1 && rank <= reward.length) {
					int tempReward = reward[rank - 1];// 盈利
					int drawoff = (int) (tempReward * DRAWOFF_RATE);// 抽成
					tempReward -= drawoff;// 盈利减去抽成
					kbpBuilder.setReward(tempReward);
					kProfitData.insert(knockoutSequence.get(i).id, roomLevel, 0, tempReward);
					memberData.memberGoldAdd(knockoutSequence.get(i).id, tempReward);
					// 更新最大赢取
					if (knockoutSequence.get(i).info.getMaxScore() < tempReward) {
						memberData.memberMaxScoreUpdate(knockoutSequence.get(i).id, tempReward);
					}
				}
				broadcast(712065, kbpBuilder.build().toByteArray());
				log.debugln("\r\nNotice：Player " + knockoutSequence.get(i).id + " is number " + rank);
				knockoutSequence.get(i).resetAnteList();
			}
		}
		// 计算未出局玩家数量
		int remainPlayersCount = 0;
		Iterator<Player> playerItr = seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.status != PlayerStatus.KNOCKED_OUT) {
				remainPlayersCount++;
			}
		}
		// 如果未被淘汰的玩家数量不大于1，则一局淘汰赛结束
		if (remainPlayersCount == 1) {
			Player leftOne = knockoutSequence.get(knockoutSequence.size() - 1);
			KnockoutBroadcastProto.Builder kbpBuilder = KnockoutBroadcastProto.newBuilder();
			kbpBuilder.setPlayerId(leftOne.id);
			kbpBuilder.setSeat(leftOne.seat);
			kbpBuilder.setRanking(1);
			int tempReward = this.reward[0];// 盈利
			kbpBuilder.setReward(tempReward);
			kProfitData.insert(leftOne.id, roomLevel, 0, tempReward);
			memberData.memberGoldAdd(leftOne.id, tempReward);
			// 更新最大赢取
			if (leftOne.info.getMaxScore() < tempReward) {
				memberData.memberMaxScoreUpdate(leftOne.id, tempReward);
			}
			broadcast(712065, kbpBuilder.build().toByteArray());
			log.debugln("NoticeFirst：Player " + leftOne.id + " is number " + 1);
			leftOne.resetAnteList();
			broadcast(712085, null);
			newKnockoutPlay = true;
		} else {
			newKnockoutPlay = false;
			playerItr = seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && curPlayer.status == PlayerStatus.FOLD) {
					if (curPlayer.anteTotal() > maxAnteLevelLowerLimit) {
						int returnBack = curPlayer.anteTotal() - maxAnteLevelLowerLimit;
						curPlayer.bankroll += returnBack;
						log.debugln("Player " + curPlayer.id + " get back gold：" + returnBack);
					}
				}
			}
		}
	}

	@Override
	protected void waitingForNextPlay() {
		log.debugln("Knockout " + this.id + " end:" + newKnockoutPlay);
		if (newKnockoutPlay) {
			allPlayerLeave();
			phase = Phase.WAITING;
		} else {
			// 开始下一局小。
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					begin();
				}
			};
			taskContinue(task, ROOM_PAUSE_TIME);
		}
	}

	@Override
	public void cleanTimeoutPlayers() {
		if (phase == Phase.WAITING) {
			super.cleanTimeoutPlayers();
		}
	}

	protected void allPlayerLeave() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				curPlayer.connectionOk = false;
				curPlayer.handler.channelCtx.close();
				leaveGamersGroup(curPlayer);
			}
		}
	}

	@Override
	public void requestHandler(Player player, int code, byte[] buff) {
		if (code == 0 || code == 721050 || code == 721060 || code == 721070) {
			if (code == 0) {
				player.connectionOk = false;
			}
			return;
		} else {
			super.requestHandler(player, code, buff);
		}
	}
}
