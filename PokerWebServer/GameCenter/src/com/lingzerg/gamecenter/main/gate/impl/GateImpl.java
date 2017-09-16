package com.lingzerg.gamecenter.main.gate.impl;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.lingzerg.gamecenter.config.Config;
import com.lingzerg.gamecenter.data.ConnectionPool;
import com.lingzerg.gamecenter.main.gate.Gate;
import com.lingzerg.gamecenter.main.player.Player;
import com.lingzerg.gamecenter.main.player.impl.PlayerImpl;
import com.lingzerg.gamecenter.main.room.Room;
import com.lingzerg.gamecenter.main.room.RoomKnockoutVm;
import com.lingzerg.gamecenter.main.room.RoomVm;
import com.lingzerg.gamecenter.main.room.impl.RoomImpl;
import com.lingzerg.gamecenter.main.room.impl.RoomKnockoutImpl;
import com.lingzerg.gamecenter.proto.ValidationPro.ValidationProto;
import com.lingzerg.gamecenter.util.JLog;
import com.lingzerg.gamecenter.util.JsonHelper;

public class GateImpl implements Gate {
	protected static final String WEB_SERVICE_URL = Config.getInstance().webServiceUrl;

	protected List<Room> roomList = new ArrayList<>();

	@Override
	public void startup() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Config.getInstance().port);
		} catch (BindException e) {
			JLog.errorln("Bind failed.");
			return;
		} catch (Exception e) {
			JLog.errorln("ServerSocket create failed.");
			return;
		}

		getRoomConfig();

		while (true) {
			clientHandler(ss);
			Runtime.getRuntime().gc();
		}

	}

	private void clientHandler(ServerSocket ss) {
		Socket s = null;
		Player player = null;
		ValidationProto validationProto = null;
		JLog.infoln();
		try {
			s = ss.accept();
		} catch (IOException e) {
			JLog.info("Accept failed." + e.getMessage());
			return;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd hh:mm:ss");
		JLog.infoln(simpleDateFormat.format(new Date()) + "  Player " + s.getInetAddress().getHostAddress() + " connected.Room count：" + roomList.size());
		player = new PlayerImpl(s);
		try {
			validationProto = player.validate();
		} catch (IOException e) {
			JLog.debugln("Player " + player.getId() + " validate IO error.");
		}
		if (validationProto != null && validationProto.getRoomId() > 0) {
			int roomId = validationProto.getRoomId();
			// 进入房间
			boolean hasEntered = enterExistRoom(roomId, player, validationProto.getIsSpectator());
			if (!hasEntered) {
				enterNewRoom(validationProto.getRoomType(), roomId, player, validationProto.getIsSpectator());
			}
		}
	}

	// 进入已实例化房间
	private boolean enterExistRoom(int roomId, Player player, boolean isSpectator) {
		int roomCount = roomList.size();
		for (int i = 0; i < roomCount; i++) {
			// 判断是否有该房间号。。有则加入
			if (roomList.get(i) != null && roomList.get(i).getPlayingCount() < roomList.get(i).getMaxPlayingCount() && roomList.get(i).getId() == roomId) {
				if (roomList.get(i).playerIn(player, isSpectator) > 0) {
					return true;
				}
			}
		}
		return false;
	}

	// 进入未实例化的房间
	private void enterNewRoom(int roomType, int roomId, Player player, boolean isSpectator) {
		URL url = null;
		try {
			if(roomType==1){
				url = new URL(WEB_SERVICE_URL + "/room/getRoomById?id=" + roomId);				
			}else{
				url = new URL(WEB_SERVICE_URL + "/room/getRoomKnockoutById?id=" + roomId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		Scanner scannerWeb = null;
		try {
			scannerWeb = new Scanner(url.openStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Room room = null;
		String buff = scannerWeb.next();
		if (roomType == 1) {
			RoomVm rv = JsonHelper.parseFromJson(buff, RoomVm.class);
			if (rv != null) {
				room = new RoomImpl(rv);
			}
		} else if (roomType == 2) {
			RoomKnockoutVm roomKnockoutVm = JsonHelper.parseFromJson(buff, RoomKnockoutVm.class);
			if (roomKnockoutVm != null) {
				room = new RoomKnockoutImpl(roomKnockoutVm);
			}
		}
		if (room != null) {
			roomList.add(room);
			// 判断是否进入成功
			room.playerIn(player, isSpectator);
		} else {
			JLog.debugln("Room " + roomId + " with type " + roomType + " is not exist.");
		}
	}

	private void getRoomConfig() {
		JLog.infoln("Initializing...");
		try {
			ConnectionPool.getConnection().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		URL url = null;
		try {
			url = new URL(WEB_SERVICE_URL + "/room/roomListReset");
			try {
				url.openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		JLog.infoln("Initialization is complete.");
	}

}
