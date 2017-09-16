package poker.main.gamergroup;

import poker.main.player.Player;

public interface GamerGroupListener {

	void gamerGroupJoin(Player player);
	
	void gamerGroupLeave(Player player);
	
//	void gamerGroupReconnect();

}
