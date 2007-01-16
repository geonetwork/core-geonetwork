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

package org.fao.gast.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fao.geonet.util.McKoiDB;

//=============================================================================

public class EmbeddedDBLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public EmbeddedDBLib(String appPath) throws IOException
	{
		this.appPath = appPath;

		lines = Lib.text.load(appPath + MCKOI_CONFIG);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getPort()
	{
		return Lib.text.getProperty(lines, "jdbc_server_port");
	}

	//---------------------------------------------------------------------------

	public String getUser()
	{
		try
		{
			List<String> lines = Lib.text.load(appPath + MCKOI_ACCOUNT);

			return Lib.text.getProperty(lines, "username");
		}
		catch (IOException e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public String getPassword()
	{
		try
		{
			List<String> lines = Lib.text.load(appPath + MCKOI_ACCOUNT);

			return Lib.text.getProperty(lines, "password");
		}
		catch (IOException e)
		{
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public void setPort(String port)
	{
		Lib.text.setProperty(lines, "jdbc_server_port", port);
	}

	//---------------------------------------------------------------------------

	public void save() throws FileNotFoundException, IOException
	{
		Lib.text.save(appPath + MCKOI_CONFIG, lines);
	}

	//---------------------------------------------------------------------------

	public void createDB() throws Exception
	{
		//--- first : remove old files

		Lib.io.cleanDir(new File(appPath + MCKOI_DATA));

		//--- second : generate a new random account

		String user = Lib.text.getRandomString(8);
		String pass = Lib.text.getRandomString(8);

		//--- third : save it to a file

		ArrayList<String> al = new ArrayList<String>();
		al.add("#--- DO NOT EDIT : file automatically generated");
		al.add("username="+ user);
		al.add("password="+ pass);

		Lib.text.save(appPath + MCKOI_ACCOUNT, al);

		//--- fourth : create database files

		String dbPath = appPath + MCKOI_CONFIG;

		McKoiDB mcKoi = new McKoiDB();
		mcKoi.setConfigFile(dbPath);

		mcKoi.create(user, pass);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String       appPath;
	private List<String> lines;

	private static final String MCKOI_CONFIG = "/web/WEB-INF/db/db.conf";
	private static final String MCKOI_ACCOUNT= "/web/WEB-INF/db/account.prop";
	private static final String MCKOI_DATA   = "/web/WEB-INF/db/data";
}

//=============================================================================

