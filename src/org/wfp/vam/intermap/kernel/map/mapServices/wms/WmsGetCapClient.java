/**
 * WmsGetCapClient.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

public class WmsGetCapClient
{
	/**
	 * Retrieves capabilities from a WMS server.
	 * Capabilities are handled by CapabilitiesStore, which provides also
	 * an optional caching mechanism.
	 *
	 * @param    serverUrl           the server URL
	 *
	 * @return   the getCapabilities response from the map server as a Jdom element
	 *
	 * @throws   IOException if a connection failure occurs
	 * @throws   JDOMException if a xml parsing error occurs
	 */

	/* package private */ static Element sendGetCapRequest(String serverUrl) throws IOException, Exception
	{
		Element capabilities = null;
		boolean jdomError = false;

		System.out.println("Sending getCapabilities request to " + serverUrl);

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
//System.out.println("CAP111 --> " + capabilities);
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
//System.out.println("CAP100 --> " + capabilities);
			conn.disconnect();
		}

		return capabilities;
	}

}

