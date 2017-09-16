package main;

import java.sql.SQLException;
import java.util.List;

import data.MemberData;
import entity.MemberInfo;

public class Launch {
	private static String ip = "42.96.192.233";
	private static final int minGold = 10000;
	public static int roomType;
	public static int roomLevel;
	private static int start = 0;
	private static int count = 0;
	public static int actionTimeout;

	public Launch() {
	}

	public void startup() {
		System.out.println("Initializing...");
		try {
			data.ConnectionPool.getConnection().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		System.out.println("Game client is running...");

		List<MemberInfo> memList = MemberData.getMemberList(start, count);

		System.out.println("Count:" + memList.size());
		for (int i = 0; i < memList.size(); i++) {
			if (memList.get(i).getToken() != null && memList.get(i).getToken().isEmpty() == false && memList.get(i).getGold() > minGold) {
				Client user = new Client(ip, memList.get(i));
				new Thread(user).start();
			}
		}
		System.out.println("Done.");

	}

	public static void main(String[] args) {
		if (args.length > 0) {
			ip = args[0];
			roomType = Integer.parseInt(args[1]);
			roomLevel = Integer.parseInt(args[2]);
			start = Integer.parseInt(args[3]);
			count = Integer.parseInt(args[4]);
			actionTimeout = Integer.parseInt(args[5]);
		}
		if (start < 0) {
			start = 0;
		}
		if (count < 1) {
			count = 1;
		}
		new Launch().startup();
	}
}
