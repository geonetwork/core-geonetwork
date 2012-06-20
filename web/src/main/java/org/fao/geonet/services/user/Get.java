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
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.util.LangUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.sql.SQLException;

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

		UserSession usrSess = context.getUserSession();
		if (!usrSess.isAuthenticated()){
            Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);
            return loadSharedUser(dbms,id);
		}

		String      myProfile = usrSess.getProfile();
		String      myUserId  = usrSess.getUserId();

		if (id == null) return new Element(Jeeves.Elem.RESPONSE);

		if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) || myProfile.equals("UserAdmin") || myUserId.equals(id)) {

			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			Element elUser = dbms.select ("SELECT * FROM Users WHERE id=?", Integer.valueOf(id));

			if(elUser.getChild("record") != null && elUser.getChild("record").getChildText("profile").equals(Geocat.Profile.SHARED)) {
				return loadSharedUser(dbms, id);
			}

		//--- retrieve user groups

			Element elGroups = new Element(Geonet.Elem.GROUPS);

			java.util.List list =dbms.select("SELECT groupId FROM UserGroups WHERE userId=?",Integer.valueOf(id)).getChildren();

			for(int i=0; i<list.size(); i++)
			{
				String grpId = ((Element)list.get(i)).getChildText("groupid");

				elGroups.addContent(new Element(Geonet.Elem.ID).setText(grpId));
			}

			if (!(myUserId.equals(id)) && myProfile.equals("UserAdmin")) {
			
		//--- retrieve session user groups and check to see whether this user is 
		//--- allowed to get this info

				java.util.List adminlist = dbms.select("SELECT groupId FROM UserGroups WHERE userId=? or userId=? group by groupId having count(*) > 1",Integer.valueOf(myUserId),Integer.valueOf(id)).getChildren();
				if (adminlist.size() == 0) {
					throw new IllegalArgumentException("You don't have rights to do this because the user you want to edit is not part of your group");
				}
			}

		//--- return data

			elUser.addContent(elGroups);
	        String[] elementsToResolve = { "organisation"};
	        LangUtils.resolveMultiLingualElements(elUser, elementsToResolve);
	        
			return elUser;
		} else {
            Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);
			return loadSharedUser(dbms,id);
		}

	}

    private Element loadSharedUser(Dbms dbms, String id) throws SQLException, IOException, JDOMException {

        Element elUser = dbms.select ("SELECT * FROM Users WHERE id=? and profile=?", Integer.valueOf(id),Geocat.Profile.SHARED);

        if(elUser.getChild("record")==null) {
        	return elUser;
        }
        
        Element elGroups = new Element(Geonet.Elem.GROUPS);

        java.util.List list =dbms.select("SELECT groupId FROM UserGroups WHERE userId=?", Integer.valueOf(id)).getChildren();

        for(int i=0; i<list.size(); i++)
        {
            String grpId = ((Element)list.get(i)).getChildText("groupid");

            elGroups.addContent(new Element(Geonet.Elem.ID).setText(grpId));
        }
        
        elUser.addContent(elGroups);
        
		//--- get the validity of the parent if it exists

		Element parentInfoId = elUser.getChild("record").getChild("parentinfo");
        if (parentInfoId.getTextTrim().length() > 0) {
            Integer integer = Integer.parseInt(parentInfoId.getTextTrim());
            Element validatedResults = dbms.select("SELECT validated FROM Users where id="+integer);

            Element elValidated = validatedResults.getChild("record").getChild("validated");

            if (elValidated != null){
                elValidated.detach();
                elValidated.setName("parentValidated");
                elUser.addContent(elValidated);
            }
		}

        //--- return data

        String[] elementsToResolve = { "organisation", "positionname", "orgacronym","onlinename","onlinedescription", "onlineresource", Geocat.Params.CONTACTINST};
        LangUtils.resolveMultiLingualElements(elUser, elementsToResolve);

        return elUser;
    }
}

//=============================================================================

