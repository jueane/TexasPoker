package poker.common;

public class SwitchTest {

	public static void main(String[] args) {

		int a = 3;
		switch (a) {
		case 3:
			System.out.println("hi");
		case 2:
			System.out.println("2");
			a = 5;
		case 1:
			System.out.println("1");
		default:
			System.out.println("hello");
		}

	}

}
