package poker.main.player;

import poker.Hall;
import poker.data.MemberData;
import poker.entity.MemberInfo;
import poker.main.player.Player;
import poker.main.room.RoomImpl;
import poker.util.JLog;

public class Player {
	protected static JLog log = new JLog("Player");

	public ChannelEventProxy proxy;
	public int id;
	public int seat = -1;
	public int bankroll = 0;
	public MemberInfo info;
	public RoomImpl room = null;
	public PlayerStatus status;// 待删除，等待加入，正常，让牌，弃牌，已出局
	public boolean connectionOk = true;
	// 每局状态保存
	public int gained;// 未减去所下注
	public int drawoff;
	public int[] anteList = new int[4];// 玩家下注列表，共4轮
	public byte[] holeCards;
	// 补充筹码
	public int recharge = 0;// 下一局补充至多少筹码
	public long requestDate;

	MemberData memberData = new MemberData();

	ChannelEventProxy eventHandler;

	public Player() {
	}

	public void enterRoom(int roomLevel) {
		Hall.uniqueHall().enterRoomByLevel(roomLevel, this);
	}

	public void robotEnterRoom(int roomLevel) {
		Hall.uniqueHall().robotEnterRoom(roomLevel, this);
	}

	public void reconnect() {

		Player existPlayer = Hall.uniqueHall().getPlayer(id);
		if (existPlayer != null) {
			if (existPlayer.room != null) {
				// 尝试重连。成功则进入房间。失败则通知客户端重连失败，并返回大厅。

				// 1.成功
				log.debugln("Player " + id + " reconnected room " + existPlayer.room.getId() + ".");

				// 向大厅登记有玩家接入
				Hall.uniqueHall().playerEnter(this);

				// 2.失败

				// 通知客户端重连失败

				return;
			}
		}
	}

	public boolean chaseFriend(int friendId) {
		// 获取好友房间id
		Player friend = Hall.uniqueHall().getPlayer(friendId);
		if (friend == null || friend.room == null) {
			log.debugln("Friend " + friendId + " has been left the room.");
		}
		// 进入该房间
		int roomId = friend.room.getId();
		return Hall.uniqueHall().enterRoomById(roomId, this);
	}

	// 坐下（要求原本是观众）
	public boolean sitDown() {
		return room.dealer.sitDown(this);
	}

	public boolean standUp() {
		return room.dealer.standUp(this);
	}

	public boolean leave() {
		return room.dealer.leave(this);
	}

	public int anteTotal() {
		int total = 0;
		for (int i = 0; i < anteList.length; i++) {
			total += anteList[i];
		}
		return total;
	}

	public void resetAnteList() {
		for (int i = 0; i < anteList.length; i++) {
			anteList[i] = 0;
		}
	}

	public int blindBet(int count) {
		if (bankroll < count) {
			count = bankroll;
		}
		bankroll -= count;
		anteList[0] = count;
		return anteList[0];
	}

	public void action(Player player, int act, byte[] buff) {
		room.dealer.actionRule.actionHandler(player, act, buff);
	}

	// 检查状态
	public void send(int code, byte[] bytes) {
		if (connectionOk) {
			proxy.send(code, bytes);
		}
	}

	// 发送不检查状态
	public void sendDirect(int code, byte[] bytes) {
		proxy.send(code, bytes);
	}

}
