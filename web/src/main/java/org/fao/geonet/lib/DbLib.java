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

package org.fao.geonet.lib;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

//=============================================================================

public class DbLib {
	// -----------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// -----------------------------------------------------------------------------

	private static final String SQL_EXTENSION = ".sql";
	private static final String SETUP_SQL_PATH = "/WEB-INF/classes/setup/sql/";

	public Element select(Dbms dbms, String table, String name)
			throws SQLException {
		return select(dbms, table, name, null);
	}

	// -----------------------------------------------------------------------------

	public Element select(Dbms dbms, String table, String name, String where)
			throws SQLException {
		String query = "SELECT * FROM " + table;

		if (where != null)
			query += " WHERE " + where;

		Element result = dbms.select(query);

		Iterator i = result.getChildren().iterator();

		while (i.hasNext()) {
			Element record = (Element) i.next();
			record.setName(name);
		}

		return result.setName(table.toLowerCase());
	}

	/**
	 * Check if database is an empty one or not and look for the Metadata table.
	 * 
	 * @param dbms
	 * @return true if the Metadata table exists, the database is a GeoNetwork
	 *         database.
	 */
	public boolean touch(Dbms dbms) {
		try {
			select(dbms, "Metadata", "Touch", "id = 0");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getDBType(Dbms dbms) {
		String url = dbms.getURL();
		String file = "default";

		if (url.indexOf("oracle") != -1)
			file = "oracle";
		else if (url.indexOf("mckoi") != -1)
			file = "mckoi";
		else if (url.indexOf("db2") != -1)
			file = "db2";
		else if (url.indexOf("mysql") != -1)
			file = "mysql";
		else if (url.indexOf("postgresql") != -1)
			file = "postgres";
		else if (url.indexOf("postgis") != -1)
			file = "postgis";
        else if (url.indexOf("sqlserver") != -1)
			file = "sqlserver";

		return file;
	}

	/**
	 * Remove all objects in the database. Read the SQL file and check all
	 * CREATE TABLE statements to collect the list of table to remove.
	 * 
	 * @param dbms
	 * @param cb
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void removeObjects(Dbms dbms, String appPath)
			throws FileNotFoundException, IOException {
		Log.debug(Geonet.DB, "Removing database objects");
		List<String> schema = loadSchemaFile(dbms, appPath);

		// --- step 1 : collect objects to remove
		ArrayList<ObjectInfo> objects = new ArrayList<ObjectInfo>();

		for (String row : schema)
			if (row.toUpperCase().startsWith("CREATE ")) {
				ObjectInfo oi = new ObjectInfo();
				oi.name = getObjectName(row);
				oi.type = getObjectType(row);

				if (!oi.type.toLowerCase().equals("index"))
					objects.add(oi);
			}

		// --- step 2 : remove objects
		while (true) {
			boolean removed = false;

			for (Iterator<ObjectInfo> i = objects.iterator(); i.hasNext();) {
				ObjectInfo oi = i.next();
				Log.debug(Geonet.DB, "  * Dropping " + oi.name);
				String query = "DROP " + oi.type + " " + oi.name;

				if (safeExecute(dbms, query)) {
					removed = true;
					i.remove();
				}
			}

			if (objects.size() == 0)
				return;

			// --- if no object was removed then we have a cyclic loop
			if (!removed) {
				ArrayList<String> al = new ArrayList<String>();

				for (ObjectInfo oi : objects)
					al.add(oi.name);
				return;
			}
		}
	}

	/**
	 * Create database schema.
	 * 
	 * @param dbms
	 */
	public void createSchema(Dbms dbms, String appPath) throws Exception {
		Log.debug(Geonet.DB, "Creating database schema");

		List<String> schema = loadSchemaFile(dbms, appPath);
		runSQL(dbms, schema);
	}

	public void insertData(Dbms dbms, String appPath) throws Exception {
		Log.debug(Geonet.DB, "Filling database tables");

		List<String> data = loadSqlDataFile(dbms, appPath);
		runSQL(dbms, data);
	}

	/**
	 * SQL File MUST be in UTF-8.
	 * 
	 * @param dbms
	 * @param sqlFile
	 * @throws Exception
	 */
	public void runSQL(Dbms dbms, File sqlFile) throws Exception {
		runSQL(dbms, sqlFile, true);
	}

	public void runSQL(Dbms dbms, File sqlFile, boolean failOnError) throws Exception {
		List<String> data = Lib.text.load(sqlFile.getCanonicalPath(), "UTF-8");
		runSQL(dbms, data, failOnError);
	}
	
	private void runSQL(Dbms dbms, List<String> data, boolean failOnError) throws Exception {
		StringBuffer sb = new StringBuffer();

		for (String row : data) {
			if (!row.toUpperCase().startsWith("REM") && !row.startsWith("--")
					&& !row.trim().equals("")) {
				sb.append(" ");
				sb.append(row);

				if (row.endsWith(";")) {
					String sql = sb.toString();

					sql = sql.substring(0, sql.length() - 1);

					Log.debug(Geonet.DB, "Executing " + sql);
					
					try {
						if (sql.trim().startsWith("SELECT")) {
							dbms.select(sql);
						} else {
							dbms.execute(sql);
						}
					} catch (SQLException e) {
						Log.warning(Geonet.DB, "SQL failure for: " + sql + ", error is:" + e.getMessage());

						if (failOnError)
							throw e;						
					}
					sb = new StringBuffer();
				}
			}
		}
		dbms.commit();		
	}
	private void runSQL(Dbms dbms, List<String> data) throws Exception {
		runSQL(dbms, data, true);
	}

	/**
	 * Execute query and commit
	 * 
	 * @param dbms
	 * @param query
	 * @return
	 */
	private boolean safeExecute(Dbms dbms, String query) {
		try {
			dbms.execute(query);

			// --- as far as I remember, PostgreSQL needs a commit even for DDL
			dbms.commit();

			return true;
		} catch (SQLException e) {
			Log.debug(Geonet.DB, "Safe execute error: " + query + ", error is:" + e.getMessage());
			dbms.abort();
			return false;
		}
	}

	/**
	 * 
	 * @param dbms
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private List<String> loadSchemaFile(Dbms dbms, String appPath) // FIXME :
																	// use
																	// resource
																	// dir
																	// instead
																	// of
																	// appPath
			throws FileNotFoundException, IOException {
		// --- find out which dbms schema to load
		String file = checkFilePath(appPath, "create/create-db-", getDBType(dbms));
		
		Log.debug(Geonet.DB, "Database creation script is:" + file);

		// --- load the dbms schema
		return Lib.text.load(file);
	}

	/**
	 * Check if db specific SQL script exist, if not return default SQL script path.
	 * 
	 * @param appPath
	 * @param prefix
	 * @param type
	 * @return
	 */
	private String checkFilePath (String appPath, String prefix, String type) {
		String dbFilePath = appPath + SETUP_SQL_PATH + prefix + type + SQL_EXTENSION;
		File dbFile = new File(dbFilePath);
		if (dbFile.exists())
			return dbFilePath;
		
		String defaultFilePath = appPath + SETUP_SQL_PATH + prefix + "default" + SQL_EXTENSION;
		File defaultFile = new File(defaultFilePath);
		if (defaultFile.exists())
			return defaultFilePath;
		else
			Log.debug(Geonet.DB, "No default SQL script found: " + defaultFilePath);

		return "";
	}
	
	private List<String> loadSqlDataFile(Dbms dbms, String appPath)
			throws FileNotFoundException, IOException {
		// --- find out which dbms data file to load
		String file = checkFilePath(appPath, "data/data-db-", getDBType(dbms));
		
		// --- load the sql data
		return Lib.text.load(file, "UTF-8");
	}

	private String getObjectName(String createStatem) {
		StringTokenizer st = new StringTokenizer(createStatem, " ");
		st.nextToken();
		st.nextToken();

		return st.nextToken();
	}

	// ---------------------------------------------------------------------------

	private String getObjectType(String createStatem) {
		StringTokenizer st = new StringTokenizer(createStatem, " ");
		st.nextToken();

		return st.nextToken();
	}

	class ObjectInfo {
		public String name;
		public String type;
	}
}
