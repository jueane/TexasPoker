package poker.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ScoreData {

	// 插入一条记录
	public int insertGoldGained(int id, int actualGained, int drawoff) {
		String sql = "insert into score(deleted,attr,memberId,gold,brokerage,createDate) values(0,0,?,?,?,?)";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setInt(1, id);
			preStatement.setInt(2, actualGained);
			preStatement.setInt(3, drawoff);
			preStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
			preStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Insert GoldGained error.Id:" + id);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				System.err.println("Close connection error." + e.getMessage());
			}
		}
		return 0;
	}

}
