package poker.synctest;

import java.util.ArrayList;
import java.util.List;

public class ProcessTest implements Runnable {
	public static List<String> strList = new ArrayList<>();

	public boolean threadSuspend = false;

	@Override
	public void run() {
		while (true) {
			// System.out.println("strList is empty:" + strList.isEmpty());

			while (strList.size() >= 1) {
				String aString = strList.remove(0);
				// System.out.println(aString);
				if (aString == null) {
					try {
						System.out.println("空..");
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			System.out.println("`````````````````````````````等待中。。。");
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
