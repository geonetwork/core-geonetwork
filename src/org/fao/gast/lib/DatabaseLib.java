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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.BinaryFile;
import org.fao.gast.lib.druid.Import;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;

//=============================================================================

public class DatabaseLib
{
	//---------------------------------------------------------------------------
	//---
	//--- Interfaces
	//---
	//---------------------------------------------------------------------------

	public static interface CallBack
	{
		public void schemaObjects(int count);
		public void removed(String object, String type);
		public void cyclicRefs(List<String> objects);
		public void creating(String object, String type);
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

	/** Transactional */

	public void setup(Resource resource, CallBack cb) throws Exception
	{
		Dbms dbms = (Dbms) resource.open();

		try
		{
			removeObjects(dbms, cb);
			createSchema (dbms, cb);
			fillTables   (dbms, cb);
			setupSiteId  (dbms);
			setupVersion (dbms);

			//--- the commit is needed by subsequent addTemplates method
			dbms.commit();

			addTemplates(dbms);
			Lib.metadata.clearIndexes();

			resource.close();
		}
		catch(Exception e)
		{
			resource.abort();
			throw e;
		}
	}

	//---------------------------------------------------------------------------
	/** Transactional */

	public void removeObjects(Dbms dbms, CallBack cb)
									 throws FileNotFoundException, IOException
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		//--- step 1 : collect objects to remove

		ArrayList<ObjectInfo> objects = new ArrayList<ObjectInfo>();

		for (String row : schema)
			if (row.toUpperCase().startsWith("CREATE "))
			{
				ObjectInfo oi = new ObjectInfo();
				oi.name = getObjectName(row);
				oi.type = getObjectType(row);

				if (!oi.type.toLowerCase().equals("index"))
					objects.add(oi);
			}

		//--- step 2 : remove objects

		if (cb != null)
			cb.schemaObjects(objects.size());

		while(true)
		{
			boolean removed = false;

			for (Iterator<ObjectInfo> i=objects.iterator(); i.hasNext();)
			{
				ObjectInfo oi    = i.next();
				String     query = "DROP "+ oi.type +" "+ oi.name;

				if (safeExecute(dbms, query))
				{
					removed = true;
					i.remove();

					if (cb != null)
						cb.removed(oi.name, oi.type);
				}
			}

			if (objects.size() == 0)
				return;

			//--- if no object was removed then we have a cyclic loop

			if (!removed)
			{
				if (cb != null)
				{
					ArrayList<String> al = new ArrayList<String>();

					for (ObjectInfo oi : objects)
						al.add(oi.name);

					cb.cyclicRefs(al);
				}

				return;
			}
		}
	}

	//---------------------------------------------------------------------------
	/** Transactional */

	public void createSchema(Dbms dbms, CallBack cb) throws 	FileNotFoundException,
																				IOException, SQLException
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		StringBuffer sb = new StringBuffer();

		try
		{
			for (String row : schema)
				if (!row.toUpperCase().startsWith("REM") && !row.startsWith("--") && !row.trim().equals(""))
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
						dbms.commit();
						sb = new StringBuffer();
					}
				}
		}
		catch(SQLException e)
		{
			dbms.abort();
			throw e;
		}
	}

	//---------------------------------------------------------------------------
	/** Transactional */

	public void fillTables(Dbms dbms, CallBack cb) throws Exception
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		try
		{
			for(String row : schema)
				if (row.toUpperCase().startsWith("CREATE TABLE "))
				{
					String table = getObjectName(row);
					String file  = appPath +SETUP_DIR+ "/db/"+ table +".ddf";

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
						dbms.commit();
					}
				}
		}
		catch(Exception e)
		{
			dbms.abort();
			throw e;
		}
	}

	//---------------------------------------------------------------------------
	/** NOT Transactional */

	public String getSetting(Dbms dbms, String path) throws SQLException
	{
		String query = "SELECT id, value FROM Settings WHERE parentId=? AND name=?";

		StringTokenizer st = new StringTokenizer(path , "/");

		int    parent = 0;
		String value  = null;

		while (st.hasMoreTokens())
		{
			String name = st.nextToken();
			List   list  = dbms.select(query, parent, name).getChildren();

			if (list.size() == 0)
				return null;

			Element sett = (Element) list.get(0);

			parent = Integer.parseInt(sett.getChildText("id"));
			value  = sett.getChildText("value");
		}

		return value;
	}

	//---------------------------------------------------------------------------

	public String getSiteId(Dbms dbms) throws SQLException
	{
		String  query = "SELECT value FROM Settings WHERE name='siteId'";
		List    list  = dbms.select(query).getChildren();
		Element rec   = (Element) list.get(0);

		return rec.getChildText("value");
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

		return Lib.text.load(appPath +SETUP_DIR+ "/sql/"+ file);
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

	private boolean safeExecute(Dbms dbms, String query)
	{
		try
		{
			dbms.execute(query);
			dbms.commit();

			return true;
		}
		catch (SQLException e)
		{
			dbms.abort();

			return false;
		}
	}

	//---------------------------------------------------------------------------

	private void addTemplates(Dbms dbms) throws Exception
	{
		String siteId = getSiteId(dbms);
		String siteURL= Lib.metadata.getSiteURL(dbms);
		String date   = new ISODate().toString();

		int serial = 1;

		File schemaDir = new File(appPath, "gast/setup/templates");

		for (File schema : Lib.io.scanDir(schemaDir))
			//--- skip '.svn' folders and other hidden files
			if (!schema.getName().startsWith("."))
				for (File temp : Lib.io.scanDir(schema, "xml"))
				{
					Document doc  = Lib.xml.load(temp);
					String   uuid = UUID.randomUUID().toString();
					Element  xml  = Lib.metadata.updateFixedInfo(serial+"", doc.getRootElement(),
																			  uuid, date, schema.getName(), siteURL);

					XmlSerializer.insert(dbms, schema.getName(), xml, serial,
											   siteId, uuid, date, date, null, "y", null);

					setupTemplatePriv(dbms, serial);
					dbms.commit();
					serial++;
				}
	}

	//---------------------------------------------------------------------------

	private void setupTemplatePriv(Dbms dbms, int id) throws SQLException
	{
		String query = "INSERT INTO OperationAllowed(groupId, metadataId, operationId) "+
							"VALUES(?, ?, ?)";

		dbms.execute(query, 1, id, AccessManager.OPER_VIEW);
	}

	//---------------------------------------------------------------------------

	private void setupSiteId(Dbms dbms) throws SQLException, IOException
	{
		String uuid = UUID.randomUUID().toString();

		//--- duplicate dummy logo to reflect the uuid

		FileInputStream  is = new FileInputStream (appPath +"/gast/images/dummy.png");
		FileOutputStream os = new FileOutputStream(appPath +"/web/images/logos/"+ uuid +".png");
		BinaryFile.copy(is, os, true, true);

		dbms.execute("UPDATE Settings SET value=? WHERE name='siteId'", uuid);
	}

	//---------------------------------------------------------------------------

	private void setupVersion(Dbms dbms) throws SQLException
	{
		String version    = Lib.server.getVersion();
		String subVersion = Lib.server.getSubVersion();

		dbms.execute("UPDATE Settings SET value=? WHERE name='version'",    version);
		dbms.execute("UPDATE Settings SET value=? WHERE name='subVersion'", subVersion);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private String appPath;

	//---------------------------------------------------------------------------

	private static final String SETUP_DIR = "/gast/setup";
}

//=============================================================================

class ObjectInfo
{
	public String name;
	public String type;
}

//=============================================================================

