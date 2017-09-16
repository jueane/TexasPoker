package poker.main.room.impl.imperial;

import java.util.List;

import poker.main.room.RoomType;
import poker.main.room.impl.GameRule;
import poker.main.room.impl.RoomImpl;
import poker.main.room.impl.RoomVm;
import poker.main.room.impl.GameRule.Score;
import poker.util.JLog;

public class ImperialRoomImpl extends RoomImpl {

	public ImperialRoomImpl(RoomVm rv) {
		super(rv);
	}

	@Override
	protected void init() {
		this.roomType = RoomType.IMPERIAL;
		this.minPlayerForBegin = 2;
		this.minPlayerForRun = 2;
		log = new JLog("room/Imperial_" + this.id);
		cardRule = new CardRuleInImperial(this.log);
		gameRule = new GameRule();
	}

	@Override
	public void updateMaxCards(List<Score> scoreList) {
	}

}
