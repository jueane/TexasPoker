package poker.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import poker.config.RoomConfig;
import poker.data.MemberData;
import poker.entity.MemberInfo;
import poker.entity.RoomInfo;
import poker.entity.RoomKnockoutInfo;
import poker.main.external.CtrlCenterSync;
import poker.main.player.Player;
import poker.main.room.Room;
import poker.main.room.RoomType;
import poker.main.room.impl.RoomImpl;
import poker.main.room.impl.RoomVm;
import poker.main.room.impl.imperial.ImperialRoomImpl;
import poker.main.room.impl.knockout.KnockoutRoomImpl;
import poker.main.room.impl.knockout.KnockoutRoomVm;
import poker.main.room.impl.omaha.OmahaRoomImpl;
import poker.proto.ProtoFactory.EnterRoomProto;
import poker.proto.ProtoFactory.EnterRoomRespProto;
import poker.proto.ProtoFactory.ValidationProto;
import poker.proto.ProtoFactory.ValidationRespProto;
import poker.proto.ProtoFactoryForConsole.RoomInfoReqProto;
import poker.proto.ProtoFactoryForConsole.RoomListProto;
import poker.proto.ProtoFactoryForConsole.RoomListProto.RoomProto;
import poker.util.JLog;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChannelHandler extends ChannelInboundHandlerAdapter {
	public static Map<Integer, Player> playerMap = new ConcurrentHashMap<>();

	public static List<Room> roomList = new ArrayList<>();
	public static List<Room> roomKnockList = new ArrayList<>();
	public static List<Room> roomImperialList = new ArrayList<>();
	public static List<Room> roomOmahaList = new ArrayList<>();
	protected static int uniqueNumber = 1;
	protected static Timer timer = new Timer();

	public ChannelHandlerContext channelCtx = null;
	protected ByteBuf buf = null;
	public Player player = null;
	protected MemberData memberData = new MemberData();
	protected static JLog log = new JLog("EventHandler");

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		buf = Unpooled.buffer(1024 * 10);
		ctx.fireChannelRegistered();
		channelCtx = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;
		try {
			buf.writeBytes(in);
			while (true) {
				if (buf.readableBytes() < 8) {
					break;
				}
				// 获取head
				int iCode = 0;
				int iLength = 0;
				byte[] head = new byte[8];
				buf.readBytes(head);
				iCode |= head[0] & 0xff;
				iCode |= (head[1] << 8) & 0xffff;
				iCode |= (head[2] << 16) & 0xffffff;
				iCode |= (head[3] << 24) & 0xffffffff;
				iLength |= head[4] & 0xff;
				iLength |= (head[5] << 8) & 0xffff;
				iLength |= (head[6] << 16) & 0xffffff;
				iLength |= (head[7] << 24) & 0xffffffff;
				if (iLength < 0 || iLength > 1024 * 10) {
					if (iLength != 1414012975) {
						log.debugln("Packet length error.Length:" + iLength + ",hash:" + this.hashCode());
					}
					ctx.close();
					return;
				}
				// 内容不完整，缓冲区回滚
				if (buf.readableBytes() < iLength) {
					buf.readerIndex(buf.readerIndex() - 8);
					log.debugln("recv:" + iCode + "," + iLength + ",roll back.");
					break;
				}
				Pack pack = new Pack();
				pack.code = iCode;
				pack.length = iLength;
				// 获取 content
				if (iLength > 0) {
					pack.content = new byte[iLength];
					buf.readBytes(pack.content);
				}
				buf.discardReadBytes();
				// log.debugln("recv：" + iCode + "，" + iLength + "，" +
				// iLength);
				packProcessor(pack);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		((ByteBuf) msg).release();
	}

	private void packProcessor(Pack pack) {
		switch (pack.code) {
		case 721010: {
			validation(pack.content);
			break;
		}
		case 721020: {
			enterRoom(pack.content);
			break;
		}
		default:
			if (player != null && player.room != null) {
				player.room.requestHandler(player, pack.code, pack.content);
			}
			if (pack.code >= 100 && pack.code <= 1000) {
				log.debugln("Recv command from console：" + pack.code);
				manage(pack);
			}
			break;
		}

	}

	protected void manage(Pack pack) {
		switch (pack.code) {
		case 201: {
			if (pack.content == null) {
				log.errorln("Content is null in manage 201.");
				return;
			}
			RoomInfoReqProto roomInfoReqProto = null;
			try {
				roomInfoReqProto = RoomInfoReqProto.parseFrom(pack.content);
			} catch (InvalidProtocolBufferException e) {
				log.infoln("Manage 201 error.");
				return;
			}

			Iterator<Room> roomItr = roomList.iterator();
			while (roomItr.hasNext()) {
				Room room = roomItr.next();
				if (room != null && room.getId() == roomInfoReqProto.getRoomId()) {
					send(101, room.roomStatusBytes());
					return;
				}
			}
			roomItr = roomKnockList.iterator();
			while (roomItr.hasNext()) {
				Room room = roomItr.next();
				if (room != null && room.getId() == roomInfoReqProto.getRoomId()) {
					send(101, room.roomStatusBytes());
					return;
				}
			}
			roomItr = roomImperialList.iterator();
			while (roomItr.hasNext()) {
				Room room = roomItr.next();
				if (room != null && room.getId() == roomInfoReqProto.getRoomId()) {
					send(101, room.roomStatusBytes());
					return;
				}
			}
			roomItr = roomOmahaList.iterator();
			while (roomItr.hasNext()) {
				Room room = roomItr.next();
				if (room != null && room.getId() == roomInfoReqProto.getRoomId()) {
					send(101, room.roomStatusBytes());
					return;
				}
			}
			break;
		}
		case 202: {
			// 请求房间列表
			RoomListProto.Builder roomListBuilder = RoomListProto.newBuilder();
			for (int i = 0; i < roomList.size(); i++) {
				Room room = roomList.get(i);
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
			for (int i = 0; i < roomKnockList.size(); i++) {
				Room room = roomKnockList.get(i);
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
			for (int i = 0; i < roomImperialList.size(); i++) {
				Room room = roomImperialList.get(i);
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
			for (int i = 0; i < roomOmahaList.size(); i++) {
				Room room = roomOmahaList.get(i);
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
			send(102, roomListBuilder.build().toByteArray());
			break;
		}
		default:
			break;
		}

	}

	protected void validation(byte[] buff) {
		if (buff == null) {
			log.errorln("Buff is null in validation.");
			return;
		}
		ValidationProto validationProto = null;
		try {
			validationProto = ValidationProto.parseFrom(buff);
		} catch (InvalidProtocolBufferException e) {
			log.infoln("Player validation data error.");
			return;
		}
		MemberInfo memberInfo = memberData.getByToken(validationProto.getToken());// 异步处理

		if (memberInfo == null) {
			this.send(711015, null);
			log.infoln("Player Token expired.");
			return;
		} else {
			log.debugln("Player " + memberInfo.getUsername() + " login.");
			ValidationRespProto.Builder validationRespBuilder = ValidationRespProto.newBuilder();
			validationRespBuilder.setId(memberInfo.getId());
			this.send(711013, validationRespBuilder.build().toByteArray());
			player = new Player();
			player.id = memberInfo.getId();
			player.info = memberInfo;
			player.handler = this;
			player.requestDate = new Date().getTime();
			// 通知中控有玩家接入
			CtrlCenterSync.getChannel().noticePlayerJoin(player.id);
		}
	}

	private void enterRoom(byte[] buff) {
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

		boolean reconnectSucceed = false;
		// 不判断是否重连，直接重连
		if (playerMap.get(player.id) != null) {
			if ((this.player.room = playerMap.get(player.id).room) != null) {
				reconnectSucceed = this.player.room.reconnect(this.player);
				log.debugln("Player " + player.id + " reconnected room " + this.player.room.getId() + ".");
			}
		}
		// 如果不是重连，或者重连失败，则正常进入
		if (reconnectSucceed == false) {
			// 判断是否追朋友
			if (enterRoomProto.hasChasePlayerId()) {
				// 查找好友所在房间
				Room roomFriend = null;
				int friendId = enterRoomProto.getChasePlayerId();
				if (playerMap.get(friendId) != null) {
					if ((roomFriend = playerMap.get(friendId).room) != null) {
						log.debugln("Friend " + friendId + " in room " + playerMap.get(friendId).room.getId());
					} else {
						log.debugln("Friend " + friendId + " has been left the room.");
					}
				} else {
					log.debugln("Friend " + friendId + " not found.");
				}
				// 是否成功
				if (roomFriend != null && roomFriend.getSeats().getSittingCount() < roomFriend.getSeats().getSeatCount()) {
					log.debugln("Player " + player.id + " chase friend " + friendId + ",room found.");
					player.room = roomFriend;
					roomFriend.playerEnter(player);
				} else {
					EnterRoomRespProto.Builder enteredRoomBuilder = EnterRoomRespProto.newBuilder();
					enteredRoomBuilder.setRoomId(-1);
					enteredRoomBuilder.setSeat(-1);
					enteredRoomBuilder.setRoomType(-1);
					this.send(711025, enteredRoomBuilder.build().toByteArray());
					log.debugln("Player " + player.id + " chase friend " + friendId + " failed.");
				}
				// 追好友结束。
			} else {
				// 机器人进入
				if (enterRoomProto.getRoomId() == -100) {
					robotEnter(enterRoomProto);
				} else if (enterRoomProto.getRoomType() == RoomType.NORMAL) {
					// 普通房间
					synchronized (roomList) {
						boolean hasEntered = false;
						for (int i = 0; i < roomList.size(); i++) {
							if (roomList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomList.get(i).getSeats().getSittingCount() < roomList.get(i).getSeats().getSeatCount()) {
								player.room = roomList.get(i);
								hasEntered = roomList.get(i).playerEnter(player);
								break;
							}
						}
						if (hasEntered == false) {
							Room room = null;
							RoomVm rv = new RoomVm();
							rv.id = uniqueNumber++;
							RoomInfo roomInfo = RoomConfig.getNormalByLevel(enterRoomProto.getRoomLevel());
							if (roomInfo == null) {
								log.debugln("Get normal level " + enterRoomProto.getRoomLevel() + " failed.");
								return;
							}
							rv.seatCount = roomInfo.getSeatCount();
							rv.smallBlind = roomInfo.getSmallBlind();
							rv.bigBlind = roomInfo.getBigBlind();
							rv.minTake = roomInfo.getMinTake();
							rv.maxTake = roomInfo.getMaxTake();
							rv.averageTake = roomInfo.getAverageTake();
							rv.level = enterRoomProto.getRoomLevel();
							room = new RoomImpl(rv);
							room.setTimer(timer);
							roomList.add(room);
							player.room = room;
							room.playerEnter(player);
						}
					}
				} else if (enterRoomProto.getRoomType() == RoomType.KNOCKOUT) {
					// 淘汰赛房间
					synchronized (roomKnockList) {
						log.debugln("Player " + this.player.id + " request to enter.");
						boolean hasEntered = false;
						for (int i = 0; i < roomKnockList.size(); i++) {
							if (roomKnockList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomKnockList.get(i).getPhase() == 0
									&& roomKnockList.get(i).getSeats().getSittingCount() < roomKnockList.get(i).getSeats().getSeatCount()) {
								player.room = roomKnockList.get(i);
								hasEntered = roomKnockList.get(i).playerEnter(player);
								break;
							}
						}
						if (hasEntered == false) {
							Room room = null;
							KnockoutRoomVm krv = new KnockoutRoomVm();
							krv.id = uniqueNumber++;
							RoomKnockoutInfo roomKnockoutInfo = RoomConfig.getKnockoutByLevel(enterRoomProto.getRoomLevel());
							if (roomKnockoutInfo == null) {
								log.debugln("Get knockout level " + enterRoomProto.getRoomLevel() + " failed.");
								return;
							}
							krv.seatCount = roomKnockoutInfo.getSeatCount();
							krv.smallBlind = roomKnockoutInfo.getSmallBlind();
							krv.bigBlind = roomKnockoutInfo.getBigBlind();
							krv.minTake = 0;
							krv.maxTake = 0;
							krv.entryFee = roomKnockoutInfo.getEntryFee();
							krv.initBankroll = roomKnockoutInfo.getInitBankroll();
							krv.reward = roomKnockoutInfo.getReward();
							krv.level = enterRoomProto.getRoomLevel();
							room = new KnockoutRoomImpl(krv);
							room.setTimer(timer);
							roomKnockList.add(room);
							player.room = room;
							room.playerEnter(player);
						}
					}
				} else if (enterRoomProto.getRoomType() == RoomType.IMPERIAL) {
					// 皇家场
					synchronized (roomImperialList) {
						boolean hasEntered = false;
						for (int i = 0; i < roomImperialList.size(); i++) {
							if (roomImperialList.get(i).getLevel() == enterRoomProto.getRoomLevel()
									&& roomImperialList.get(i).getSeats().getSittingCount() < roomImperialList.get(i).getSeats().getSeatCount()) {
								player.room = roomImperialList.get(i);
								hasEntered = roomImperialList.get(i).playerEnter(player);
								break;
							}
						}
						if (hasEntered == false) {
							Room room = null;
							RoomVm rv = new RoomVm();
							rv.id = uniqueNumber++;
							RoomInfo roomInfo = RoomConfig.getImperialByLevel(enterRoomProto.getRoomLevel());
							if (roomInfo == null) {
								log.debugln("Get imperial level " + enterRoomProto.getRoomLevel() + " failed.");
								return;
							}
							rv.seatCount = roomInfo.getSeatCount();
							rv.smallBlind = roomInfo.getSmallBlind();
							rv.bigBlind = roomInfo.getBigBlind();
							rv.minTake = roomInfo.getMinTake();
							rv.maxTake = roomInfo.getMaxTake();
							rv.averageTake = roomInfo.getAverageTake();
							rv.level = enterRoomProto.getRoomLevel();
							room = new ImperialRoomImpl(rv);
							room.setTimer(timer);
							roomImperialList.add(room);
							player.room = room;
							room.playerEnter(player);
						}
					}
				} else if (enterRoomProto.getRoomType() == RoomType.OMAHA) {
					// 奥马哈场
					synchronized (roomOmahaList) {
						boolean hasEntered = false;
						for (int i = 0; i < roomOmahaList.size(); i++) {
							if (roomOmahaList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomOmahaList.get(i).getSeats().getSittingCount() < roomOmahaList.get(i).getSeats().getSeatCount()) {
								player.room = roomOmahaList.get(i);
								hasEntered = roomOmahaList.get(i).playerEnter(player);
								break;
							}
						}
						if (hasEntered == false) {
							Room room = null;
							RoomVm rv = new RoomVm();
							rv.id = uniqueNumber++;
							RoomInfo roomInfo = RoomConfig.getOmahaByLevel(enterRoomProto.getRoomLevel());
							if (roomInfo == null) {
								log.debugln("Get omaha level " + enterRoomProto.getRoomLevel() + " failed.");
								return;
							}
							rv.seatCount = roomInfo.getSeatCount();
							rv.smallBlind = roomInfo.getSmallBlind();
							rv.bigBlind = roomInfo.getBigBlind();
							rv.minTake = roomInfo.getMinTake();
							rv.maxTake = roomInfo.getMaxTake();
							rv.averageTake = roomInfo.getAverageTake();
							rv.level = enterRoomProto.getRoomLevel();
							room = new OmahaRoomImpl(rv);
							room.setTimer(timer);
							roomOmahaList.add(room);
							player.room = room;
							room.playerEnter(player);
						}
					}
				}
			}
			// 加入playerMap
			playerMap.put(player.id, player);
		}

	}

	private void robotEnter(EnterRoomProto enterRoomProto) {
		final int maxLimit = 3;
		if (enterRoomProto.getRoomType() == RoomType.NORMAL) {
			synchronized (roomList) {
				// 常规场（机器人）
				boolean hasEntered = false;
				for (int i = 0; i < roomList.size(); i++) {
					if (roomList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomList.get(i).getSeats().getSittingCount() < maxLimit) {
						player.room = roomList.get(i);
						hasEntered = roomList.get(i).playerEnter(player);
						break;
					}
				}
				if (hasEntered == false) {
					Room room = null;
					RoomVm rv = new RoomVm();
					rv.id = uniqueNumber++;
					RoomInfo roomInfo = RoomConfig.getNormalByLevel(enterRoomProto.getRoomLevel());
					if (roomInfo == null) {
						log.debugln("Get normal level " + enterRoomProto.getRoomLevel() + " failed.");
						return;
					}
					rv.seatCount = roomInfo.getSeatCount();
					rv.smallBlind = roomInfo.getSmallBlind();
					rv.bigBlind = roomInfo.getBigBlind();
					rv.minTake = roomInfo.getMinTake();
					rv.maxTake = roomInfo.getMaxTake();
					rv.averageTake = roomInfo.getAverageTake();
					rv.level = enterRoomProto.getRoomLevel();
					room = new RoomImpl(rv);
					room.setTimer(timer);
					roomList.add(room);
					player.room = room;
					room.playerEnter(player);
				}
			}
		} else if (enterRoomProto.getRoomType() == RoomType.KNOCKOUT) {
			// 淘汰赛房间（机器人）
			synchronized (roomKnockList) {
				log.debugln("Player " + this.player.id + " request to enter.");
				boolean hasEntered = false;
				for (int i = 0; i < roomKnockList.size(); i++) {
					if (roomKnockList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomKnockList.get(i).getPhase() == 0
							&& roomKnockList.get(i).getSeats().getSittingCount() < roomKnockList.get(i).getSeats().getSeatCount() - 1) {
						player.room = roomKnockList.get(i);
						hasEntered = roomKnockList.get(i).playerEnter(player);
						break;
					}
				}
				if (hasEntered == false) {
					Room room = null;
					KnockoutRoomVm krv = new KnockoutRoomVm();
					krv.id = uniqueNumber++;
					RoomKnockoutInfo roomKnockoutInfo = RoomConfig.getKnockoutByLevel(enterRoomProto.getRoomLevel());
					if (roomKnockoutInfo == null) {
						log.debugln("Get knockout level " + enterRoomProto.getRoomLevel() + " failed.");
						return;
					}
					krv.seatCount = roomKnockoutInfo.getSeatCount();
					krv.smallBlind = roomKnockoutInfo.getSmallBlind();
					krv.bigBlind = roomKnockoutInfo.getBigBlind();
					krv.minTake = 0;
					krv.maxTake = 0;
					krv.entryFee = roomKnockoutInfo.getEntryFee();
					krv.initBankroll = roomKnockoutInfo.getInitBankroll();
					krv.reward = roomKnockoutInfo.getReward();
					krv.level = enterRoomProto.getRoomLevel();
					room = new KnockoutRoomImpl(krv);
					room.setTimer(timer);
					roomKnockList.add(room);
					player.room = room;
					room.playerEnter(player);
				}
			}
		} else if (enterRoomProto.getRoomType() == RoomType.IMPERIAL) {
			// 皇家场（机器人）
			synchronized (roomImperialList) {
				boolean hasEntered = false;
				for (int i = 0; i < roomImperialList.size(); i++) {
					if (roomImperialList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomImperialList.get(i).getSeats().getSittingCount() < maxLimit) {
						player.room = roomImperialList.get(i);
						hasEntered = roomImperialList.get(i).playerEnter(player);
						break;
					}
				}
				if (hasEntered == false) {
					Room room = null;
					RoomVm rv = new RoomVm();
					rv.id = uniqueNumber++;
					RoomInfo roomInfo = RoomConfig.getImperialByLevel(enterRoomProto.getRoomLevel());
					if (roomInfo == null) {
						log.debugln("Get imperial level " + enterRoomProto.getRoomLevel() + " failed.");
						return;
					}
					rv.seatCount = roomInfo.getSeatCount();
					rv.smallBlind = roomInfo.getSmallBlind();
					rv.bigBlind = roomInfo.getBigBlind();
					rv.minTake = roomInfo.getMinTake();
					rv.maxTake = roomInfo.getMaxTake();
					rv.averageTake = roomInfo.getAverageTake();
					rv.level = enterRoomProto.getRoomLevel();
					room = new ImperialRoomImpl(rv);
					room.setTimer(timer);
					roomImperialList.add(room);
					player.room = room;
					room.playerEnter(player);
				}
			}
		} else {
			// 奥马哈场（机器人）
			synchronized (roomOmahaList) {
				boolean hasEntered = false;
				for (int i = 0; i < roomOmahaList.size(); i++) {
					if (roomOmahaList.get(i).getLevel() == enterRoomProto.getRoomLevel() && roomOmahaList.get(i).getSeats().getSittingCount() < maxLimit) {
						player.room = roomOmahaList.get(i);
						hasEntered = roomOmahaList.get(i).playerEnter(player);
						break;
					}
				}
				if (hasEntered == false) {
					Room room = null;
					RoomVm rv = new RoomVm();
					rv.id = uniqueNumber++;
					RoomInfo roomInfo = RoomConfig.getOmahaByLevel(enterRoomProto.getRoomLevel());
					if (roomInfo == null) {
						log.debugln("Get omaha level " + enterRoomProto.getRoomLevel() + " failed.");
						return;
					}
					rv.seatCount = roomInfo.getSeatCount();
					rv.smallBlind = roomInfo.getSmallBlind();
					rv.bigBlind = roomInfo.getBigBlind();
					rv.minTake = roomInfo.getMinTake();
					rv.maxTake = roomInfo.getMaxTake();
					rv.averageTake = roomInfo.getAverageTake();
					rv.level = enterRoomProto.getRoomLevel();
					room = new OmahaRoomImpl(rv);
					room.setTimer(timer);
					roomOmahaList.add(room);
					player.room = room;
					room.playerEnter(player);
				}
			}
		}
	}

	// 离开服务器
	public void leaveServer() {
		player.room = null;
		playerMap.remove(player.id);
		CtrlCenterSync.getChannel().noticePlayerLeave(player.id); // 通知中控有玩家离开
	}

	public void send(int code, byte[] bytes) {
		ByteBuf buf = Unpooled.buffer((bytes == null ? 0 : bytes.length) + 8);
		int length = bytes == null ? 0 : bytes.length;
		byte[] header = new byte[8];
		header[0] = (byte) (code & 0xff);
		header[1] = (byte) ((code >> 8) & 0xff);
		header[2] = (byte) ((code >> 16) & 0xff);
		header[3] = (byte) ((code >> 24) & 0xff);
		header[4] = (byte) (length & 0xff);
		header[5] = (byte) ((length >> 8) & 0xff);
		header[6] = (byte) ((length >> 16) & 0xff);
		header[7] = (byte) ((length >> 24) & 0xff);
		buf.writeBytes(header);
		if (bytes != null) {
			buf.writeBytes(bytes);
		}
		channelCtx.writeAndFlush(buf);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.debugln("Channel exception caught.");
		cause.printStackTrace();
		player.room.requestHandler(player, 0, null);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debugln("Channel inactive.");
		super.channelInactive(ctx);
	}

}
