package poker.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import poker.entity.RoomKnockoutInfo;

public class RoomKnockoutData {
	public static List<RoomKnockoutInfo> getRoomTypeList() {
		List<RoomKnockoutInfo> roomTypeList = new ArrayList<>();
		String sql = "select * from roomKnockout";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				RoomKnockoutInfo roomKnockoutInfo = new RoomKnockoutInfo();
				roomKnockoutInfo.setId(rs.getInt("id"));
				roomKnockoutInfo.setSeatCount(rs.getInt("maxPlayingCount"));
				roomKnockoutInfo.setSmallBlind(rs.getInt("minBlind"));
				roomKnockoutInfo.setBigBlind(rs.getInt("maxBlind"));
				roomKnockoutInfo.setEntryFee(rs.getInt("entryFee"));
				roomKnockoutInfo.setInitBankroll(rs.getInt("initialChip"));
				String rewardStr = rs.getString("bonus");
				String[] rewardArr = rewardStr.split(",");
				int[] rewardInt = new int[rewardArr.length];
				for (int i = 0; i < rewardArr.length; i++) {
					rewardInt[i] = Integer.parseInt(rewardArr[i]);
				}
				roomKnockoutInfo.setReward(rewardInt);
				roomKnockoutInfo.setType(rs.getInt("type"));
				roomTypeList.add(roomKnockoutInfo);
			}
		} catch (Exception e) {
			System.err.println("GetRoomTypeList error.");
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				System.err.println("Close connection error." + e.getMessage());
			}
		}
		return roomTypeList;
	}

}
