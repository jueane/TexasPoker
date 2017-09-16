package poker.synctest;

import java.lang.Thread.State;

public class Synctestmain {

	public static void main(String[] args) {

		ProcessTest pt = new ProcessTest();
		Thread th = new Thread(pt);
		th.start();

		int i = 0;
		while (true) {
			ProcessTest.strList.add("abc              " + i++);

			if (th.getState() == State.WAITING) {
				synchronized (pt) {
					pt.notify();

				}
			}

		}

	}

}
