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

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;
import java.util.List;
import java.util.Iterator;
import org.wfp.vam.intermap.services.map.MapUtil;
import jeeves.utils.Xml;

//=============================================================================

/** main.result service. shows search results
  */

public class Add implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// get some request parameters
		String serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		//int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		String serviceName = params.getChildText(Constants.MAP_SERVICE);

		//Element response = new Element("response");
		//response.addContent(new Element("status").setAttribute("services", "true"));

		MapMerger mm = MapUtil.getMapMerger(context);

		// Set a flag to indicate that no services were in the MapMerger
		boolean mmWasEmpty = false;
		if (mm.size() == 0)
			mmWasEmpty = true;

		MapUtil.addService(2, serverUrl, serviceName, null, mm);

		// Calculate the starting BoudingBox if flag is set
		if (mmWasEmpty)
			MapUtil.setDefBoundingBox(mm);

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null) {
//			System.out.println("defaultImageSize = " + MapUtil.getDefaultImageSize()); // DEBUG
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());
		}

//		System.out.println("mm" + Xml.getString(mm.toElement()));

		return null;
	}

}

//=============================================================================

