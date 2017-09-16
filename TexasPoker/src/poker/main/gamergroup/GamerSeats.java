package poker.main.gamergroup;

import java.util.Iterator;

import poker.main.exceptions.BetablePlayerNotEnoughException;
import poker.main.player.Player;
import poker.main.player.PlayerStatus;
import poker.util.JLog;

public class GamerSeats implements Iterable<Player> {

	// 未坐下
	public static final int NOSEAT = -1;

	private final int size;
	private Player[] playerArray = null;
	private int bettingSeat = -1;

	static JLog log = new JLog("Seats");

	public GamerSeats(int count) {
		this.size = count;
		playerArray = new Player[count];
	}

	// 坐下，并返回座号
	public int join(Player player) {
		player.seat = GamerSeats.NOSEAT;
		synchronized (playerArray) {
			for (int i = 0; i < this.size; i++) {
				if (playerArray[i] == null) {
					playerArray[i] = player;
					player.seat = i;
					break;
				}
			}
		}
		return player.seat;
	}

	// 返回false说明player不存在
	public boolean leave(Player player) {
		boolean hasLeft = false;
		synchronized (playerArray) {
			for (int i = 0; i < this.size; i++) {
				if (playerArray[i] != null && playerArray[i].id == player.id) {
					playerArray[i] = null;
					player.seat = -1;
					hasLeft = true;
					break;
				}
			}
		}
		return hasLeft;
	}

	public int getPlayingCount() {
		int count = 0;
		Player tempPlayer = null;
		for (int i = 0; i < this.size; i++) {
			if ((tempPlayer = playerArray[i]) != null && tempPlayer.status != PlayerStatus.READY) {
				count++;
			}
		}
		return count;
	}

	public int getSittingCount() {
		int count = 0;
		for (int i = 0; i < this.size; i++) {
			if (playerArray[i] != null) {
				count++;
			}
		}
		return count;
	}

	public int getSeatCount() {
		return this.size;
	}

	// 获取可下注玩家数量（不包括状态为normal但筹码为0的玩家）
	public int getBetableCount() {
		int count = 0;
		Player tempPlayer = null;
		for (int i = 0; i < this.size; i++) {
			if ((tempPlayer = playerArray[i]) != null && tempPlayer.bankroll > 0 && (tempPlayer.status == PlayerStatus.NORMAL || tempPlayer.status == PlayerStatus.CHECK)) {
				count++;
			}
		}
		return count;
	}

	// 获取未弃牌玩家数（包括状态为normal但筹码为0的玩家）
	public int getNoFoldCount() {
		int count = 0;
		Iterator<Player> playerItr = iterator();
		while (playerItr.hasNext()) {
			Player player = playerItr.next();
			if (player.status == PlayerStatus.NORMAL || player.status == PlayerStatus.CHECK) {
				count++;
			}
		}
		return count;
	}

	public void reset(int position) {
		bettingSeat = position;
	}

	public Player getBettingPlayer() {
		return bettingSeat == -1 ? null : playerArray[bettingSeat];
	}

	public Player nextBetable() throws BetablePlayerNotEnoughException {
		int lastSeat = bettingSeat;
		int curSeat = bettingSeat + 1;
		int counter = 0;// 防止发生意外，强制退出循环
		Player player = null;
		do {
			if (curSeat >= this.size) {
				curSeat = 0;
			}
			player = playerArray[curSeat++];
			if (player != null && (player.status == PlayerStatus.NORMAL || player.status == PlayerStatus.CHECK) && player.bankroll > 0) {
				bettingSeat = player.seat;
				return player;
			}
			// 防止发生意外，强制退出循环
			counter++;
			if (counter > this.size * 2) {
				log.errorln("Endless loop.");
				break;
			}
		} while (curSeat != lastSeat);
		if (player != null) {
			log.debugln("Only one player left.");
		} else {
			log.debugln("No player left.");
		}
		log.errorln("BetablePlayerNotEnoughException");
		throw new BetablePlayerNotEnoughException();
	}

	@Override
	public Iterator<Player> iterator() {
		return new Itr();
	}

	public class Itr implements Iterator<Player> {
		int cursor;

		@Override
		public boolean hasNext() {
			while (cursor < size) {
				if (playerArray[cursor] != null) {
					return true;
				}
				cursor++;
			}
			return false;
		}

		@Override
		public Player next() {
			while (cursor < size) {
				if (playerArray[cursor] != null) {
					return playerArray[cursor++];
				}
			}
			return null;
		}

		@Override
		public void remove() {
		}

	}

	public static void main(String args[]) {
		GamerSeats s = new GamerSeats(3);
		Player p1 = new Player();
		p1.id = 6;
		Player p2 = new Player();
		p2.id = 2;
		Player p3 = new Player();
		p3.id = 5;
		s.join(p1);
		s.join(p3);
		s.join(p2);

		Iterator<Player> players = s.iterator();
		int a = 2;
		while (players.hasNext()) {
			a--;
			if (a == 0) {
				break;
			}
			System.out.println(players.next().id);
		}

		Iterator<Player> playerssssIterator = s.iterator();
		while (playerssssIterator.hasNext()) {
			System.out.println("...." + playerssssIterator.next().id);
		}

	}

}
