package poker.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import poker.main.player.Player;

public class HashTest {
	public static void main(String args[]) {
		Player player = new Player();
		player.id = 8;
		player.bankroll = 100;
		Player player2 = new Player();
		player2.id = 200;
		player2.bankroll = 200;
		Player player3 = new Player();
		player3.id = 300;
		player3.bankroll = 300;
		Player player4 = new Player();
		player4.id = 15100;
		player4.bankroll = 300;

		Map<Integer, Player> playerMap = new HashMap<>();
		playerMap.put(player.id, player);
		playerMap.put(player2.id, player2);
		playerMap.put(player3.id, player3);
		playerMap.put(player4.id, player4);


		Iterator<Player> playerItr = playerMap.values().iterator();
		Player[] playerArr = new Player[playerMap.size()];
		List<Player> players = new ArrayList<>();

		while (playerItr.hasNext()) {
			Player curPlayer = playerItr.next();
			System.out.println("Id:" + curPlayer.id + ",bankroll:" + curPlayer.bankroll);
		}

	}

}
