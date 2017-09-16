package poker.main.player;

import java.util.Date;
import java.util.Iterator;

import com.google.protobuf.InvalidProtocolBufferException;

import poker.ChannelHandler;
import poker.Hall;
import poker.data.MemberData;
import poker.entity.MemberInfo;
import poker.external.CtrlCenterSync;
import poker.main.room.RoomImpl;
import poker.main.room.RoomType;
import poker.proto.ProtoFactory.EnterRoomProto;
import poker.proto.ProtoFactory.EnterRoomRespProto;
import poker.proto.ProtoFactory.RechargeBankrollProto;
import poker.proto.ProtoFactory.ValidationProto;
import poker.proto.ProtoFactory.ValidationRespProto;
import poker.proto.ProtoFactoryForConsole.RoomInfoReqProto;
import poker.proto.ProtoFactoryForConsole.RoomListProto;
import poker.proto.ProtoFactoryForConsole.RoomListProto.RoomProto;
import poker.util.JLog;

public class ChannelEventProxy {
	private static JLog log = new JLog("PlayerEventHandler");

	private MemberData memberData = new MemberData();

	private ChannelHandler channelHandler;
	
	public Player player;

	public ChannelEventProxy(ChannelHandler channelHandler) {
		this.channelHandler = channelHandler;
	}

	// 处理来自客户端的事件
	public void handle(int code, byte[] content) {
		switch (code) {
		case 721010: {
			validation(content);
			break;
		}
		case 721020: {
			enterRoom(content);
			break;
		}
		default:
			if (player != null && player.room != null) {
				requestHandler(player, code, content);
			}
			if (code >= 100 && code <= 1000) {
				log.debugln("Recv command from console：" + code);
				manage(code, content);
			}
			break;
		}
	}

	// 向客户端发送事件
	public void send(int code, byte[] content) {
		channelHandler.send(code, content);
	}

	protected void validation(byte[] content) {
		if (content == null) {
			log.errorln("Buff is null in validation.");
			return;
		}
		ValidationProto validationProto = null;
		try {
			validationProto = ValidationProto.parseFrom(content);
		} catch (InvalidProtocolBufferException e) {
			log.infoln("Player validation data error.");
			return;
		}
		MemberInfo memberInfo = memberData.getByToken(validationProto.getToken());// 异步处理

		if (memberInfo == null) {
			channelHandler.send(711015, null);
			log.infoln("Player Token expired.");
			return;
		} else {
			log.debugln("Player " + memberInfo.getUsername() + " login.");
			ValidationRespProto.Builder validationRespBuilder = ValidationRespProto.newBuilder();
			validationRespBuilder.setId(memberInfo.getId());
			// 验证成功
			channelHandler.send(711013, validationRespBuilder.build().toByteArray());
			player = new Player();
			player.id = memberInfo.getId();
			player.info = memberInfo;
			player.proxy = this;
			player.requestDate = new Date().getTime();

			// 通知中控有玩家接入
			// 这里使用生产者消费者模式
			CtrlCenterSync.getChannel().noticePlayerJoin(player.id);
		}
	}

	protected void enterRoom(byte[] buff) {
		if (player == null) {
			log.errorln("Need to validate.");
			return;
		}
		if (buff == null) {
			log.errorln("Buff is null in enterRoom.");
			return;
		}
		EnterRoomProto enterRoomProto = null;
		try {
			enterRoomProto = EnterRoomProto.parseFrom(buff);
		} catch (InvalidProtocolBufferException e) {
			log.debugln("Player " + player.id + " validation error.");
			e.printStackTrace();
			return;
		}

		// 判断是否要求重连
		boolean isReconnect = enterRoomProto.getIsReconnect();
		if (isReconnect) {
			player.reconnect();
			return;
		}

		if (enterRoomProto.hasChasePlayerId()) {
			int friendId = enterRoomProto.getChasePlayerId();
			if (player.chaseFriend(friendId) == false) {
				EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
				enteredRoomBuilder.setRoomId(-1);
				enteredRoomBuilder.setSeat(-1);
				enteredRoomBuilder.setRoomType(-1);
				send(711025, enteredRoomBuilder.build().toByteArray());
				log.debugln("Player " + player.id + " chase friend " + friendId + " failed.");
			}
			// 追好友结束。
		} else {
			// 机器人进入
			if (enterRoomProto.getRoomId() == -100) {
				player.enterRoom(enterRoomProto.getRoomLevel());
				return;
			}
			// 正常玩家进入
			if (enterRoomProto.getRoomType() == RoomType.NORMAL) {
				// 普通房间
				player.robotEnterRoom(enterRoomProto.getRoomLevel());
			}
		}

	}

	public void requestHandler(Player player, int code, byte[] buff) {
		switch (code) {
		case 0:
			// 通道异常（包括客户端主动断开网络，却不发送离开消息）
			player.proxy.noResponse();
			break;
		case 721000: {
			if (player.connectionOk) {
				player.send(711000, null);
				player.requestDate = new Date().getTime();
			} else {
				// 通知同步状态
			}
			break;
		}
		case 721030:
			// 同步房间即时状态
			player.send(711035, player.room.roomStatusBytes());
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
			if (player.sitDown() == false) {
				player.send(711066, null);// 单独通知坐下失败
				log.debugln("Player " + player.id + " sit down failed.");
			}
			break;
		case 721070: {
			// 站起（并成为观众）
			if (player.standUp() == false) {
				log.debugln("Player " + player.id + " stand up failed.");
			}
			break;
		}
		case 721090: {
			// 离开
			log.debugln("Player " + player.id + " request to leave.");
			player.leave();
			log.debugln("Player " + player.id + " leave room.");
			break;
		}
		default:
			player.action(player, code, buff);
			break;
		}
	}

	protected void manage(int code, byte[] content) {
		switch (code) {
		case 201: {
			if (content == null) {
				log.errorln("Content is null in manage 201.");
				return;
			}
			RoomInfoReqProto roomInfoReqProto = null;
			try {
				roomInfoReqProto = RoomInfoReqProto.parseFrom(content);
			} catch (InvalidProtocolBufferException e) {
				log.infoln("Manage 201 error.");
				return;
			}

			Iterator<RoomImpl> roomItr = Hall.uniqueHall().roomList.iterator();
			while (roomItr.hasNext()) {
				RoomImpl room = roomItr.next();
				if (room != null && room.getId() == roomInfoReqProto.getRoomId()) {
					player.send(101, room.roomStatusBytes());
					return;
				}
			}
			break;
		}
		case 202: {
			// 请求房间列表
			RoomListProto.Builder roomListBuilder = RoomListProto.newBuilder();
			for (int i = 0; i < Hall.uniqueHall().roomList.size(); i++) {
				RoomImpl room = Hall.uniqueHall().roomList.get(i);
				RoomProto.Builder roomBuilder = RoomProto.newBuilder();
				roomBuilder.setRoomId(room.getId());
				roomBuilder.setRoomType(room.getType());
				roomBuilder.setRoomLevel(room.getLevel());
				roomBuilder.setRoomPhase(room.getPhase());
				roomBuilder.setPlayingCount(room.getSeats().getPlayingCount());
				roomBuilder.setSittingCount(room.getSeats().getSittingCount());
				roomBuilder.setSeatsCount(room.getSeats().getSeatCount());
				roomBuilder.setBetableCount(room.getSeats().getBetableCount());
				roomBuilder.setNoFoldCount(room.getSeats().getNoFoldCount());
				roomListBuilder.addRoomList(roomBuilder);
			}
			break;
		}
		default:
			break;
		}

	}

	// 无响应
	public void noResponse() {
		player.connectionOk = false;
	}

	// 通道异常
	public void connectionException() {
	}

}
