package org.wfp.vam.intermap.services.map;

import java.util.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;

//=============================================================================

/** main.result service. shows search results
  */

public class ShowExtents implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText(Constants.MAP_SERVICE_ID));
//		System.out.println("Request: " + Xml.getString(request)); // DEBUG

		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		// Get the service Element
		WmsService s = (WmsService)mm.getService(id);
		Element elService = s.toElement();

		List extents = params.getChildren();
		for (Iterator i = extents.iterator(); i.hasNext(); ) {
			Element elExtent = (Element)i.next();
			String name = elExtent.getName();
			String value = elExtent.getText();
			if (!name.equals(Constants.MAP_SERVICE_ID)) {
				s.setExtent(name, value);
			}
		}

		Element response = new Element("response")
			.addContent(elService)
			.addContent(new Element("id").setText(id + ""))
			.addContent(MapUtil.getExtents(s));
		
		return response;
	}
	
}

//=============================================================================

