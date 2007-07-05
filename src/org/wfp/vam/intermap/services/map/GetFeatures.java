package org.wfp.vam.intermap.services.map;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.ArcIMSService;

//=============================================================================

/** main.result service. shows search results
  */

public class GetFeatures implements Service
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
		BoundingBox bb = mm.getBoundingBox();
		int width = MapUtil.getImageWidth(context);
		int height = MapUtil.getImageHeight(context);
		
		// Get the shapefile URL
		ArcIMSService s = (ArcIMSService)mm.getService(id);
		String shapeUrl = s.getShapefileUrl(bb, width, height);
		
		return new Element("response")
			.addContent(new Element(Constants.URL).setText(shapeUrl));
	}
	
}

//=============================================================================

