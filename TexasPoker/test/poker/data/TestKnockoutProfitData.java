package poker.data;

import static org.junit.Assert.*;

import org.junit.Test;

import poker.data.KnockoutProfitData;

/*
 * Description:无
 * Author:je
 * Date:2015年1月26日
 */
public class TestKnockoutProfitData extends KnockoutProfitData {

	@Test
	public void test() {
		assertTrue(1 == insert(2, 2, 1000, 0));
		assertTrue(1 == insert(3, 2, 0, 100));

	}

}
