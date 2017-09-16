package poker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import poker.config.RoomConfig;
import poker.entity.RoomInfo;
import poker.external.CtrlCenterSync;
import poker.main.player.Player;
import poker.main.room.RoomImpl;
import poker.main.room.RoomProperty;
import poker.util.JLog;

public class Hall {
	private static JLog log = new JLog("Hall");

	private static Hall hall = new Hall();
	private static int uniqueNumber = 1;
	private static Timer timer = new Timer();

	public Map<Integer, Player> playerMap = new ConcurrentHashMap<>();

	public List<RoomImpl> roomList = new ArrayList<>();
	// public static List<Room> roomKnockList = new ArrayList<>();
	// public static List<Room> roomImperialList = new ArrayList<>();
	// public static List<Room> roomOmahaList = new ArrayList<>();

	public static Hall uniqueHall() {
		return hall;
	}

	public RoomImpl getNonFullRoom(int roomLevel) {
		for (int i = 0; i < roomList.size(); i++) {
			if (roomList.get(i).getLevel() == roomLevel && roomList.get(i).getSeats().getSittingCount() < roomList.get(i).getSeats().getSeatCount()) {
				return roomList.get(i);
			}
		}
		return null;
	}

	public Player getPlayer(int playerId) {
		return playerMap.get(playerId);
	}

	// 玩家进入大厅
	public void playerEnter(Player player) {
		playerMap.put(player.id, player);
	}

	// 进入房间
	public void enterRoomByLevel(int roomLevel, Player player) {
		synchronized (Hall.uniqueHall().roomList) {
			boolean hasEntered = false;
			for (int i = 0; i < roomList.size(); i++) {
				if (roomList.get(i).getLevel() == roomLevel && roomList.get(i).getSeats().getSittingCount() < roomList.get(i).getSeats().getSeatCount()) {
					player.room = roomList.get(i);
					hasEntered = roomList.get(i).playerEnter(player);
					break;
				}
			}
			if (hasEntered == false) {
				RoomImpl room = null;
				RoomProperty rv = new RoomProperty();
				rv.id = uniqueNumber++;
				RoomInfo roomInfo = RoomConfig.getNormalByLevel(roomLevel);
				if (roomInfo == null) {
					log.debugln("Get normal level " + roomLevel + " failed.");
					return;
				}
				rv.seatCount = roomInfo.getSeatCount();
				rv.smallBlind = roomInfo.getSmallBlind();
				rv.bigBlind = roomInfo.getBigBlind();
				rv.minTake = roomInfo.getMinTake();
				rv.maxTake = roomInfo.getMaxTake();
				rv.averageTake = roomInfo.getAverageTake();
				rv.level = roomLevel;
				room = new RoomImpl(rv, timer);
				roomList.add(room);
				player.room = room;
				room.playerEnter(player);
			}
		}
	}

	// 进入指定id的房间
	public boolean enterRoomById(int roomId, Player player) {
		synchronized (Hall.uniqueHall().roomList) {
			for (RoomImpl room : roomList) {
				if (roomId == room.getId()) {
					return room.playerEnter(player);
				}
			}
		}
		return false;
	}

	// 机器人进入房间
	public void robotEnterRoom(int roomLevel, Player player) {
		final int maxLimit = 3;
		synchronized (Hall.uniqueHall().roomList) {
			// 常规场（机器人）
			boolean hasEntered = false;
			for (int i = 0; i < roomList.size(); i++) {
				if (roomList.get(i).getLevel() == roomLevel && roomList.get(i).getSeats().getSittingCount() < maxLimit) {
					player.room = roomList.get(i);
					hasEntered = roomList.get(i).playerEnter(player);
					break;
				}
			}
			if (hasEntered == false) {
				RoomImpl room = null;
				RoomProperty rv = new RoomProperty();
				rv.id = uniqueNumber++;
				RoomInfo roomInfo = RoomConfig.getNormalByLevel(roomLevel);
				if (roomInfo == null) {
					log.debugln("Get normal level " + roomLevel + " failed.");
					return;
				}
				rv.seatCount = roomInfo.getSeatCount();
				rv.smallBlind = roomInfo.getSmallBlind();
				rv.bigBlind = roomInfo.getBigBlind();
				rv.minTake = roomInfo.getMinTake();
				rv.maxTake = roomInfo.getMaxTake();
				rv.averageTake = roomInfo.getAverageTake();
				rv.level = roomLevel;
				room = new RoomImpl(rv, timer);
				roomList.add(room);
				player.room = room;
				room.playerEnter(player);
			}

		}
	}

	//
	// protected void validation(byte[] buff, Player player, ChannelHandler
	// handler) {
	// if (buff == null) {
	// log.errorln("Buff is null in validation.");
	// return;
	// }
	// ValidationProto validationProto = null;
	// try {
	// validationProto = ValidationProto.parseFrom(buff);
	// } catch (InvalidProtocolBufferException e) {
	// log.infoln("Player validation data error.");
	// return;
	// }
	// MemberInfo memberInfo =
	// memberData.getByToken(validationProto.getToken());// 异步处理
	//
	// if (memberInfo == null) {
	// handler.send(711015, null);
	// log.infoln("Player Token expired.");
	// return;
	// } else {
	// log.debugln("Player " + memberInfo.getUsername() + " login.");
	// ValidationRespProto.Builder validationRespBuilder =
	// ValidationRespProto.newBuilder();
	// validationRespBuilder.setId(memberInfo.getId());
	// handler.send(711013, validationRespBuilder.build().toByteArray());
	// player = new Player();
	// player.id = memberInfo.getId();
	// player.info = memberInfo;
	// player.proxy = handler;
	// player.requestDate = new Date().getTime();
	// // 通知中控有玩家接入
	// CtrlCenterSync.getChannel().noticePlayerJoin(player.id);
	// }
	// }
	//
	// protected void enterRoom(byte[] buff, Player player) {
	// if (player == null) {
	// log.errorln("Need to validate.");
	// return;
	// }
	// if (buff == null) {
	// log.errorln("Buff is null in enterRoom.");
	// return;
	// }
	// EnterRoomProto enterRoomProto = null;
	// try {
	// enterRoomProto = EnterRoomProto.parseFrom(buff);
	// } catch (InvalidProtocolBufferException e) {
	// log.debugln("Player " + player.id + " validation error.");
	// e.printStackTrace();
	// return;
	// }
	//
	// boolean reconnectSucceed = false;
	// // 不判断是否重连，直接重连
	// if (playerMap.get(player.id) != null) {
	// if ((player.room = playerMap.get(player.id).room) != null) {
	// reconnectSucceed = player.room.eventHandler.reconnect(player);
	// log.debugln("Player " + player.id + " reconnected room " +
	// player.room.getId() + ".");
	// }
	// }
	// // 如果不是重连，或者重连失败，则正常进入
	// if (reconnectSucceed == false) {
	// // 判断是否追朋友
	// if (enterRoomProto.hasChasePlayerId()) {
	// // 查找好友所在房间
	// RoomImpl roomFriend = null;
	// int friendId = enterRoomProto.getChasePlayerId();
	// if (playerMap.get(friendId) != null) {
	// if ((roomFriend = playerMap.get(friendId).room) != null) {
	// log.debugln("Friend " + friendId + " in room " +
	// playerMap.get(friendId).room.getId());
	// } else {
	// log.debugln("Friend " + friendId + " has been left the room.");
	// }
	// } else {
	// log.debugln("Friend " + friendId + " not found.");
	// }
	// // 是否成功
	// if (roomFriend != null && roomFriend.getSeats().getSittingCount() <
	// roomFriend.getSeats().getSeatCount()) {
	// log.debugln("Player " + player.id + " chase friend " + friendId + ",room
	// found.");
	// player.room = roomFriend;
	// roomFriend.eventHandler.playerEnter(player);
	// } else {
	// EnterRoomRespProto.Builder enteredRoomBuilder =
	// EnterRoomRespProto.newBuilder();
	// enteredRoomBuilder.setRoomId(-1);
	// enteredRoomBuilder.setSeat(-1);
	// enteredRoomBuilder.setRoomType(-1);
	// player.handler.send(711025, enteredRoomBuilder.build().toByteArray());
	// log.debugln("Player " + player.id + " chase friend " + friendId + "
	// failed.");
	// }
	// // 追好友结束。
	// } else {
	// // 机器人进入
	// if (enterRoomProto.getRoomId() == -100) {
	// robotEnter(enterRoomProto, player);
	// } else if (enterRoomProto.getRoomType() == RoomType.NORMAL) {
	// // 普通房间
	// synchronized (roomList) {
	// boolean hasEntered = false;
	// for (int i = 0; i < roomList.size(); i++) {
	// if (roomList.get(i).getLevel() == enterRoomProto.getRoomLevel() &&
	// roomList.get(i).getSeats().getSittingCount() <
	// roomList.get(i).getSeats().getSeatCount()) {
	// player.room = roomList.get(i);
	// hasEntered = roomList.get(i).eventHandler.playerEnter(player);
	// break;
	// }
	// }
	// if (hasEntered == false) {
	// RoomImpl room = null;
	// RoomProperty rv = new RoomProperty();
	// rv.id = uniqueNumber++;
	// RoomInfo roomInfo =
	// RoomConfig.getNormalByLevel(enterRoomProto.getRoomLevel());
	// if (roomInfo == null) {
	// log.debugln("Get normal level " + enterRoomProto.getRoomLevel() + "
	// failed.");
	// return;
	// }
	// rv.seatCount = roomInfo.getSeatCount();
	// rv.smallBlind = roomInfo.getSmallBlind();
	// rv.bigBlind = roomInfo.getBigBlind();
	// rv.minTake = roomInfo.getMinTake();
	// rv.maxTake = roomInfo.getMaxTake();
	// rv.averageTake = roomInfo.getAverageTake();
	// rv.level = enterRoomProto.getRoomLevel();
	// room = new RoomImpl(rv, timer);
	// roomList.add(room);
	// player.room = room;
	// room.eventHandler.playerEnter(player);
	// }
	// }
	// }
	// }
	// // 加入playerMap
	// playerMap.put(player.id, player);
	// }
	//
	// }

	// 离开服务器
	public void leaveServer(Player player) {
		player.room = null;
		playerMap.remove(player.id);
		CtrlCenterSync.getChannel().noticePlayerLeave(player.id); // 通知中控有玩家离开
	}

}
