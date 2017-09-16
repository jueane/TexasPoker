package poker.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JLog {
	public static boolean test = false;
	public static boolean debug = false;
	public static boolean info = true;
	public static boolean error = true;

	protected String flag = "common";

	FileUtil file;

	protected static String dateString;

	protected JLog() {
	}

	public JLog(String flag) {
		if (dateString == null) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			dateString = simpleDateFormat.format(new Date());
		}
		this.flag = flag;
		file = new FileUtil("logs/log_" + dateString + "/" + flag + ".txt");
	}

	public void test(String msg) {
		if (test) {
			jprint(msg);
		}
	}

	public void testln(String msg) {
		if (test) {
			jprintln(msg);
		}
	}

	public void debug(String msg) {
		if (debug) {
			jprint(msg);
		}
	}

	public void debugln(String msg) {
		if (debug) {
			jprintln(msg);
		}
	}

	public void info(String msg) {
		if (info) {
			jprint(msg);
		}
	}

	public void infoln(String msg) {
		if (info) {
			jprintln(msg);
		}
	}

	public void error(String msg) {
		if (error) {
			jprint(msg);
		}
	}

	public void errorln(String msg) {
		if (error) {
			jprintln(msg);
		}
	}

	// 换行
	public void debugln() {
		if (debug) {
			jprintemptyln();
		}
	}

	public void infoln() {
		if (info) {
			jprintemptyln();
		}
	}

	public void errorln() {
		if (error) {
			jprintemptyln();
		}
	}

	// 内部方法
	private void jprint(String msg) {		
		file.write(msg);
	}

	private void jprintln(String msg) {
		file.write(msg + "\r\n");
	}

	private void jprintemptyln() {
		file.write("\r\n");
	}

}
