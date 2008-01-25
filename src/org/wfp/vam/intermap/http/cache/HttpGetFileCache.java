//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.wfp.vam.intermap.http.cache;

import java.io.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.Header;
import org.wfp.vam.intermap.kernel.CachedFiles;
import org.wfp.vam.intermap.kernel.TempFiles;

public class HttpGetFileCache implements HttpCache
{
//	private File directory;
	private static Map uriHt = new HashMap();
	private static Map headersHt = new HashMap();
	private TempFiles tf;

	// constructor
	public HttpGetFileCache(String servletEnginePath, String cacheDir, int expireTime) throws Exception {
		tf = new TempFiles(servletEnginePath, cacheDir, expireTime + 5);
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
//			System.out.println("name: " + loopheader.getName() + " = " + loopheader.getValue());
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

