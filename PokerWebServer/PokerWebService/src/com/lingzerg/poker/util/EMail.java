package com.lingzerg.poker.util;

import java.util.Properties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.lingzerg.poker.config.Config;

public class EMail {
	// example---:String notice = eMail.send("59757359@qq.com",
	// "poker password reset validation", "密码重置 验证码为：321");

	private static final String host = Config.getInstance().emailHost;
	private static final String port = Config.getInstance().emailPort;
	private static final String username = Config.getInstance().emailUsername;
	private static final String password = Config.getInstance().emailPassword;

	public static String send(String toAddress, String title, String content) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", port);
		props.setProperty("mail.smtp.ssl.enable", "true");
		props.setProperty("mail.smtp.auth", "true");
		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(host);
		javaMailSenderImpl.setUsername(username);
		javaMailSenderImpl.setPassword(password);
		javaMailSenderImpl.setJavaMailProperties(props);
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		try {
			mailMessage.setTo(toAddress);
			mailMessage.setFrom(username);
			mailMessage.setSubject(title);
			mailMessage.setText(content);
			javaMailSenderImpl.send(mailMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(true);
	}

	public static String generateValidationCode() {
		String code = "123";
		return code;
	}

}
