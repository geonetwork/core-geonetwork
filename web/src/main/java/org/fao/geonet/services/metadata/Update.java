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

package org.fao.geonet.services.metadata;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

//=============================================================================

/** For editing : update leaves information. Access is restricted
  */

public class Update implements Service
{
	private ServiceConfig config;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		config = params;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		EditUtils.preprocessUpdate(params, context);

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan = gc.getDataManager();
		UserSession		session = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id         = Util.getParam(params, Params.ID);
		String version    = Util.getParam(params, Params.VERSION);
		String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
		String showValidationErrors = 
						Util.getParam(params, Params.SHOWVALIDATIONERRORS, "false");
		String title      = params.getChildText(Params.TITLE);
		String data       = params.getChildText(Params.DATA);

		boolean finished = config.getValue(Params.FINISHED, "no").equals("yes");
		boolean forget   = config.getValue(Params.FORGET, "no").equals("yes");

		if (!forget) {
			if (data != null) {
				Element md = Xml.loadString(data, false);

				if (!dataMan.updateMetadata(context.getUserSession(), dbms, id, md, false, version, context.getLanguage()))
					throw new ConcurrentUpdateEx(id);
			} else {
				EditUtils.updateContent(params, context, false, true);
			}

			dataMan.setTemplate(dbms, Integer.parseInt(id), isTemplate, title);
		}

		//-----------------------------------------------------------------------
		//--- update element and return status

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
		elResp.addContent(new Element(Geonet.Elem.SHOWVALIDATIONERRORS)
													.setText(showValidationErrors));

		//--- if finished then remove the XML from the session
		if (finished) {
			dataMan.removeMetadataEmbedded(session, id);
		}

		return elResp;
	}
}

//=============================================================================

