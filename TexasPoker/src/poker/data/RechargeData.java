package poker.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RechargeData {

	// 插入一条记录
	public int insertGoldGained(int id, int goldCount) {
		String sql = "insert into recharge(deleted,attr,memberId,count,sourceType,createDate) values(0,0,?,?,10,?)";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setInt(1, id);
			preStatement.setInt(2, goldCount);
			preStatement.setDate(3, new Date(new java.util.Date().getTime()));
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

	public static void main(String[] argv) {

		new RechargeData().insertGoldGained(1, -20);

	}

}
