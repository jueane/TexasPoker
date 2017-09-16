package main;

import Util.Encryption;
import data.MemberData;
import entity.MemberInfo;

public class MemberGenerator {
	static int start = 40001;
	static int count = 10000;

	public static void main(String[] args) {
		System.out.println("Start~");
		for (int i = start; i < start + count; i++) {
			String username = "testuser" + i;
			String token = Encryption.md5(username);
			MemberInfo mem = new MemberInfo();
			mem.setUsername(username);
			mem.setToken(token);
			MemberData.insertMember(mem);
		}
		System.out.println("end~");

	}

}
