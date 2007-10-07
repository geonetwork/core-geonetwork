package org.wfp.vam.intermap.services.map.layers;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import java.util.List;
import java.util.Iterator;
import org.wfp.vam.intermap.services.map.MapUtil;
import jeeves.utils.Xml;

//=============================================================================

/** main.result service. shows search results
  */

public class Add implements Service
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
		//int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		String serviceName = params.getChildText(Constants.MAP_SERVICE);

		//Element response = new Element("response");
		//response.addContent(new Element("status").setAttribute("services", "true"));

		MapMerger mm = MapUtil.getMapMerger(context);

		// Set a flag to indicate that no services were in the MapMerger
		boolean mmWasEmpty = false;
		if (mm.size() == 0)
			mmWasEmpty = true;

		MapUtil.addService(2, serverUrl, serviceName, null, mm);

		// Calculate the starting BoudingBox if flag is set
		if (mmWasEmpty)
			MapUtil.setDefBoundingBox(mm);

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null) {
//			System.out.println("defaultImageSize = " + MapUtil.getDefaultImageSize()); // DEBUG
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());
		}

//		System.out.println("mm" + Xml.getString(mm.toElement()));

		return null;
	}

}

//=============================================================================

