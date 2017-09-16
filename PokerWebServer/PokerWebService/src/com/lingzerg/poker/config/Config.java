package com.lingzerg.poker.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.lingzerg.poker.util.StringHelper;

public class Config {
	public int hiden = 0; // 0全显示不隐藏, 1 隐藏德币兑换元宝, 2 隐藏兑换介绍
	public double lootChance = 0.02; // 奖金抽取比例 1为100%, 0.1为10% 0.01为1%;
	// 赔率
	public double oneArmBandit_pArray[] = { 64.3, 80, 95, 96, 97, 98, 99, 99.5, 100 }; // 苹果机中奖概率
	public int oneArmBandit_reward[] = { 0, 2, 3, 4, 5, 6, 8, 10, 20 }; // 苹果机赔率

	public int wheel_pArray[] = { 25, 50, 70, 80, 90, 95, 100 }; // 转轮中奖概率
	public int wheel_reward[] = { 300, 500, 800, 1000, 2000, 5000, 9999 }; // 转轮奖励

	// --------------------------------------------------------------------------------------
	public int registerInitGold = 3000;// 新注册用户赠送金币数量
	public int baseOnlineCount = 800;// 在线玩家基数
	public int challengerGoldRequirement = 10000001;// 最强王者德币下限

	public String emailHost = "smtp.163.com";
	public String emailPort = "465";
	public String emailUsername = "lpoker@163.com";
	public String emailPassword = "poker123";
	// --------------------------------------------------------------------------------------

	private static Config uniqueInstance = null;

	private Config() {
		try {
			Properties p = new Properties();
			p.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));

			hiden = Integer.valueOf(p.getProperty("hiden"));
			lootChance = Double.valueOf(p.getProperty("lootChance"));
			oneArmBandit_pArray = StringHelper.stringArrayToDoubleArray(p.getProperty("oneArmBandit_pArray").split(","));
			oneArmBandit_reward = StringHelper.stringArrayToIntArray(p.getProperty("oneArmBandit_reward").split(","));
			wheel_pArray = StringHelper.stringArrayToIntArray(p.getProperty("wheel_pArray").split(","));
			wheel_reward = StringHelper.stringArrayToIntArray(p.getProperty("wheel_reward").split(","));

			// --------------------------------------------------------------------------------------
			registerInitGold = Integer.valueOf(p.getProperty("registerInitGold"));
			baseOnlineCount = Integer.valueOf(p.getProperty("baseOnlineCount"));
			challengerGoldRequirement = Integer.valueOf(p.getProperty("challengerGoldRequirement"));
			//邮箱信息
			emailHost=p.getProperty("emailHost");
			emailPort=p.getProperty("emailPort");
			emailUsername=p.getProperty("emailUsername");
			emailPassword=p.getProperty("emailPassword");
			// --------------------------------------------------------------------------------------
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
