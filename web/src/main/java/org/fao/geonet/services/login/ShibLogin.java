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

package org.fao.geonet.services.login;

import jeeves.exceptions.UserLoginEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.IDFactory;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

//=============================================================================

/**
 * <code>ShibLogin</code> processes the result of a Shibboleth (or other external 
 * authentication system) login. The user will have already been challenged for 
 * userid and password and will have had their credentials placed in the HTTP 
 * headers. These are then used to find or create the user's account.
 * 
 * @author James Dempsey <James.Dempsey@csiro.au>
 * @version $Revision: 1629 $
 */
public class ShibLogin implements Service
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

	/* (non-Javadoc)
	 * @see jeeves.interfaces.Service#exec(org.jdom.Element, jeeves.server.context.ServiceContext)
	 */
	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the header keys to lookup from the settings
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();
		String prefix = "system/shib";
		String usernameKey = sm.getValue(prefix + "/attrib/username");
		String surnameKey = sm.getValue(prefix + "/attrib/surname");
		String firstnameKey = sm.getValue(prefix + "/attrib/firstname");
		String profileKey = sm.getValue(prefix + "/attrib/profile");
        String groupKey = sm.getValue(prefix + "/attrib/group");
        String defGroup =  sm.getValue(prefix +"/defaultGroup");

		// Read in the data from the headers
		Map<String, String> headers = context.getHeaders();
		String username = Util.getHeader(headers, usernameKey, "");
		String surname = Util.getHeader(headers, surnameKey, "");
		String firstname = Util.getHeader(headers, firstnameKey, "");
		String profile = Util.getHeader(headers, profileKey, "");
        String group    = Util.getHeader(headers, groupKey, "");
	
		// Make sure the profile name is an exact match
		profile = context.getProfileManager().getCorrectCase(profile);
		if (profile.equals(""))
		{
			profile = ProfileManager.GUEST;
		}

        if (group.equals("")) {
            group = defGroup;
        }

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		// Create or update the user 
		if (username != null && username.length() > 0)
		{
			if (username.length() > 256) // only accept the first 256 chars
			{
				username = username.substring(0, 256);
			}
			updateUser(context, dbms, username, surname, firstname, profile, group);
		}

		//--- attempt to load user from db

		String query = "SELECT * FROM Users WHERE username = ? ";

		List list = dbms.select(query, username).getChildren();

		if (list.size() == 0)
			throw new UserLoginEx(username);

		Element user = (Element) list.get(0);

		String sId       = user.getChildText(Geonet.Elem.ID);
		String sName     = user.getChildText(Geonet.Elem.NAME);
		String sSurname  = user.getChildText(Geonet.Elem.SURNAME);
		String sProfile  = user.getChildText(Geonet.Elem.PROFILE);
		String sEmailAdd = user.getChildText(Geonet.Elem.EMAIL);

		context.info("User '"+ username +"' logged in as '"+ sProfile +"'");
		context.getUserSession().authenticate(sId, username, sName, sSurname, sProfile, sEmailAdd);

		return new Element("ok");
	}


	//--------------------------------------------------------------------------

	/**
	 * Update the user to match the provided details, or create a new record
	 * for them if they don't have one already.
	 * 
	 * @param context The Jeeves ServiceContext
	 * @param dbms The database connection.
	 * @param username The user's username, must not be null.
	 * @param surname The surname of the user
	 * @param firstname The first name of the user.
	 * @param profile The name of the user type.
	 * @throws SQLException If the record cannot be saved.
	 */
	private void updateUser(ServiceContext context, Dbms dbms, String username,
			String surname, String firstname, String profile, String group) throws SQLException
	{
        boolean groupProvided = ((group != null) && (!(group.equals(""))));
        String groupId = "-1";
        String userId = "-1";

        if (groupProvided) {
            String query = "SELECT id FROM Groups WHERE name=?";

            List list  = dbms.select(query, group).getChildren();

            if (list.isEmpty()) {
                groupId = IDFactory.newID();

                query = "INSERT INTO GROUPS(id, name) VALUES(?,?)";
                dbms.execute(query, groupId, group);
                Lib.local.insert(dbms, "Groups", groupId, group);

            } 
            else {
                groupId = ((Element) list.get(0)).getChildText("id");
            }
        }
		//--- update user information into the database

		String query = "UPDATE Users SET name=?, surname=?, profile=?, password=? WHERE username=?";

		int res = dbms.execute(query, firstname, surname, profile, "Via Shibboleth", username);

		//--- if the user was not found --> add it

		if (res == 0) {
			userId = IDFactory.newID();

			query = "INSERT INTO Users(id, username, name, surname, profile, password) VALUES(?,?,?,?,?,?)";

			dbms.execute(query, userId, username, firstname, surname, profile, "Via Shibboleth");

            if (groupProvided) {
                String query2 = "SELECT count(*) as numr FROM UserGroups WHERE groupId=? and userId=?";
                List list  = dbms.select(query2, groupId, userId).getChildren();

                String count = ((Element) list.get(0)).getChildText("numr");

                 if (count.equals("0")) {
                     query = "INSERT INTO UserGroups(userId, groupId) VALUES(?,?)";
                     dbms.execute(query, userId, groupId);
                 }
            }
		}

		dbms.commit();
	}

}
