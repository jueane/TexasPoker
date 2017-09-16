package poker.main.player;

import poker.entity.MemberInfo;
import poker.main.ChannelHandler;
import poker.main.player.Player;
import poker.main.room.Room;

public class Player {
	public ChannelHandler handler;
	public int id;
	public int seat = -1;
	public int bankroll = 0;
	public MemberInfo info;
	public Room room = null;
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

	public Player() {
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

	// 检查状态
	public void send(int code, byte[] bytes) {
		if (connectionOk) {
			handler.send(code, bytes);
		}
	}

	// 发送不检查状态
	public void sendDirect(int code, byte[] bytes) {
		handler.send(code, bytes);
	}

}
