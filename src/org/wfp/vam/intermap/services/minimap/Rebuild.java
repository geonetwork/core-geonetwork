package org.wfp.vam.intermap.services.minimap;


import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.services.map.MapUtil;

//=============================================================================

/** main.result service. shows search results
  */

public class Rebuild implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception
	{
	}


	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the current image size from the user session
//		int width = MapUtil.getImageWidth(context);
//		int height = MapUtil.getImageHeight(context);

		// Merge the images now
		MapMerger mm = MapUtil.getMapMerger(context);

		int w = MapUtil.getMiniMapWidth();
		int h = MapUtil.getMiniMapHeight();

		String imagename = mm.merge(w, h);
		String url = MapUtil.getTempUrl() + "/" + imagename;

		return new Element("response")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement()) // "extent"
			.addContent(new Element("width").setText(""+w))
			.addContent(new Element("height").setText(""+h));
	}

}

//=============================================================================

