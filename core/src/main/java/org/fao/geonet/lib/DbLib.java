//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;

//=============================================================================

public class DbLib {
    // -----------------------------------------------------------------------------
    // ---
    // --- API methods
    // ---
    // -----------------------------------------------------------------------------

    private static final String SQL_EXTENSION = ".sql";

    public void insertData(ServletContext servletContext, final ServiceContext context, Path appPath, Path filePath,
                           String filePrefix) throws Exception {
        if (Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Filling database tables");

        final List<String> data = loadSqlDataFile(servletContext, context.getApplicationContext(), appPath, filePath, filePrefix);
        runSQL(context, data);
    }

    public static void runSQL(final ServiceContext context, final List<String> data) {
        TransactionManager.runInTransaction("Apply SQL statements in database", context.getApplicationContext(),
            TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED, TransactionManager.CommitBehavior.ALWAYS_COMMIT, false,
            new TransactionTask<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                    runSQL(context.getEntityManager(), data, true);
                    return null;
                }
            });
    }

    public void insertData(ServletContext servletContext, Statement statement, Path appPath, Path filePath,
                           String filePrefix) throws Exception {
        if (Log.isDebugEnabled(Geonet.DB))
            Log.debug(Geonet.DB, "Filling database tables");

        List<String> data = loadSqlDataFile(servletContext, statement, appPath, filePath, filePrefix);
        runSQL(statement, data, true);
    }

    private static void runSQL(EntityManager entityManager, List<String> data, boolean failOnError) throws Exception {
        StringBuffer sb = new StringBuffer();

        boolean inBlock = false;
        for (String row : data) {
            if (!row.toUpperCase().startsWith("REM") && !row.startsWith("--")
                && !row.trim().equals("")) {
                sb.append(" ");
                sb.append(row);

                if (!inBlock && row.contains("BEGIN")) {
                    inBlock = true;
                } else if (inBlock && row.contains("END")) {
                    inBlock = false;
                }

                if (!inBlock && row.endsWith(";")) {
                    String sql = sb.toString();

                    sql = sql.substring(0, sql.length() - 1);

                    if (Log.isDebugEnabled(Geonet.DB))
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

        boolean inBlock = false;
        for (String row : data) {
            if (!row.toUpperCase().startsWith("REM") && !row.startsWith("--")
                && !row.trim().equals("")) {
                sb.append(" ");
                sb.append(row);

                if (!inBlock && row.contains("BEGIN")) {
                    inBlock = true;
                } else if (inBlock && row.contains("END")) {
                    inBlock = false;
                }
                if (!inBlock && row.endsWith(";")) {
                    String sql = sb.toString();

                    sql = sql.substring(0, sql.length() - 1);

                    if (Log.isDebugEnabled(Geonet.DB))
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
     * @param type @return
     */
    private Path checkFilePath(ServletContext servletContext, Path appPath, Path filePath, String prefix, String type) throws IOException {
        Path finalPath;
        finalPath = testPath(filePath.resolve(prefix + type + SQL_EXTENSION));

        if (finalPath == null) {
            finalPath = testPath(appPath.resolve(filePath).resolve(prefix + type + SQL_EXTENSION));
        }

        if (finalPath == null && servletContext != null) {
            String realPath = servletContext.getRealPath(filePath.resolve(prefix + type + SQL_EXTENSION).toString());
            if (realPath != null) {
                finalPath = testPath(toPath(realPath));
            }
        }
        if (finalPath == null) {
            finalPath = testPath(filePath.resolve(prefix + "default" + SQL_EXTENSION));
        }
        if (finalPath == null) {
            finalPath = testPath(appPath.resolve(filePath.resolve(prefix + "default" + SQL_EXTENSION)));
        }

        if (finalPath == null && servletContext != null) {
            final String realPath = servletContext.getRealPath(filePath.resolve(prefix + "default" + SQL_EXTENSION).toString());
            if (realPath != null) {
                finalPath = testPath(toPath(realPath));
            }
        }

        if (finalPath != null)
            return finalPath;
        else {
            String msg = String.format("SQL script not found: %s", filePath + "/" + prefix + type + SQL_EXTENSION);
            Log.debug(Geonet.DB, msg);
            throw new IOException(msg);
        }
    }

    private Path toPath(String pathString) {
        try {
            return IO.toPath(pathString);
        } catch (java.nio.file.InvalidPathException e) {
            return null;
        }
    }

    private Path testPath(Path dbFilePath) {
        if (dbFilePath != null && Files.exists(dbFilePath)) {
            return dbFilePath;
        }
        return null;
    }

    private List<String> loadSqlDataFile(ServletContext servletContext, ApplicationContext appContext, Path appPath, Path filePath, String filePrefix)
        throws IOException, SQLException {
        final DataSource dataSource = appContext.getBean(DataSource.class);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            // --- find out which dbms data file to load
            Path file = checkFilePath(servletContext, appPath, filePath, filePrefix, DatabaseType.lookup(connection).toString());

            // --- load the sql data
            return Lib.text.load(file, Constants.ENCODING);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private List<String> loadSqlDataFile(ServletContext servletContext, Statement statement, Path appPath, Path filePath, String filePrefix)
        throws IOException, SQLException {
        // --- find out which dbms data file to load
        Path file = checkFilePath(servletContext, appPath, filePath, filePrefix, DatabaseType.lookup(statement.getConnection()).toString());

        // --- load the sql data
        return Lib.text.load(file, Constants.ENCODING);
    }

}
