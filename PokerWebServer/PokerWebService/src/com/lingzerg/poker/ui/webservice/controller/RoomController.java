package com.lingzerg.poker.ui.webservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lingzerg.poker.service.RoomService;
import com.lingzerg.poker.util.JsonHelper;

@Controller
@RequestMapping("room")
public class RoomController {

	@Autowired
	private RoomService roomService;

	@RequestMapping("getRoomList")
	@ResponseBody
	public byte[] getRoomNormalList(int type) {
		return JsonHelper.toJson(roomService.getRoomList(type));
	}

	//game center用
	@RequestMapping("roomListReset")
	@ResponseBody
	public byte[] roomListReset() {
		return JsonHelper.toJson(roomService.roomListReset());
	}

	@RequestMapping("getRoomKnockoutList")
	@ResponseBody
	public byte[] getRoomKnockoutList(int type) {
		return JsonHelper.toJson(roomService.getRoomKnockoutList(type));
	}

	//game center用
	@RequestMapping("getRoomById")
	@ResponseBody
	public byte[] getRoomById(int id) {
		return JsonHelper.toJson(roomService.getRoomById(id));
	}

	//game center用
	@RequestMapping("getRoomKnockoutById")
	@ResponseBody
	public byte[] getRoomKnockoutById(int id) {
		return JsonHelper.toJson(roomService.getRoomKnockoutById(id));
	}

	//game center用
	@RequestMapping("modifyRoom")
	@ResponseBody
	public byte[] modifyRoom(int id, int playingCount) {
		return JsonHelper.toJson(roomService.modifyRoom(id, playingCount));
	}
}
