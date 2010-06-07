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

import java.util.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;

import org.wfp.vam.intermap.Constants;

//=============================================================================

/** main.result service. shows search results
  */

public class SetServices implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the request parameters
		int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		String serverUrl = params.getChildText(Constants.MAP_SERVER_URL);
		List lServices = params.getChildren(Constants.MAP_SERVICE);
		String vsp = params.getChildText("vendor_spec_par"); // vendor specific parameters
		String bbox = params.getChildText("BBOX");
		
//		System.out.println("vsp: " + vsp); // DEBUG
		
		if (lServices.size() > 0) {
			// Get the MapMerger object from the user session
			MapMerger mm = MapUtil.getMapMerger(context);

			// Set a flag to indicate that no services were in the MapMerger
			boolean flag = false;
			if (mm.size() == 0) flag = true;

			// Create the Service objects and attach them to the MapMerge object
			for (Iterator i = lServices.iterator(); i.hasNext(); ) {
				String serviceName = ((Element)i.next()).getText();
				MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
			}
			
			// Set the bounding box as specified in the URL
			if (bbox != null) {
				MapUtil.setBBoxFromUrl(bbox, mm);
			}
			
			// Calculate the starting BoudingBox if flag is set
			else if (flag) {
				MapUtil.setDefBoundingBox(mm);
			}
			
			// Update the user session
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null)
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());

		return null;
	}
	
}

//=============================================================================

