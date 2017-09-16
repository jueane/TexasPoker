package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import main.robot.Robot;
import main.robot.impl.RobotImpl;
import data.MemberData;
import entity.MemberInfo;

public class Launch {
	public static String ip = "42.96.192.233";
	public static int port = 7000;
	public static int count = 60;
	private static final int minGold = 10000;
	private static int[][] roomProp = { { 1, 1 }, { 1, 2 }, { 1, 3 }, { 1, 4 }, { 2, 1 }, { 2, 2 }, { 3, 1 }, { 4, 1 } };

	public static List<Robot> robotList = new ArrayList<>();

	public void startup() {
		System.out.println("Robots is running...");
		List<MemberInfo> memList = MemberData.getMemberList(count);
		System.out.println("Count:" + memList.size());

		for (int i = 0; i < memList.size(); i++) {
			if (memList.get(i).getToken() != null && memList.get(i).getToken().isEmpty() == false && memList.get(i).getGold() > minGold) {
				int[] rp = roomProp[i % roomProp.length];
				Robot robot = new RobotImpl(ip, port, memList.get(i), rp[0], rp[1]);
				robot.setTime(new Date().getTime());
				robot.start();
				robotList.add(robot);
			}
		}
		System.out.println("Start robots checker.");
		RobotsChecker.startup();
		System.out.println("Done.");
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			ip = args[0];
			port = Integer.parseInt(args[1]);
			count = Integer.parseInt(args[2]);
		}
		new Launch().startup();
	}
}
