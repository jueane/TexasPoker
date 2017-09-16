package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entity.MemberInfo;

public class MemberData {
	// 获取指定数量的member列表
	public static List<MemberInfo> getMemberList(int start, int count) {
		List<MemberInfo> memberList = new ArrayList<>();
		String sql = "select * from member where gold>=10000 and token!='' order by id desc limit " + start + "," + count;
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			ResultSet rs;
			rs = preStatement.executeQuery();
			while (rs.next()) {
				MemberInfo memberInfo = new MemberInfo();
				memberInfo.setId(rs.getInt("id"));
				memberInfo.setToken(rs.getString("token"));
				memberInfo.setGold(rs.getInt("gold"));
				memberList.add(memberInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return memberList;
	}

	// 插入一个member对象
	public static int insertMember(MemberInfo mem) {
		String sql = "insert into member (attr,deleted,male,gem,gold,maxCardsValue,maxScore,maxTotalGold,portrait,status,isVip,winTimes,loseTimes,password,token,username,verified) values (0,0,0,0,26000,0,0,0,0,0,0,0,0,'123',?,?,0);";
		Connection connection = ConnectionPool.getConnection();
		PreparedStatement preStatement = null;
		try {
			preStatement = connection.prepareStatement(sql);
			preStatement.setString(1, mem.getToken());
			preStatement.setString(2, mem.getUsername());
			preStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static void main(String[] args) {

		MemberInfo mem = new MemberInfo();
		mem.setToken("he11");
		mem.setUsername("userajfkdls");
		insertMember(mem);

	}
}
