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

package org.fao.geonet.services.category;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.util.ServiceMetadataReindexer;
import org.jdom.Element;

import java.util.List;

//=============================================================================

/** Removes a category from the system.
  */

public class Remove implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id = Util.getParam(params, Params.ID);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		int iId = new Integer(id);
		String query = "SELECT metadataId FROM MetadataCateg WHERE categoryId=?";

		List<Element> reindex = dbms.select(query, iId).getChildren();

		dbms.execute ("DELETE FROM MetadataCateg WHERE categoryId=?",iId);
		dbms.execute ("DELETE FROM CategoriesDes WHERE idDes=?",iId);
		dbms.execute ("DELETE FROM Categories    WHERE id=?",iId);

		//--- reindex affected metadata

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getDataManager();

		ServiceMetadataReindexer s = new ServiceMetadataReindexer(dm, dbms, reindex);
		s.process();

		return new Element(Jeeves.Elem.RESPONSE)
							.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.REMOVED));
	}
}

//=============================================================================

