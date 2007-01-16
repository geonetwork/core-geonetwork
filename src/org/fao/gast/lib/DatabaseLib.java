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
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import jeeves.resources.dbms.Dbms;
import org.fao.gast.lib.druid.Import;

//=============================================================================

public class DatabaseLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Interfaces
	//---
	//---------------------------------------------------------------------------

	public static interface RemoveCallBack
	{
	}

	//---------------------------------------------------------------------------

	public static interface CreateCallBack
	{
		public void creating(String object, String type);
	}

	//---------------------------------------------------------------------------

	public static interface FillCallBack
	{
		public void skipping(String table);
		public void filling (String table, String file);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public DatabaseLib(String appPath)
	{
		this.appPath = appPath;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void removeObjects(Dbms dbms)
	{
	}

	//---------------------------------------------------------------------------

	public void createSchema(Dbms dbms, CreateCallBack cb)
									 throws FileNotFoundException, IOException, SQLException
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		StringBuffer sb = new StringBuffer();

		for (String row : schema)
			if (!row.startsWith("REM") && !row.startsWith("--") && !row.equals(""))
			{
				sb.append(" ");
				sb.append(row);

				if (row.endsWith(";"))
				{
					String sql = sb.toString();

					sql = sql.substring(0, sql.length() -1);

					if (cb != null)
						cb.creating(getObjectName(sql), getObjectType(sql));

					dbms.execute(sql);
					sb = new StringBuffer();
				}
			}
	}

	//---------------------------------------------------------------------------

	public void fillTables(Dbms dbms, FillCallBack cb) throws FileNotFoundException, IOException, SQLException
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		for(String row : schema)
			if (row.startsWith("CREATE TABLE "))
			{
				String table = getObjectName(row);
				String file  = appPath +"/setup/db/"+ table +".ddf";

				if (!new File(file).exists())
				{
					if (cb != null)
						cb.skipping(table);
				}
				else
				{
					if (cb != null)
						cb.filling(table, file);
					Import.load(dbms.getConnection(), table, file);
				}
			}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private List<String> loadSchemaFile(String url) throws FileNotFoundException, IOException
	{
		//--- find out which dbms schema to load

		String file = "create-db-mckoi.sql";

		if (url.indexOf("oracle") != -1)
			file = "create-db-oracle.sql";

		else if (url.indexOf("mysql") != -1)
			file = "create-db-mysql.sql";

		else if (url.indexOf("postgresql") != -1)
			file = "create-db-postgres.sql";

		//--- load the dbms schema

		return Lib.text.load(appPath +"/setup/sql/"+file);
	}

	//---------------------------------------------------------------------------

	private String getObjectName(String createStatem)
	{
		StringTokenizer st = new StringTokenizer(createStatem, " ");
		st.nextToken();
		st.nextToken();

		return st.nextToken();
	}

	//---------------------------------------------------------------------------

	private String getObjectType(String createStatem)
	{
		StringTokenizer st = new StringTokenizer(createStatem, " ");
		st.nextToken();

		return st.nextToken();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String appPath;
}

//=============================================================================

