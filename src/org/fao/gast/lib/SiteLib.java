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

package org.fao.gast.lib;

import java.io.File;
import java.sql.SQLException;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;

//=============================================================================

public class SiteLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public SiteLib() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getSiteId(Dbms dbms) throws SQLException
	{
		return Lib.database.getSetting(dbms, "system/site/siteId");
	}

	//--------------------------------------------------------------------------

	public String getSiteURL(Dbms dbms) throws SQLException
	{
		String host    = Lib.database.getSetting(dbms, "system/server/host");
		String port    = Lib.database.getSetting(dbms, "system/server/port");
		String servlet = Lib.embeddedSC.getServlet();

		String locService = "/"+ servlet +"/"+ Jeeves.Prefix.SERVICE +"/en";

		return "http://" + host + (port == "80" ? "" : ":" + port) + locService;
	}

	//---------------------------------------------------------------------------

	public void setSiteId(Dbms dbms, String uuid) throws SQLException
	{
		String oldUuid = getSiteId(dbms);
		String appPath = Lib.server.getAppPath();

		dbms.execute("UPDATE Metadata SET source=? WHERE isHarvested='n'", uuid);
		dbms.execute("UPDATE Settings SET value=?  WHERE name='siteId'",   uuid);
		dbms.execute("UPDATE Sources  SET uuid=?   WHERE uuid=?",          uuid, oldUuid);

		File srcImg = new File(appPath +"/web/geonetwork/images/logos/"+ oldUuid +".gif");
		File desImg = new File(appPath +"/web/geonetwork/images/logos/"+ uuid    +".gif");

		srcImg.renameTo(desImg);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

}

//=============================================================================

