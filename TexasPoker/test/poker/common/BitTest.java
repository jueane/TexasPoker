package poker.common;

public class BitTest {
	public static void main(String[] argv) {
		int a = 721030;
		a = 201;

		byte[] arr = new byte[4];

		arr[0] = (byte) (a & 0xff);
		arr[1] = (byte) (a >> 8 & 0xff);
		arr[2] = (byte) (a >> 16 & 0xff);
		arr[3] = (byte) (a >> 24 & 0xff);

		int b = 0;
		b = arr[0];
		b |= arr[1] << 8;
		b |= arr[2] << 16;
		b |= arr[3] << 24;
		System.out.println("最终1：" + b);
		// byte[] s = (arr[1] << 8);

		int c = 0;
		c = arr[0];
		c |= arr[1] << 8;
		c |= arr[2] << 16;
		c |= arr[3] << 24;
		System.out.println("最终2：" + c);

		int min = 5678;
		int max = 10000;
		System.out.println(min & max);
		for (int i = 0; i < 20000; i++) {
			int rest = i & max;
			if (rest > i) {
				System.out.println(i + "&" + max + "=" + rest);
			}

		}

	}
}
