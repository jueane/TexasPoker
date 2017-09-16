package com.lingzerg.gamecenter.main.room.impl;

import java.util.ArrayList;

import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.room.Room;
import com.lingzerg.gamecenter.main.room.RoomVm;

public class RoomImpl extends BaseRoom implements Room {

	// 房间信息初始化，从数据库或Webservice获取
	public RoomImpl(RoomVm rv) {
		if (rv.maxPlayingCount <= 2) {
			rv.maxPlayingCount = 6;
		}
		this.minPlayerCountForBegin = 2;
		this.minPlayerCountForRun = 2;
		this.id = rv.id;
		this.type = rv.type;
		this.title = rv.title;
		this.maxPlayingCount = rv.maxPlayingCount;
		this.smallBlind = rv.smallBlind;
		this.bigBlind = rv.bigBlind;
		this.minTake = rv.smallTake;
		this.maxTake = rv.bigTake;
		this.playerList = new Player[maxPlayingCount];
		// 初始化底池
		potList = new ArrayList<>();
	}

}
