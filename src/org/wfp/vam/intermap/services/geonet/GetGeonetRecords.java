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

		double mapx  = bb.getWest() + (bb.getEast() - bb.getWest()) * minx / imageWidth;
		double mapy  = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * miny / imageHeight;
		double mapx2 = bb.getWest() + (bb.getEast() - bb.getWest()) * maxx / imageWidth;
		double mapy2 = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * maxy / imageHeight;

		//System.out.println(Xml.getString(Geonet.getGeonetRecords(mapx, mapy, mapx2, mapy2))); // DEBUG
		return Geonet.getGeonetRecords(mapx, mapy, mapx2, mapy2, from, to);
	}

}

//=============================================================================

