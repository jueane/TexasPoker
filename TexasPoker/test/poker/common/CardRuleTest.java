package poker.common;

import org.junit.Test;
import org.junit.runner.notification.Failure;

import poker.main.room.impl.CardRule;
import poker.util.JLog;

/*
 * Description:无
 * Author:je
 * Date:2015年1月20日
 */
public class CardRuleTest extends CardRule {

	public CardRuleTest(JLog log) {
		super(log);
		// TODO Auto-generated constructor stub
	}

	public void test() {
		JLog.debug = true;

		shuffle();

		// System.out.println("remain:" + remain);
		// System.out.println("card length:" + cardList.length);
		// showCards(cardList);

		// for (int i = 0; i < positions.length; i++) {
		// System.out.print(positions[i] + ",");
		// }

		for (int i = 0; i < positions.length - 1; i++) {
			for (int j = i + 1; j < positions.length; j++) {
				if (positions[i] > positions[j]) {
					int t = positions[i];
					positions[i] = positions[j];

					positions[j] = t;
				}

			}

		}

		System.out.println();
		// for (int i = 0; i < positions.length; i++) {
		// System.out.print(positions[i] + ",");
		// }
		if (cardList.length == 52 && positions.length == 52 && positions[0] == 0 && positions[51] == 51) {

		} else {
			System.out.println("失败");
		}
	}

	@Test
	public void testTimes() {
		for (int i = 0; i < 100000; i++) {
			test();
		}
	}

}
