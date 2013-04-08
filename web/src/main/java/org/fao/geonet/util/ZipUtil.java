package org.fao.geonet.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip or unzip files
 * 
 */
public class ZipUtil {

	// Buffer size
	private static final int BUFFER = 8192;

	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[BUFFER];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	/**
	 * Recursive function Delete the specified repository and its content.
	 * 
	 * @param directory
	 */
	public static void deleteAllFiles(File directory) {

		String[] listFile = directory.list();
		for (int i = 0; i < listFile.length; i++) {
			File file = new File(directory.getPath() + "/" + listFile[i]);
			if (file.isDirectory()) {
				deleteAllFiles(file);
			} else if (file.exists())
				file.delete();
		}
		directory.delete();
	}

	/**
	 * Extracts a zip file to a specified directory.
	 * 
	 * @param zipFile
	 *            the zip file to extract
	 * @param toDir
	 *            the target directory
	 * @throws java.io.IOException
	 */
	public static void extract(ZipFile zipFile, File toDir) throws IOException {
		if (!toDir.exists()) {
			toDir.mkdirs();
		}
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if (zipEntry.isDirectory()) {
				File dir = new File(toDir, zipEntry.getName());
				if (!dir.exists()) { // make sure also empty directories get
										// created!
					dir.mkdirs();
				}
			} else {
				extract(zipFile, zipEntry, toDir);
			}
		}
	}

	/**
	 * Extracts an entry of a zip file to a specified directory.
	 * 
	 * @param zipFile
	 *            the zip file to extract from
	 * @param zipEntry
	 *            the entry of the zip file to extract
	 * @param toDir
	 *            the target directory
	 * @throws java.io.IOException
	 */
	public static void extract(ZipFile zipFile, ZipEntry zipEntry, File toDir)
			throws IOException {
		File file = new File(toDir, zipEntry.getName());
		File parentDir = file.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			InputStream istr = zipFile.getInputStream(zipEntry);
			bis = new BufferedInputStream(istr);
			FileOutputStream fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			copy(bis, bos);
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}

	/**
	 * Copies bytes from an InputStream to an OutputStream.
	 * 
	 * @param in
	 *            the InputStream
	 * @param out
	 *            the OutputStream
	 * @throws java.io.IOException
	 */
	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		int c;

		while ((c = in.read()) != -1) {
			out.write(c);
		}
	}

}
