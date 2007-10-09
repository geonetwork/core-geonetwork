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

package org.fao.geonet.services.login;

import java.sql.SQLException;
import java.util.List;
import jeeves.exceptions.UserLoginEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
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
		String username = Util.getParam(params, Params.USERNAME);
		String password = Util.getParam(params, Params.PASSWORD);

		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		LDAPContext lc = new LDAPContext(sm);

		if (!isAdmin(dbms, username) && lc.isInUse())
		{
			LDAPInfo info = lc.lookUp(username, password);

			if (info == null)
				throw new UserLoginEx(username);

			updateUser(context, dbms, info);
		}

		//--- attempt to load user from db

		String query = "SELECT * FROM Users WHERE username = ? AND password = ?";

		List list = dbms.select(query, username, Util.scramble(password)).getChildren();

		if (list.size() == 0)
			throw new UserLoginEx(username);

		Element user = (Element) list.get(0);

		String sId       = user.getChildText(Geonet.Elem.ID);
		String sName     = user.getChildText(Geonet.Elem.NAME);
		String sSurname  = user.getChildText(Geonet.Elem.SURNAME);
		String sProfile  = user.getChildText(Geonet.Elem.PROFILE);

		context.info("User '" + user + "' logged in.");
		context.getUserSession().authenticate(sId, username, sName, sSurname, sProfile);

		return new Element("ok");
	}

	//--------------------------------------------------------------------------

	private boolean isAdmin(Dbms dbms, String username) throws SQLException
	{
		String query = "SELECT id FROM Users WHERE username=? AND profile=?";

		List list = dbms.select(query, username, "Administrator").getChildren();

		return (list.size() != 0);
	}

	//--------------------------------------------------------------------------

	private void updateUser(ServiceContext context, Dbms dbms, LDAPInfo info) throws SQLException
	{
		//--- update user information into the database

		String query = "UPDATE Users SET password=?, name=?, profile=? WHERE username=?";

		int res = dbms.execute(query, Util.scramble(info.password), info.name, info.profile, info.username);

		//--- if the user was not found --> add it

		if (res == 0)
		{
			int id = context.getSerialFactory().getSerial(dbms, "Users");

			query = 	"INSERT INTO Users(id, username, password, surname, name, profile) "+
						"VALUES(?,?,?,?,?,?)";

			dbms.execute(query, id, info.username, Util.scramble(info.password), "(LDAP)", info.name, info.profile);
		}

		dbms.commit();
	}
}

//=============================================================================


