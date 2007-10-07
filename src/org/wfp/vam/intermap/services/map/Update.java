package org.wfp.vam.intermap.services.map;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.util.Util;

//=============================================================================

/**
  */

public class Update implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the current image size from the user session
		/*DEBUG*/if(params.getChildText("width") == null) System.out.println("\n\nNO WIDTH SPECIFIED IN Update()\n");

		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));

		MapMerger mm = MapUtil.getMapMerger(context);

		// Add default context if none exists
		if (mm.size() == 0) // No layers to merge
		{
			System.out.println("Update: SETTING DEFAULT CONTEXT");
			MapUtil.setDefaultContext(mm);
			// Update the user session
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		BoundingBox bb = Util.parseBoundingBox(params); // search bb in params
		if( bb != null)
			mm.setBoundingBox(bb);

		// Merge the images now
		String imagename = mm.merge(width, height);
		String url = MapUtil.getTempUrl() + "/" + imagename;

		return new Element("response")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement())
			.addContent(new Element("width").setText(""+width))
			.addContent(new Element("height").setText(""+height));
	}

}

//=============================================================================

