package org.wfp.vam.intermap.services.map;

import java.util.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;

//=============================================================================

/** main.result service. shows search results
  */

public class ZoomToService implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText("id"));
		
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);
		
//		MapUtil.setActiveLayer(params, mm);
//		MapUtil.setVisibleLayers(params, mm);
//		MapUtil.setTransparency(params, mm);
		
		MapService ms = mm.getService(id);
		mm.setBoundingBox(ms.getDefBoundingBox());
		
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		
		return null;
	}
	
}

//=============================================================================

