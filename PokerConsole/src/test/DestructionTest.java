package test;

public class DestructionTest {
	public static void main(String args[]) {
		Jool jool = new Jool();
		jool.getA();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
