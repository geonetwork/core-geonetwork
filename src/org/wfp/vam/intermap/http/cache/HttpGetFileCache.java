/**
 * HttpGetFileCache
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.http.cache;

import java.io.*;
import java.util.*;

import org.wfp.vam.intermap.util.TempFiles;
import org.apache.commons.httpclient.Header;

public class HttpGetFileCache implements HttpCache {
	private File directory;
	private static HashMap uriHt = new HashMap();
	private static HashMap headersHt = new HashMap();
	private TempFiles tf;

	// constructor
	public HttpGetFileCache(String cacheDir, int expireTime) throws Exception {
		tf = new TempFiles(cacheDir, expireTime + 5);
	}

	public void put(String uri, byte[] response, Header[] headers) throws IOException {
		File f = tf.getFile();
		ByteArrayInputStream is = new ByteArrayInputStream(response);
		FileOutputStream os = new FileOutputStream(f);

		byte[] buf = new byte[1024];
		for (int nRead; (nRead = is.read(buf, 0, 1024)) > 0; )
			os.write(buf, 0, nRead);

		uriHt.put(uri, f.getAbsolutePath());
		headersHt.put(uri, headers);
	}

	public void put(String uri, InputStream is, Header[] headers) throws IOException {
		File f = tf.getFile();
		FileOutputStream os = new FileOutputStream(f);

		byte[] buf = new byte[1024];
		for (int nRead; (nRead = is.read(buf, 0, 1024)) > 0; )
			os.write(buf, 0, nRead);

		uriHt.put(uri, f.getAbsolutePath());
		headersHt.put(uri, headers);
	}

	public byte[] get(String uri) throws IOException {
		String name = (String)uriHt.get(uri);

		if (name == null) return null;

		File f = new File(name);

		if (!f.exists()) return null;

		FileInputStream is = new FileInputStream(f);
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		byte[] buf = new byte[1024];
		for (int nRead; (nRead = is.read(buf, 0, 1024)) > 0; )
			os.write(buf, 0, nRead);

		return os.toByteArray();
	}

	public String getResponseFilePath(String uri) {
		return (String)uriHt.get(uri);
	}

	public String getHeaderValue(String uri, String header) {
		Header[] h = (Header[])headersHt.get(uri);
		if (h == null) return null;

		for (Header loopheader: h) //int i = 0; i < h.length; i++) {
		{
			System.out.println("name: " + loopheader.getName() + " = " + loopheader.getValue());
			if (header.equalsIgnoreCase(loopheader.getName()))
				return loopheader.getValue();
		}
		return null;
	}

	public Calendar getCachedTime(String uri) {
		String name = (String)uriHt.get(uri);

		if (name == null) return null;

		File f = new File(name);
		if (!f.exists()) return null;

		Date t = new Date(f.lastModified());
		Calendar c = Calendar.getInstance();
		c.set(t.getYear(), t.getMonth(), t.getDate(), t.getHours(), t.getMinutes(), t.getSeconds());

		return c;
	}

	public void clear() {
		uriHt.clear();
	}

}

