package poker.main.gamergroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import poker.Hall;
import poker.data.MemberData;
import poker.main.dealer.Score;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.main.room.RoomImpl;
import poker.main.room.RoomInstantConfig;
import poker.proto.ProtoFactory.PlayerLeaveProto;
import poker.util.JLog;

public class GamerGroup {
	protected JLog log = new JLog("GamerGroup");

	// 数据持久化对象
	protected MemberData memberData = new MemberData();

	protected RoomImpl room;

	public GamerSeats seats;

	List<GamerGroupListener> gamerGroupListeners = new ArrayList<>();

	public GamerGroup(GamerSeats seats) {
		this.seats = seats;
	}

	public void addGamerGroupListener(GamerGroupListener listener) {
		gamerGroupListeners.add(listener);
	}

	// 广播信息
	public void broadcast(int code, byte[] bytes) {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				curPlayer.send(code, bytes);
			}
		}
	}

	// 加入游戏组
	public boolean joinGamersGroup(Player player) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
		String strData = simpleDateFormat.format(new Date());
		if (seats.getSittingCount() >= this.seats.getSeatCount() || player.info.getGold() < room.property.minTake) {
			player.seat = -1;
			log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup failed.Room is full or gold is not enough.Gold:" + player.info.getGold());
			return false;
		}
		// 检查是否已存在。若是，则重连
		Iterator<Player> itr = seats.iterator();
		while (itr.hasNext()) {
			Player curPlayer = itr.next();
			if (curPlayer != null && curPlayer.id == player.id) {
				curPlayer.proxy = player.proxy;
				curPlayer.proxy.player = curPlayer;
				curPlayer.info = player.info;
				curPlayer.connectionOk = true;
				// 如果游戏进行中的话，获取游戏即时信息
				// 另启动一线程处理“如果重连时刚好轮到自己下注”的情况
				log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup.Reconnected.");
				// log.infoln("[Enter]" + strData + " Player " + player.id + "
				// joinGamersGroup.Reconnected.Phase:" + phase);
				return true;
			}
		}

		// 检查游戏进行阶段
		player.status = PlayerStatus.READY;
		if (seats.join(player) != GamerSeats.NOSEAT) {
			log.infoln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup.Player count:" + seats.getPlayingCount() + "/" + seats.getSittingCount() + "/" + seats.getSeatCount());
			// 调用监视者们
			for (GamerGroupListener listener : gamerGroupListeners) {
				listener.gamerGroupJoin(player);
			}
			return true;
		} else {
			// 如果前边没有退出方法，说明没有进入或重连成功，则失败。
			log.debugln("[Enter]" + strData + " Player " + player.id + " joinGamersGroup failed.");
			return false;
		}
	}

	// 离开游戏组
	public boolean leaveGamersGroup(Player player) {
		if (seats.leave(player)) {
			// 所有筹码兑换为金币
			if (player.bankroll > 0) {
				memberData.memberGoldAdd(player.id, player.bankroll);
				player.info.setGold(player.info.getGold() + player.bankroll);
				player.bankroll = 0;
			}
			player.seat = -1;
			log.infoln("Player " + player.id + " leaveGamersGroup.");

			// 调用监视者们
			for (GamerGroupListener listener : gamerGroupListeners) {
				listener.gamerGroupJoin(player);
			}

			return true;
		} else {
			log.infoln("Player " + player.id + " leaveGamersGroup failed.");
			return false;
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
				if (curPlayer.recharge >= room.property.minTake) {
					wannaToAdd = curPlayer.recharge - curPlayer.bankroll;
					if (wannaToAdd > curPlayer.info.getGold()) {
						wannaToAdd = curPlayer.info.getGold();
					}
				} else if (curPlayer.bankroll < room.property.bigBlind) {
					// 检查筹码是否少于大盲并且未设置手动补充
					if (curPlayer.info.getGold() >= room.property.bigBlind) {
						if (curPlayer.info.getGold() >= room.property.averageTake) {
							wannaToAdd = room.property.averageTake;
						} else {
							wannaToAdd = curPlayer.info.getGold();
						}
					}
				}
				// 如果"筹码+想要补充的筹码"或"筹码+金币"<大盲，则站起
				if (curPlayer.bankroll + wannaToAdd < room.property.bigBlind || curPlayer.bankroll + curPlayer.info.getGold() < room.property.bigBlind) {
					log.debugln("Player " + curPlayer.id + " in " + curPlayer.seat + " standup.");
					// 金币不足无法补充，则移至观众席
					log.debugln("Player " + curPlayer.id + " has not enough gold to play.");
					leaveGamersGroup(curPlayer);
					room.audienceGroup.joinSpectatorGroup(curPlayer);
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

	public int playerExist(int id) {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null && curPlayer.id == id) {
				return curPlayer.connectionOk ? 1 : 2;
			}
		}
		return 0;
	}

	public void cleanTimeoutPlayers() {
		Iterator<Player> playerItr = this.seats.iterator();
		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			if (curPlayer != null) {
				if (new Date().getTime() - curPlayer.requestDate > RoomInstantConfig.MAX_CHECK_TIME) {
					PlayerLeaveProto.Builder playerLeaveBuilder = PlayerLeaveProto.newBuilder();
					playerLeaveBuilder.setPlayerId(curPlayer.id);
					playerLeaveBuilder.setSeatsRemain(seats.getSeatCount() - seats.getSittingCount());
					broadcast(711095, playerLeaveBuilder.build().toByteArray());
					log.debugln("Remove timeout player " + curPlayer.id);
					leaveGamersGroup(curPlayer);
					Hall.uniqueHall().leaveServer(curPlayer);
				}
			}
		}

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

}
