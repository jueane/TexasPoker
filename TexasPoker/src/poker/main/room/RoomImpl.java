package poker.main.room;

import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.ByteString;

import poker.entity.MemberInfo;
import poker.main.dealer.Dealer;
import poker.main.dealer.Phase;
import poker.main.dealer.Pot;
import poker.main.gamergroup.AudienceGroup;
import poker.main.gamergroup.GamerGroup;
import poker.main.gamergroup.GamerSeats;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.proto.ProtoFactory.ActionNoticeProto;
import poker.proto.ProtoFactory.EnterRoomRespProto;
import poker.proto.ProtoFactory.PlayerJoinProto;
import poker.proto.ProtoFactory.PlayerProto;
import poker.proto.ProtoFactory.PotListProto;
import poker.proto.ProtoFactory.RoomInfoProto;
import poker.util.JLog;

public class RoomImpl {
	public RoomProperty property;

	protected JLog log;

	public Dealer dealer;

	public GamerGroup gamerGroup;
	public AudienceGroup audienceGroup;
	// public GamerSeats seats = null;

	public int minPlayerForBegin = 2;// 游戏开始条件：玩家数量下限
	public int minPlayerForRun = 2;// 游戏运行条件：玩家数量下限

	// 房间信息初始化，从数据库或Webservice获取
	public RoomImpl(RoomProperty property, Timer timer) {
		if (property.seatCount <= 2) {
			property.seatCount = 6;
		}
		gamerGroup.seats = new GamerSeats(property.seatCount);
		this.property = property;
		// init中执行每个房间都不同的初始化
		this.minPlayerForBegin = 2;
		this.minPlayerForRun = 2;
		log = new JLog("room/normal_" + this.property.id);

		dealer = new Dealer(this, timer);
	}

	public int getId() {
		return this.property.id;
	}

	public int getType() {
		return RoomType.NORMAL;
	}

	public int getLevel() {
		return this.property.level;
	}

	public GamerSeats getSeats() {
		return gamerGroup.seats;
	}

	public int getPhase() {
		return dealer.getPhase();
	}

	public boolean playerEnter(Player player) {
		gamerGroup.joinGamersGroup(player);
		EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
		enteredRoomBuilder.setRoomId(property.id);
		enteredRoomBuilder.setSeat(player.seat);
		enteredRoomBuilder.setRoomType(property.roomType);
		player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
		player.sendDirect(711035, roomStatusBytes());
		
		// 进入阶段完毕，将玩家通道状态置为正常
		player.connectionOk = true;
		if (player.seat > -1 && (dealer.getPhase() != Phase.WAITING || gamerGroup.seats.getSittingCount() < minPlayerForBegin)) {
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
			playerJoinBuilder.setSeatsRemain(gamerGroup.seats.getSeatCount() - gamerGroup.seats.getSittingCount());

			gamerGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下
			audienceGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下
		}
		return true;
	}

	public boolean reconnect(Player player) {
		log.debugln("Player " + player.id + " reconnect type " + property.roomType + " room " + property.id + ".");

		// 检查是否可以重连（player是否已在房间中）
		// int exist = gamerGroup.playerExist(player.id);
		// if (exist != 0) {
		// }
		// exist = audienceGroup.playerExist(player.id);

		// 检查是否已加入过此房间
		boolean reconnected = false;
		Iterator<Player> playerItr = gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.id == player.id) {
				curPlayer.proxy = player.proxy;
				curPlayer.info = player.info;
				curPlayer.proxy.player = curPlayer;
				player = curPlayer;
				reconnected = true;
				break;
			}
		}
		if (reconnected == false) {
			Player spct = audienceGroup.spectatorMap.get(player.id);
			if (spct != null) {
				spct.proxy = player.proxy;
				spct.info = player.info;
				spct.proxy.player = spct;
				player = spct;
				reconnected = true;
			}
		}
		if (reconnected) {
			EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
			enteredRoomBuilder.setRoomId(property.id);
			enteredRoomBuilder.setSeat(player.seat);
			enteredRoomBuilder.setRoomType(property.roomType);
			player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
			player.sendDirect(711035, roomStatusBytes());
			// 给重连玩家发送奖池列表
			if (dealer.getPhase() > Phase.WAITING) {
				PotListProto.Builder potListBuilder = PotListProto.newBuilder();
				Iterator<Pot> potItr = dealer.actionRule.potPool.potMap.values().iterator();
				while (potItr.hasNext()) {
					potListBuilder.addPotList(potItr.next().total);
				}
				player.sendDirect(712050, potListBuilder.build().toByteArray());
			}
			// 进入阶段完毕，将玩家通道状态置为正常
			player.connectionOk = true;
			// 重连时刚好在等待该玩家下注
			Player actingPlayer = gamerGroup.seats.getBettingPlayer();
			if (player.seat > -1 && dealer.getPhase() == Phase.BETTING && actingPlayer != null && actingPlayer.id == player.id) {
				long remainTime = dealer.flowTask.scheduledExecutionTime() - new Date().getTime();
				dealer.taskPause();
				log.debugln("Reconnection time remain：" + remainTime);
				if (remainTime >= 3000) {
					ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
					actionNoticeBuilder.setPlayerId(actingPlayer.id);
					actionNoticeBuilder.setSeat(gamerGroup.seats.getBettingPlayer().seat);
					actionNoticeBuilder.setRemainBankroll(actingPlayer.bankroll);
					actionNoticeBuilder.setMinCall(dealer.actionRule.minCall - actingPlayer.anteList[dealer.actionRule.turn]);
					actionNoticeBuilder.setMinRaise(dealer.actionRule.minRaise);
					actionNoticeBuilder.setCheckable(dealer.actionRule.checkable);
					actionNoticeBuilder.setTimeout(remainTime - 1000);
					byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
					player.send(712020, actionNoticeMsg);
					final Player playerTemp = actingPlayer;
					TimerTask task = new TimerTask() {
						@Override
						public void run() {
							dealer.taskPause();
							log.debug("(Auto action 1)");
							playerTemp.proxy.noResponse();
							// 代替玩家尝试让牌
							dealer.actionRule.actionHandler(playerTemp, 722031, null);

						}
					};
					dealer.taskContinue(task, remainTime);
				} else {
					// 代替玩家尝试让牌
					log.debug("(Auto action 2)");
					dealer.actionRule.actionHandler(actingPlayer, 722031, null);
				}

			}
			log.debug("Reconnect succeed.");
			return true;
		}
		return false;
	}

	public void pushRoomInfo() {
		byte[] roomInfoBytes = roomStatusBytes();
		Iterator<Player> playerItr = gamerGroup.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.connectionOk) {
				curPlayer.send(712005, roomInfoBytes);
			}
		}
		Iterator<Player> spectItr = audienceGroup.spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null && curSpect.connectionOk) {
				curSpect.send(712005, roomInfoBytes);
			}
		}
	}

	public void statusReset() {
		Iterator<Player> playerItr = gamerGroup.seats.iterator();
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
	public byte[] roomStatusBytes() {
		RoomInfoProto.Builder roomInfo = RoomInfoProto.newBuilder();
		Iterator<Player> playerItr = gamerGroup.seats.iterator();
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
		roomInfo.setRoomId(property.id);
		roomInfo.setMinTake(property.minTake);
		roomInfo.setMaxTake(property.maxTake);
		roomInfo.setGamePhase(dealer.getPhase());
		roomInfo.setTurn(dealer.actionRule.turn);
		roomInfo.setSmallBlind(property.smallBlind);
		roomInfo.setBankerSeat(dealer.actionRule.bankerSeat);
		roomInfo.setSmallBlindSeat(dealer.actionRule.smallBlindSeat);
		roomInfo.setSmallBlindBetted(dealer.actionRule.smallBlindBettedCount);
		roomInfo.setBigBlindSeat(dealer.actionRule.bigBlindSeat);
		roomInfo.setBigBlindBetted(dealer.actionRule.bigBlindBettedCount);
		if (gamerGroup.seats.getBettingPlayer() != null) {
			roomInfo.setBettingSeat(gamerGroup.seats.getBettingPlayer().seat);
		}
		if (dealer.actionRule.dealtBoardCardsCount > 0) {
			byte[] dealtCards = new byte[dealer.actionRule.dealtBoardCardsCount];
			for (int i = 0; i < dealer.actionRule.dealtBoardCardsCount; i++) {
				dealtCards[i] = dealer.getBoardcards()[i];
			}
			roomInfo.setDealtBoardCards(ByteString.copyFrom(dealtCards));
		}
		return roomInfo.build().toByteArray();
	}


}
