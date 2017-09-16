package poker.main.room.impl.omaha;

import poker.main.player.Player;
import poker.main.room.impl.GameRule;

public class GameRuleOmaha extends GameRule {
	@Override
	public int add(int id, Player player, byte[] cards) {
		byte[] holeCards = new byte[4];
		int maxValue = 0;
		byte[] maxHoleCards = new byte[2];
		byte[] combineCards = new byte[7];
		for (int i = 0; i < 5; i++) {
			combineCards[i] = cards[i];
		}
		for (int i = 0; i < holeCards.length; i++) {
			holeCards[i] = cards[i + 5];
		}
		GameRule gr = new GameRule();
		for (int i = 0; i < holeCards.length - 1; i++) {
			for (int j = i + 1; j < holeCards.length; j++) {
				// System.out.println("i and j:" + i + "," + j);
				combineCards[5] = holeCards[i];
				combineCards[6] = holeCards[j];
				gr.add(id, player, combineCards);
				// System.out.println(gr.scoreList.get(gr.scoreList.size() - 1).value);
				// gr.compare();
				if (gr.scoreList.get(gr.scoreList.size() - 1).value > maxValue) {
					maxValue = gr.scoreList.get(gr.scoreList.size() - 1).value;
					maxHoleCards[0] = combineCards[5];
					maxHoleCards[1] = combineCards[6];
				}

				// CardRule.showCards(combineCards);
				// CardRule.showCards(maxHoleCards);
			}
		}
		combineCards[5] = maxHoleCards[0];
		combineCards[6] = maxHoleCards[1];

		return super.add(id, player, combineCards);
	}
}
