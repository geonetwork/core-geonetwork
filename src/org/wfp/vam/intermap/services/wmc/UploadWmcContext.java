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

import java.io.File;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Params;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl.WMCFactory;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.Utils;
import org.wfp.vam.intermap.services.map.MapUtil;

/**
 * Set the WMC from a context file passed as file upload.
 *
 * @author Etj
 */
public class UploadWmcContext implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String uploadDir = context.getUploadDir();

		String fname = params.getChildText(Params.FNAME);
		if(fname == null)
			throw new IllegalArgumentException("Invalid file");

		File   file  = new File(uploadDir, fname);
		Element mapContext;
		try
		{
			mapContext = Xml.loadFile(file);
		}
		catch (JDOMException e)
		{
			throw new IllegalArgumentException("Error in parsing the context file");
		}

//		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
//		System.out.println(" ============= request wmc is:\n\n" +xo.outputString(mapContext));

		// Create a new MapMerger object
		String sreplace  = params.getChildText("clearLayers");
		boolean breplace = Utils.getBooleanAttrib(sreplace, true);

		MapMerger mm = breplace?
							new MapMerger():
							MapUtil.getMapMerger(context);

		WMCViewContext vc = WMCFactory.parseViewContext(mapContext);
		WMCWindow win = vc.getGeneral().getWindow();

		String url = MapUtil.setContext(mm, vc);

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

		return new Element("response")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(new Element("scale").setText(mm.getDistScale()))
			.addContent(mm.getBoundingBox().toElement())
			.addContent(new Element("width").setText("" + win.getWidth()))
			.addContent(new Element("height").setText("" + win.getHeight()));
	}


}

//=============================================================================
