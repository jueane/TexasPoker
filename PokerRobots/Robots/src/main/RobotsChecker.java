package main;

import java.util.Date;
import java.util.List;

import main.robot.Robot;

/*
 * Description:无
 * Author:je
 * Date:2015年1月21日
 */
public class RobotsChecker {

	protected static int normal = 0;
	protected static int total = 0;

	public static void startup() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				RobotsChecker rChecker = new RobotsChecker();
				rChecker.doCheck();
			}
		}).start();

	}

	protected void doCheck() {
		while (true) {
			int counter = 0;
			List<Robot> rbtList = Launch.robotList;
			for (int i = 0; i < rbtList.size(); i++) {
				// 检查心跳是否超时
				if (new Date().getTime() - rbtList.get(i).getTime() > 60000) {
					rbtList.get(i).start();
					counter++;
				}

				// 检查在同一房间超时3小时未退出的机器人
				if (System.currentTimeMillis() - rbtList.get(i).getLastBeginTime() > 60000 * 60 * 3) {
					rbtList.get(i).start();
				}

				// 暂停一秒
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// 统计数量
			int norCount = rbtList.size() - counter;
			if (norCount != normal || total != rbtList.size()) {
				normal = norCount;
				total = rbtList.size();
				System.out.println("Robots count:" + normal + "/" + total + ".");

			}

		}

	}
}
