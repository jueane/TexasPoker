package poker.main.room.impl.knockout;

public class BlindIncreaseRule {
	protected static int[] blindArraySix = { 100, 200, 400, 800, 1600, 3000, 4000, 6000, 10000, 18000, 24000 };
	protected static int[] blindArrayNine = { 100, 300, 600, 1000, 1800, 3600, 4800, 7200, 12000, 24000, 36000 };

	public static int getBlind(int type, long time) {
		int position = (int) time / 120000;
		if (type < 9) {
			if (position >= blindArraySix.length) {
				position = blindArraySix.length - 1;
			}
			return blindArraySix[position];
		} else {
			if (position >= blindArrayNine.length) {
				position = blindArrayNine.length - 1;
			}
			return blindArrayNine[position];
		}
	}

	public static void main(String args[]) {
		int x = BlindIncreaseRule.getBlind(3, 1532032200);
		System.out.println(x);
	}
}
