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

import java.util.ArrayList;
import java.util.Vector;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

//=============================================================================

/** Update the information of a user
  */

public class Update implements Service
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
		String id       = params.getChildText(Params.ID);
		String username = Util.getParam(params, Params.USERNAME);
		String password = Util.getParam(params, Params.PASSWORD);
		String surname  = Util.getParam(params, Params.SURNAME);
		String name     = Util.getParam(params, Params.NAME);
		String profile  = Util.getParam(params, Params.PROFILE);
		String address  = Util.getParam(params, Params.ADDRESS, "");
		String state    = Util.getParam(params, Params.STATE,   "");
		String zip      = Util.getParam(params, Params.ZIP,     "");
		String country  = Util.getParam(params, Params.COUNTRY, "");
		String email    = Util.getParam(params, Params.EMAIL,   "");
		String organ    = Util.getParam(params, Params.ORG,     "");
		String kind     = Util.getParam(params, Params.KIND,    "");

		if (!context.getProfileManager().exists(profile))
			throw new Exception("Unkown profile : "+ profile);

		java.util.List listGroups = params.getChildren(Params.GROUPS);

		if (profile.equals(Geonet.Profile.ADMINISTRATOR))
			listGroups = new ArrayList();

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		if (id == null)	// For Adding new user
		{
			id = context.getSerialFactory().getSerial(dbms, "Users") +"";

			String query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
								"address, state, zip, country, email, organisation, kind) "+
								"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			dbms.execute(query, id, username, password, surname, name, profile,
									  address, state, zip, country, email, organ, kind);

			//--- add groups

			for(int i=0; i<listGroups.size(); i++)
			{
				String group = ((Element) listGroups.get(i)).getText();
				addGroup(dbms, id, group);
			}
		}

		else 	//--- For Update
		{
			String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, "+
								"profile=?, address=?, state=?, zip=?, country=?, email=?," +
								"organisation=?, kind=? WHERE id=?";

			dbms.execute (query, username, password, surname, name,
							 			profile, address, state, zip, country, email,
							 			organ, kind, id);

			//--- add groups

			dbms.execute("DELETE FROM UserGroups WHERE userId=?", id);

			for(int i=0; i<listGroups.size(); i++)
			{
				String group = ((Element) listGroups.get(i)).getText();
				addGroup(dbms, id, group);
			}
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Adds a user to a group
	  */

	private void addGroup(Dbms dbms, String user, String group) throws Exception
	{
		dbms.execute("INSERT INTO UserGroups(userId, groupId) VALUES (?, ?)", user, group);
	}
}

//=============================================================================

