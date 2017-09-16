package com.lingzerg.gamecenter.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.lingzerg.gamecenter.entity.MemberInfo;
import com.lingzerg.gamecenter.main.room.impl.CardRule;

public class MemberData {

	// 通过token获取一个member实体
	public MemberInfo getByToken(String token) {
		if (token == null || token.isEmpty()) {
			return null;
		}
		MemberInfo member = null;
		String sql = "select * from member where token=?";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setString(1, token);
			ResultSet rs = preStatement.executeQuery();
			if (rs.next()) {
				member = new MemberInfo();
				member.setId(rs.getInt("id"));
				member.setUsername(rs.getString("username"));
				member.setNickname(rs.getString("nickname"));
				member.setWinTimes(rs.getInt("winTimes"));
				member.setLoseTimes(rs.getInt("loseTimes"));
				member.setPortrait(rs.getInt("portrait"));
				member.setPortraitBorder(rs.getInt("portraitBorder"));
				member.setMale(rs.getBoolean("male"));
				member.setSign(rs.getString("sign"));
				member.setGold(rs.getInt("gold"));
				member.setMaxCards(rs.getBytes("maxCards"));
				member.setMaxCardsValue(rs.getInt("maxCardsValue"));
				member.setMaxScore(rs.getInt("maxScore"));
			}
		} catch (SQLException e) {
			System.err.println("Member getByToken error.Token:" + token);
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
		return member;
	}

	// 通过id获取一个member实体
	public MemberInfo getById(int id) {
		MemberInfo member = null;
		String sql = "select * from member where id=?";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setInt(1, id);
			ResultSet rs = preStatement.executeQuery();
			if (rs.next()) {
				member = new MemberInfo();
				member.setId(rs.getInt("id"));
				member.setUsername(rs.getString("username"));
				member.setNickname(rs.getString("nickname"));
				member.setWinTimes(rs.getInt("winTimes"));
				member.setLoseTimes(rs.getInt("loseTimes"));
				member.setPortrait(rs.getInt("portrait"));
				member.setPortraitBorder(rs.getInt("portraitBorder"));
				member.setMale(rs.getBoolean("male"));
				member.setSign(rs.getString("sign"));
				member.setGold(rs.getInt("gold"));
				member.setMaxCards(rs.getBytes("maxCards"));
				member.setMaxCardsValue(rs.getInt("maxCardsValue"));
				member.setMaxScore(rs.getInt("maxScore"));
			}
		} catch (SQLException e) {
			System.err.println("Member getByToken error.Id:" + id);
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
		return member;
	}

	// 某玩家增加count金币.
	public int memberGoldAdd(int id, int count) {
		int effectRows = 0;
		String sql = "update member set gold=gold+? where id=?";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setInt(1, count);
			preStatement.setInt(2, id);
			effectRows = preStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Gold update error.Id:" + id);
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
		return effectRows;
	}

	// 更新玩家的史上最大牌
	public int memberMaxCardsUpdate(int id, byte[] cards, int value) {
		int effectRows = 0;
		String sql = "update member set maxCards=?,maxCardsValue=? where id=?";
		Connection connection = ConnectionPool.getConnection();
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setBytes(1, cards);
			preStatement.setInt(2, value);
			preStatement.setInt(3, id);
			effectRows = preStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Max cards update error.Id:" + id);
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
		return effectRows;
	}

	// 更新胜负场次
	public int memberWinLostUpdate(List<Integer> winnerIdList, List<Integer> loserIdList) {
		int effectRows = 0;
		final String sqlWin = "update member set winTimes=winTimes+1 where id=?";
		final String sqlLose = "update member set loseTimes=loseTimes+1 where id=?";
		Connection connection = ConnectionPool.getConnection();
		for (int i = 0; winnerIdList != null && i < winnerIdList.size(); i++) {
			try {
				PreparedStatement preStatement = connection.prepareStatement(sqlWin);
				preStatement.setInt(1, winnerIdList.get(i));
				effectRows += preStatement.executeUpdate();
			} catch (Exception e) {
				System.out.println("Update win time error.");
			}
		}
		for (int i = 0; loserIdList != null && i < loserIdList.size(); i++) {
			try {
				PreparedStatement preStatement = connection.prepareStatement(sqlLose);
				preStatement.setInt(1, loserIdList.get(i));
				effectRows += preStatement.executeUpdate();
			} catch (Exception e) {
				System.out.println("Update lose time error.");
			}
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
		return effectRows;
	}

	public void memberMaxScoreUpdate(int id, int score) {
		Connection connection = ConnectionPool.getConnection();
		final String sql = "update member set maxScore=? where id=?";
		try {
			PreparedStatement preStatement = connection.prepareStatement(sql);
			preStatement.setInt(1, score);
			preStatement.setInt(2, id);
			preStatement.executeUpdate();
		} catch (Exception e) {
			System.out.println("Update maxScore error.");
		}
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Close connection error." + e.getMessage());
		}
	}

	public static void main(String args[]) {
		System.out.println("hi~");
		MemberData memberData = new MemberData();
		MemberInfo mem = memberData.getById(2);
		if (mem != null) {
			System.out.println("不为空");
		}
		if (mem.getMaxCards() == null) {
			System.out.println("空");
		} else {
			CardRule.showCards(mem.getMaxCards());
		}

		// CardRule cr = new CardRule();
		// byte[] bts = cr.getCards(5);
		// memberData.memberMaxCardsUpdate(1, bts);
	}
}
