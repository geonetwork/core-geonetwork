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

package org.fao.geonet.services.ownership;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//=============================================================================

public class OwnershipUtils
{

	public static List<Element> getOwnerUsers(ServiceContext context, UserSession us, Dbms dbms) throws SQLException
	{
		if (!us.isAuthenticated())
			return new ArrayList<Element>();


		String query = "SELECT DISTINCT Users.id, username, name, surname, profile FROM Users, Metadata WHERE owner=Users.id";

		@SuppressWarnings("unchecked")
        List<Element> list  = dbms.select(query).getChildren();

		return getUsers(context,us,dbms,list);
	}

	public static List<Element> getEditorUsers(ServiceContext context, UserSession us, Dbms dbms) throws SQLException
	{
		if (!us.isAuthenticated())
			return new ArrayList<Element>();

		String query = "SELECT DISTINCT id, username, name, surname, profile FROM Users WHERE profile not like 'RegisteredUser'";

		@SuppressWarnings("unchecked")
        List<Element>   list  = dbms.select(query).getChildren();

		return getUsers(context,us,dbms,list);
	}

	public static List<Element> getUsers(ServiceContext context, UserSession us, Dbms dbms, List<Element> list) throws SQLException
	{

		int id = us.getUserIdAsInt();

		if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			return list;

		//--- we have a user admin

		Set<String> hsMyGroups = getUserGroups(dbms, id);

		Set<String> profileSet = context.getProfileManager().getProfilesSet(us.getProfile());

		//--- now filter them

		List<Element> newList = new ArrayList<Element>();

		for (Element elRec : list)
		{
			String userId = elRec.getChildText("id");
			String profile= elRec.getChildText("profile");

			if (profileSet.contains(profile))
				if (hsMyGroups.containsAll(getUserGroups(dbms, Integer.parseInt(userId))))
					newList.add(elRec);
		}

		//--- return result

		return newList;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private static Set<String> getUserGroups(Dbms dbms, int id) throws SQLException
	{

		Set<String> hs = new HashSet<String>();

		String query = "SELECT groupId AS id FROM UserGroups WHERE userId=?";
		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query, id).getChildren();
		for (Element el : list) {
			hs.add(el.getChildText("id"));
		}

		return hs;
	}
}

//=============================================================================

