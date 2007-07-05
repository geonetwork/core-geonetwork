/**
 * HttpClient.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.wfp.vam.intermap.kernel.map.mapServices;

import java.io.*;
import java.util.*;
import java.net.*;

import org.jdom.*;

import jeeves.utils.Xml;

public class HttpClient {
	private static final int BUF_LEN = 1024;

	private URL url;
	private String user; // User id for HTTP authentication
	private String password; // Password for HTTP authentication
	private String request;
	private HttpURLConnection conn;
	private Hashtable htHeaders = new Hashtable(); // Contains all the response headers

	// Cache management
	private static Vector vUrl = new Vector();
	private static Vector vReq = new Vector();
	private static Vector vTime = new Vector();

	private boolean cached = false;

	public HttpClient(String url, String request) throws MalformedURLException
	{
		int pos = vUrl.indexOf(url);
		if (pos != -1 && vReq.get(pos).equals(request)) // DEBUG: Aggiungere il controllo sulla data!!!!!!!!!!
			cached = true;

		this.url = new URL(url);
		this.request = request;
	}

	public HttpClient(String url) throws MalformedURLException
	{
		this(url, null);
	}

	/** Sets the user id for HTTP basic authentication */
	public void setUser(String user) { this.user = user; }

	/** Set the password for HTTP basic authentication */
	public void setPassword(String password) { this.password = password; }

	/** Connexts to the remote server */
	private void connect() throws IOException {
		conn = (HttpURLConnection)url.openConnection();
		getResponseHeaders();
//		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK);
	}

	/** Disconnects from the server */
	public void disconnect() { conn.disconnect(); }

	/** Returns the response code from the server */
	public int getResponseCode() throws IOException { return conn.getResponseCode(); }

	/** Sets a request property */
	public void setRequestHeader(String key, String value) {
		conn.setRequestProperty(key, value);
	}

	/** Returns the HTTP response header with the given key */
	public String getResponseHeader(String key) {
		return (String)htHeaders.get(key.toLowerCase());
	}

	/**
	 * Sends the request and gets the response as a JDOM Element
	 *
	 * @return  the response as a JDOM Element
	 *
	 * @throws   IOException if a connection failure occurs
	 * @throws   JDOMException if a xml parsing error occurs
	 *
	 */
	public Element getElement() throws Exception
	{
		return  Xml.loadString(getString(), false); // DEBUG
	}

	/**
	 * Sends the request and gets the response as a String
	 *
	 * @return   the response as a String
	 *
	 * @throws   IOException if a connection failure occurs
	 *
	 */
	public String getString() throws IOException
	{
		sendRequest();

		// Get the response from the server
		InputStream is = conn.getInputStream();
		String response = "";
		byte[] buf = new byte[BUF_LEN];
		for (int n; (n = is.read(buf, 0, BUF_LEN)) > 0; )
			response += new String(buf, 0, n);

		try { is.close(); } catch (Exception e) {}

		return response;
	}

	/**
	 * Gets the image from the remote server, and save in the given file
	 *
	 * @return   the response as a String
	 *
	 * @throws   IOException if a connection failure occurs
	 *
	 */
	public void getFile(File f)
		throws IOException
	{
		sendRequest();

		BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));

		byte[] buf = new byte[BUF_LEN];
		for (int n; (n = is.read(buf, 0, BUF_LEN)) > 0; )
			os.write(buf, 0, n);

		// Close the streams
		try { is.close(); } catch (Exception e) {}
		try { os.close(); } catch (Exception e) {}
	}

	/**
	 * Send the request to the ArcIMS map server
	 *
	 * @throws   IOException if a connection failure occurs
	 *
	 */
	private void sendRequest()
		throws IOException
	{
		// Connect to the server if not already connected
		if (conn == null) connect();

		// Set the authentication parameters
		if (user != null && password != null) {
			sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
			String userPwd = encoder.encode((user + ":" + password).getBytes());
			conn.setRequestProperty("Authentication", "Basic " + userPwd);
		}

		if (request != null) {
			// Send the request to to the server
			conn.setDoOutput(true);
			BufferedOutputStream os = new BufferedOutputStream(conn.getOutputStream());

			os.write(request.getBytes());

			os.flush();
			try { os.close(); } catch (Exception e) {}
		}
	}

	/** Save headers in an Hashtable */
	private void getResponseHeaders() {
		String h;
		for (int i = 1; (h = conn.getHeaderField(i)) != null; i++)
			htHeaders.put(conn.getHeaderFieldKey(i).toLowerCase(), h);
	}

}

