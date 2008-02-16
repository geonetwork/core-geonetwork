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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;
import org.wfp.vam.intermap.util.Util;

//=============================================================================

/** main.result service. shows search results
  */

public class Action implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String mapTool = params.getChildText("maptool");
		int mapimgx = Integer.parseInt(params.getChildText("mapimgx"));
		int mapimgy = Integer.parseInt(params.getChildText("mapimgy"));
		int mapimgx2 = Integer.parseInt(params.getChildText("mapimgx2"));
		int mapimgy2 = Integer.parseInt(params.getChildText("mapimgy2"));

//		int width  = Util.parseInt(params.getChildText("width"), MapUtil.getImageWidth(context));
//		int height = Util.parseInt(params.getChildText("height"), MapUtil.getImageHeight(context));
		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));

		MapMerger mm = MapUtil.getMapMerger(context);

		BoundingBox bb = Util.parseBoundingBox(params); // search bb in params
		if( bb == null)
			bb = mm.getBoundingBox();

		// zoom in
		if ("zoomin".equals(mapTool)) {
			if (mapimgx2 == mapimgx && mapimgy2 == mapimgy) {
				MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
				bb.zoom(2);
			}
			else {
				BoundingBox newbb = MapUtil.zoomInBox(bb, mapimgx, mapimgy, mapimgx2, mapimgy2, width, height);
				mm.setBoundingBox(newbb);
			}
		}

		// zoom out
		if ("zoomout".equals(mapTool)) {
			if (mapimgx2 == mapimgx && mapimgy2 == mapimgy) {
				MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
				bb.zoom(1 / (float)2);
			}
			else { // TODO
				BoundingBox newbb = MapUtil.zoomOutBox(bb, mapimgx, mapimgy, mapimgx2, mapimgy2, width, height);
				mm.setBoundingBox(newbb);
			}
		}

		// zoom out
		if ("pan".equals(mapTool)) {
			MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
		}

		if ("identify".equals(mapTool)) {
			// reanslate arguments to keep compatibility with previous versions
			params.addContent(new Element(Constants.MAP_X).setText("" + mapimgx));
			params.addContent(new Element(Constants.MAP_Y).setText("" + mapimgy));
			Identify identify = new Identify();
			return identify.exec(params, context);
		}

		// Merge the images now
		String imagename = mm.merge(width, height);
		String url = MapUtil.getTempUrl() + "/" + imagename;

		// Prepare response
		Element response = new Element("response")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement())
			.addContent(new Element("width").setText(""+width))
			.addContent(new Element("height").setText(""+height));

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);
		if(ms != null)
			response.addContent(ms.select(mm.getBoundingBox()).toElement());

		return response;
	}
}

//=============================================================================

