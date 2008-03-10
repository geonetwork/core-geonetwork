//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

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

		return mm.toElementSimple();
	}

}

//=============================================================================

