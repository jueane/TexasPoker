package com.lingzerg.gamecenter.main.room;

import com.lingzerg.gamecenter.main.player.Player;

public interface Room extends Runnable {

	int getId();

	int getPlayingCount();

	int getMaxPlayingCount();

	int playerIn(Player player,boolean isSpectator);

}
