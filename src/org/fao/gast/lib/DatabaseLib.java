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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.BinaryFile;
import org.fao.gast.lib.druid.Import;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
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

	public static interface Mapper
	{
		public Object map(String field, Object value);
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

			//--- needed for PostgreSQL
			dbms.commit();

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
	/** NOT Transactional */

	public void insert(Dbms dbms, String table, List records, String fields[],
							 Mapper mapper) throws SQLException
	{
		for(Object rec : records)
			insert(dbms, table, (Element) rec, fields, mapper);
	}

	//---------------------------------------------------------------------------
	/** NOT Transactional */

	public void insert(Dbms dbms, String table, Element rec, Mapper mapper) throws SQLException
	{
		Map<String, Object> fields = new HashMap<String, Object>();

		for(Object e : rec.getChildren())
		{
			Element elem = (Element) e;
			String  name = elem.getName();
			String  value= elem.getText();

			fields.put(name, value);
		}

		insert(dbms, table, fields, mapper);
	}

	//---------------------------------------------------------------------------
	/** NOT Transactional */

	public void insert(Dbms dbms, String table, Element rec, String fields[],
							 Mapper mapper) throws SQLException
	{
		Map<String, Object> map = new HashMap<String, Object>();

		for(String field : fields)
			map.put(field, rec.getChildText(field.toLowerCase()));

		insert(dbms, table, map, mapper);
	}

	//---------------------------------------------------------------------------
	/** NOT Transactional */

	public void insert(Dbms dbms, String table, Map<String,Object> fields,
							 Mapper mapper) throws SQLException
	{
		StringBuffer names = new StringBuffer();
		StringBuffer marks = new StringBuffer();

		ArrayList<Object> values = new ArrayList<Object>();

		for(Iterator<String> i=fields.keySet().iterator(); i.hasNext();)
		{
			String name = i.next();
			Object value= fields.get(name);

			names.append(name);
			marks.append("?");

			if (mapper != null)
				value = mapper.map(name, value);

			values.add(value);

			if (i.hasNext())
			{
				names.append(", ");
				marks.append(", ");
			}
		}

		String query = "INSERT INTO "+ table +"("+ names +") VALUES ("+ marks +")";

		dbms.execute(query, values.toArray());
	}

	//---------------------------------------------------------------------------

	public void insert(Dbms dbms, String table, Set<String> langs, String id,
							 String label) throws SQLException
	{
		Map<String, Object> hm = new HashMap<String, Object>();

		hm.put("idDes", new Integer(id));
		hm.put("label", label);

		for (String lang : langs)
		{
			hm.put("langId", lang);

			Lib.database.insert(dbms, table, hm, null);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private setup methods
	//---
	//---------------------------------------------------------------------------

	/** Transactional */

	private void removeObjects(Dbms dbms, CallBack cb)
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

	private boolean safeExecute(Dbms dbms, String query)
	{
		try
		{
			dbms.execute(query);

			//--- as far as I remember, PostgreSQL needs a commit even for DDL
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

	public void createSchema(Dbms dbms, CallBack cb) throws 	FileNotFoundException,
																				IOException, SQLException
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

		StringBuffer sb = new StringBuffer();

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
					sb = new StringBuffer();
				}
			}
	}

	//---------------------------------------------------------------------------
	/** Transaction must be aborted on error */

	public void fillTables(Dbms dbms, CallBack cb) throws Exception
	{
		List<String> schema = loadSchemaFile(dbms.getURL());

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

	private void addTemplates(Dbms dbms) throws Exception
	{
		String siteId = getSiteId(dbms);
		String siteURL= Lib.site.getSiteURL(dbms);
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
					Element  xml  = doc.getRootElement();

					DataManager.setNamespacePrefix(xml);

					String file  = temp.getName();
					String templ = "y";
					String title = null;

					if (file.startsWith("sub-"))
					{
						templ = "s";
						title = file.substring(4, file.length() -4);
					}

					//--- templates are by default assigned to administrator/intranet group
					XmlSerializer.insert(dbms, schema.getName(), xml, serial,
											   siteId, uuid, date, date, templ, title, 1, null);

					setupTemplatePriv(dbms, serial);
					dbms.commit();
					serial++;
				}
	}

	//---------------------------------------------------------------------------
	/** NOT Transactional */
	/** This method should be called only during setup, when the database is empty
	  * and there is only 1 siteId string into settings */

	private String getSiteId(Dbms dbms) throws SQLException
	{
		String  query = "SELECT value FROM Settings WHERE name='siteId'";
		List    list  = dbms.select(query).getChildren();
		Element rec   = (Element) list.get(0);

		return rec.getChildText("value");
	}

	//---------------------------------------------------------------------------

	private void setupTemplatePriv(Dbms dbms, int id) throws SQLException
	{
		String query = "INSERT INTO OperationAllowed(groupId, metadataId, operationId) "+
							"VALUES(?, ?, ?)";

		dbms.execute(query, 1, id, new Integer(AccessManager.OPER_VIEW));
	}

	//---------------------------------------------------------------------------

	private void setupSiteId(Dbms dbms) throws SQLException, IOException
	{
		String uuid = UUID.randomUUID().toString();

		//--- duplicate dummy logo to reflect the uuid

		FileInputStream  is = new FileInputStream (appPath +"/gast/images/dummy.gif");
		FileOutputStream os = new FileOutputStream(appPath +"/web/geonetwork/images/logos/"+ uuid +".gif");
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

