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
import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

/**
 * Creates a metadata copying data from a given template.
  */
public class Create implements Service {
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		
		String child = Util.getParam(params, Params.CHILD, "n");
		String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
		String id = "";
		String uuid = "";
		
		// does the request contain a UUID ?
		try {
			uuid = Util.getParam(params, Params.UUID);
			// lookup ID by UUID
			id = dm.getMetadataId(dbms, uuid);
		}
		catch(BadInputEx x) {
			try {
				id = Util.getParam(params, Params.ID);
				uuid = dm.getMetadataUuid(dbms, id);
			}
			// request does not contain ID
			catch(BadInputEx xx) {
				// give up
				throw new Exception("Request must contain a UUID or an ID");
			}		
		}
		
		//***
		// String groupOwner= Util.getParam(params, Params.GROUP);

		//--- query the data manager

		//***
		// String newId = dm.createMetadata(context, dbms, id, groupOwner, gc.getSiteId(),
        //        context.getUserSession().getUserId(), (child.equals("n")?null:uuid), isTemplate);

        //TODO let user select more than one group at md creation time
        String editGroup = Util.getParam(params, Params.GROUP);

        String newId = dm.createMetadata(context, dbms, id, editGroup, gc.getSiteId(), context.getUserSession().getUserId(),
												  (child.equals("n")?null:uuid), isTemplate);

        Element response = new Element(Jeeves.Elem.RESPONSE);
        response.addContent(new Element(Geonet.Elem.JUSTCREATED).setText("true"));
        response.addContent(new Element(Geonet.Elem.ID).setText(newId));
		return response;
	}
}
