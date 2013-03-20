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
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;
import java.util.List;

/**
 * Removes a user from the system. It removes the relationship to a group too.
 */
public class Remove extends NotInReadOnlyModeService {
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
		String id = Util.getParam(params, Params.ID);

		UserSession usrSess = context.getUserSession();
		String      myProfile = usrSess.getProfile();
		String      myUserId  = usrSess.getUserId();

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();

		if (myUserId.equals(id)) {
			throw new IllegalArgumentException("You cannot delete yourself from the user database");
		}

		int iId = Integer.parseInt(id);

		if (myProfile.equals(Geonet.Profile.ADMINISTRATOR) || myProfile.equals("UserAdmin"))  {

			Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

			if (myProfile.equals("UserAdmin")) {
				Element admin = dbms.select("SELECT groupId FROM UserGroups WHERE userId=? or userId=? group by groupId having count(*) > 1", new Integer(myUserId), iId);
				if (admin.getChildren().size() == 0) {
				  throw new IllegalArgumentException("You don't have rights to delete this user because the user is not part of your group");
				}
			}

			// Before processing DELETE check that the user is not referenced 
			// elsewhere in the GeoNetwork database - an exception is thrown if
			// this is the case
			if (dataMan.isUserMetadataOwner(dbms, iId)) {
				throw new IllegalArgumentException("Cannot delete a user that is also a metadata owner");
			}

			if (dataMan.isUserMetadataStatus(dbms, iId)) {
				throw new IllegalArgumentException("Cannot delete a user that has set a metadata status");
			}

			dbms.execute ("DELETE FROM UserGroups WHERE userId=?",iId);
			dbms.execute ("DELETE FROM Users      WHERE     id=?",iId);
		} else {
			throw new IllegalArgumentException("You don't have rights to delete this user");
		}

		return new Element(Jeeves.Elem.RESPONSE);
	}
}