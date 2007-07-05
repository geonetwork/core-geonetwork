package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;


//=============================================================================

/** main.result service. shows search results
  */

public class SetAoi implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int minx = Integer.parseInt(params.getChildText("minx"));
		int miny = Integer.parseInt(params.getChildText("miny"));
		int maxx = Integer.parseInt(params.getChildText("maxx"));
		int maxy = Integer.parseInt(params.getChildText("maxy"));
		
		BoundingBox bb = MapUtil.getMapMerger(context).getBoundingBox();
		
		int imageWidth = MapUtil.getImageWidth(context);
		int imageHeight = MapUtil.getImageHeight(context);
		
		float mapx = bb.getWest() + (bb.getEast() - bb.getWest()) * minx / imageWidth;
		float mapy = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * miny / imageHeight;
		float mapx2 = bb.getWest() + (bb.getEast() - bb.getWest()) * maxx / imageWidth;
		float mapy2 = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * maxy / imageHeight;
		
		MapUtil.setAoi(context, mapx, mapy, mapx2, mapy2);
		
		//System.out.println(MapUtil.getAoi(context)); // DEBUG
		
		return null;
	}
	
}

//=============================================================================

