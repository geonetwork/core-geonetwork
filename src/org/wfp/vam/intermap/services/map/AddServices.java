package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import java.util.List;
import java.util.Iterator;

//=============================================================================

/** main.result service. shows search results
  */

public class AddServices implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// get some request parameters
		String serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		List<Element> lServices = params.getChildren(Constants.MAP_SERVICE);

		// if no services are specified then forward to GetServices
		if (lServices.size() == 0)
		{
			Element response = new Element("response");
			response.addContent(new Element("status").setAttribute("services", "false"));
			response.addContent(new Element("mapserver").setText("-" + serverType + ""));
			response.addContent(new Element("url").setText(serverUrl));

			return response;
		}
//		else
//			response.addContent(new Element("status").setAttribute("services", "true"));

		// Get the other request parameters
		String vsp = params.getChildText("vendor_spec_par"); // vendor specific parameters
		String bbox = params.getChildText("BBOX");

		MapMerger mm = MapUtil.getMapMerger(context);

		Element added = new Element("added");

		if (lServices.size() > 0)
		{
			// Get the MapMerger object from the user session

			// Set a flag to indicate that no services were in the MapMerger
			boolean mmWasEmpty = mm.size() == 0;

			// Create the Service objects and attach them to the MapMerge object
			for (Element eService : lServices)
			{
				String serviceName = eService.getText();
				if ( MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm) )
					added.addContent(new Element(serviceName));
			}

			// Set the bounding box as specified in the URL
			if (bbox != null)
			{
				MapUtil.setBBoxFromUrl(bbox, mm);
			}

			// Calculate the starting BoundingBox if flag is set
			else if (mmWasEmpty)
			{
				MapUtil.setDefBoundingBox(mm);
			}

			// Update the user session
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null) {
//			System.out.println("defaultImageSize = " + MapUtil.getDefaultImageSize()); // DEBUG
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());
		}

		return mm.toElementSimple()
						.addContent(added);
	}

}

//=============================================================================

