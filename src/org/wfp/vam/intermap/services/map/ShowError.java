package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;

//=============================================================================

/** main.result service. shows search results
  */

public class ShowError implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_ID));

		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		return new Element("response")
			.addContent(mm.getService(id).toElement())
			.addContent(mm.getErrors());
	}
	
}

//=============================================================================

