package com.lingzerg.poker.service;

import java.util.List;

import com.lingzerg.poker.ui.webservice.viewmodel.ResultVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RoomKnockoutVm;
import com.lingzerg.poker.ui.webservice.viewmodel.RoomVm;

public interface RoomService {
	List<RoomVm> getRoomList(int type);

	List<RoomKnockoutVm> getRoomKnockoutList(int type);

	ResultVm roomListReset();

	RoomVm getRoomById(int id);

	RoomKnockoutVm getRoomKnockoutById(int id);

	ResultVm modifyRoom(int id, int playingCount);
}
