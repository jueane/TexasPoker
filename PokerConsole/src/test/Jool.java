package test;

public class Jool {
	public int a = 1;

	public int getA() {
		return a;
	}

	@Override
	protected void finalize() throws Throwable {
		System.out.println("desctruct.");
		super.finalize();
	}
}
