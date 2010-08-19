//==============================================================================
//===
//===   McKoiDB
//===
//==============================================================================
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

package org.fao.geonet.util;

import com.mckoi.database.control.DBConfig;
import com.mckoi.database.control.DBController;
import com.mckoi.database.control.DBSystem;
import com.mckoi.database.control.DefaultDBConfig;
import com.mckoi.database.control.TCPJDBCServer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

//==============================================================================

public class McKoiDB
{
    private String        _configFile;
    private DBSystem      _database;
    private TCPJDBCServer _server;

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setConfigFile(String file)
	{
		_configFile = file;
	}

	//---------------------------------------------------------------------------

	public void start() throws Exception
	{
		DBConfig config = getDBConfig();

		DBController controller = DBController.getDefault();
		_database = controller.startDatabase(config);

		_server = new TCPJDBCServer(_database);
		_server.start();
	}

	//---------------------------------------------------------------------------

	public void start(String address) throws Exception
	{

		DBConfig config = getDBConfig();

		int port = Integer.parseInt(config.getValue("jdbc_server_port"));

		DBController controller = DBController.getDefault();
		_database = controller.startDatabase(config);

		_server = new TCPJDBCServer(_database, InetAddress.getByName(address), port);
		_server.start();
	}

	//---------------------------------------------------------------------------

	public void stop()
	{
		_server.stop();
		_database.close();
	}

	//---------------------------------------------------------------------------

	public void create(String username, String password) throws Exception
	{
		DBConfig config = getDBConfig();

		DBController controller = DBController.getDefault();
		DBSystem     database   = controller.createDatabase(config, username, password);

		database.close();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private DBConfig getDBConfig() throws Exception
	{
		if (_configFile == null)
			throw new NullPointerException("ConfigFile not specified.");

		File configFile = new File(_configFile).getAbsoluteFile();
		File rootFile   = configFile.getParentFile();

		DefaultDBConfig config = new DefaultDBConfig(rootFile);

		try
		{
			config.loadFromFile(configFile);
			return config;
		}
		catch (IOException ex)
		{
			throw new Exception("Unable to open the db.conf file", ex);
		}
    }
}

//==============================================================================

