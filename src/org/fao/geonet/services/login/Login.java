//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.login;

import java.util.List;
import java.util.Vector;
import jeeves.exceptions.UserLoginEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.jdom.Element;

//=============================================================================

/** Try to login a user, checking the username and password
  */

public class Login implements Service
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
		String sUser = Util.getParam(params, Params.USERNAME);
		String sPass = Util.getParam(params, Params.PASSWORD);

		//--- attempt to load user from db

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Vector args = new Vector();
		args.add(sUser);
		args.add(sPass);

		Element elUser = dbms.select("SELECT * FROM Users WHERE username = ? AND password = ?", args);

		List list = elUser.getChildren();

		if (list.size() == 0)
			throw new UserLoginEx(sUser);
		else
		{
			elUser = (Element) list.get(0);

			String sId       = elUser.getChildText(Geonet.Elem.ID);
			String sName     = elUser.getChildText(Geonet.Elem.NAME);
			String sSurname  = elUser.getChildText(Geonet.Elem.SURNAME);
			String sProfile  = elUser.getChildText(Geonet.Elem.PROFILE);

			context.info("User '" + sUser + "' logged in.");

			context.getUserSession().authenticate(sId, sUser, sName, sSurname, sProfile);
		}

		return new Element("ok");
	}
}

//=============================================================================

