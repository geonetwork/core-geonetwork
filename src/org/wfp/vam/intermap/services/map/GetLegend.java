package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;

//=============================================================================

/** main.result service. shows search results
  */

public class GetLegend implements Service
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

		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		MapService ms = mm.getService(id);
		ms.getLegendUrl();
		return new Element("response").addContent(ms.toElement()); // ETj: why dont we simply use the value returned by getLegendURL()?
	}

}

//=============================================================================

