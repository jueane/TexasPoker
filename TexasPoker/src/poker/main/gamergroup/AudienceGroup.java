package poker.main.gamergroup;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import poker.Hall;
import poker.main.player.Player;
import poker.main.room.RoomInstantConfig;
import poker.util.JLog;

public class AudienceGroup {
	protected JLog log;

	public ConcurrentHashMap<Integer, Player> spectatorMap = new ConcurrentHashMap<>();
	// 加入观众组
	public boolean joinSpectatorGroup(Player player) {
		spectatorMap.put(player.id, player);
		player.seat = -1;
		log.debugln("Player " + player.id + " joinSpectatorGroup.");
		return true;
	}

	// 离开观众组
	public boolean leaveSpectatorGroup(Player player) {
		spectatorMap.remove(player.id);
		log.debugln("Player " + player.id + " leaveSpectatorGroup.");
		return true;
	}
	


	public int playerExist(int id) {
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null && curSpect.id == id) {
				return curSpect.connectionOk ? 1 : 2;
			}
		}
		return 0;
	}

	public void cleanTimeoutPlayers() {
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curSpect = spectItr.next();
			if (curSpect != null) {
				if (new Date().getTime() - curSpect.requestDate >RoomInstantConfig.MAX_CHECK_TIME) {
					log.debugln("Remove timeout spectator " + curSpect.id);
					leaveSpectatorGroup(curSpect);
					Hall.uniqueHall().leaveServer(curSpect);
				}
			}
		}

	}

	//广播信息
	public void broadcast(int code, byte[] bytes) {
		Iterator<Player> spectItr = spectatorMap.values().iterator();
		while (spectItr.hasNext()) {
			Player curPlayer = spectItr.next();
			curPlayer.send(code, bytes);
		}
	}
}
