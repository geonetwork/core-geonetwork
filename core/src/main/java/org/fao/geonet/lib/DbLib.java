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

import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

	public void insertData(ServletContext servletContext, ServiceContext context, String appPath, String filePath,
                           String filePrefix) throws Exception {
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Filling database tables");

		List<String> data = loadSqlDataFile(servletContext, context.getApplicationContext(), appPath, filePath, filePrefix);
		runSQL(context.getEntityManager(), data, true);
	}

	public void insertData(ServletContext servletContext, Statement statement, String appPath, String filePath,
                           String filePrefix) throws Exception {
        if(Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Filling database tables");

		List<String> data = loadSqlDataFile(servletContext, statement, appPath, filePath, filePrefix);
		runSQL(statement, data, true);
	}

	private void runSQL(EntityManager entityManager, List<String> data, boolean failOnError) throws Exception {
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
                        final String trimmedSQL = sql.trim();
                        final Query query = entityManager.createNativeQuery(trimmedSQL);
                        if (trimmedSQL.startsWith("SELECT")) {
							query.setMaxResults(1);
                            query.getSingleResult();
						} else {
							query.executeUpdate();
						}
					} catch (Throwable e) {
						Log.warning(Geonet.DB, "SQL failure for: " + sql + ", error is:" + e.getMessage(), e);

						if (failOnError)
							throw new RuntimeException(e);
					}
					sb = new StringBuffer();
				}
			}
		}
        entityManager.flush();
        entityManager.clear();

	}
	private void runSQL(Statement statement, List<String> data, boolean failOnError) throws Exception {
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
							statement.executeQuery(sql).close();
						} else {
							statement.execute(sql);
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
        statement.getConnection().commit();
	}

	/**
	 * Check if db specific SQL script exist, if not return default SQL script path.
	 * 
	 *
     * @param servletContext
     * @param appPath
     *@param filePath
     * @param prefix
     * @param type    @return
	 */
	private String checkFilePath(ServletContext servletContext, String appPath, String filePath, String prefix, String type) {
        String finalPath;
        finalPath = testPath(filePath + "/" +  prefix + type + SQL_EXTENSION);

        if (finalPath == null) {
            finalPath = testPath(appPath + "/" + filePath + "/" +  prefix + type + SQL_EXTENSION);
        }

        if (finalPath == null && servletContext != null) {
            finalPath = testPath(servletContext.getRealPath(filePath + "/" + prefix + type + SQL_EXTENSION));
        }
        if (finalPath == null) {
            finalPath = testPath(filePath + "/" +  prefix + "default" + SQL_EXTENSION);
        }
        if (finalPath == null) {
            finalPath = testPath(appPath + "/" + filePath + "/" +  prefix + "default" + SQL_EXTENSION);
        }

        if (finalPath == null && servletContext != null) {
            finalPath = testPath(servletContext.getRealPath(filePath + "/" +  prefix + "default" + SQL_EXTENSION));
        }

		if (finalPath != null)
			return finalPath;
		else {
            Log.debug(Geonet.DB, "  No default SQL script found: " + (filePath + "/" +  prefix + type + SQL_EXTENSION));
        }
		return "";
	}

    private String testPath(String dbFilePath) {
        File dbFile = new File(dbFilePath);
        if (dbFile.exists()) {
            return dbFilePath;
        }
        return null;
    }

    private List<String> loadSqlDataFile(ServletContext servletContext, ApplicationContext appContext, String appPath, String filePath, String filePrefix)
            throws IOException, SQLException {
        final DataSource dataSource = appContext.getBean(DataSource.class);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            // --- find out which dbms data file to load
            String file = checkFilePath(servletContext, appPath, filePath, filePrefix, DatabaseType.lookup(connection).toString());

            // --- load the sql data
            return Lib.text.load(servletContext, appPath, file, Constants.ENCODING);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
	}
    private List<String> loadSqlDataFile(ServletContext servletContext, Statement statement, String appPath, String filePath, String filePrefix)
            throws IOException, SQLException {
            // --- find out which dbms data file to load
            String file = checkFilePath(servletContext, appPath, filePath, filePrefix, DatabaseType.lookup(statement.getConnection()).toString());

            // --- load the sql data
            return Lib.text.load(servletContext, appPath, file, Constants.ENCODING);
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
