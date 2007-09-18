package org.wfp.vam.intermap.services.mapServers;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.Constants;

import org.wfp.vam.intermap.kernel.map.*;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.*;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.*;

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

		Element response = new Element("response")
			.addContent(new Element("url").setText(serverUrl))
			.addContent(new Element("type").setText(serverType + ""))
			.addContent(new Element("jscallback").setText(params.getChildText("jscallback")));

		switch (serverType)
		{
			// ArcIMS Services
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
					throw new JeevesException("connect"); // TODO
//					response.setAttribute(new Attribute(Jeeves.ATTR_STATUS, Jeeves.STATUS_ERROR));
				}
				break;
			// WMS Services
			case WmsService.TYPE:
//				Element capabilities = null;
				try
				{
					Element capabilities = WmsGetCapClient.getCapabilities(serverUrl);
//					Element capabilities = CapabilitiesStore.getCapabilities(serverUrl);
					response.addContent(capabilities);
				}
				catch (Exception e) {
					throw new JeevesException("connect"); // TODO
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


