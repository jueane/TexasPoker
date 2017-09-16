package data;

import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionPool {
	private static ComboPooledDataSource ds = new ComboPooledDataSource();

	public static Connection getConnection() {
		try {
			// System.out.println("当前连接数：" + ds.getNumConnections() + ", busy："
			// + ds.getNumBusyConnections());
			return ds.getConnection();
		} catch (SQLException e) {
			System.err.println("获取数据库连接错误！");
			throw new RuntimeException(e);
		}

	}

}
