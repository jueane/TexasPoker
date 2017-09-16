package poker.config;

import java.util.List;

import poker.data.RoomData;
import poker.data.RoomKnockoutData;
import poker.entity.RoomInfo;
import poker.entity.RoomKnockoutInfo;
import poker.main.room.RoomType;

public class RoomConfig {
	private List<RoomInfo> roomTypeList = RoomData.getRoomTypeList(RoomType.NORMAL);
	private List<RoomKnockoutInfo> roomKnockoutTypeList = RoomKnockoutData.getRoomTypeList();
	private List<RoomInfo> roomImperialTypeList = RoomData.getRoomTypeList(RoomType.IMPERIAL);
	private List<RoomInfo> roomOmahaTypeList = RoomData.getRoomTypeList(RoomType.OMAHA);

	private static RoomConfig uniqueInstance = null;

	public static RoomConfig getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new RoomConfig();
		}
		return uniqueInstance;
	}

	public static RoomInfo getNormalByLevel(int level) {
		if (level < 1 || level > uniqueInstance.roomTypeList.size()) {
			level = 1;
		}
		for (int i = 0; i < uniqueInstance.roomTypeList.size(); i++) {
			if (uniqueInstance.roomTypeList.get(i).getType() == level) {
				return uniqueInstance.roomTypeList.get(i);
			}
		}
		System.out.println("Can not find normal room level " + level);
		return null;
	}

	public static RoomKnockoutInfo getKnockoutByLevel(int level) {
		if (level < 1 || level > uniqueInstance.roomKnockoutTypeList.size()) {
			level = 1;
		}
		for (int i = 0; i < uniqueInstance.roomKnockoutTypeList.size(); i++) {
			if (uniqueInstance.roomKnockoutTypeList.get(i).getType() == level) {
				return uniqueInstance.roomKnockoutTypeList.get(i);
			}
		}
		System.out.println("Can not find knockout room level " + level);
		return null;
	}

	public static RoomInfo getImperialByLevel(int level) {
		if (level < 1 || level > uniqueInstance.roomImperialTypeList.size()) {
			level = 1;
		}
		for (int i = 0; i < uniqueInstance.roomImperialTypeList.size(); i++) {
			if (uniqueInstance.roomImperialTypeList.get(i).getType() == level) {
				return uniqueInstance.roomImperialTypeList.get(i);
			}
		}
		System.out.println("Can not find imperial room level " + level);
		return null;
	}

	public static RoomInfo getOmahaByLevel(int level) {
		if (level < 1 || level > uniqueInstance.roomOmahaTypeList.size()) {
			level = 1;
		}
		for (int i = 0; i < uniqueInstance.roomOmahaTypeList.size(); i++) {
			if (uniqueInstance.roomOmahaTypeList.get(i).getType() == level) {
				return uniqueInstance.roomOmahaTypeList.get(i);
			}
		}
		System.out.println("Can not find omaha room level " + level);
		return null;
	}

}
