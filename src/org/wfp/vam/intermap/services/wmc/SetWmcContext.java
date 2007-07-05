package org.wfp.vam.intermap.services.wmc;

import java.util.*;

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

//=============================================================================

/** main.result service. shows search results
  */

public class SetWmcContext implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		Element mapContext = params.getChild("wmc_context");
		String stContext = mapContext.getText();
//		System.out.println("stContext = " + stContext); // DEBUG
		String decoded = URLDecoder.decode(stContext);

		mapContext = Xml.loadString(decoded, false);

//		WmcManager a = new WmcManager(mapContext);

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
			String vsp = layer.getChildText("vendor_spec_par"); // DEBUG
			
			try {
				MapUtil.addService(serverType, serverUrl, serviceName, vsp, mm);
			} catch (Exception e) { e.printStackTrace(); } // DEBUG
		}
		
		// set the bounding box
		Element elbb = context.getChild("General", ns).getChild("BoundingBox", ns);
		float minx = Float.parseFloat(elbb.getAttributeValue("minx"));
		float miny = Float.parseFloat(elbb.getAttributeValue("miny"));
		float maxx = Float.parseFloat(elbb.getAttributeValue("maxx"));
		float maxy = Float.parseFloat(elbb.getAttributeValue("maxy"));
		BoundingBox bb = new BoundingBox(maxx, minx, maxy, miny);
		mm.setBoundingBox(bb);
		
//		mm.setBoundingBox(new BoundingBox()); // DEBUG
		return mm;
	}
	
}

//=============================================================================
