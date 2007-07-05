package org.wfp.vam.intermap.services.map;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.util.Util;

//=============================================================================

/** main.result service. shows search results
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
		int width  = Util.parseInt(params.getChildText("width"), MapUtil.getImageWidth(context));
		int height = Util.parseInt(params.getChildText("height"), MapUtil.getImageHeight(context));

		MapMerger mm = MapUtil.getMapMerger(context);

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

