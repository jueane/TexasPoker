package poker.common;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Description:无
 * Author:je
 * Date:2015年1月27日
 */
public class TestLock {

	int a = 1;
	Lock lock = new ReentrantLock();
	class Peop implements Runnable {
		@Override
		public void run() {
			System.out.println("hi" + (a++));
		}
	}

	public static void main(String[] args) {
		TestLock tl = new TestLock();
		for (int i = 0; i < 9; i++) {
			tl.lock.lock();
			if (tl.a == 1) {
				new Thread(tl.new Peop()).start();
				tl.a = 2;
			}
			tl.lock.unlock();
		}

	}

}
