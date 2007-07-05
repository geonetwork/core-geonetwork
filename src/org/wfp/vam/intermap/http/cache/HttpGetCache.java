/**
 * HttpGetCache
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.http.cache;

import java.io.*;
import java.util.*;
import org.apache.commons.httpclient.Header;

public class HttpGetCache implements HttpCache {
	
	/**
	 * Method put
	 *
	 * @param    uri                 a  String
	 * @param    response            an InputStream
	 * @param    headers             a  Header[]
	 *
	 * @exception   IOException
	 *
	 */
	public void put(String uri, InputStream response, Header[] headers) throws IOException {
		// TODO
	}
	
	
	/**
	 * Method getHeaderValue
	 *
	 * @param    uri                 a  String
	 * @param    header              a  String
	 *
	 * @return   a String
	 *
	 */
	public String getHeaderValue(String uri, String header) {
		// TODO
		return null;
	}
	
		
	private HashMap uris = new HashMap();
	private HashMap cachedTime = new HashMap();
	
	public void put(String uri, byte[] response, Header[] headers) {
		uris.put(uri, response);
		cachedTime.put(uri, Calendar.getInstance());
	}
	
	public byte[] get(String uri) {
		return (byte[])uris.get(uri);
	}
	
	public Calendar getCachedTime(String uri) {
		return (Calendar)cachedTime.get(uri);
	}
	
	public void clear() {
		uris.clear();
		cachedTime.clear();
	}
	
}

