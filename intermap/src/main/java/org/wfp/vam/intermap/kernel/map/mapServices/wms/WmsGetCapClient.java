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

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.wfp.vam.intermap.Constants;
import jeeves.utils.Log;
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

		Log.debug(Constants.WMS,"Sending getCapabilities request to" + serverUrl);
		
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

			Log.debug(Constants.WMS," - GetCapabilities request : "+ capabilities);
			
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

            Log.debug(Constants.WMS," - GetCapabilities request : "+ capabilities);

            conn.disconnect();
		}

		return capabilities;
	}

}

