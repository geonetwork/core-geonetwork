package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;

//=============================================================================

/** main.result service. shows search results
  */

public class ZoomOut implements Service
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
		int x = Integer.parseInt(params.getChildText(Constants.MAP_X));
		int y = Integer.parseInt(params.getChildText(Constants.MAP_Y));

		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		MapUtil.setActiveLayer(params, mm);
		MapUtil.setVisibleLayers(params, mm);

		// Get the BoundinbBox object from the user session
		BoundingBox bb = MapUtil.getMapMerger(context).getBoundingBox();

		// Move and zoom
		MapUtil.moveTo(bb, x, y, MapUtil.getImageWidth(context), MapUtil.getImageHeight(context));
		bb.zoom(1 / (float)2);

		context.getUserSession().setProperty(Constants.SESSION_TOOL, "zoomIn");

		return null;
	}
	
}

//=============================================================================

