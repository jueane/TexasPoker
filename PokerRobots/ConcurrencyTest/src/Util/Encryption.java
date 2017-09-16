package Util;

import java.security.MessageDigest;

public class Encryption {

	public static String md5(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(message.getBytes());
			byte[] digest = md.digest();

			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				sb.append(String.format("%02x", b & 0xff));
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
