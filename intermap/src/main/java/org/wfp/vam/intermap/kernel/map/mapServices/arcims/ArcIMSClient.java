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

package org.wfp.vam.intermap.kernel.map.mapServices.arcims;

import java.net.*;
import java.io.*;

import org.jdom.*;

import jeeves.utils.Xml;

public class ArcIMSClient {
	private static final int BUF_SIZE = 1024;

	private String server; // Server name
	private String service; // Service name
	private String user =  null;
	private String password = null;
	private String customService = null;
	private Element request;

	private HttpURLConnection conn;

	public ArcIMSClient(String server, String service, String customService, Element request) {
		this.server = server;
		this.service = service;
		this.request = request;
		this.customService = customService;
	}

	public ArcIMSClient(String server, String service, Element request) {
		this(server, service, null, request);
	}

	/**
	 * Sets the user id for authentication
	 *
	 * @param    user                a  String
	 *
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Set the password for authentication
	 *
	 * @param    password            a  String
	 *
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sends the request and gets the response as a Jdom Element
	 *
	 * @return  the response as a Jdom Element
	 *
	 * @throws   IOException if a connection failure occurs
	 * @throws   JDOMException if a xml parsing error occurs
	 *
	 */
	public Element getElement() throws Exception
	{
		// Get the response from the server
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
		byte[] buf = new byte[BUF_SIZE];
		for (int n; (n = is.read(buf, 0, BUF_SIZE)) > 0; )
			response += new String(buf, 0, n);
		conn.disconnect();

		return response;
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
		try {
			String stUrl = server + "?ServiceName=" + service + "&ClientVersion=4.0";
			if (customService != null) {
				stUrl += "&CustomService=" + customService;
			}
			URL url = new URL(stUrl);
			
			// Connect to the server
			conn = (HttpURLConnection)url.openConnection();
			
			// Set the authentication parameters
			if (user != null && password != null) {
				sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
				String userPwd = encoder.encode((user + ":" + password).getBytes());
				conn.setRequestProperty("Authentication", "Basic " + userPwd);
			}
			
			// Send the request to the ArcIMS server
			conn.setDoOutput(true);
			BufferedOutputStream os = new BufferedOutputStream(conn.getOutputStream());
			
			// Send the request to the server
			os.write(Xml.getString(request).getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw(e);
		}
	}

}

