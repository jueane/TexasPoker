package com.lingzerg.poker.util;

import java.net.URL;

public class PhoneUtil {
	public static void send(String phoneNumber, String msg) {
		String urlSend = "http://106.ihuyi.cn/webservice/sms.php?method=Submit&account=cf_baoyisheng&password=sheng888&mobile=" + phoneNumber + "&content=" + msg;
		URL url = null;
		try {
			url = new URL(urlSend);
			url.openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String generateValidationCode(){
		String code="123";
		return code;
	}

}
