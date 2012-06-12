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

package org.fao.geonet.services.user;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

import java.util.ArrayList;

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
		String operation = Util.getParam(params, Params.OPERATION);
		String id       = params.getChildText(Params.ID);
		String username = Util.getParam(params, Params.USERNAME);
		String password = Util.getParam(params, Params.PASSWORD);
		String surname  = Util.getParam(params, Params.SURNAME, "");
		String name     = Util.getParam(params, Params.NAME,    "");
		String profile  = Util.getParam(params, Params.PROFILE);
		String address  = Util.getParam(params, Params.ADDRESS, "");
		String city     = Util.getParam(params, Params.CITY,    "");
		String state    = Util.getParam(params, Params.STATE,   "");
		String zip      = Util.getParam(params, Params.ZIP,     "");
		String country  = Util.getParam(params, Params.COUNTRY, "");
		String email    = Util.getParam(params, Params.EMAIL,   "");
		String organ    = Util.getParam(params, Params.ORG,     "");
		String kind     = Util.getParam(params, Params.KIND,    "");

		UserSession usrSess = context.getUserSession();
		String      myProfile = usrSess.getProfile();
		String      myUserId  = usrSess.getUserId();

		java.util.List listGroups = params.getChildren(Params.GROUPS);

		if (!operation.equals(Params.Operation.RESETPW)) {
			if (!context.getProfileManager().exists(profile))
				throw new Exception("Unknown profile : "+ profile);

			if (profile.equals(Geonet.Profile.ADMINISTRATOR))
				listGroups = new ArrayList();
		}

		if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) ||
				myProfile.equals("UserAdmin") ||
				myUserId.equals(id)) {


			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			// Before we do anything check (for UserAdmin) that they are not trying
			// to add a user to any group outside of their own - if they are then
			// raise an exception - this shouldn't happen unless someone has
			// constructed their own malicious URL!
			//
			if (operation.equals("newuser") || operation.equals("editinfo")) {
				if (!(myUserId.equals(id)) && myProfile.equals("UserAdmin")) {
                    Element bull = dbms.select("SELECT groupId from UserGroups WHERE userId=?", new Integer(myUserId));
                    java.util.List adminlist = bull.getChildren();
					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						Boolean found = false;
						for (int j=0;j<adminlist.size();j++) {
							String testGroup = ((Element) adminlist.get(j)).getChild("groupid").getText();
							System.out.println("Testing group "+group+" against "+testGroup);
							if (group.equals(testGroup)) {
								found = true;
							}
						}
						if (!found) {
							throw new IllegalArgumentException("tried to add group id "+group+" to user "+username+" - not allowed because you are not a member of that group!");	
						}
					}
				}
			}

		// -- For Adding new user
			if (operation.equals(Params.Operation.NEWUSER)) {
				id = context.getSerialFactory().getSerial(dbms, "Users") +"";

				String query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
							"address, city, state, zip, country, email, organisation, kind) "+
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				dbms.execute(query, new Integer(id), username, Util.scramble(password), surname, name, profile, address, city, state, zip, country, email, organ, kind);

			//--- add groups

				for(int i=0; i<listGroups.size(); i++) {
					String group = ((Element) listGroups.get(i)).getText();
					addGroup(dbms, id, group);
				}
			}

			else {

			// -- full update
				if (operation.equals(Params.Operation.FULLUPDATE)) {
					String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, profile=?, address=?, city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";

					dbms.execute (query, username, Util.scramble(password), surname, name, profile, address, city, state, zip, country, email, organ, kind, new Integer(id));

					//--- add groups

					dbms.execute("DELETE FROM UserGroups WHERE userId=?", new Integer(id));

					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						addGroup(dbms, id, group);
					}

			// -- edit user info
				} else if (operation.equals(Params.Operation.EDITINFO)) {
					String query = "UPDATE Users SET username=?, surname=?, name=?, profile=?, address=?, city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";
					dbms.execute (query, username, surname, name, profile, address, city, state, zip, country, email, organ, kind, new Integer(id));
					//--- add groups
				
					dbms.execute ("DELETE FROM UserGroups WHERE userId=" + id);
					for(int i=0; i<listGroups.size(); i++) {
						String group = ((Element) listGroups.get(i)).getText();
						addGroup(dbms, id, group);
					}

			// -- reset password
				} else if (operation.equals(Params.Operation.RESETPW)) {
					String query = "UPDATE Users SET password=? WHERE id=?";
					dbms.execute (query, Util.scramble(password),new Integer(id));
				} else {
					throw new IllegalArgumentException("unknown user update operation "+operation);
				}
			} 
		} else {
			throw new IllegalArgumentException("you don't have rights to do this");
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
		dbms.execute("INSERT INTO UserGroups(userId, groupId) VALUES (?, ?)",
						 new Integer(user), new Integer(group));
	}
}

//=============================================================================

