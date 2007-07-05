package org.wfp.vam.intermap.services.map;

import java.util.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;

//=============================================================================

/** main.result service. shows search results
  */

public class SetContext implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText(Constants.CONTEXT_ID));

		// Create a new MapMerger object
		MapMerger mm = new MapMerger();

		// Get the element relating the choosen map context
		Element mapContext = DefaultMapServers.getContext(id);

		// Add each layer in the map context to the map
		List<Element> lServers = mapContext.getChildren("server");
		for (Element elServer: lServers)
		{
			String serverType = elServer.getAttributeValue(Constants.MAP_SERVER_TYPE);
			String serverUrl  = elServer.getAttributeValue(Constants.MAP_SERVER_URL);

			List<Element> elLayers = elServer.getChildren(Constants.MAP_LAYER);
			for (Element elLayer: elLayers)
			{
				try {
					String serviceName = elLayer.getAttributeValue("name");
					MapUtil.addService(Integer.parseInt(serverType), serverUrl, serviceName, "", mm);
				}
				catch (Exception e) { e.printStackTrace(); } // DEBUG: tell the user
			}
		}

		MapUtil.setDefBoundingBox(mm);

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null)
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		return null;
	}

}

//=============================================================================

