/**
 * 
 */
package poker.main.room.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import poker.util.JLog;

/*
 * Description:无
 * Author:je
 * Date:2015年1月27日
 */
/**
 * @author Administrator
 *
 */
public class TestCardRule {
	CardRule cr;

	@Before
	public void setUp() throws Exception {
		JLog log = new JLog("hi");
		JLog.debug = true;
		cr = new CardRule(log);
		// cr.cardList
		byte[] cds = cr.getCards(52);
		cr.showCards(cds);
		System.out.println("长度:" + cds.length);
		byte cd = cr.getCard();
		cr.showCard(cd);
	}

	@After
	public void tearDown() throws Exception {
		System.out.println(4);
	}

	@Test
	public void test() {
		System.out.println("hi");

	}

}
