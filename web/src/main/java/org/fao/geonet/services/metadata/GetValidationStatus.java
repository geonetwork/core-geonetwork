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
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.jdom.Content;
import org.jdom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//=============================================================================

/** Given a metadata id returns all associated status records. Called by the
  * metadata.status service
  */

public class GetValidationStatus implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();


        String metadataId = Utils.getIdentifierFromParameters(params, context);
        Lib.resource.checkPrivilege(context, metadataId, AccessManager.OPER_VIEW);

        @SuppressWarnings("unchecked")
        final List<Element> validDetails = dataMan.buildInfoElem(context, metadataId, null).getChildren("valid_details");

        //-----------------------------------------------------------------------
		//--- put it all together

		Element elRes = new Element(Jeeves.Elem.RESPONSE)
										.addContent(new Element(Geonet.Elem.ID).setText(metadataId));

        for (Element validationEl : validDetails) {
            elRes.addContent((Content) validationEl.clone());
        }

        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        boolean published = gc.getAccessManager().isVisibleToAll(dbms, metadataId);

        elRes.addContent(new Element("published").setText(Boolean.toString(published)));

		return elRes;
	}
}

//=============================================================================


