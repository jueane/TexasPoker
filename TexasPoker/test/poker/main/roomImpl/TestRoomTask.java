package poker.main.roomImpl;

import static org.junit.Assert.*;

import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

public class TestRoomTask {

	String flowtaskLock = new String();
	TimerTask flowTask = null;

	Timer timer = new Timer();

	public void test() throws InterruptedException {

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("runned1." + Thread.currentThread().getId());

			}
		};

		taskContinue(task, 2000);
		task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("runned2." + Thread.currentThread().getId());
				timer.cancel();

			}
		};

		taskContinue(task, 2500);

		Thread.sleep(2400);
		task.cancel();

		task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("runned3." + Thread.currentThread().getId());
				timer.cancel();

			}
		};
		taskContinue(task, 2700);
		
//		System.out.println("清除数量：" + timer.purge());

	}

	public static void main(String args[]) {
		try {
			new TestRoomTask().test();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void taskContinue(TimerTask task, long delay) {
		synchronized (this.flowtaskLock) {
			this.flowTask = task;
			timer.schedule(task, delay);
		}
	}

	protected void taskPause() {
		synchronized (this.flowtaskLock) {
			if (flowTask != null) {
				flowTask.cancel();
				flowTask = null;
			}
		}
	}

}
