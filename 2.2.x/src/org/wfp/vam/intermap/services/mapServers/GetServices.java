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

package org.wfp.vam.intermap.services.mapServers;

import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.DefaultMapServers;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.ArcIMSClient;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.ArcIMSService;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.AxlRequestBuilder;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.CapabilitiesStore;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;

//=============================================================================

/** main.result service. shows search results
  */

public class GetServices implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id = params.getChildText("mapserver");
		String user = params.getChildText("user");
		String pwd = params.getChildText("password");
        Boolean forceCacheRefresh = "true".equals(params.getChildText("refreshCache"));

		System.out.println("MAPSERVER  ---> " + id);

		String serverUrl;
		int serverType;
		if (!id.startsWith("-")) { // Choosen from the list
			serverType = DefaultMapServers.getType(id);
			serverUrl = DefaultMapServers.getUrl(id);
		}
		else { // Manually inserted in the text field
			if (id.equals("-1"))
				serverType = ArcIMSService.TYPE;
			else
				serverType = WmsService.TYPE;

			serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		}

		System.out.println("URL  ---> " + serverUrl);
		System.out.println("TYPE ---> " + serverType);
        System.out.println("Force refreshCache ---> " + forceCacheRefresh);

		Element response = new Element("response")
			.addContent(new Element("url").setText(serverUrl))
			.addContent(new Element("type").setText(serverType + ""))
			.addContent(new Element("jscallback").setText(params.getChildText("jscallback")));

		switch (serverType)
		{
			case ArcIMSService.TYPE:
				// Build the request
				ArcIMSClient client = new ArcIMSClient(
					serverUrl, "catalog", AxlRequestBuilder.getRequest("getClientServices.xml") );
				// Set userId and password
				if (user != null && pwd != null) {
					client.setUser(user);
					client.setPassword(pwd);
				}
				try {
					// Get the service list
					response.addContent(client.getElement());
				}
				catch (Exception e) {
					throw new OperationAbortedEx("connect"); // TODO
//					response.setAttribute(new Attribute(Jeeves.ATTR_STATUS, Jeeves.STATUS_ERROR));
				}
				break;

			case WmsService.TYPE:
				try
				{
//					Element capabilities = WmsGetCapClient.getCapabilities(serverUrl, forceCacheRefresh);
					Element capabilities = CapabilitiesStore.getCapabilities(serverUrl, forceCacheRefresh);
					response.addContent(capabilities);
				}
				catch (Exception e) {
					throw new OperationAbortedEx("connect"); // TODO
//					response.setAttribute(new Attribute(Jeeves.ATTR_STATUS, Jeeves.STATUS_ERROR));
				}
				break;
			default:
				throw new Exception("Illegal map server type");
		}

		return response;
	}

}

//=============================================================================

