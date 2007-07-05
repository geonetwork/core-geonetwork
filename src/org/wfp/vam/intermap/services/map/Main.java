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

