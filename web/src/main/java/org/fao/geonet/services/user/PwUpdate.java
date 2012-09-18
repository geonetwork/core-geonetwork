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
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

//=============================================================================

/** Update the password of logged user
  */

public class PwUpdate implements Service
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
		String password    = Util.getParam(params, Params.PASSWORD);
		String newPassword = Util.scramble(Util.getParam(params, Params.NEW_PASSWORD));

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		UserSession session = context.getUserSession();
		String      userId  = session.getUserId();

		if (userId == null) throw new UserNotFoundEx(null);

		// check valid user 
        Element elUser = dbms.select(	"SELECT * FROM Users WHERE id=?", userId);
		if (elUser.getChildren().size() == 0)
			throw new UserNotFoundEx(userId);

		// check old password
		String query = "SELECT * FROM Users WHERE id=? AND password=?";
		elUser = dbms.select(query, userId, Util.scramble(password));
		if (elUser.getChildren().size() == 0) {
			// Check old password hash method
			elUser = dbms.select(query, userId, Util.oldScramble(password));

			if (elUser.getChildren().size() == 0)
				throw new IllegalArgumentException("Old password is not correct");
		}
		
		// all ok so change password
		dbms.execute("UPDATE Users SET password=? WHERE id=?", newPassword, userId);

		return new Element(Jeeves.Elem.RESPONSE);
	}
}

//=============================================================================

