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

public class GetPrintImage implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		String imagename = mm.merge(Constants.PRINT_WIDTH, Constants.PRINT_HEIGHT);
		String url =MapUtil.getTempUrl() + "/" + imagename;
		String tool = MapUtil.getTool(context);

		// Sets the legends element, containing the URLs of the legends of all
		// The layers in the map
		Element legends = new Element("legends");
//		for (Enumeration e = mm.getServices(); e.hasMoreElements(); ) {
//			MapService ms = (MapService)e.nextElement();
		for(MapService service: mm.getServices())
		{
			String u = service.getLegendUrl();
			if ( u != null && u != "" )
				legends.addContent(new Element("legend").addContent(u));
		}

		// Bulid the XML response
		return new Element("response")
			.addContent(new Element(Constants.URL).setText(url))
			.addContent(new Element("tool").setText(tool))
			.addContent(mm.toElement())
			.addContent(legends);
	}

}

//=============================================================================

