package com.lingzerg.gamecenter.util;

public class JLog {

	public static boolean debug = true;
	public static boolean info = true;
	public static boolean error = true;

	public static void debug(String msg) {
		if (debug) {
			jprint(msg);
		}
	}

	public static void debugln(String msg) {
		if (debug) {
			jprintln(msg);
		}
	}

	public static void info(String msg) {
		if (info) {
			jprint(msg);
		}
	}

	public static void infoln(String msg) {
		if (info) {
			jprintln(msg);
		}
	}

	public static void error(String msg) {
		if (error) {
			jprint(msg);
		}
	}

	public static void errorln(String msg) {
		if (error) {
			jprintln(msg);
		}
	}

	// int型
	public static void debug(int msg) {
		if (debug) {
			jprint(msg);
		}
	}

	public static void debugln(int msg) {
		if (debug) {
			jprintln(msg);
		}
	}

	public static void info(int msg) {
		if (info) {
			jprint(msg);
		}
	}

	public static void infoln(int msg) {
		if (info) {
			jprintln(msg);
		}
	}

	public static void error(int msg) {
		if (error) {
			jprint(msg);
		}
	}

	public static void errorln(int msg) {
		if (error) {
			jprintln(msg);
		}
	}

	// 换行
	public static void debugln() {
		if (debug) {
			jprintemptyln();
		}
	}

	public static void infoln() {
		if (info) {
			jprintemptyln();
		}
	}

	public static void errorln() {
		if (error) {
			jprintemptyln();
		}
	}

	// 内部方法
	private static void jprint(String msg) {
		System.out.print(msg);
	}

	private static void jprint(int msg) {
		System.out.print(msg);
	}

	private static void jprintln(String msg) {
		System.out.println(msg);
	}

	private static void jprintln(int msg) {
		System.out.println(msg);
	}

	private static void jprintemptyln() {
		System.out.println();
	}

}
