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

//=============================================================================

/** main.result service. shows search results
  */

public class Main implements Service
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

		// Show the start page if no services selected
		Element response = new Element("response");
		if (mm.size() == 0) // No layers to merge
			response.addContent(new Element("status").setAttribute("empty", "true"));
			// throw new JeevesException("empty");
		else {
			// Get the current image size from the user session
			int width = MapUtil.getImageWidth(context);
			int height = MapUtil.getImageHeight(context);

			// Merge the images now, because errors in merging have to be reported
			// in the layers frame
			mm.merge(width, height);
			
			response.addContent(new Element("status").setAttribute("empty", "false"));
			response.addContent(new Element("layersRoot").addContent(layers(params, context)));
			response.addContent(new Element("mapRoot").addContent(map(params, context)));
		}

		return response;
	}
	
	public Element layers(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);
		
		if (mm.size() > 0)
			return new Element("response").setContent(mm.toElement());
		else
			return null;
	}
	
	public Element map(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		String url = MapUtil.getTempUrl() + "/" + mm.getImageName();

		String tool = MapUtil.getTool(context);

		return new Element("response")
			.addContent(new Element(Constants.URL).setText(url))
			.addContent(new Element("tool").setText(tool))
			.addContent(mm.toElement())
			.addContent(new Element("imageWidth").setText(MapUtil.getImageWidth(context) + ""))
			.addContent(new Element("imageHeight").setText(MapUtil.getImageHeight(context) + ""))
			.addContent(new Element("imageSize")
							.setText((String)context.getUserSession().getProperty(Constants.SESSION_SIZE)));
	}

}

//=============================================================================

