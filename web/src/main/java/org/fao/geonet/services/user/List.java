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
import jeeves.utils.Util;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;

import java.util.*;

//=============================================================================

/** Retrieves all users in the system
  */

public class List implements Service
{
    private Type type;
    
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {
	    this.type = Type.valueOf(params.getValue("type", "NORMAL"));
	}

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

		String userProfile = session.getProfile();
		Set<String> hsMyGroups = Collections.emptySet();
		
		if (userProfile != null) {
		    hsMyGroups = getGroups(dbms, session.getUserId(), userProfile);
		}
		Set profileSet = (userProfile == null) ?
							Collections.emptySet():context.getProfileManager().getProfilesSet(userProfile);

        boolean sortByValidated = "true".equalsIgnoreCase(Util.getParam(params, "sortByValidated", "false"));
        String sortBy;
        String sortVals;
        if(sortByValidated) {
            sortBy = "validAsInt, lname ASC";
            sortVals = "case when TRIM(name||surname) = '' then 'zz' else LOWER(name||surname) end as lname,case when validated = 'n' then 2 else 1 end as validAsInt,";
        } else {
            sortVals = "";
            sortBy = "username";
        }

        String profilesParam = params.getChildText(Params.PROFILE);
        String extraWhere;
        switch(type) {
        case NON_VALIDATED_SHARED:
            profileSet = Collections.singleton(Geocat.Profile.SHARED);
            extraWhere = " not validated='y' and profile='"+Geocat.Profile.SHARED+"'";
            break;
        case VALIDATED_SHARED:
            profileSet = Collections.singleton(Geocat.Profile.SHARED);
            extraWhere = " validated='y' and profile='"+Geocat.Profile.SHARED+"'";
            break;
        case SHARED:
            profileSet = Collections.singleton(Geocat.Profile.SHARED);
            extraWhere = " profile='"+Geocat.Profile.SHARED+"'";
            break;
        default:
            if( profilesParam!=null && profileSet.contains(profilesParam)){
                profileSet.retainAll(Collections.singleton(profilesParam));
            }
            extraWhere = " not profile='"+Geocat.Profile.SHARED+"'";                
            break;
        }

        String where = "WHERE"+extraWhere;
        String name = params.getChildText(Params.NAME);
		Element elUsers = null;

		if (name == null || name.trim().isEmpty())
    		//--- retrieve all users
			elUsers = dbms.select ("SELECT "+sortVals+"* FROM Users "+where+" ORDER BY " + sortBy);
		else {
			// TODO : Add organisation
			elUsers = dbms.select ("SELECT "+sortVals+"* FROM Users WHERE " + extraWhere
					+ " and (username ilike '%" + name + "%' "
                    + "or surname ilike '%" + name + "%' "
                    + "or email ilike '%" + name + "%' "
                    + "or organisation ilike '%" + name + "%' "
                    + "or orgacronym ilike '%" + name + "%' "
					+ "or name ilike '%" + name + "%') and publicaccess = 'y' "
					+ "ORDER BY "+sortBy);
		}

		//--- now filter them

		java.util.List<Element> alToRemove = new ArrayList<Element>();

		for(Iterator i=elUsers.getChildren().iterator(); i.hasNext(); )
		{
			Element elRec = (Element) i.next();

			String userId = elRec.getChildText("id");
			String profile= elRec.getChildText("profile");

			if (!hsMyGroups.containsAll(getGroups(dbms, userId, profile)))
				alToRemove.add(elRec);

			if (profileSet != null && !profileSet.contains(profile))
				alToRemove.add(elRec);
		}

		//--- remove unwanted users

		for (Element elem : alToRemove) elem.detach();

		ArrayList<Element> toResolve = new ArrayList(elUsers.getChildren());

		for (Element e : toResolve) {

            String[] elementsToResolve = { "organisation", "positionname", "orgacronym","onlinename","onlinedescription" };
            LangUtils.resolveMultiLingualElements(e, elementsToResolve);
        }


		//--- return result

		return elUsers;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private Set<String> getGroups(Dbms dbms, String id, String profile) throws Exception
	{
		Element groups;
		if (profile.equals(ProfileManager.ADMIN)) {
			groups = dbms.select("SELECT id FROM Groups");
		} else {
			groups = dbms.select("SELECT groupId AS id FROM UserGroups WHERE userId=?", new Integer(id));
		}

		java.util.List<Element> list = groups.getChildren();

		Set<String> hs = new HashSet<String>();

		for(Element el : list) {
			hs.add(el.getChildText("id"));
		}
        return hs;
    }

}

//=============================================================================

