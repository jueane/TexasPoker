package com.lingzerg.gamecenter.config;

import java.io.IOException;
import java.util.Properties;

public class Config {
	public int port=7777;
	public int nextGameWait = 10000;
	public int nextGameWaitInKnockout = 60000;
	public String webServiceUrl = "http://127.0.0.1:88/poker";

	private static Config uniqueInstance = null;

	private Config() {

		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
			
			port=Integer.valueOf(p.getProperty("port"));
			nextGameWait = Integer.valueOf(p.getProperty("nextGameWait"));
			nextGameWaitInKnockout = Integer.valueOf(p.getProperty("nextGameWaitInKnockout"));
			webServiceUrl = p.getProperty("webServiceUrl");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Config getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new Config();
		}
		return uniqueInstance;
	}

}
