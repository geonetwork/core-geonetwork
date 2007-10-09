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
