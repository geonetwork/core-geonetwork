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

package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Logger;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

//=============================================================================

public class CswDispatcher implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------
	
	private Logger logger;
	
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		logger = context.getLogger();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		SettingManager settingMan = gc.getSettingManager();
		
		boolean cswEnable    = settingMan.getValueAsBool("system/csw/enable", false);
		
		Element response = new Element(Jeeves.Elem.RESPONSE);
		
		// FIXME : response should be more explicit, an exception should be return ?
		if (!cswEnable) {
			logger.info("CSW harvesting is disabled");
			Element info  = new Element("info").setText("CSW harvesting is disabled");
			response.addContent(info);
		} else
			response = gc.getCatalogDispatcher().dispatch(params, context);
		
		return response;
	}
}

//=============================================================================

