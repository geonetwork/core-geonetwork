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

/** main.result service. shows search results
  */

public class Action implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String mapTool = params.getChildText("maptool");
		int mapimgx = Integer.parseInt(params.getChildText("mapimgx"));
		int mapimgy = Integer.parseInt(params.getChildText("mapimgy"));
		int mapimgx2 = Integer.parseInt(params.getChildText("mapimgx2"));
		int mapimgy2 = Integer.parseInt(params.getChildText("mapimgy2"));
		
//		int width  = Util.parseInt(params.getChildText("width"), MapUtil.getImageWidth(context));
//		int height = Util.parseInt(params.getChildText("height"), MapUtil.getImageHeight(context));
		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));
		
		MapMerger mm = MapUtil.getMapMerger(context);

		BoundingBox bb = Util.parseBoundingBox(params); // search bb in params
		if( bb == null)
			bb = mm.getBoundingBox();

		// zoom in
		if ("zoomin".equals(mapTool)) {
			if (mapimgx2 == mapimgx && mapimgy2 == mapimgy) {
				MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
				bb.zoom(2);
			}
			else {
				BoundingBox newbb = MapUtil.zoomInBox(bb, mapimgx, mapimgy, mapimgx2, mapimgy2, width, height);
				mm.setBoundingBox(newbb);
			}
		}

		// zoom out
		if ("zoomout".equals(mapTool)) {
			if (mapimgx2 == mapimgx && mapimgy2 == mapimgy) {
				MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
				bb.zoom(1 / (float)2);
			}
			else { // TODO
				BoundingBox newbb = MapUtil.zoomOutBox(bb, mapimgx, mapimgy, mapimgx2, mapimgy2, width, height);
				mm.setBoundingBox(newbb);
			}
		}

		// zoom out
		if ("pan".equals(mapTool)) {
			MapUtil.moveTo(bb, mapimgx, mapimgy, width, height);
		}

		if ("identify".equals(mapTool)) {
			// reanslate arguments to keep compatibility with previous versions
			params.addContent(new Element(Constants.MAP_X).setText("" + mapimgx));
			params.addContent(new Element(Constants.MAP_Y).setText("" + mapimgy));
			Identify identify = new Identify();
			return identify.exec(params, context);
		}

		// Get the current image size from the user session
		// 20070622 ETj: already defined
//		int width = MapUtil.getImageWidth(context);
//		int height = MapUtil.getImageHeight(context);

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

