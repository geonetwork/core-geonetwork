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

import jeeves.server.*;
import org.fao.geonet.constants.*;

import jeeves.constants.Jeeves;
import jeeves.exceptions.UserNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.jdom.Element;

//=============================================================================

/** Update the profile of logged user
  */

public class InfoUpdate implements Service
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
		String surname  = Util.getParam(params, Params.SURNAME);
		String name     = Util.getParam(params, Params.NAME);
		String address  = Util.getParam(params, Params.ADDRESS, "");
		String state    = Util.getParam(params, Params.STATE,   "");
		String zip      = Util.getParam(params, Params.ZIP,     "");
		String country  = Util.getParam(params, Params.COUNTRY, "");
		String email    = Util.getParam(params, Params.EMAIL,   "");
		String organ    = Util.getParam(params, Params.ORG,     "");
		String kind     = Util.getParam(params, Params.KIND,    "");

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

		// check old password
		UserSession session = context.getUserSession();
		String      userId  = session.getUserId();

		if (userId == null)
			throw new UserNotFoundEx(null);

		// change profile
		String query = "UPDATE Users SET surname=?, name=?, "+
							"address=?, state=?, zip=?, country=?, email=?," +
							"organisation=?, kind=? WHERE id=?";

		dbms.execute (query, surname, name,
									address, state, zip, country, email,
									organ, kind, new Integer(userId));

		return new Element(Jeeves.Elem.RESPONSE);
	}
}

//=============================================================================

