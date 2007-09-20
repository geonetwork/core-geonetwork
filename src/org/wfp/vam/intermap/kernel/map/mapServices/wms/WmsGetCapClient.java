/**
 * WmsGetCapClient.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.net.*;
import java.io.*;
import java.util.*;

import org.jdom.*;

import jeeves.utils.Xml;

public class WmsGetCapClient
{
	private static Hashtable htCapabilities = new Hashtable(); // Capabilities repository
	private static Hashtable htDates = new Hashtable(); // Last request date
	private static final int CACHE_TIME = 12;
	private static boolean useCache;
	
    public static Element getCapabilities(String serverUrl)
    throws Exception
    {
        return getCapabilities(serverUrl, false);
    }
    
	/**
	 * Get
	 *
	 * @param    serverUrl           the url of the map server
	 *
	 * @return   the getCapabilities response from the map server as a Jdom element
	 *
	 * @throws   IOException if a connection failure occurs
	 * @throws   JDOMException if a xml parsing error occurs
	 *
	 */
	public static Element getCapabilities(String serverUrl, boolean forceCacheRefresh)
		throws Exception
	{
		if (!useCache) {
			return sendGetCapRequest(serverUrl);
		}
		
		Element capabilities = null; // DEBUG

		// Calculate expiration date for the server
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, -CACHE_TIME);
		Calendar last = (Calendar)(htDates.get(serverUrl));

		if (last == null || c.after(last) || forceCacheRefresh) {
			capabilities = sendGetCapRequest(serverUrl);
			htCapabilities.put(serverUrl, capabilities);
			htDates.put(serverUrl, Calendar.getInstance());
		}
		else
			capabilities = (Element)htCapabilities.get(serverUrl);

		return (Element)capabilities.clone();
	}
	
	public static void useCache(boolean useCache) {
		WmsGetCapClient.useCache = useCache;
	}
	
	/**
	 * Method sendGetCapRequest
	 *
	 * @param    serverUrl           the server URL
	 *
	 * @return   the getCapabilities response from the map server as a Jdom element
	 *
	 * @throws   IOException if a connection failure occurs
	 * @throws   JDOMException if a xml parsing error occurs
	 *
	 */
	private static Element sendGetCapRequest(String serverUrl)
		throws Exception
	{
		Element capabilities = null;
		boolean jdomError = false;

		if (serverUrl.indexOf("?") == -1) serverUrl += "?";
		else if (!serverUrl.endsWith("?")) serverUrl += "&";

		try
		{
			//Request WMS version 1.1.1 by default
			//Changed 15-12-2004 by J. Ticheler: added the optional VERSION parameter
			URL u = new URL(serverUrl + "SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
			capabilities = Xml.loadStream(is);
			conn.disconnect();
		}
		catch (JDOMException e) {
			e.printStackTrace(); // DEBUG
			jdomError = true; // sometimes the error message is not well-formed XML
		}

		// For WMS 1.0.0 compliance a capabilities request should be sent
		// if the GetCapabilities request fails
		if (jdomError == true || capabilities.getChild("Capability").getChild("Error") != null) {
			URL u = new URL(serverUrl + "REQUEST=capabilities");
			HttpURLConnection conn = (HttpURLConnection)u.openConnection();
			BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
			capabilities = Xml.loadStream(is);
			conn.disconnect();
		}

		return capabilities;
	}

}

