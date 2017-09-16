package poker.main.room.impl.omaha;

import poker.main.room.RoomType;
import poker.main.room.impl.CardRule;
import poker.main.room.impl.RoomImpl;
import poker.main.room.impl.RoomVm;
import poker.util.JLog;

public class OmahaRoomImpl extends RoomImpl {

	public OmahaRoomImpl(RoomVm rv) {
		super(rv);
	}

	@Override
	protected void init() {
		this.roomType = RoomType.OMAHA;
		this.holeCardsCount = 4;
		this.minPlayerForBegin = 2;
		this.minPlayerForRun = 2;
		log = new JLog("room/Omaha_" + this.id);
		cardRule = new CardRule(this.log);
		gameRule = new GameRuleOmaha();
	}

}
