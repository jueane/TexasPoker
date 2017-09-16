package poker.config;

import java.io.IOException;
import java.util.Properties;

public class ConstantConfig {
	// 中控
	public String ccip;
	public int ccport;

	private static ConstantConfig uniqueInstance = null;

	private ConstantConfig() {

		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));

			ccip = p.getProperty("ccip");
			ccport = Integer.valueOf(p.getProperty("ccport"));

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
