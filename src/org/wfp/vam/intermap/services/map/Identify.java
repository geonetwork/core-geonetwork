package org.wfp.vam.intermap.services.map;

import java.util.*;

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

public class Identify implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}
	
	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------
	
	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get request parameters
		String activeLayer = params.getChildText("activeLayer");
		int x = Integer.parseInt(params.getChildText("mapimgx"));
		int y = Integer.parseInt(params.getChildText("mapimgy"));
		String responseFormat = params.getChildText(Constants.IDENTIFY_FORMAT);
		
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);
//		BoundingBox bb = mm.getBoundingBox();
		
		if ("".equals(activeLayer)) return null;
		
		int service = Integer.parseInt(activeLayer);
		MapService ms = mm.getService(service);
		ms.identify(0, x, y, MapUtil.getImageWidth(context), MapUtil.getImageHeight(context), Constants.PIXEL_TOLERANCE, responseFormat);
		return new Element("response").addContent(ms.toElement());
	}
	
}

//=============================================================================

