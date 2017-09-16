package com.lingzerg.poker.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lingzerg.poker.dao.RoomDao;
import com.lingzerg.poker.dao.RoomKnockoutDao;
import com.lingzerg.poker.entity.Room;
import com.lingzerg.poker.entity.RoomKnockout;
import com.lingzerg.poker.service.RoomService;
import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RoomKnockoutVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RoomVm;

@Service
@Transactional
public class RoomServiceImpl implements RoomService, ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private RoomDao roomDao;
	@Autowired
	private RoomKnockoutDao roomKnockoutDao;

	private static List<List<RoomVm>> roomVmList = new ArrayList<>();

	private static List<List<RoomKnockoutVm>> roomKnockoutVmList = new ArrayList<>();

	private static int uniqueId = 0;

	private int generateUniqueId() {
		return ++uniqueId;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		if (arg0.getApplicationContext().getParent() == null) {
			for (int i = 0; i < 4; i++) {
				List<RoomVm> roomVms = new ArrayList<>();
				roomVmList.add(roomVms);
			}
			List<Room> roomList = roomDao.getPagedList(0, 0);// 获取全部
			int roomTypeCount = roomList.size();
			for (int i = 0; i < roomTypeCount; i++) {
				int thisTypeCount = roomList.get(i).getCount();
				for (int j = 0; j < thisTypeCount; j++) {
					RoomVm roomVm = new RoomVm();
					roomVm.id = generateUniqueId();
					roomVm.type = roomList.get(i).getType();
					roomVm.title = roomList.get(i).getTitle() + roomVm.id;
					roomVm.maxPlayingCount = roomList.get(i).getMaxPlayingCount();
					roomVm.smallBlind = roomList.get(i).getSmallBlind();
					roomVm.bigBlind = roomList.get(i).getBigBlind();
					roomVm.smallTake = roomList.get(i).getSmallTake();
					roomVm.bigTake = roomList.get(i).getBigTake();
					switch (roomList.get(i).getType()) {
					case 1:
						roomVmList.get(0).add(roomVm);
						break;
					case 2:
						roomVmList.get(1).add(roomVm);
						break;
					case 3:
						roomVmList.get(2).add(roomVm);
						break;
					case 4:
						roomVmList.get(3).add(roomVm);
						break;
					default:
						break;
					}
				}
			}
			System.out.println("Room type count：" + roomVmList.size());

			for (int i = 0; i < 3; i++) {
				List<RoomKnockoutVm> roomKnockoutVms = new ArrayList<>();
				roomKnockoutVmList.add(roomKnockoutVms);
			}
			List<RoomKnockout> roomKnockoutList = roomKnockoutDao.getPagedList(0, 0);
			roomTypeCount = roomKnockoutList.size();
			for (int i = 0; i < roomTypeCount; i++) {
				int thisTypeCount = roomKnockoutList.get(i).getCount();
				for (int j = 0; j < thisTypeCount; j++) {
					RoomKnockoutVm roomKnockoutVm = new RoomKnockoutVm();
					roomKnockoutVm.id = generateUniqueId();
					roomKnockoutVm.type = roomKnockoutList.get(i).getType();
					roomKnockoutVm.title = roomKnockoutList.get(i).getTitle() + roomKnockoutVm.id;
					roomKnockoutVm.entryFee = roomKnockoutList.get(i).getEntryFee();
					roomKnockoutVm.serviceFee = roomKnockoutList.get(i).getServiceFee();
					roomKnockoutVm.maxPlayingCount = roomKnockoutList.get(i).getMaxPlayingCount();
					roomKnockoutVm.smallBlind = roomKnockoutList.get(i).getSmallBlind();
					roomKnockoutVm.bigBlind = roomKnockoutList.get(i).getBigBlind();
					switch (roomKnockoutList.get(i).getType()) {
					case 1:
						roomKnockoutVmList.get(0).add(roomKnockoutVm);
						break;
					case 2:
						roomKnockoutVmList.get(1).add(roomKnockoutVm);
						break;
					case 3:
						roomKnockoutVmList.get(2).add(roomKnockoutVm);
						break;
					default:
						break;
					}
				}
			}
			System.out.println("RoomKnockout type count：" + roomKnockoutVmList.size());
		}
	}

	@Override
	public List<RoomVm> getRoomList(int type) {
		if (type < 1 || type > 3) {
			type = 1;
		}
		System.out.println("Room count with type " + type + "：" + roomVmList.get(type - 1).size());
		return roomVmList.get(type - 1);
	}

	@Override
	public List<RoomKnockoutVm> getRoomKnockoutList(int type) {
		if (type < 1 || type > 2) {
			type = 1;
		}
		System.out.println("RoomKnockout count with type " + type + "：" + roomKnockoutVmList.get(type - 1).size());
		return roomKnockoutVmList.get(type - 1);
	}

	@Override
	public ResultVm roomListReset() {
		ResultVm resultVm = new ResultVm();
		int roomTypeCount = roomVmList.size();
		for (int i = 0; i < roomTypeCount; i++) {
			int roomCount = roomVmList.get(i).size();
			for (int j = 0; j < roomCount; j++) {
				roomVmList.get(i).get(j).playingCount = 0;
			}
		}
		int roomKnockoutTypeCount = roomKnockoutVmList.size();
		for (int i = 0; i < roomKnockoutTypeCount; i++) {
			int roomCount = roomKnockoutVmList.get(i).size();
			for (int j = 0; j < roomCount; j++) {
				roomKnockoutVmList.get(i).get(j).playingCount = 0;
			}
		}
		resultVm.status = 1;
		return resultVm;
	}

	@Override
	public RoomVm getRoomById(int id) {
		int typeCount = roomVmList.size();
		for (int i = 0; i < typeCount; i++) {
			int count = roomVmList.get(i).size();
			for (int j = 0; j < count; j++) {
				if (roomVmList.get(i).get(j).id == id) {
					return roomVmList.get(i).get(j);
				}
			}
		}
		return null;
	}

	@Override
	public RoomKnockoutVm getRoomKnockoutById(int id) {
		int typeCount = roomKnockoutVmList.size();
		for (int i = 0; i < typeCount; i++) {
			int count = roomKnockoutVmList.get(i).size();
			for (int j = 0; j < count; j++) {
				if (roomKnockoutVmList.get(i).get(j).id == id) {
					return roomKnockoutVmList.get(i).get(j);
				}
			}
		}
		return null;
	}

	@Override
	public ResultVm modifyRoom(int id, int playingCount) {
		ResultVm resultVm = new ResultVm();
		RoomVm roomVm = getRoomById(id);
		if (roomVm != null) {
			roomVm.playingCount = playingCount;
		} else {
			RoomKnockoutVm roomKnockoutVm = getRoomKnockoutById(id);
			if (roomKnockoutVm != null) {
				roomKnockoutVm.playingCount = playingCount;
			} else {
				resultVm.status = 2;
				resultVm.msg = "Room not found.";
				return resultVm;
			}
		}
		resultVm.status = 1;
		return resultVm;
	}
}
