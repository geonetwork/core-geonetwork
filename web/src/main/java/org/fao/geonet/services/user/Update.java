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
import jeeves.utils.PasswordUtil;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashSet;

import javax.servlet.ServletContext;

/**
 * Update the information of a user.
 */
public class Update extends NotInReadOnlyModeService {
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

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
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
		
		java.util.List<Element> userGroups = params.getChildren(Params.GROUPS);

		if (!operation.equals(Params.Operation.RESETPW)) {
			if (!context.getProfileManager().exists(profile))
				throw new Exception("Unknown profile : "+ profile);

			if (profile.equals(Geonet.Profile.ADMINISTRATOR))
				userGroups = new ArrayList<Element>();
		}

		if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) ||
				myProfile.equals(Geonet.Profile.USER_ADMIN) ||
				myUserId.equals(id)) {

			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			// Before we do anything check (for UserAdmin) that they are not trying
			// to add a user to any group outside of their own - if they are then
			// raise an exception - this shouldn't happen unless someone has
			// constructed their own malicious URL!
			//
			if (operation.equals(Params.Operation.NEWUSER) || operation.equals(Params.Operation.EDITINFO)) {
				if (!(myUserId.equals(id)) && myProfile.equals("UserAdmin")) {
					Element grps = dbms.select("SELECT groupId from UserGroups WHERE userId=?", new Integer(myUserId));
					java.util.List<Element> myGroups = grps.getChildren();
					for(Element userGroup : userGroups) {
						String group = userGroup.getText();
						boolean found = false;
						for (Element myGroup : myGroups) {
							if (group.equals(myGroup.getChild("groupid").getText())) {
								found = true;
							}
						}
						if (!found) {
							throw new IllegalArgumentException("Tried to add group id "+group+" to user "+username+" - not allowed because you are not a member of that group!");	
						}
					}
				}
			}

		// -- For adding new user
			if (operation.equals(Params.Operation.NEWUSER)) {

				// check if the new username already exists - if so then don't do this
				String query= "SELECT * FROM Users WHERE username=?";
				Element usersTest = dbms.select(query, username);
				if (usersTest.getChildren().size() != 0) throw new IllegalArgumentException("User with username "+username+" already exists");

				id = context.getSerialFactory().getSerial(dbms, "Users") +"";

				query = "INSERT INTO Users (id, username, password, surname, name, profile, "+
							"address, city, state, zip, country, email, organisation, kind) "+
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				dbms.execute(query, new Integer(id), username, PasswordUtil.encode(context, password), surname, name, profile, address, city, state, zip, country, email, organ, kind);

				setUserGroups(id, profile, params, dbms);
			}

			else {

			// -- full update
				if (operation.equals(Params.Operation.FULLUPDATE)) {
					String query = "UPDATE Users SET username=?, password=?, surname=?, name=?, profile=?, address=?, city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";

					dbms.execute (query, username, PasswordUtil.encode(context,password), surname, name, profile, address, city, state, zip, country, email, organ, kind, new Integer(id));

					//--- add groups

					dbms.execute("DELETE FROM UserGroups WHERE userId=?", new Integer(id));

					setUserGroups(id, profile, params, dbms);

			// -- edit user info
				} else if (operation.equals(Params.Operation.EDITINFO)) {
					String query = "UPDATE Users SET username=?, surname=?, name=?, profile=?, address=?, city=?, state=?, zip=?, country=?, email=?, organisation=?, kind=? WHERE id=?";
					dbms.execute (query, username, surname, name, profile, address, city, state, zip, country, email, organ, kind, new Integer(id));
					//--- add groups
				
					dbms.execute ("DELETE FROM UserGroups WHERE userId=?", new Integer(id));
					setUserGroups(id, profile, params, dbms);

			// -- reset password
				} else if (operation.equals(Params.Operation.RESETPW)) {
					ServletContext servletContext = context.getServlet().getServletContext();
					PasswordUtil.updatePasswordWithNew(false, null, password, new Integer(id), servletContext, dbms);
				} else {
					throw new IllegalArgumentException("unknown user update operation "+operation);
				}
			} 
		} else {
			throw new IllegalArgumentException("You don't have rights to do this");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}

	private void setUserGroups(String id, String userProfile, Element params,
			Dbms dbms) throws Exception {
		String[] profiles = {Geonet.Profile.USER_ADMIN, Geonet.Profile.REVIEWER, Geonet.Profile.EDITOR, Geonet.Profile.REGISTERED_USER};
		java.util.Set<Integer> editingGroups = new java.util.HashSet<Integer>();
		int userId = new Integer(id);
		for (String profile : profiles) {
			java.util.List<Element> userGroups = params.getChildren(Params.GROUPS + '_' + profile);
			for(Element userGroup : userGroups) {
				String group = userGroup.getText();
				if (!group.equals("")) {
					int groupId = new Integer(group);
					
					// Combine all groups editor and reviewer groups
					if (profile.equals(Geonet.Profile.REVIEWER) || profile.equals(Geonet.Profile.EDITOR)) {
						editingGroups.add(groupId);
					}
					
					if (!profile.equals(Geonet.Profile.EDITOR)) {
						dbms.execute("INSERT INTO UserGroups(userId, groupId, profile) VALUES (?, ?, ?)",
							 userId, groupId, profile);
					}
				}
			}
		}
		
		
		// Save all editor groups
		for (Integer groupId : editingGroups) {
			dbms.execute("INSERT INTO UserGroups(userId, groupId, profile) VALUES (?, ?, ?)",
					 userId, groupId, Geonet.Profile.EDITOR);
		}
	}
	
	public static void addGroup(Dbms dbms, int userId, int groupId, String profile) throws Exception {
        dbms.execute("INSERT INTO UserGroups(userId, groupId, profile) VALUES (?, ?, ?)",
                 userId, groupId, profile);
    }
}