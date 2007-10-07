/**
 * GetInfo.java
 *
 */

package org.wfp.vam.intermap.services.map.layers;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLegendURL;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyle;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * Provides some info about styles of a given Layer.
 * We're exporting a DOM response not related to the WMS one, so that it does not depend on WMS version.
 *
 * @author ETj
 */
public class GetStyles implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int id = Integer.parseInt(params.getChildText(Constants.MAP_SERVICE_ID));
		MapMerger mm = MapUtil.getMapMerger(context);

		Element ret = new Element("response");
		Element eLayer = new Element("layer");
		ret.addContent(eLayer);

		MapService ms = mm.getService(id);

		if(ms instanceof WmsService)
		{
			WmsService ws = (WmsService)ms;
			WMSLayer wlayer = ws.getWmsLayer();

			eLayer.addContent(new Element("type").setText("WMS"));
			eLayer.addContent(new Element("id").setText(""+id));
			eLayer.addContent(new Element("name").setText(wlayer.getName()));
			eLayer.addContent(new Element("title").setText(wlayer.getTitle()));

			for(WMSStyle wstyle: wlayer.getStyleIterator())
			{
				Element estyle = new Element("style");
				ret.addContent(estyle);
				estyle.addContent(new Element("name").setText(wstyle.getName()));
				estyle.addContent(new Element("title").setText(wstyle.getTitle()));
				if( wstyle.getAbstract() != null)
					estyle.addContent(new Element("abstract").setText(wstyle.getAbstract()));

				if(wstyle.getName().equals(ws.getStyleName()))
					estyle.setAttribute("selected", "true");

				for(WMSLegendURL wlegend: wstyle.getLegendURLIterator())
				{
					Element elegend = new Element("legend");
					estyle.addContent(elegend);

					elegend.setAttribute("format", wlegend.getFormat().toString());
					elegend.addContent(new Element("href").setText(wlegend.getOnlineResource().getHref()));
				}
			}
		}
		else
		{
			eLayer.addContent(new Element("type").setText("ARCIMS"));
			eLayer.addContent(new Element("title").setText(ms.getTitle()));
		}

		return ret;
	}

}

