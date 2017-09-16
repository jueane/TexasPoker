package poker.common;

public class ThreadTest {
	// 测试：子线程崩溃（runtimeException）是否影响父线程
	// 结论：不影响

	class child {

	}

	public static void main(String[] args) {

		System.out.println("Start~");
		new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (true) {
					System.out.println("child：" + i);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;
					if (i > 5) {
						throw new RuntimeException();
					}
				}

			}
		}).start();

		int i = 0;
		while (true) {
			System.out.println("main：" + i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}

	}

}
