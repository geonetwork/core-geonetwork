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

