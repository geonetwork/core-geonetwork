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

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

import javax.servlet.ServletContext;
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

	public Element select(Dbms dbms, String table, String name)
			throws SQLException {
		return select(dbms, table, name, null);
	}

	// -----------------------------------------------------------------------------

	private Element select(Dbms dbms, String table, String name, String where)
			throws SQLException {
		String query = "SELECT * FROM " + table;

		if (where != null)
			query += " WHERE " + where;

		Element result = dbms.select(query);

		@SuppressWarnings("unchecked")
        List<Element> resultChildren = result.getChildren();

		for (Element record : resultChildren) {
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

	/**
	 * Remove all objects in the database. Read the SQL file and check all
	 * CREATE TABLE statements to collect the list of table to remove.
	 * 
	 * @param dbms
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void removeObjects(ServletContext servletContext, Dbms dbms, String appPath, String filePath, String filePrefix)
			throws FileNotFoundException, IOException {
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Removing database objects");
		List<String> schema = loadSchemaFile(servletContext, dbms, appPath, filePath, filePrefix);

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
                if(Log.isDebugEnabled(Geonet.DB))
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
     * @param servletContext
     *
     * @param dbms
     */
	public void createSchema(ServletContext servletContext, Dbms dbms, String appPath, String filePath, String filePrefix) throws Exception {
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Creating database schema");

		List<String> schema = loadSchemaFile(servletContext, dbms, appPath, filePath, filePrefix);
		runSQL(dbms, schema);
	}

	public void insertData(ServletContext servletContext, Dbms dbms, String appPath, String filePath, String filePrefix) throws Exception {
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Filling database tables");

		List<String> data = loadSqlDataFile(servletContext, dbms, appPath, filePath, filePrefix);
		runSQL(dbms, data);
	}

	/**
	 * SQL File MUST be in UTF-8.
	 * 
	 * @param dbms
	 * @param sqlFile
	 * @throws Exception
	 */
	public void runSQL(ServletContext servletContext, Dbms dbms, File sqlFile) throws Exception {
		runSQL(servletContext, dbms, sqlFile, true);
	}

	public void runSQL(ServletContext servletContext, Dbms dbms, File sqlFile, boolean failOnError) throws Exception {
		List<String> data = Lib.text.load(servletContext, sqlFile.getCanonicalPath(), Jeeves.ENCODING);
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

                    if(Log.isDebugEnabled(Geonet.DB))
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
            if(Log.isDebugEnabled(Geonet.DB))
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
	private List<String> loadSchemaFile(ServletContext servletContext, Dbms dbms, String appPath, String filePath, String filePrefix) // FIXME :
																	// use
																	// resource
																	// dir
																	// instead
																	// of
																	// appPath
			throws FileNotFoundException, IOException {
		// --- find out which dbms schema to load
		String file = checkFilePath(filePath, filePrefix, DatabaseType.lookup(dbms).toString());

        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "  Loading script:" + file);

		// --- load the dbms schema
		return Lib.text.load(servletContext, appPath, file);
	}

	/**
	 * Check if db specific SQL script exist, if not return default SQL script path.
	 * 
	 * @param filePath
	 * @param prefix
	 * @param type
	 * @return
	 */
	private String checkFilePath (String filePath, String prefix, String type) {
		String dbFilePath = filePath + "/" +  prefix + type + SQL_EXTENSION;
		File dbFile = new File(dbFilePath);
		if (dbFile.exists())
			return dbFilePath;
		
		String defaultFilePath = filePath + "/" +  prefix + "default" + SQL_EXTENSION;
		File defaultFile = new File(defaultFilePath);
		if (defaultFile.exists())
			return defaultFilePath;
		else
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "  No default SQL script found: " + defaultFilePath);

		return "";
	}
	
	private List<String> loadSqlDataFile(ServletContext servletContext, Dbms dbms, String appPath, String filePath, String filePrefix)
			throws FileNotFoundException, IOException {
		// --- find out which dbms data file to load
		String file = checkFilePath(filePath, filePrefix, DatabaseType.lookup(dbms).toString());
		
		// --- load the sql data
		return Lib.text.load(servletContext, appPath, file, Jeeves.ENCODING);
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

	static final class ObjectInfo {
		public String name;
		public String type;
	}

}
