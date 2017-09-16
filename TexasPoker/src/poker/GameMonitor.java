package poker;

import java.util.List;

import poker.main.room.RoomImpl;
import poker.util.JLog;

public class GameMonitor implements Runnable {
	private int lastPlayerCount = 0;
	static JLog log = new JLog("GameMonitor");

	public static void startup() {
		new Thread(new GameMonitor()).start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}

			int roomRunning = 0;
			int roomKncRunning = 0;
			int roomImpRunning = 0;
			int roomAmaRunning = 0;
			List<RoomImpl> rmList = Hall.uniqueHall().roomList;
			for (int i = 0; i < rmList.size(); i++) {
				if (rmList.get(i).getPhase() != 0) {
					roomRunning++;
				}
			}
			// rmList = ChannelHandler.roomKnockList;
			// for (int i = 0; i < rmList.size(); i++) {
			// if (rmList.get(i).getPhase() != 0) {
			// roomKncRunning++;
			// }
			// }
			// rmList = ChannelHandler.roomImperialList;
			// for (int i = 0; i < rmList.size(); i++) {
			// if (rmList.get(i).getPhase() != 0) {
			// roomImpRunning++;
			// }
			// }
			// rmList = ChannelHandler.roomOmahaList;
			// for (int i = 0; i < rmList.size(); i++) {
			// if (rmList.get(i).getPhase() != 0) {
			// roomAmaRunning++;
			// }
			// }

			if (this.lastPlayerCount != Hall.uniqueHall().playerMap.size()) {
				this.lastPlayerCount = Hall.uniqueHall().playerMap.size();

				String roomRunningCount = roomRunning + "/" + roomKncRunning + "/" + roomImpRunning + "/" + roomAmaRunning;
				String roomCount = Hall.uniqueHall().roomList.size() + "";

				log.infoln("[Monitor]Player count " + Hall.uniqueHall().playerMap.size() + ",room running count " + roomRunningCount + ",room count " + roomCount);

			}
		}

	}

}
