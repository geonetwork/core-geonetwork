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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

//=============================================================================

public class Editors implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		UserSession   us   = context.getUserSession();
		List<Element> list = getUsers(context, us, dbms);

		Element result = new Element("root");

		for (Element user : list)
		{
			user = (Element) user.clone();
			user.removeChild("password");
			user.setName("editor");

			result.addContent(user);
		}

		return result;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private List<Element> getUsers(ServiceContext context, UserSession us, Dbms dbms) throws SQLException
	{
		if (!us.isAuthenticated())
			return new ArrayList<Element>();

		int id = Integer.parseInt(us.getUserId());

		String query = "SELECT DISTINCT Users.id, name, surname FROM Users, Metadata "+
							"WHERE owner=Users.id AND profile='Editor'";

		List   list  = dbms.select(query).getChildren();

		if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			return (List<Element>) list;

		//--- we have a user admin

		Set<String> hsMyGroups = getUserGroups(dbms, id);

		Set profileSet = context.getProfileManager().getProfilesSet(us.getProfile());

		//--- now filter them

		ArrayList<Element> newList = new ArrayList<Element>();

		for(Object o : list)
		{
			Element elRec = (Element) o;

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

	private Set<String> getUserGroups(Dbms dbms, int id) throws SQLException
	{
		String query = "SELECT groupId AS id FROM UserGroups WHERE userId=?";

		List list = dbms.select(query, id).getChildren();

		HashSet<String> hs = new HashSet<String>();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);
			hs.add(el.getChildText("id"));
		}

		return hs;
	}
}

//=============================================================================

