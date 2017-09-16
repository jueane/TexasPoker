package poker.main.room.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import poker.data.MemberData;
import poker.data.ScoreData;
import poker.entity.MemberInfo;
import poker.main.player.ActionResult;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.main.room.Phase;
import poker.main.room.Room;
import poker.main.room.RoomType;
import poker.main.room.impl.GameRule.Score;
import poker.proto.ProtoFactory.ActionBroadcastProto;
import poker.proto.ProtoFactory.ActionNoticeProto;
import poker.proto.ProtoFactory.CardsProto;
import poker.proto.ProtoFactory.EnterRoomRespProto;
import poker.proto.ProtoFactory.PlayerJoinProto;
import poker.proto.ProtoFactory.PlayerLeaveProto;
import poker.proto.ProtoFactory.PlayerProto;
import poker.proto.ProtoFactory.PotListProto;
import poker.proto.ProtoFactory.RaiseProto;
import poker.proto.ProtoFactory.RechargeBankrollProto;
import poker.proto.ProtoFactory.ResultProto;
import poker.proto.ProtoFactory.RoomInfoProto;
import poker.util.JLog;
import poker.util.OrderHelper;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class RoomImpl implements Room {
	// 常量配置区
	public static final int ACTION_TIMEOUT = 20000;// 下注超时
	public static final int ROOM_PAUSE_TIME = 5000;// 房间暂停时间
	public static final double DRAWOFF_RATE = 0.05;// 抽成
	public static final int MAX_CHECK_TIME = 40000;// 玩家响应超时
	// 基础属性区
	protected int id;
	protected int roomType;
	protected int roomLevel;
	protected int holeCardsCount = 2;
	protected int minPlayerForBegin = 2;// 游戏开始条件：玩家数量下限
	protected int minPlayerForRun = 2;// 游戏运行条件：玩家数量下限
	protected int smallBlind;
	protected int bigBlind;
	protected int minTake;
	protected int maxTake;
	protected int averageTake;
	// 运行时区
	protected int phase = Phase.WAITING;
	protected Lock phaseLock = new ReentrantLock();
	protected Seats seats = null;
	protected ConcurrentHashMap<Integer, Player> spectatorMap = new ConcurrentHashMap<>();
	protected byte[] boardCards = new byte[5];
	protected List<Integer> anteLevelList;// 边池等级列表
	protected ConcurrentSkipListMap<Integer, Pot> potMap = new ConcurrentSkipListMap<>();
	protected CardRule cardRule = null;
	protected GameRule gameRule = null;
	protected int bankerSeat = -1;
	protected int smallBlindSeat = -1;
	protected int bigBlindSeat = -1;
	protected int smallBlindBettedCount = 0;
	protected int bigBlindBettedCount = 0;
	protected int dealtBoardCardsCount = 0;
	// 下注期间的临时状态
	protected Timer timer = null;
	protected Boolean flowtaskLock = new Boolean(true);// 流程锁
	protected TimerTask flowTask = null;// 流程任务
	protected int turn = 0;
	protected int minCall = 0;// 当前跟注额，未减个人已下注
	protected int minRaise = 0;
	protected boolean checkable = false;
	protected boolean bigBlindActed = false;
	protected ConcurrentHashMap<Integer, Player> leftPlayerMap = new ConcurrentHashMap<>();// 中途离开的玩家暂存，以持久化资产
	protected int maxAnteLevelLowerLimit;
	// 数据持久化对象
	protected MemberData memberData = new MemberData();
	protected ScoreData scoreData = new ScoreData();
	protected JLog log;

	// 房间信息初始化，从数据库或Webservice获取
	public RoomImpl(RoomVm rv) {
		if (rv.seatCount <= 2) {
			rv.seatCount = 6;
		}
		this.id = rv.id;
		this.smallBlind = rv.smallBlind;
		this.bigBlind = rv.bigBlind;
		this.minTake = rv.minTake;
		this.maxTake = rv.maxTake;
		this.averageTake = rv.averageTake;
		this.seats = new Seats(rv.seatCount);
		this.roomLevel = rv.level;
		init();// init中执行每个房间都不同的初始化
	}

	protected void init() {
		this.roomType = RoomType.NORMAL;
		this.holeCardsCount = 2;
		this.minPlayerForBegin = 2;
		this.minPlayerForRun = 2;
		log = new JLog("room/normal_" + this.id);
		cardRule = new CardRule(log);
		gameRule = new GameRule();
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public int getType() {
		return this.roomType;
	}

	@Override
	public int getLevel() {
		return this.roomLevel;
	}

	@Override
	public Seats getSeats() {
		return seats;
	}

	@Override
	public int getPhase() {
		return phase;
	}

	@Override
	public void setTimer(Timer timer) {
		this.timer = timer;
	};

	@Override
	public boolean playerEnter(Player player) {
		joinGamersGroup(player);
		EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
		enteredRoomBuilder.setRoomId(id);
		enteredRoomBuilder.setSeat(player.seat);
		enteredRoomBuilder.setRoomType(this.roomType);
		player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
		player.sendDirect(711035, roomStatusBytes());
		// 进入阶段完毕，将玩家通道状态置为正常
		player.connectionOk = true;
		if (player.seat > -1 && (phase != Phase.WAITING || seats.getSittingCount() < minPlayerForBegin)) {
			// 如果房间不能开始或正在运行中，则广播有人加入游戏
			PlayerJoinProto.Builder playerJoinBuilder = PlayerJoinProto.newBuilder();
			PlayerProto.Builder playerBuilder = PlayerProto.newBuilder();
			playerBuilder.setPlayerId(player.id);
			playerBuilder.setSeat(player.seat);
			if (player.info.getNickname() != null && player.info.getNickname().isEmpty() == false) {
				playerBuilder.setNickname(player.info.getNickname());
			}
			playerBuilder.setWinTimes(player.info.getWinTimes());
			playerBuilder.setLoseTimes(player.info.getLoseTimes());
			playerBuilder.setPortrait(player.info.getPortrait());
			playerBuilder.setPortraitBorder(0);
			playerBuilder.setMale(player.info.isMale());
			playerBuilder.setIsChallenger(false);
			playerBuilder.setBankRoll(player.bankroll);
			playerBuilder.setGold(player.info.getGold());
			playerBuilder.setMaxScore(player.info.getMaxScore());
			playerJoinBuilder.setPlayer(playerBuilder);
			playerJoinBuilder.setPlayerId(player.id);
			playerJoinBuilder.setSeat(player.seat);
			playerJoinBuilder.setSeatsRemain(this.seats.getSeatCount() - seats.getSittingCount());
			broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下
		}
		return true;
	}

	@Override
	public boolean reconnect(Player player) {
		log.debugln("Player " + player.id + " reconnect type " + this.roomType + " room " + id + ".");
		// 检查是否已加入过此房间
		boolean reconnected = false;
		Iterator<Player> playerItr = seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.id == player.id) {
				curPlayer.handler = player.handler;
				curPlayer.info = player.info;
				curPlayer.handler.player = curPlayer;
				player = curPlayer;
				reconnected = true;
				break;
			}
		}
		if (reconnected == false) {
			Player spct = spectatorMap.get(player.id);
			if (spct != null) {
				spct.handler = player.handler;
				spct.info = player.info;
				spct.handler.player = spct;
				player = spct;
				reconnected = true;
			}
		}
		if (reconnected) {
			EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
			enteredRoomBuilder.setRoomId(id);
			enteredRoomBuilder.setSeat(player.seat);
			enteredRoomBuilder.setRoomType(this.roomType);
			player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
			player.sendDirect(711035, roomStatusBytes());
			// 给重连玩家发送奖池列表
			if (phase > Phase.WAITING) {
				PotListProto.Builder potListBuilder = PotListProto.newBuilder();
				Iterator<Pot> potItr = potMap.values().iterator();
				while (potItr.hasNext()) {
					potListBuilder.addPotList(potItr.next().total);
				}
				player.sendDirect(712050, potListBuilder.build().toByteArray());
			}
			// 进入阶段完毕，将玩家通道状态置为正常
			player.connectionOk = true;
			// 重连时刚好在等待该玩家下注
			Player actingPlayer = this.seats.getBettingPlayer();
			if (player.seat > -1 && phase == Phase.BETTING && actingPlayer != null && actingPlayer.id == player.id) {
				long remainTime = flowTask.scheduledExecutionTime() - new Date().getTime();
				taskPause();
				log.debugln("Reconnection time remain：" + remainTime);
				if (remainTime >= 3000) {
					ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
					actionNoticeBuilder.setPlayerId(actingPlayer.id);
					actionNoticeBuilder.setSeat(this.seats.getBettingPlayer().seat);
					actionNoticeBuilder.setRemainBankroll(actingPlayer.bankroll);
					actionNoticeBuilder.setMinCall(minCall - actingPlayer.anteList[turn]);
					actionNoticeBuilder.setMinRaise(minRaise);
					actionNoticeBuilder.setCheckable(checkable);
					actionNoticeBuilder.setTimeout(remainTime - 1000);
					byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
					player.send(712020, actionNoticeMsg);
					final Player playerTemp = actingPlayer;
					TimerTask task = new TimerTask() {
						@Override
						public void run() {
							taskPause();
							log.debug("(Auto action 1)");
							playerTemp.handler.channelCtx.close();
							playerTemp.connectionOk = false;
							// 代替玩家尝试让牌
							actionHandler(playerTemp, 722031, null);

						}
					};
					taskContinue(task, remainTime);
				} else {
					// 代替玩家尝试让牌
					log.debug("(Auto action 2)");
					actionHandler(actingPlayer, 722031, null);
				}

			}
			log.debug("Reconnect succeed.");
			return true;
		}
		return false;
	}

	@Override
	public void requestHandler(Player player, int code, byte[] buff) {
		switch (code) {
		case 0:
			// 通道异常（包括客户端主动断开网络，却不发送离开消息）
			player.handler.channelCtx.close();
			player.connectionOk = false;
			if (phase == Phase.BETTING && seats.getBettingPlayer() != null && seats.getBettingPlayer().id == player.id) {
				taskPause();
				actionHandler(player, 722031, null);
			} else if (phase == Phase.WAITING) {
				if (player.seat > -1) {
					leaveGamersGroup(player);
				} else {
					leaveSpectatorGroup(player);
				}
				log.debugln("Player " + player.id + " leave room.");
				player.handler.leaveServer();
			}
			break;
		case 721000: {
			if (player.connectionOk) {
				player.send(711000, null);
				player.requestDate = new Date().getTime();
			}
			break;
		}
		case 721030:
			// 房间即时状态
			player.send(711035, roomStatusBytes());
			break;
		case 721050:
			// 补充筹码
			RechargeBankrollProto rechargeBankrollProto = null;
			try {
				if (buff != null) {
					rechargeBankrollProto = RechargeBankrollProto.parseFrom(buff);
				}
			} catch (InvalidProtocolBufferException e) {
			}
			if (rechargeBankrollProto != null && player.info.getGold() > 0) {
				player.recharge = rechargeBankrollProto.getCount();
				log.debugln("Player request " + player.id + " recharging " + player.recharge);
				// RechargeBankrollRespProto.Builder rechargeBuilder =
				// RechargeBankrollRespProto.newBuilder();
				// rechargeBuilder.setBankroll(player.bankroll);
				player.send(711053, null);
			} else {
				player.send(711055, null);
			}
			break;
		case 721060:
			// 坐下（原本是观众）
			if (spectatorMap.containsValue(player)) {
				log.debugln("[Player "+player.id+" exist in spectatorGroup.]");
				leaveSpectatorGroup(player);
				joinGamersGroup(player);
			}
			if (player.seat >= 0) {
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
				playerJoinBuilder.setSeatsRemain(this.seats.getSeatCount() - seats.getSittingCount());
				broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下成功
			} else {
				player.send(711066, null);// 单独通知坐下失败
				log.debugln("Player " + player.id + " sit down failed.");
			}
			break;
		case 721070: {
			// 站起（并成为观众）
			log.debugln("Player " + player.id + " request to stand up.");
			if (joinSpectatorGroup(player)) {
				// 先离开游戏组再加入观众组的话，会导致收不到游戏结束的消息。
				leaveGamersGroup(player);
				PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
				playerLeaveBuilder.setPlayerId(player.id);
				playerLeaveBuilder.setSeatsRemain(this.getSeats().getSeatCount() - seats.getSittingCount());
				broadcast(711075, playerLeaveBuilder.build().toByteArray());
			}
			break;
		}
		case 721090: {
			// 离开
			log.debugln("Player " + player.id + " request to leave.");
			if (player.seat > -1) {
				leaveGamersGroup(player);
				PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
				playerLeaveBuilder.setPlayerId(player.id);
				playerLeaveBuilder.setSeatsRemain(this.getSeats().getSeatCount() - seats.getSittingCount());
				broadcast(711095, playerLeaveBuilder.build().toByteArray());
			} else {
				leaveSpectatorGroup(player);
			}
			log.debugln("Player " + player.id + " leave room.");
			player.handler.channelCtx.close();
			player.handler.leaveServer();
			break;
		}
		default:
			if (phase == Phase.BETTING && seats.getBettingPlayer() != null && seats.getBettingPlayer().id == player.id) {
				taskPause();
			}
			actionHandler(player, code, buff);
			break;
		}
	}

	protected void taskContinue(TimerTask task, long delay) {
		synchronized (this.flowtaskLock) {
			this.flowTask = task;
			timer.schedule(task, delay);
		}
	}

	protected void taskPause() {
		synchronized (this.flowtaskLock) {
			if (flowTask != null) {
				flowTask.cancel();
				flowTask = null;
			}
		}
	}

	// 加入游戏组
	protected boolean joinGamersGroup(Player player) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		String strData = simpleDateFormat.format(new Date());
		if (seats.getSittingCount() >= this.seats.getSeatCount() || player.info.getGold() < minTake) {
			player.seat = -1;
			log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup failed.Room is full or gold is not enough.Gold:" + player.info.getGold());
			return false;
		}
		// 检查是否已存在。若是，则重连
		Iterator<Player> itr = seats.iterator();
		while (itr.hasNext()) {
			Player curPlayer = itr.next();
			if (curPlayer != null && curPlayer.id == player.id) {
				curPlayer.handler = player.handler;
				curPlayer.handler.player = curPlayer;
				curPlayer.info = player.info;
				curPlayer.connectionOk = true;
				// 如果游戏进行中的话，获取游戏即时信息
				// 另启动一线程处理“如果重连时刚好轮到自己下注”的情况
				log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup.Reconnected.Phase:" + phase);
				return true;
			}
		}
		// 检查是否已在观众组中存在
		// Player curSpect = spectatorMap.get(player.id);
		// if (curSpect != null) {
		// curSpect.handler = player.handler;
		// curSpect.handler.player = curSpect;
		// curSpect.info = player.info;
		// curSpect.connectionOk = true;
		// // 加入游戏组
		// log.infoln("[Enter]" + strData + " Player " + player.id +
		// " joinGamersGroup.Be spectator.Phase:" + phase);
		// return false;
		// }

		// 检查游戏进行阶段
		player.status = PlayerStatus.READY;
		if (seats.join(player) >= 0) {
			log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup.Player count:" + seats.getPlayingCount() + "/" + seats.getSittingCount() + "/" + seats.getSeatCount()
					+ ".Phase:" + phase);
			// 另启动一线程启动游戏
			phaseLock.lock();
			if (phase == Phase.WAITING && seats.getSittingCount() == minPlayerForBegin) {
				phase = Phase.INITING;
				new Thread(new Runnable() {
					@Override
					public void run() {
						// 当房间玩家数量达到下限，则开始游戏
						log.debugln("\r\nCondition met,game begin.Player count:" + seats.getPlayingCount() + "/" + seats.getSittingCount() + "/" + getSeats().getSeatCount());
						begin();
					}
				}).start();
			}
			phaseLock.unlock();
			return true;
		} else {
			// 如果前边没有退出方法，说明没有进入或重连成功，则失败。
			log.debugln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup failed.");
			return false;
		}
	}

	// 离开游戏组
	protected boolean leaveGamersGroup(Player player) {
		if (seats.leave(player)) {
			// 原来为phase>=2
			if (phase == Phase.BETTING && player.status != PlayerStatus.READY) {
				// 加入离开组
				if (player.anteTotal() > 0) {
					leftPlayerMap.put(player.id, player);
				}
				// 若该玩家的离开导致可下注人数不足minPlayerCountForRun，则立即进入afterAction
				// 不用<minRun可以防止多线程多次执行
				if (this.seats.getBetableCount() == minPlayerForRun - 1) {
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
				} else if (seats.getBettingPlayer() != null && seats.getBettingPlayer().id == player.id) {
					final Player tempPlayer = player;
					new Thread(new Runnable() {
						@Override
						public void run() {
							taskPause();
							actionHandler(tempPlayer, 722035, null);
						}
					}).start();
				}
			}
			// 所有筹码兑换为金币
			if (player.bankroll > 0) {
				memberData.memberGoldAdd(player.id, player.bankroll);
				player.info.setGold(player.info.getGold() + player.bankroll);
				player.bankroll = 0;
			}
			player.seat = -1;
			log.infoln("Player " + player.id + " leaveGamersGroup.");
			return true;
		} else {
			log.infoln("Player " + player.id + " leaveGamersGroup failed.");
			return false;
		}
	}

	// 加入观众组
	protected boolean joinSpectatorGroup(Player player) {
		spectatorMap.put(player.id, player);
		player.seat = -1;
		log.debugln("Player " + player.id + " joinSpectatorGroup.");
		return true;
	}

	// 离开观众组
	protected boolean leaveSpectatorGroup(Player player) {
		spectatorMap.remove(player.id);
		log.debugln("Player " + player.id + " leaveSpectatorGroup.");
		return true;
	}

	public void begin() {
		// 在begin中，不进行minPlayerForBegin的判断，因为淘汰赛中一大局中的第二小局会是2个人。
		phase = Phase.INITING;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		String strData = simpleDateFormat.format(new Date());
		log.testln("\r\n[Begin]" + strData + " Room " + id + " level " + roomLevel + ",blind" + this.smallBlind + "/" + this.bigBlind + "-----------------------------------------------------");
		cleanTimeoutPlayers();
		checkBankroll();
		log.debugln("Room " + id + "，playerCount：" + seats.getSittingCount() + "，player list：");
		int totalBankroll = 0;
		Iterator<Player> playerItr = seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				totalBankroll += curPlayer.bankroll;
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
			prepareAndBlind();
		} catch (BetablePlayerNotEnoughException e) {
			log.debugln("No player left in PrepareAndBlind.");
			interrupt();
			return;
		}
		pushRoomInfo();
		dealHoleCards();
		phase = Phase.BETTING;
		actionInTurn();
	}

	protected boolean couldBegin() {
		if (seats.getSittingCount() >= minPlayerForBegin) {
			return true;
		}
		return false;
	}

	public void interrupt() {
		// 房间进入暂停中
		phase = Phase.WAITING;
		pushRoomInfo();
		log.testln("----------------------------------Interrupt----------------------------------------");
	}

	protected void pushRoomInfo() {
		byte[] roomInfoBytes = roomStatusBytes();
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.connectionOk) {
				curPlayer.send(712005, roomInfoBytes);
			}
		}
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null && curSpect.connectionOk) {
				curSpect.send(712005, roomInfoBytes);
			}
		}
	}

	public void checkBankroll() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				curPlayer.info = memberData.getById(curPlayer.id);// 做成异步？？？？？
				if (curPlayer.info == null) {
					continue;
				}
				if (curPlayer.status == PlayerStatus.READY) {
					curPlayer.status = PlayerStatus.NORMAL;
				}
				int wannaToAdd = 0;
				if (curPlayer.recharge >= minTake) {
					wannaToAdd = curPlayer.recharge - curPlayer.bankroll;
					if (wannaToAdd > curPlayer.info.getGold()) {
						wannaToAdd = curPlayer.info.getGold();
					}
				} else if (curPlayer.bankroll < bigBlind) {
					// 检查筹码是否少于大盲并且未设置手动补充
					if (curPlayer.info.getGold() >= bigBlind) {
						if (curPlayer.info.getGold() >= averageTake) {
							wannaToAdd = averageTake;
						} else {
							wannaToAdd = curPlayer.info.getGold();
						}
					}
				}
				// 如果"筹码+想要补充的筹码"或"筹码+金币"<大盲，则站起
				if (curPlayer.bankroll + wannaToAdd < bigBlind || curPlayer.bankroll + curPlayer.info.getGold() < bigBlind) {
					log.debugln("Player " + curPlayer.id + " in " + curPlayer.seat + " standup.");
					// 金币不足无法补充，则移至观众席
					log.debugln("Player " + curPlayer.id + " has not enough gold to play.");
					leaveGamersGroup(curPlayer);
					joinSpectatorGroup(curPlayer);
				} else if (wannaToAdd > 0) {
					// 金币兑换为筹码
					memberData.memberGoldAdd(curPlayer.id, -wannaToAdd);
					curPlayer.info.setGold(curPlayer.info.getGold() - wannaToAdd);
					curPlayer.bankroll += wannaToAdd;
					curPlayer.recharge = 0;
					log.debugln("Player " + curPlayer.id + " in " + curPlayer.seat + " recharge：" + wannaToAdd);
				}
			}
		}
	}

	protected void prepareAndBlind() throws BetablePlayerNotEnoughException {
		// 清空奖池
		potMap.clear();
		// 洗牌
		cardRule.shuffle();
		// 初始化这局的公牌
		dealtBoardCardsCount = 0;
		for (int i = 0; i < boardCards.length; i++) {
			boardCards[i] = cardRule.getCard();
		}
		log.debug("Board cards：");
		cardRule.showCards(boardCards);
		// 设置庄家和当前玩家
		try {
			smallBlindBettedCount = 0;
			bigBlindBettedCount = 0;
			this.seats.reset(bankerSeat);// 取上一局庄家
			bankerSeat = this.seats.nextBetable().seat;// 设置本局庄家
			// 进行盲注并设置大小盲注座号
			Player smallBlindPlayer = this.seats.nextBetable();
			smallBlindBettedCount = smallBlindPlayer.blindBet(smallBlind);
			smallBlindSeat = smallBlindPlayer.seat;
			Player bigBlindPlayer = this.seats.nextBetable();
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
			log.debugln("Banker:" + +bankerSeat + ",player " + smallBlindPlayer.id + " blind:" + smallBlindPlayer.anteList[0] + ",player " + bigBlindPlayer.id + " blind:" + bigBlindPlayer.anteList[0]);
		} catch (BetablePlayerNotEnoughException e) {
			log.debugln("All player disconnected！");
			throw e;
		}
		turn = 0;
		leftPlayerMap.clear();
	}

	// 发牌，每个玩家2张牌，并通知观众正在发牌
	public void dealHoleCards() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player tempPlayer = playerItr.next();
			if (tempPlayer != null && tempPlayer.status == PlayerStatus.NORMAL) {
				tempPlayer.holeCards = cardRule.getCards(holeCardsCount);
				CardsProto.Builder cardsBuilder = CardsProto.newBuilder();
				cardsBuilder.setCards(ByteString.copyFrom(tempPlayer.holeCards));
				tempPlayer.send(712010, cardsBuilder.build().toByteArray());
			}
		}
		Iterator<Player> spectItr = spectatorMap.values().iterator();
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
				this.seats.reset(bigBlindSeat);
			} else {
				minRaise = bigBlind;
				this.seats.reset(bankerSeat);
			}
			// 将上轮让牌状态的重置为正常
			Iterator<Player> playerItr = this.seats.iterator();
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
		if (this.seats.getNoFoldCount() < minPlayerForRun || (this.seats.getBetableCount() < minPlayerForRun && minCall <= 0)) {

			afterOneTurn();
			return;
		}
		Player nextPlayer = this.seats.nextBetable();
		// 本轮结束条件：1.全部让牌，注额全0
		if (minCall <= 0 && nextPlayer.status == PlayerStatus.CHECK) {
			boolean allCheck = true;
			Iterator<Player> playerItr = this.seats.iterator();
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
			Iterator<Player> playerItr = this.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null && (curPlayer.status == PlayerStatus.NORMAL || curPlayer.status == PlayerStatus.CHECK) && curPlayer.bankroll > 0 && curPlayer.anteList[turn] < minCall) {
					allBetted = false;
					break;
				}
			}
			// 如果大盲下注最多（可能是all in并小于bigBlind），并且其它人都已无筹码，则取消大盲的第一轮话语权。
			if (turn == 0 && this.seats.getBetableCount() == 1 && seats.getBettingPlayer() != null
					&& (seats.getBettingPlayer().status == PlayerStatus.NORMAL || seats.getBettingPlayer().status == PlayerStatus.CHECK) && seats.getBettingPlayer().bankroll > 0) {
				bigBlindActed = true;
				log.debugln("Cancel big blind right.");
			}
			// 要考虑到大盲第一轮下注前离开的情况.
			playerItr = this.seats.iterator();
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
			log.debug("    Id:" + nextPlayer.id + " in " + this.seats.getBettingPlayer().seat + ",status:" + nextPlayer.status + ",bankroll：" + nextPlayer.bankroll + ",anted："
					+ nextPlayer.anteTotal() + "," + (checkable ? "checkable" : ("minCall：" + (minCall - nextPlayer.anteList[turn]))) + ",bigActed:" + bigBlindActed + ",minRaise:" + minRaise + "...");

			// 通知客户端下注
			ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
			actionNoticeBuilder.setPlayerId(nextPlayer.id);
			actionNoticeBuilder.setSeat(this.seats.getBettingPlayer().seat);
			actionNoticeBuilder.setRemainBankroll(nextPlayer.bankroll);
			actionNoticeBuilder.setMinCall(minCall - nextPlayer.anteList[turn]);
			actionNoticeBuilder.setMinRaise(minRaise);
			actionNoticeBuilder.setCheckable(checkable);
			byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
			broadcast(712020, actionNoticeMsg);

			final Player playerTemp = nextPlayer;
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					taskPause();
					playerTemp.handler.channelCtx.close();
					playerTemp.connectionOk = false;
					// 代替玩家尝试让牌
					log.debug("(Auto action 3)");
					actionHandler(playerTemp, 722031, null);
				}
			};
			int actionTimeountTemp = ACTION_TIMEOUT;
			if (playerTemp.connectionOk == false) {
				actionTimeountTemp = 3000;
			}
			taskContinue(task, actionTimeountTemp);

		}
	}

	public void actionHandler(Player player, int act, byte[] buff) {
		if (phase != Phase.BETTING || seats.getBettingPlayer() == null || seats.getBettingPlayer().id != player.id) {
			log.debugln("Player " + player.id + " action in a wrong time.");
			return;
		}
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
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 1, actionResult.ante);
			break;
		case 2:// 跟注
			log.debugln("call " + actionResult.ante);
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 2, actionResult.ante);
			break;
		case 3:// 加注
			log.debugln("raise " + actionResult.ante);
			minRaise = (player.anteList[turn] - minCall) * 2 + minCall;// 下一次的最小加注值
			minCall = player.anteList[turn];// 改跟注下限
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 3, actionResult.ante);
			break;
		case 4:// all in
			log.debugln("All in " + actionResult.ante);
			if (player.anteList[turn] > minCall) {
				minRaise = (player.anteList[turn] - minCall) * 2 + minCall;// 下一次的最小加注值
				minCall = player.anteList[turn];// 改跟注下限
			}
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 4, actionResult.ante);
			break;
		case 5:// 弃牌
			log.debugln("fold");
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 5, actionResult.ante);
			break;
		default:
			log.debugln("exception fold");
			somebodyAction(player.id, this.seats.getBettingPlayer().seat, 5, actionResult.ante);
			break;
		}

		if (this.seats.getBettingPlayer().seat == bigBlindSeat) {
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
			computePots();
			log.debug("       Pot list：");
			PotListProto.Builder potListBuilder = PotListProto.newBuilder();
			for (int i = 0; i < anteLevelList.size(); i++) {
				Pot curPot = potMap.get(anteLevelList.get(i));
				potListBuilder.addPotList(curPot.total);
				log.debug(curPot.total + "(" + curPot.lowerLimit + ")；");
			}
			log.debugln();
			broadcast(712050, potListBuilder.build().toByteArray());
		}
		// 发布公牌
		if (turn < 3 && this.seats.getBetableCount() >= minPlayerForRun) {
			if (turn == 0) {
				broadcastBoardCards(dealtBoardCardsCount + 1, 3);
			} else {
				broadcastBoardCards(dealtBoardCardsCount + 1, 1);
			}
			turn++;
			// 下一轮前将让牌状态改为正常
			Iterator<Player> playerItr = this.seats.iterator();
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
		if (this.seats.getNoFoldCount() > 1) {
			broadcastBoardCards(dealtBoardCardsCount + 1, 5 - dealtBoardCardsCount);
		}
		afterAction();
	}

	// 计算目前为止所有的奖池
	protected void computePots() {
		potMap.clear();
		// 计算出边池等级
		anteLevelList = new ArrayList<>();
		Iterator<Player> playerItr = this.seats.iterator();
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
			Iterator<Player> playerCompItr = this.seats.iterator();
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
		Iterator<Player> folderItr = this.seats.iterator();// 弃牌玩家
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

	protected void afterAction() {
		phase = Phase.DATA_PERSISTENT;
		log.debugln();
		// 计算胜出
		List<Score> scoreList = computeVictory();
		// 更新最大牌
		updateMaxCards(scoreList);
		// 分配奖励
		distributePots(scoreList);
		// 广播胜出
		broadcastVictory(scoreList);
		// 结算并持久化
		dataPersistence();
		// 状态重置
		statusReset();

		log.debugln("\r\nPlayer list：");
		Iterator<Player> playerItr = this.seats.iterator();
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

	public void updateMaxCards(List<Score> scoreList) {
		if (scoreList == null) {
			return;
		}
		for (int i = 0; i < scoreList.size(); i++) {
			// 更新玩家史上最大牌型（皇家德州扑克除外）
			if (scoreList.get(i).player.info.getMaxCardsValue() < scoreList.get(i).value) {
				memberData.memberMaxCardsUpdate(scoreList.get(i).id, scoreList.get(i).maxCards, scoreList.get(i).value);
			}
		}

	}

	protected void waitingForNextPlay() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// 清理无效玩家
				cleanTimeoutPlayers();
				if (seats.getSittingCount() >= minPlayerForBegin) {
					begin();
				} else {
					// 房间进入暂停中
					phase = Phase.WAITING;
					broadcast(712100, null);
					log.testln("Room " + id + " end,playing count:" + seats.getPlayingCount() + ",sitting count:" + seats.getSittingCount() + ",phase:" + phase);
				}
			}
		};
		taskContinue(task, ROOM_PAUSE_TIME);
	}

	public void broadcast(int code, byte[] bytes) {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				curPlayer.send(code, bytes);
			}
		}
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curPlayer=spectItr.next();
			curPlayer.send(code, bytes);
		}
	}

	public List<Score> computeVictory() {
		gameRule.reset();// 重置scoreList...
		// 将在座普通玩家加入计算
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && (curPlayer.status == PlayerStatus.NORMAL || curPlayer.status == PlayerStatus.CHECK || curPlayer.status == PlayerStatus.FOLD)) {
				if (curPlayer.holeCards == null) {
					log.debugln("Player " + curPlayer.id + " holecards is null");
					continue;
				}
				byte[] allCards = new byte[5 + this.holeCardsCount];
				for (int j = 0; j < 5; j++) {
					allCards[j] = boardCards[j];
				}
				for (int j = 0; j < this.holeCardsCount; j++) {
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
			memberData.memberWinLostUpdate(winnerIdList, loserIdList);
		}
		return playersCardsList;
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
			if (this.roomType == RoomType.KNOCKOUT) {
				scoreList.get(i).player.drawoff = 0;
			} else if (gainedTotal - scoreList.get(i).player.anteTotal() > 0) {
				scoreList.get(i).player.drawoff = (int) ((gainedTotal - scoreList.get(i).player.anteTotal()) * DRAWOFF_RATE);
			}
			scoreList.get(i).player.bankroll += scoreList.get(i).player.gained - scoreList.get(i).player.drawoff;
		}

	}

	public void broadcastVictory(List<Score> scoreList) {
		ResultProto.Builder resultBuilder = ResultProto.newBuilder();
		int noFoldCountTmp = this.seats.getNoFoldCount();
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
			if (this.roomType != RoomType.KNOCKOUT && actualGained > 0) {
				actualGained -= scoreList.get(i).player.drawoff;
			}
			winnerBuilder.setActualGained(actualGained);
			resultBuilder.addWinnerList(winnerBuilder);
		}
		byte[] result = resultBuilder.build().toByteArray();
		// 广播……胜负结果
		broadcast(712080, result);
		if (JLog.debug) {
			log.debugln("\r\nResult：");
			Iterator<Player> playerItr = this.seats.iterator();
			while (playerItr.hasNext()) {
				Player curPlayer = playerItr.next();
				if (curPlayer != null) {
					log.debugln("	id：" + curPlayer.id + " in " + curPlayer.seat + "，bankroll：" + curPlayer.bankroll + "，gained：" + curPlayer.gained + ",drawoff:" + curPlayer.drawoff);
				}
			}
		}
	}

	public void dataPersistence() {
		// 结算……按玩家获胜顺序结算……玩家状态重置
		Iterator<Player> playerItr = this.seats.iterator();
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
			int returnBack = curPlayer.anteTotal() - maxAnteLevelLowerLimit;
			if (returnBack > 0) {
				memberData.memberGoldAdd(curPlayer.id, returnBack);
				log.debugln("Player " + curPlayer.id + " get back gold：" + returnBack);
			}
		}
	}

	@Override
	public void cleanTimeoutPlayers() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				if (new Date().getTime() - curPlayer.requestDate > MAX_CHECK_TIME) {
					PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
					playerLeaveBuilder.setPlayerId(curPlayer.id);
					playerLeaveBuilder.setSeatsRemain(this.getSeats().getSeatCount() - seats.getSittingCount());
					broadcast(711095, playerLeaveBuilder.build().toByteArray());
					log.debugln("Remove timeout player " + curPlayer.id);
					leaveGamersGroup(curPlayer);
					curPlayer.handler.leaveServer();
				}
			}
		}
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null) {
				if (new Date().getTime() - curSpect.requestDate > MAX_CHECK_TIME) {
					log.debugln("Remove timeout spectator " + curSpect.id);
					leaveSpectatorGroup(curSpect);
					curSpect.handler.leaveServer();
				}
			}
		}

	}

	protected void statusReset() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				if (curPlayer.status == PlayerStatus.CHECK || curPlayer.status == PlayerStatus.FOLD) {
					curPlayer.status = PlayerStatus.NORMAL;
				}
				curPlayer.holeCards = null;
				curPlayer.gained = 0;// 清0
				curPlayer.drawoff = 0;
				curPlayer.resetAnteList();
			}
		}
	}

	// 房间即时状态
	@Override
	public byte[] roomStatusBytes() {
		RoomInfoProto.Builder roomInfo = RoomInfoProto.newBuilder();
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				MemberInfo memberInfo = curPlayer.info;// 从数据库取最新数据
				PlayerProto.Builder playerBuilder = PlayerProto.newBuilder();
				playerBuilder.setPlayerId(curPlayer.id);
				playerBuilder.setSeat(curPlayer.seat);
				playerBuilder.setNickname(memberInfo.getNickname() == null ? "" : memberInfo.getNickname());
				playerBuilder.setWinTimes(memberInfo.getWinTimes());
				playerBuilder.setLoseTimes(memberInfo.getLoseTimes());
				playerBuilder.setPortrait(memberInfo.getPortrait());
				playerBuilder.setPortraitBorder(0);
				playerBuilder.setMale(memberInfo.isMale());
				playerBuilder.setIsChallenger(false);
				playerBuilder.setBankRoll(curPlayer.bankroll);
				playerBuilder.setGold(memberInfo.getGold());
				playerBuilder.setMaxScore(memberInfo.getMaxScore());
				if (curPlayer.holeCards != null) {
					playerBuilder.setHoleCards(ByteString.copyFrom(curPlayer.holeCards));// 游戏开始前此值为空
				}
				for (int j = 0; j < curPlayer.anteList.length; j++) {
					playerBuilder.addAnteList(curPlayer.anteList[j]);
				}
				playerBuilder.setHeadImg(memberInfo.getHeadImg() == null ? "" : memberInfo.getHeadImg());
				// 加入列表
				roomInfo.addPlayerList(playerBuilder);
			}
		}
		roomInfo.setRoomId(id);
		roomInfo.setMinTake(minTake);
		roomInfo.setMaxTake(maxTake);
		roomInfo.setGamePhase(phase);
		roomInfo.setTurn(turn);
		roomInfo.setSmallBlind(smallBlind);
		roomInfo.setBankerSeat(bankerSeat);
		roomInfo.setSmallBlindSeat(smallBlindSeat);
		roomInfo.setSmallBlindBetted(smallBlindBettedCount);
		roomInfo.setBigBlindSeat(bigBlindSeat);
		roomInfo.setBigBlindBetted(bigBlindBettedCount);
		if (this.seats.getBettingPlayer() != null) {
			roomInfo.setBettingSeat(this.seats.getBettingPlayer().seat);
		}
		if (dealtBoardCardsCount > 0) {
			byte[] dealtCards = new byte[dealtBoardCardsCount];
			for (int i = 0; i < dealtBoardCardsCount; i++) {
				dealtCards[i] = boardCards[i];
			}
			roomInfo.setDealtBoardCards(ByteString.copyFrom(dealtCards));
		}
		return roomInfo.build().toByteArray();
	}

	// 广播___某人进行了某操作
	public void somebodyAction(int playerId, int seat, int act, int ante) {
		ActionBroadcastProto.Builder actionBroadcastBuilder = ActionBroadcastProto.newBuilder();
		actionBroadcastBuilder.setPlayerId(playerId);
		actionBroadcastBuilder.setSeat(seat);
		actionBroadcastBuilder.setAct(act);
		actionBroadcastBuilder.setAnte(ante);
		byte[] actionBroadcastBytes = actionBroadcastBuilder.build().toByteArray();
		broadcast(712040, actionBroadcastBytes);
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
		broadcast(712070, cardsInfoBytes);
		dealtBoardCardsCount += count;
	}

	@Override
	public int playerExist(int id) {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.id == id) {
				return curPlayer.connectionOk ? 1 : 2;
			}
		}
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null && curSpect.id == id) {
				return curSpect.connectionOk ? 1 : 2;
			}
		}
		return 0;
	}

}
