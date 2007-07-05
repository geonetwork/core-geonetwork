package org.wfp.vam.intermap.services.map.layers;

import java.util.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.Constants;

//=============================================================================

/** main.result service. shows search results
  */

public class SetTransparency implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int layerId = Integer.parseInt(params.getChildText(Constants.MAP_SERVICE_ID));
		float transparency = Float.parseFloat(params.getChildText(Constants.TRANSPARENCY));
		
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);
		
		mm.setTransparency(layerId, transparency);

		return new Element("response").addContent(mm.getStructTransparencies());
//		return null;
	}
	
}

//=============================================================================

