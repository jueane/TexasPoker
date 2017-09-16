package main.robot;

import main.Pack;
import entity.MemberInfo;

/*
 * Description:无
 * Author:赵俊杰
 * Date:2015年3月6日
 */
public interface Robot {
	MemberInfo getUsernfo();

	void setUserinfo(MemberInfo mem);
	
	int getType();
	
	int getLevel();

	void start();
	
	void restart();

	void setTime(long time);

	long getTime();
	
	long getLastBeginTime();

	void action(byte[] buff);
	
	void packHandler(Pack pack);

	void setHoleCards(byte[] cards);
	
	void broadcastCards(byte[] cards);

	void gameEnd();// 一局游戏结束时调用

}
