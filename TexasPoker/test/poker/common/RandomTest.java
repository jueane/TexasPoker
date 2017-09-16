package poker.common;

import java.util.Random;

public class RandomTest {
	public static void main(String args[]) {
		for (int i = 0; i < 30; i++) {
			Random rd = new Random();
			System.out.println(rd.nextInt(10));
		}

	}

}
