package poker.main.room;

import java.util.Timer;

import poker.main.player.Player;
import poker.main.room.impl.Seats;

public interface Room {

	int getId();

	int getType();

	int getLevel();

	Seats getSeats();

	int getPhase();

	boolean playerEnter(Player player);

	boolean reconnect(Player player);

	void requestHandler(Player player, int code, byte[] buff);

	void setTimer(Timer timer);

	int playerExist(int id);

	void cleanTimeoutPlayers();

	byte[] roomStatusBytes();
}
