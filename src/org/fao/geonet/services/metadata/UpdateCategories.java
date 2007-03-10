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


import java.util.*;
import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.*;

import org.fao.geonet.constants.*;
import org.fao.geonet.kernel.*;
import org.fao.geonet.*;

//=============================================================================

/** Stores all operations allowed for a metadata. Called by the metadata.admin service
  */

public class UpdateCategories implements Service
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

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- check access

		if (!dataMan.existsMetadata(dbms, id))
			throw new IllegalArgumentException("Metadata not found --> " + id);

		//-----------------------------------------------------------------------
		//--- remove old operations

		dataMan.deleteAllMetadataCateg(dbms, id);

		//-----------------------------------------------------------------------
		//--- set new ones

		List list = params.getChildren();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);

			String name = el.getName();

			if (name.startsWith("_"))
				dataMan.setCategory(dbms, id, name.substring(1));
		}

		//-----------------------------------------------------------------------
		//--- index metadata

		dataMan.indexMetadata(dbms, id);

		//-----------------------------------------------------------------------
		//--- return id for showing

		return new Element(Jeeves.Elem.RESPONSE)
							.addContent(new Element(Geonet.Elem.ID).setText(id));
	}
}

//=============================================================================

