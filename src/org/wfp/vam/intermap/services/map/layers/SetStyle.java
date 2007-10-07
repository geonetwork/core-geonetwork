package org.wfp.vam.intermap.services.map.layers;


import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * @author ETj
 */

public class SetStyle implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int    layerId   = Integer.parseInt(params.getChildText(Constants.MAP_SERVICE_ID));
		String stylename = Util.getParam(params, "style");

		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);
		MapService ms = mm.getService(layerId);

		if(ms == null)
			throw new IllegalArgumentException("Unknown layer id");

		if( ! (ms instanceof WmsService) )
			throw new IllegalArgumentException("Bad service type");

		WmsService ws = (WmsService)ms;

		WMSLayer wlayer = ws.getWmsLayer();
		if( wlayer.getStyle(stylename) == null)
			throw new IllegalArgumentException("Unknown style name");

		ws.setStyleName(stylename);

		return new Element("response");
	}

}

//=============================================================================

