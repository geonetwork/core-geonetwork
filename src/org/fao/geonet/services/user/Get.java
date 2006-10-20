//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.user;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.resources.dbms.*;
import jeeves.server.*;
import jeeves.server.context.*;

import org.fao.geonet.constants.*;

//=============================================================================

/** Retrieves a particular user
  */

public class Get implements Service
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
		String id = params.getChildText(Params.ID);

		if (id == null)
			return new Element(Jeeves.Elem.RESPONSE);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		Element elUser = dbms.select ("SELECT * FROM Users WHERE id=" + id);

		//--- retrieve user groups

		Element elGroups = new Element(Geonet.Elem.GROUPS);

		java.util.List list =dbms.select("SELECT groupId FROM UserGroups WHERE userId=" + id).getChildren();

		for(int i=0; i<list.size(); i++)
		{
			String grpId = ((Element)list.get(i)).getChildText("groupid");

			elGroups.addContent(new Element(Geonet.Elem.ID).setText(grpId));
		}

		//--- return data

		elUser.addContent(elGroups);

		return elUser;
	}
}

//=============================================================================

