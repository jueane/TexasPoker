package poker.main.room.impl;

import java.util.ArrayList;
import java.util.List;

import poker.main.player.Player;

public class Pot {
	public int lowerLimit;
	public int total;
	public int cardsValue;
	public int shareCount;
	public List<Player> playerList = new ArrayList<>();// 下过注的玩家列表
	public List<Player> sharePlayerList = new ArrayList<>();
}
