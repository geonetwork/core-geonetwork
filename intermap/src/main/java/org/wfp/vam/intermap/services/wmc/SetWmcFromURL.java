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

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.GeoRSSCodec;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl.WMCFactory;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCExtension;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.Utils;
import org.wfp.vam.intermap.kernel.marker.MarkerSet;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * Set the WMC from an URL-retrieved document
 *
 * @author ETj
 */
public class SetWmcFromURL implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String url = params.getChildText("url");

		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
		Element mapContext = Xml.loadStream(is);
		conn.disconnect();

//		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
//		System.out.println(" ============= wmc is:\n\n" +xo.outputString(mapContext));

		// Create a new MapMerger object
		String sreplace  = params.getChildText("clear");
		boolean breplace = Utils.getBooleanAttrib(sreplace, true);

		MapMerger mm = breplace?
							new MapMerger():
							MapUtil.getMapMerger(context);

		WMCViewContext vc = WMCFactory.parseViewContext(mapContext);
		WMCWindow win = vc.getGeneral().getWindow();

		String imgurl = MapUtil.setContext(mm, vc);

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		// load markerset, if any, from context
		WMCExtension ext = vc.getGeneral().getExtension();
		if(ext != null)
		{
			Element georss = ext.getChild("georss");
			if(georss != null)
			{
				Element feed = (Element)georss.getChildren().get(0);
				MarkerSet ms = GeoRSSCodec.parseGeoRSS(feed);
				context.getUserSession().setProperty(Constants.SESSION_MARKERSET, ms);
			}
		}

		// Prepare response
		Element response = new Element("response")
			.addContent(new Element("imgUrl").setText(imgurl))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement())
			.addContent(new Element("width").setText("" + win.getWidth()))
			.addContent(new Element("height").setText("" + win.getHeight()));

		MarkerSet ms = (MarkerSet)context.getUserSession().getProperty(Constants.SESSION_MARKERSET);
		if(ms != null)
			response.addContent(ms.select(mm.getBoundingBox()).toElement());

		return response;
	}
}

//
// 2007 11 22 ETj : following there is the old version of this service.
// It handles some non-standard elements, and is deprecated.
// I leave it here in case we need to restore it in a hurry, but it has to be removed entirely.
//

//=============================================================================

/** main.result service. shows search results
  */

//public class SetWmcFromURL implements Service
//{
//	public void init(String appPath, ServiceConfig config) throws Exception {}
//
//	//--------------------------------------------------------------------------
//	//---
//	//--- Service
//	//---
//	//--------------------------------------------------------------------------
//
//	/**
//	 * Method exec
//	 *
//	 * @param    params              an Element
//	 * @param    context             a  ServiceContext
//	 *
//	 * @return   an Element
//	 *
//	 * @exception   Exception
//	 *
//	 */
//	public Element exec(Element params, ServiceContext context) throws Exception
//	{
//		String url = params.getChildText("url");
//
//		URL u = new URL(url);
//		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
//		BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
//		Element mapContext = Xml.loadStream(is);
//		conn.disconnect();
//
//		String stContext = mapContext.getText();
//		String decoded = URLDecoder.decode(stContext);
//
//		mapContext = Xml.loadString(decoded, false);
//
//		// Create a new MapMerger object
//		MapMerger mm = addContextLayers(mapContext); // DEBUG
//
//		// Set image size if not set
//		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
//		if (size == null)
//			context.getUserSession().setProperty(Constants.SESSION_SIZE, "small");
//
//		// Update the user session
//		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
//
//		return new Element("response");
//	}
//
//	public static MapMerger addContextLayers(Element context) throws Exception
//	{
//		MapMerger mm = new MapMerger();
//
//		// Add each layer in the context to the map
//		Namespace ns = org.jdom.Namespace.getNamespace("http://www.opengeospatial.net/context");
//		List layers = context.getChild("LayerList", ns).getChildren("Layer", ns);
//
//		for (Iterator i = layers.iterator(); i.hasNext(); )
//		{
//			Element layer = (Element)i.next();
//
//			int serverType = 2;
//			Element olr = layer.getChild("Server", ns).getChild("OnlineResource", ns);
//
//			Namespace linkNs = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
//			String serverUrl = olr.getAttributeValue("href", linkNs);
//			String serviceName = layer.getChildText("Name", ns);
//			String vsp = layer.getChildText("vendor_spec_par"); // vendor specific parameters
//
//			try {
//				MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
//			} catch (Exception e) { e.printStackTrace(); } // DEBUG
//		}
//
//		mm.setBoundingBox(new BoundingBox());
//		return mm;
//	}
//
//}

//=============================================================================

