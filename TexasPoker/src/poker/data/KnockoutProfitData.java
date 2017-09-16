package poker.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/*
 * Description:无
 * Author:je
 * Date:2015年1月26日
 */
public class KnockoutProfitData {
	public int insert(int memberId, int roomLevel, int entryFee, int reward) {
		int insertId = 0;
		Connection conn = ConnectionPool.getConnection();
		String sql = "insert into knockoutProfit(deleted,attr,memberId,roomLevel,entryFee,reward,createDate) values(0,0,?,?,?,?,?)";
		try {
			PreparedStatement preStatement = conn.prepareStatement(sql);
			preStatement.setInt(1, memberId);
			preStatement.setInt(2, roomLevel);
			preStatement.setInt(3, entryFee);
			preStatement.setInt(4, reward);
			preStatement.setTimestamp(5, new Timestamp(new Date().getTime()));
			insertId = preStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return insertId;
	}

}
