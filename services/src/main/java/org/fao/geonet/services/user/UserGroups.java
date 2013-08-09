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
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

import java.util.List;

//=============================================================================

/** Retrieves the groups for a particular user
  */

public class UserGroups implements Service
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

		if (id == null) return new Element(Jeeves.Elem.RESPONSE);

		UserSession usrSess = context.getUserSession();
		String      myProfile = usrSess.getProfile();
		String      myUserId  = usrSess.getUserId();

		if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) || myProfile.equals(Geonet.Profile.USER_ADMIN) || myUserId.equals(id)) {

			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			// -- get the profile of the user id supplied
			String query= "SELECT * FROM Users WHERE id=?";
			@SuppressWarnings("unchecked")
            List<Element>  uList = dbms.select(query, Integer.valueOf(id)).getChildren();

			if (uList.size() == 0)
				throw new IllegalArgumentException("user "+id+" doesn't exist");

			Element theUser    = uList.get(0);
			String  theProfile = theUser.getChildText("profile");

			//--- retrieve user groups of the user id supplied
			Element elGroups = new Element(Geonet.Elem.GROUPS);
			Element theGroups;
			
            if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) && (theProfile.equals(Geonet.Profile.ADMINISTRATOR))) {
                theGroups = dbms.select("SELECT id, name, description, 'Administrator' AS profile FROM Groups");
            } else {
                theGroups = dbms.select("SELECT id, name, description, profile FROM UserGroups, Groups WHERE groupId=id AND userId=?",Integer.valueOf(id));
            }

			@SuppressWarnings("unchecked")
            List<Element> list = theGroups.getChildren();
			for (Element group : list) {
				group.setName("group");
				elGroups.addContent((Element)group.clone());
			}

			if (!(myUserId.equals(id)) && myProfile.equals(Geonet.Profile.USER_ADMIN)) {
			
		//--- retrieve session user groups and check to see whether this user is 
		//--- allowed to get this info

				String selectGroupIdQuery = "SELECT groupId FROM UserGroups WHERE userId=? or userId =?  group by groupId having count(*) > 1";
                @SuppressWarnings("unchecked")
                List<Element> adminlist = dbms.select(selectGroupIdQuery, Integer.valueOf(myUserId), Integer.valueOf(id)).getChildren();
				if (adminlist.size() == 0) {
					throw new OperationNotAllowedEx("You don't have rights to do this because the user you want is not part of your group");
				}
			}

		//--- return data

			return elGroups;
		} else {
			throw new OperationNotAllowedEx("You don't have rights to do get the groups for this user");
		}

	}
}

//=============================================================================

