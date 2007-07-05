package org.wfp.vam.intermap.services.geonet;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.constants.*;

import org.wfp.vam.intermap.kernel.Geonet;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import jeeves.utils.Xml;

//=============================================================================

/** This service returns all information needed to build the banner with XSL
  */

public class GetGeonetRecords implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Exec
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int minx = Integer.parseInt(params.getChildText("minx"));
		int miny = Integer.parseInt(params.getChildText("miny"));
		int maxx = Integer.parseInt(params.getChildText("maxx"));
		int maxy = Integer.parseInt(params.getChildText("maxy"));
		String sFrom = params.getChildText("from");
		String sTo   = params.getChildText("to");
		
		int from = (sFrom == null) ? -1 : Integer.parseInt(sFrom);
		int to   = (sTo == null) ? -1 : Integer.parseInt(sTo);
		
		BoundingBox bb = MapUtil.getMapMerger(context).getBoundingBox();
		
		int imageWidth = MapUtil.getImageWidth(context);
		int imageHeight = MapUtil.getImageHeight(context);
		
		float mapx  = bb.getWest() + (bb.getEast() - bb.getWest()) * minx / imageWidth;
		float mapy  = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * miny / imageHeight;
		float mapx2 = bb.getWest() + (bb.getEast() - bb.getWest()) * maxx / imageWidth;
		float mapy2 = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * maxy / imageHeight;
		
		//System.out.println(Xml.getString(Geonet.getGeonetRecords(mapx, mapy, mapx2, mapy2))); // DEBUG
		return Geonet.getGeonetRecords(mapx, mapy, mapx2, mapy2, from, to);
	}

}

//=============================================================================

