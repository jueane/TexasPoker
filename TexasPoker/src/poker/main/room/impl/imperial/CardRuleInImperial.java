package poker.main.room.impl.imperial;

import poker.main.room.impl.CardRule;
import poker.main.room.impl.RoomVm;
import poker.util.JLog;

public class CardRuleInImperial extends CardRule {

	public CardRuleInImperial(JLog log) {
		super(log);
	}

	@Override
	protected void init() {
		cardList = new byte[] { 0x1a, 0x2a, 0x3a, 0x4a, 0x1b, 0x2b, 0x3b, 0x4b, 0x1c, 0x2c, 0x3c, 0x4c, 0x1d, 0x2d, 0x3d, 0x4d, 0x1e, 0x2e, 0x3e, 0x4e };
		positions = new int[cardList.length];
	}

	public static void main(String args[]) {
		RoomVm rv = new RoomVm();
		rv.id = 1;
		rv.seatCount = 6;
		rv.smallBlind = 2;
		rv.bigBlind = 4;
		rv.minTake = 400;
		rv.maxTake = 800;
	}
}
