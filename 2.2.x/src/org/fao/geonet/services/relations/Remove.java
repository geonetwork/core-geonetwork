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

package org.fao.geonet.services.relations;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.*;

import org.fao.geonet.constants.*;

//=============================================================================

/** Removes a user from the system. It removes the relationship to a group too
  */

public class Remove implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service to delete a related resource
	//--- 
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		int parentId = Util.getParamAsInt(params, Params.PARENT_ID);
        int childId = Util.getParamAsInt(params, Params.CHILD_ID);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		dbms.execute ("DELETE FROM Relations WHERE id=" + String.valueOf(parentId) 
		        + " AND relatedId=" + String.valueOf(childId));

		return new Element(Jeeves.Elem.RESPONSE);
	}
}

//=============================================================================

