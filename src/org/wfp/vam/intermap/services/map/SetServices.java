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

public class SetServices implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the request parameters
		int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		String serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		List lServices = params.getChildren(Constants.MAP_SERVICE);
		String vsp = params.getChildText("vendor_spec_par"); // vendor specific parameters
		String bbox = params.getChildText("BBOX");
		
//		System.out.println("vsp: " + vsp); // DEBUG
		
		if (lServices.size() > 0) {
			// Get the MapMerger object from the user session
			MapMerger mm = MapUtil.getMapMerger(context);

			// Set a flag to indicate that no services were in the MapMerger
			boolean flag = false;
			if (mm.size() == 0) flag = true;

			// Create the Service objects and attach them to the MapMerge object
			for (Iterator i = lServices.iterator(); i.hasNext(); ) {
				String serviceName = ((Element)i.next()).getText();
				MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
			}
			
			// Set the bounding box as specified in the URL
			if (bbox != null) {
				MapUtil.setBBoxFromUrl(bbox, mm);
			}
			
			// Calculate the starting BoudingBox if flag is set
			else if (flag) {
				MapUtil.setDefBoundingBox(mm);
			}
			
			// Update the user session
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null)
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());

		return null;
	}
	
}

//=============================================================================

