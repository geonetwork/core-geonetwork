package org.wfp.vam.intermap.http.cache;

import java.util.Calendar;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.Header;

public interface HttpCache {
	public void put(String uri, byte[] response, Header[] headers) throws IOException;
	
	public void put(String uri, InputStream response, Header[] headers) throws IOException;
	
	public byte[] get(String uri) throws IOException;
	
	public String getHeaderValue(String uri, String header);
	
	public Calendar getCachedTime(String uri);
	
	public void clear();
}
