/**
 * ConcurrentHTTPTransactionsHandler.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.http;

import org.wfp.vam.intermap.http.cache.*;

import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import java.text.SimpleDateFormat;

public class ConcurrentHTTPTransactionHandler {
	private static String proxyHost;
	private static int proxyPort;

	private int timeout = 0; // timeout in milliseconds (0 is interpreted as infinfite timeout)
	private List<String> urisToGet = new ArrayList<String>();
	private boolean checkIfModified = true;
	private HashMap responses = new HashMap();
	private HttpCache cache;

//	public static void main(String[] args) { // DEBUG
//		ConcurrentHTTPTransactionHandler c = new ConcurrentHTTPTransactionHandler();
//
//		try {
//			c.setCache(new HttpGetFileCache("/ecchice", 1000));
//		} catch (Exception e) { e.printStackTrace(); }
//
//		c.checkIfModified(false);
//
//		c.register("http://vam.wfp.org/main");
//		c.register("http://www.apple.com");
//
//		c.doTransactions();
//		System.out.println("header: " + c.getHeaderValue("http://vam.wfp.org/main", "content-type"));
//
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {}
//
//		c.doTransactions();
//		System.out.println("header: " + c.getHeaderValue("http://vam.wfp.org/main", "content-type"));
//	}

	public static void setProxy(String proxyHost, int proxyPort) {
		ConcurrentHTTPTransactionHandler.proxyHost = proxyHost;
		ConcurrentHTTPTransactionHandler.proxyPort = proxyPort;
	}

	/**
	 * Registers an uri to the handler
	 *
	 * @param    uri                 a  String
	 *
	 */
	public void register(String uri) {
		urisToGet.add(uri);
	}

	public void setCache(HttpCache cache) {
		this.cache = cache;
	}

	public byte[] getResponse(String uri) {
		return (byte[])responses.get(uri);
	}

	public String getHeaderValue(String uri, String header) {
		return cache.getHeaderValue(uri, header);
	}

	public String getResponseFilePath(String uri) {
		return ((HttpGetFileCache)cache).getResponseFilePath(uri);
	}

	/**
	 * Starts the transactions
	 *
	 */
	public void doTransactions() {
		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		HostConfiguration hConf= httpClient.getHostConfiguration();
		if (proxyHost != null)
			hConf.setProxy(proxyHost, proxyPort);

		// create a thread for each URI
		HttpThread[] threads = new HttpThread[urisToGet.size()];
		for (int i = 0; i < threads.length; i++) {
			GetMethod get = new GetMethod((String)urisToGet.get(i));
			get.setFollowRedirects(true);
			threads[i] = new HttpThread(httpClient, get, i + 1, timeout, (String)urisToGet.get(i));
		}

		// start the threads
		for (int j = 0; j < threads.length; j++) {
			threads[j].start();
		}

		for(int j = 0; j < threads.length; j++) {
			try {
				(threads[j]).join();
			} catch (InterruptedException e) {}
		}

	}

	/**
	 * Sets the connection timeout
	 *
	 * @param    timeout             connection timeout in milliseconds; Zero is interpreted as an infinite timeout.
	 *
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the connection timeout
	 *
	 * @param    timeout             connection timeout in milliseconds; Zero is interpreted as an infinite timeout.
	 *
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * If true, check if the page has changed sending an If-Modified-Since HTTP Header;<br>
	 * If false, always send the cached response.
	 *
	 * @param    b                   a  boolean
	 *
	 */
	public void checkIfModified(boolean b) {
		this.checkIfModified = b;
	}


    /**
	 * A thread that performs a GET.
	 */
    private class HttpThread extends Thread {

		private HttpClient httpClient;
		private GetMethod method;
		private int id;
		private String uri;

//		private HttpThread(HttpClient httpClient, GetMethod method, int id) {
//			this.httpClient = httpClient;
//			this.method = method;
//			this.id = id;
//		}

//		private HttpThread(HttpClient httpClient, GetMethod method, int id, int timeout) {
//			this.httpClient = httpClient;
//			this.method = method;
//			this.id = id;
//			httpClient.setTimeout(timeout);
//		}

		private HttpThread(HttpClient httpClient, GetMethod method, int id, int timeout, String uri) {
			this.httpClient = httpClient;
			this.method = method;
			this.id = id;
			this.uri = uri;
			httpClient.setTimeout(timeout);
		}

		/**
		 * Executes the HttpMethod and prints some satus information.
		 */
		public void run() {
			try
			{
				String uri = method.getURI().toString();

				if (cache != null)
				{
					Calendar whenCached = cache.getCachedTime(uri);

					if (whenCached != null)
					{
						if ( ! checkIfModified )
							return;

						// the response has been cached, check if the page has been modified
						SimpleDateFormat f = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss z");
						Date date = whenCached.getTime();
						method.setRequestHeader( new Header("If-Modified-Since", f.format(date)) );
					}
				}

				long millis = System.currentTimeMillis();
				httpClient.executeMethod(method);

				int statusCode = method.getStatusCode();
//				System.out.println("status code: " + statusCode);
				if (statusCode == 304) { // only possible if cache != null
					// the page has not been modified - use cache
//					System.out.println(id + " - using cache");
					return;
				} else {
					// document not cached - fill cache first
					InputStream is = method.getResponseBodyAsStream();
					Header[] headers = method.getResponseHeaders();
					cache.put(this.uri, is, headers);
//					System.out.println(id + " - response:\n" + new String(bytes)); // DEBUG

					System.err.println("---("+(System.currentTimeMillis()-millis)+"ms)--> ENDED HTTP request to " + uri); // DEBUG ETj
					return;
				}

			} catch (Exception e) {
//				System.out.println(id + " - error: " + e);
				e.printStackTrace();
			} finally {
				// always release the connection after we're done
				method.releaseConnection();
//				System.out.println(id + " - connection released");
			}
		}

	} // GetThread class

}

