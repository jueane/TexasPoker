package poker.data;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

/*
 * Description:无
 * Author:je
 * Date:2015年1月26日
 */
public class TestConnectionPool extends ConnectionPool {

	@Test
	public void test() {
		Connection conn = getConnection();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			assertTrue(conn.isClosed());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
