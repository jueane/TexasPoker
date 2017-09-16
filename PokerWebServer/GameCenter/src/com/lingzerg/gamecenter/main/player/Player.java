package com.lingzerg.gamecenter.main.player;

import java.io.IOException;

import com.lingzerg.gamecenter.entity.MemberInfo;
import com.lingzerg.gamecenter.proto.ValidationPro.ValidationProto;

public interface Player {

	int getId();

	ValidationProto validate() throws IOException;

	int getBankroll();
	
	void setBankroll(int bankroll);

	void setGained(int gainedGold);

	int getGained();

	int getAnte(int turn);

	int getAnteTotal();
	
	void resetAnteList();

	PlayerStatus getStatus();

	void setStatus(PlayerStatus status);

	int init();

	void resetStatus();

	void dealHoleCards(byte[] cards);

	byte[] getHoleCards();

	int blindBet(int count);

	ActionResult action(int turn, int seat, int minCall, boolean checkable);

	int send(int type, byte[] bytes) throws IOException;

	boolean ConnectionOk();

	MemberInfo getDetailInfo();

}
