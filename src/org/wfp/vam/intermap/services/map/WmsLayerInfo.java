package org.wfp.vam.intermap.services.map;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.CapabilitiesStore;

//=============================================================================

/** main.result service. shows search results
  */

public class WmsLayerInfo implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		String serviceName = params.getChildText("name");

		return new Element("response")
			.addContent(CapabilitiesStore.getCapabilities(serverUrl))
			.addContent(new Element("serviceName").setText(serviceName));
	}

}

//=============================================================================

