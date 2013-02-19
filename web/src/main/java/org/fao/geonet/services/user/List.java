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

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//=============================================================================

/** Retrieves all users in the system
  */

public class List implements Service
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
		UserSession session = context.getUserSession();

		//--- retrieve groups for myself

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		HashSet hsMyGroups = getGroups(dbms, session.getUserId(), session.getProfile());

		Set profileSet = context.getProfileManager().getProfilesSet(session.getProfile());

		//--- retrieve all users

		Element elUsers = dbms.select ("SELECT * FROM Users ORDER BY username");

		//--- now filter them

		ArrayList alToRemove = new ArrayList();

		for(Iterator i=elUsers.getChildren().iterator(); i.hasNext(); )
		{
			Element elRec = (Element) i.next();

			String userId = elRec.getChildText("id");
			String profile= elRec.getChildText("profile");

			if (!hsMyGroups.containsAll(getGroups(dbms, userId, profile)))
				alToRemove.add(elRec);

			if (!profileSet.contains(profile))
				alToRemove.add(elRec);
		}

		//--- remove unwanted users

		for(int i=0; i<alToRemove.size(); i++)
			((Element) alToRemove.get(i)).detach();

		//--- return result

		return elUsers;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private HashSet getGroups(Dbms dbms, String id, String profile) throws Exception
	{
        Element groups;
        if (profile.equals(ProfileManager.ADMIN)) {
            groups = dbms.select("SELECT id FROM Groups");
        } else {
            groups = dbms.select("SELECT groupId AS id FROM UserGroups WHERE userId=?", new Integer(id));
        }

        java.util.List<Element> list = groups.getChildren();

		HashSet hs = new HashSet();

		for(int i=0; i<list.size(); i++)
		{
			Element el = (Element) list.get(i);
			hs.add(el.getChildText("id"));
		}

		return hs;
	}
}

//=============================================================================

