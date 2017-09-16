package poker.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import poker.entity.RoomInfo;
import poker.main.room.RoomType;

public class RoomData {
	public static List<RoomInfo> getRoomTypeList(int type) {
		List<RoomInfo> roomTypeList = new ArrayList<>();
		String sql = "select * from ";
		if (type == RoomType.NORMAL) {
			sql += "room";
		} else if (type == RoomType.IMPERIAL) {
			sql += "roomImperial";
		} else if (type == RoomType.OMAHA) {
			sql += "roomomaha";
		}
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			ResultSet rs = preStatement.executeQuery();
			while (rs.next()) {
				RoomInfo roomInfo = new RoomInfo();
				roomInfo.setId(rs.getInt("id"));
				roomInfo.setSeatCount(rs.getInt("maxPlayingCount"));
				roomInfo.setSmallBlind(rs.getInt("minBlind"));
				roomInfo.setBigBlind(rs.getInt("maxBlind"));
				roomInfo.setMinTake(rs.getInt("minTake"));
				roomInfo.setMaxTake(rs.getInt("maxTake"));
				roomInfo.setAverageTake(rs.getInt("averageTake"));
				roomInfo.setType(rs.getInt("type"));
				roomTypeList.add(roomInfo);
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
