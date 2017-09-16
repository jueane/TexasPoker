package poker.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/*
 * Description:无
 * Author:je
 * Date:2015年1月23日
 */
public class FileUtil {
	public static int filesize = 10000000;
	File file;
	FileOutputStream fos = null;
	String fullname;
	int fileno = 1;

	String writerLock = new String();

	public FileUtil(String fullpath) {
		this.fullname = fullpath;
		int pos = fullpath.lastIndexOf('/');
		String path = fullpath.substring(0, pos);
		file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(fullpath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

	}

	public void write(String text) {
		if (file.length() > filesize) {
			synchronized (writerLock) {
				String headName = fullname.substring(0, fullname.length() - 4);
				String tailName = fullname.substring(fullname.length() - 4);
				String newFilename = headName + "_" + fileno + tailName;
				file = new File(newFilename);
				try {
					fos.close();
					fos = new FileOutputStream(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fos.write(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
