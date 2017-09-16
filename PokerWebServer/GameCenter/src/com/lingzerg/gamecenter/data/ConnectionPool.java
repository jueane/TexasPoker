package com.lingzerg.gamecenter.data;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionPool {
	private static ComboPooledDataSource ds = new ComboPooledDataSource();

	public static Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			System.err.println("获取数据库连接错误！");
			throw new RuntimeException(e);
		}

	}

}
