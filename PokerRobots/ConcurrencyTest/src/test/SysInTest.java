package test;

import java.io.IOException;
import java.util.Scanner;

public class SysInTest {

	public static void main(String[] args) {

		int a = getInt(1, 10);
		System.out.println("数字：" + a);
		int b = getInt(1, 10);
		System.out.println("数字：" + b);

	}

	private static int getInt(int min, int max) {
		int num = 0;
		Scanner scanner = new Scanner(System.in);
		while (true) {
			if (scanner.hasNextInt()) {
				num = scanner.nextInt();
				if (num >= min && num <= max) {
					break;
				}
			}
		}
		return num;
	}

}
