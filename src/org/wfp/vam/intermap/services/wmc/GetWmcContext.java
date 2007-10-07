package org.wfp.vam.intermap.services.wmc;

import java.net.URLEncoder;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.WmcCodec;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * @author ETj
 */

public class GetWmcContext implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int width  = Integer.parseInt(params.getChildText("width"));
		int height = Integer.parseInt(params.getChildText("height"));

		String title = params.getChildText("title"); // may be null, no probs

		MapMerger mm = MapUtil.getMapMerger(context);
		WMCViewContext vcd = WmcCodec.createViewContext(mm, title, width, height);

		Element xvcd = vcd.toElement();

		XMLOutputter xcomp = new XMLOutputter(Format.getCompactFormat());
		String comp = xcomp.outputString(xvcd);
		String enc1 = URLEncoder.encode(comp, "UTF-8");
		String enc2 = URLEncoder.encode(enc1, "UTF-8");

		return new Element("response")
			.addContent(xvcd)
			.addContent(new Element("wmcurl").setText(enc2));
	}
}



//=============================================================================
