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

import java.util.List;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;

//=============================================================================

/** main.result service. shows search results
  */

public class AddServices implements Service
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
		int serverType = Integer.parseInt(params.getChildText(Constants.MAP_SERVER_TYPE));
		List<Element> lServices = params.getChildren(Constants.MAP_SERVICE);

		// if no services are specified then forward to GetServices
		if (lServices.size() == 0)
		{
			Element response = new Element("response");
			response.addContent(new Element("status").setAttribute("services", "false"));
			response.addContent(new Element("mapserver").setText("-" + serverType + ""));
			response.addContent(new Element("url").setText(serverUrl));

			return response;
		}
//		else
//			response.addContent(new Element("status").setAttribute("services", "true"));

		// Get the other request parameters
		String vsp = params.getChildText("vendor_spec_par"); // vendor specific parameters
		String bbox = params.getChildText("BBOX");

		MapMerger mm = MapUtil.getMapMerger(context);

		Element added = new Element("added");

		if (lServices.size() > 0)
		{
			// Get the MapMerger object from the user session

			// Set a flag to indicate that no services were in the MapMerger
			boolean mmWasEmpty = mm.size() == 0;

			// Create the Service objects and attach them to the MapMerge object
			for (Element eService : lServices)
			{
				String serviceName = eService.getText();
				int servid = MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
				if ( servid != -1 )
				{
					//System.err.println("ADDING SERVICE " + serviceName + " @ " + servid);
					added.addContent(new Element("newLayer")
										 .setAttribute("name", serviceName)
										 .setAttribute("id", ""+servid)
									);
				}
			}

			// Set the bounding box as specified in the URL
			if (bbox != null)
			{
				MapUtil.setBBoxFromUrl(bbox, mm);
			}

			// Calculate the starting BoundingBox if flag is set
			else if (mmWasEmpty)
			{
				MapUtil.setDefBoundingBox(mm);
			}

			// Update the user session
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null) {
//			System.out.println("defaultImageSize = " + MapUtil.getDefaultImageSize()); // DEBUG
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());
		}
		
		//System.err.println(" --ADDED --> " + Xml.getString(added));
		return mm.toElementSimple().addContent(added);
	}

}

//=============================================================================

