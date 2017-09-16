package config;

import java.io.IOException;
import java.util.Properties;

public class ConstantConfig {
	public String databaseUrl;
	public String databaseUser;
	public String databasePwd;

	private static ConstantConfig uniqueInstance = null;

	private ConstantConfig() {

		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));

			databaseUrl = p.getProperty("databaseUrl");
			databaseUser = p.getProperty("databaseUser");
			databasePwd = p.getProperty("databasePwd");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ConstantConfig getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new ConstantConfig();
		}
		return uniqueInstance;
	}

}
