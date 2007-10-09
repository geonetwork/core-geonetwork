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

package org.wfp.vam.intermap.services.wmc;

import java.util.*;
import java.net.*;
import java.io.*;

import org.jdom.*;

import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.*;
import java.net.URLDecoder;
import jeeves.utils.Xml;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.services.map.MapUtil;
import org.wfp.vam.intermap.http.ConcurrentHTTPTransactionHandler;

//=============================================================================

/** main.result service. shows search results
  */

public class SetWmcFromURL implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	/**
	 * Method exec
	 *
	 * @param    params              an Element
	 * @param    context             a  ServiceContext
	 *
	 * @return   an Element
	 *
	 * @exception   Exception
	 *
	 */
	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String url = params.getChildText("url");
		
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
		Element mapContext = Xml.loadStream(is);
		conn.disconnect();
		
		String stContext = mapContext.getText();
		String decoded = URLDecoder.decode(stContext);

		mapContext = Xml.loadString(decoded, false);

		// Create a new MapMerger object
		MapMerger mm = addContextLayers(mapContext); // DEBUG

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null)
			context.getUserSession().setProperty(Constants.SESSION_SIZE, "small");

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		return new Element("response");
	}
	
	public static MapMerger addContextLayers(Element context) throws Exception
	{
		MapMerger mm = new MapMerger();

		// Add each layer in the context to the map
		Namespace ns = org.jdom.Namespace.getNamespace("http://www.opengeospatial.net/context");
		List layers = context.getChild("LayerList", ns).getChildren("Layer", ns);

		for (Iterator i = layers.iterator(); i.hasNext(); )
		{
			Element layer = (Element)i.next();

			int serverType = 2;
			Element olr = layer.getChild("Server", ns).getChild("OnlineResource", ns);

			Namespace linkNs = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
			String serverUrl = olr.getAttributeValue("href", linkNs);
			String serviceName = layer.getChildText("Name", ns);
			String vsp = layer.getChildText("vendor_spec_par"); // vendor specific parameters
			
			try {
				MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
			} catch (Exception e) { e.printStackTrace(); } // DEBUG
		}
		
		mm.setBoundingBox(new BoundingBox());
		return mm;
	}
	
}

//=============================================================================

