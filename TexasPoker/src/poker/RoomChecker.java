package poker;

import poker.main.room.RoomImpl;
import poker.util.JLog;

// 检查静止房间中的残留玩家
public class RoomChecker implements Runnable {

	static JLog log = new JLog("RoomChecker");

	public static void startup() {
		new Thread(new RoomChecker()).start();
	}

	int checkTimes = 1;

	private void checkRoom(RoomImpl room) {
		if (room.getPhase() == 0 && room.getSeats().getSittingCount() == 1) {
			int originPlayerCount = room.getSeats().getSittingCount();

			room.gamerGroup.cleanTimeoutPlayers();
			/// 上句替换了下句
			// room.cleanTimeoutPlayers();
			log.infoln("[Roomcheck]Clean room " + room.getId() + " done,player count:" + originPlayerCount + ">" + room.getSeats().getSittingCount() + ".");
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			if (checkTimes % 1000 == 0) {
				log.infoln("[Roomcheck]times:" + checkTimes++ + ".");
			}
			for (int i = 0; i < Hall.uniqueHall().roomList.size(); i++) {
				checkRoom(Hall.uniqueHall().roomList.get(i));
			}
			// for (int i = 0; i < ChannelHandler.roomKnockList.size(); i++) {
			// checkRoom(ChannelHandler.roomKnockList.get(i));
			// }
			// for (int i = 0; i < ChannelHandler.roomImperialList.size(); i++)
			// {
			// checkRoom(ChannelHandler.roomImperialList.get(i));
			//
			// }
			// for (int i = 0; i < ChannelHandler.roomOmahaList.size(); i++) {
			// checkRoom(ChannelHandler.roomOmahaList.get(i));
			// }
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
