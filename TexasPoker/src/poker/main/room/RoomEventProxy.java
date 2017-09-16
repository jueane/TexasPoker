//package poker.main.room;
//
//import java.util.Date;
//import java.util.Iterator;
//import java.util.TimerTask;
//
//import com.google.protobuf.InvalidProtocolBufferException;
//
//import poker.Hall;
//import poker.main.dealer.Phase;
//import poker.main.dealer.Pot;
//import poker.main.player.Player;
//import poker.proto.ProtoFactory.ActionNoticeProto;
//import poker.proto.ProtoFactory.EnterRoomRespProto;
//import poker.proto.ProtoFactory.PlayerJoinProto;
//import poker.proto.ProtoFactory.PlayerLeaveProto;
//import poker.proto.ProtoFactory.PlayerProto;
//import poker.proto.ProtoFactory.PotListProto;
//import poker.proto.ProtoFactory.RechargeBankrollProto;
//import poker.util.JLog;
//
//public class RoomEventProxy {
//
//	protected JLog log;
//	RoomImpl room;
//
//	public RoomEventProxy(RoomImpl room) {
//		this.room = room;
//	}
//
////	public boolean playerEnter(Player player) {
////		room.gamerGroup.joinGamersGroup(player);
////		EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
////		enteredRoomBuilder.setRoomId(room.property.id);
////		enteredRoomBuilder.setSeat(player.seat);
////		enteredRoomBuilder.setRoomType(room.property.roomType);
////		player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
////		player.sendDirect(711035, room.roomStatusBytes());
////		// 进入阶段完毕，将玩家通道状态置为正常
////		player.connectionOk = true;
////		if (player.seat > -1 && (room.dealer.getPhase() != Phase.WAITING || room.gamerGroup.seats.getSittingCount() < room.minPlayerForBegin)) {
////			// 如果房间不能开始或正在运行中，则广播有人加入游戏
////			PlayerJoinProto.Builder playerJoinBuilder = PlayerJoinProto.newBuilder();
////			PlayerProto.Builder playerBuilder = PlayerProto.newBuilder();
////			playerBuilder.setPlayerId(player.id);
////			playerBuilder.setSeat(player.seat);
////			if (player.info.getNickname() != null && player.info.getNickname().isEmpty() == false) {
////				playerBuilder.setNickname(player.info.getNickname());
////			}
////			playerBuilder.setWinTimes(player.info.getWinTimes());
////			playerBuilder.setLoseTimes(player.info.getLoseTimes());
////			playerBuilder.setPortrait(player.info.getPortrait());
////			playerBuilder.setPortraitBorder(0);
////			playerBuilder.setMale(player.info.isMale());
////			playerBuilder.setIsChallenger(false);
////			playerBuilder.setBankRoll(player.bankroll);
////			playerBuilder.setGold(player.info.getGold());
////			playerBuilder.setMaxScore(player.info.getMaxScore());
////			playerJoinBuilder.setPlayer(playerBuilder);
////			playerJoinBuilder.setPlayerId(player.id);
////			playerJoinBuilder.setSeat(player.seat);
////			playerJoinBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
////
////			room.gamerGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下
////			room.audienceGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下
////		}
////		return true;
////	}
//
//	public boolean reconnect(Player player) {
//		log.debugln("Player " + player.id + " reconnect type " + room.property.roomType + " room " + room.property.id + ".");
//
//		// 检查是否可以重连（player是否已在房间中）
//		// int exist = room.gamerGroup.playerExist(player.id);
//		// if (exist != 0) {
//		// }
//		// exist = room.audienceGroup.playerExist(player.id);
//
//		// 检查是否已加入过此房间
//		boolean reconnected = false;
//		Iterator<Player> playerItr = room.gamerGroup.seats.iterator();
//		while (playerItr.hasNext()) {
//			Player curPlayer = playerItr.next();
//			if (curPlayer != null && curPlayer.id == player.id) {
//				curPlayer.proxy = player.proxy;
//				curPlayer.info = player.info;
//				curPlayer.proxy.player = curPlayer;
//				player = curPlayer;
//				reconnected = true;
//				break;
//			}
//		}
//		if (reconnected == false) {
//			Player spct = room.audienceGroup.spectatorMap.get(player.id);
//			if (spct != null) {
//				spct.proxy = player.proxy;
//				spct.info = player.info;
//				spct.proxy.player = spct;
//				player = spct;
//				reconnected = true;
//			}
//		}
//		if (reconnected) {
//			EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
//			enteredRoomBuilder.setRoomId(room.property.id);
//			enteredRoomBuilder.setSeat(player.seat);
//			enteredRoomBuilder.setRoomType(room.property.roomType);
//			player.sendDirect(711025, enteredRoomBuilder.build().toByteArray());// 通知客户端进入房间成功
//			player.sendDirect(711035, room.roomStatusBytes());
//			// 给重连玩家发送奖池列表
//			if (room.dealer.getPhase() > Phase.WAITING) {
//				PotListProto.Builder potListBuilder = PotListProto.newBuilder();
//				Iterator<Pot> potItr = room.dealer.actionRule.potPool.potMap.values().iterator();
//				while (potItr.hasNext()) {
//					potListBuilder.addPotList(potItr.next().total);
//				}
//				player.sendDirect(712050, potListBuilder.build().toByteArray());
//			}
//			// 进入阶段完毕，将玩家通道状态置为正常
//			player.connectionOk = true;
//			// 重连时刚好在等待该玩家下注
//			Player actingPlayer = room.gamerGroup.seats.getBettingPlayer();
//			if (player.seat > -1 && room.dealer.getPhase() == Phase.BETTING && actingPlayer != null && actingPlayer.id == player.id) {
//				long remainTime = room.dealer.flowTask.scheduledExecutionTime() - new Date().getTime();
//				room.dealer.taskPause();
//				log.debugln("Reconnection time remain：" + remainTime);
//				if (remainTime >= 3000) {
//					ActionNoticeProto.Builder actionNoticeBuilder = ActionNoticeProto.newBuilder();
//					actionNoticeBuilder.setPlayerId(actingPlayer.id);
//					actionNoticeBuilder.setSeat(room.gamerGroup.seats.getBettingPlayer().seat);
//					actionNoticeBuilder.setRemainBankroll(actingPlayer.bankroll);
//					actionNoticeBuilder.setMinCall(room.dealer.actionRule.minCall - actingPlayer.anteList[room.dealer.actionRule.turn]);
//					actionNoticeBuilder.setMinRaise(room.dealer.actionRule.minRaise);
//					actionNoticeBuilder.setCheckable(room.dealer.actionRule.checkable);
//					actionNoticeBuilder.setTimeout(remainTime - 1000);
//					byte[] actionNoticeMsg = actionNoticeBuilder.build().toByteArray();
//					player.send(712020, actionNoticeMsg);
//					final Player playerTemp = actingPlayer;
//					TimerTask task = new TimerTask() {
//						@Override
//						public void run() {
//							room.dealer.taskPause();
//							log.debug("(Auto action 1)");
//							playerTemp.handler.channelCtx.close();
//							playerTemp.connectionOk = false;
//							// 代替玩家尝试让牌
//							room.dealer.actionRule.actionHandler(playerTemp, 722031, null);
//
//						}
//					};
//					room.dealer.taskContinue(task, remainTime);
//				} else {
//					// 代替玩家尝试让牌
//					log.debug("(Auto action 2)");
//					room.dealer.actionRule.actionHandler(actingPlayer, 722031, null);
//				}
//
//			}
//			log.debug("Reconnect succeed.");
//			return true;
//		}
//		return false;
//	}
//	//
//	// public void requestHandler(Player player, int code, byte[] buff) {
//	// switch (code) {
//	// case 0:
//	// // 通道异常（包括客户端主动断开网络，却不发送离开消息）
//	// player.handler.channelCtx.close();
//	// player.connectionOk = false;
//	// if (room.dealer.getPhase() == Phase.BETTING && room.gamerGroup.seats.getBettingPlayer() != null && room.gamerGroup.seats.getBettingPlayer().id == player.id) {
//	// room.dealer.taskPause();
//	// room.dealer.actionRule.actionHandler(player, 722031, null);
//	// } else if (room.dealer.getPhase() == Phase.WAITING) {
//	// if (player.seat > -1) {
//	// room.gamerGroup.leaveGamersGroup(player);
//	// } else {
//	// room.audienceGroup.leaveSpectatorGroup(player);
//	// }
//	// log.debugln("Player " + player.id + " leave room.");
//	// Hall.uniqueHall().leaveServer(player);
//	// }
//	// break;
//	// case 721000: {
//	// if (player.connectionOk) {
//	// player.send(711000, null);
//	// player.requestDate = new Date().getTime();
//	// }
//	// break;
//	// }
//	// case 721030:
//	// // 房间即时状态
//	// player.send(711035, room.roomStatusBytes());
//	// break;
//	// case 721050:
//	// // 补充筹码
//	// RechargeBankrollProto rechargeBankrollProto = null;
//	// try {
//	// if (buff != null) {
//	// rechargeBankrollProto = RechargeBankrollProto.parseFrom(buff);
//	// }
//	// } catch (InvalidProtocolBufferException e) {
//	// }
//	// if (rechargeBankrollProto != null && player.info.getGold() > 0) {
//	// player.recharge = rechargeBankrollProto.getCount();
//	// log.debugln("Player request " + player.id + " recharging " + player.recharge);
//	// // RechargeBankrollRespProto.Builder rechargeBuilder =
//	// // RechargeBankrollRespProto.newBuilder();
//	// // rechargeBuilder.setBankroll(player.bankroll);
//	// player.send(711053, null);
//	// } else {
//	// player.send(711055, null);
//	// }
//	// break;
//	// case 721060:
//	// // 坐下（原本是观众）
//	// if (room.audienceGroup.spectatorMap.containsValue(player)) {
//	// log.debugln("[Player " + player.id + " exist in spectatorGroup.]");
//	// room.audienceGroup.leaveSpectatorGroup(player);
//	// room.gamerGroup.joinGamersGroup(player);
//	// }
//	// if (player.seat >= 0) {
//	// PlayerJoinProto.Builder playerJoinBuilder = PlayerJoinProto.newBuilder();
//	// PlayerProto.Builder playerBuilder = PlayerProto.newBuilder();
//	// playerBuilder.setPlayerId(player.id);
//	// playerBuilder.setSeat(player.seat);
//	// playerBuilder.setNickname(player.info.getNickname() == null ? "" : player.info.getNickname());
//	// playerBuilder.setWinTimes(player.info.getWinTimes());
//	// playerBuilder.setLoseTimes(player.info.getLoseTimes());
//	// playerBuilder.setPortrait(player.info.getPortrait());
//	// playerBuilder.setPortraitBorder(0);
//	// playerBuilder.setMale(player.info.isMale());
//	// playerBuilder.setIsChallenger(false);
//	// playerBuilder.setBankRoll(player.bankroll);
//	// playerBuilder.setGold(player.info.getGold());
//	// playerBuilder.setMaxScore(player.info.getMaxScore());
//	// playerBuilder.setHeadImg(player.info.getHeadImg() == null ? "" : player.info.getHeadImg());
//	// playerJoinBuilder.setPlayer(playerBuilder);
//	// playerJoinBuilder.setPlayerId(player.id);
//	// playerJoinBuilder.setSeat(player.seat);
//	// playerJoinBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
//	// room.gamerGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下成功
//	// room.audienceGroup.broadcast(711065, playerJoinBuilder.build().toByteArray());// 广播坐下成功
//	// } else {
//	// player.send(711066, null);// 单独通知坐下失败
//	// log.debugln("Player " + player.id + " sit down failed.");
//	// }
//	// break;
//	// case 721070: {
//	// // 站起（并成为观众）
//	// log.debugln("Player " + player.id + " request to stand up.");
//	// if (room.audienceGroup.joinSpectatorGroup(player)) {
//	// // 先离开游戏组再加入观众组的话，会导致收不到游戏结束的消息。
//	// room.gamerGroup.leaveGamersGroup(player);
//	// PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
//	// playerLeaveBuilder.setPlayerId(player.id);
//	// playerLeaveBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
//	// room.gamerGroup.broadcast(711075, playerLeaveBuilder.build().toByteArray());
//	// room.audienceGroup.broadcast(711075, playerLeaveBuilder.build().toByteArray());
//	// }
//	// break;
//	// }
//	// case 721090: {
//	// // 离开
//	// log.debugln("Player " + player.id + " request to leave.");
//	// if (player.seat > -1) {
//	// room.gamerGroup.leaveGamersGroup(player);
//	// PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
//	// playerLeaveBuilder.setPlayerId(player.id);
//	// playerLeaveBuilder.setSeatsRemain(room.gamerGroup.seats.getSeatCount() - room.gamerGroup.seats.getSittingCount());
//	// room.gamerGroup.broadcast(711095, playerLeaveBuilder.build().toByteArray());
//	// room.audienceGroup.broadcast(711095, playerLeaveBuilder.build().toByteArray());
//	// } else {
//	// room.audienceGroup.leaveSpectatorGroup(player);
//	// }
//	// log.debugln("Player " + player.id + " leave room.");
//	// player.handler.channelCtx.close();
//	// Hall.uniqueHall().leaveServer(player);
//	// break;
//	// }
//	// default:
//	// if (room.dealer.getPhase() == Phase.BETTING && room.gamerGroup.seats.getBettingPlayer() != null && room.gamerGroup.seats.getBettingPlayer().id == player.id) {
//	// room.dealer.taskPause();
//	// }
//	// room.dealer.actionRule.actionHandler(player, code, buff);
//	// break;
//	// }
//	// }
//
//}
