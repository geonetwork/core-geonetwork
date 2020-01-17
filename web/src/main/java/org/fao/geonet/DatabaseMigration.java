/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet;

import com.google.common.util.concurrent.Callables;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.sources.http.ServletPathFinder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.DatabaseType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Version;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Postprocessor that runs after the jdbcDataSource bean has been initialized and migrates the
 * database as soon as it is.
 * <p/>
 * User: Jesse Date: 9/2/13 Time: 8:01 PM
 */
public class DatabaseMigration implements BeanPostProcessor {
    private static final int VERSION_NUMBER_ID_BEFORE_2_11 = 15;
    private static final int SUBVERSION_NUMBER_ID_BEFORE_2_11 = 16;
    private static final String JAVA_MIGRATION_PREFIX = "java:";

    @Autowired
    private SystemInfo systemInfo;
    @Autowired
    private ApplicationContext _applicationContext;

    private Callable<Map<String, List<String>>> _migration;

    private String initAfter;

    private Logger _logger = Log.createLogger(Geonet.GEONETWORK + ".databasemigration");
    private boolean foundErrors;

    @Override
    public final Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        return bean;  // Do nothing
    }

    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        try {
            if (Class.forName(initAfter).isInstance(bean)) {
                _logger.debug(String.format("DB Migration / Running '%s' after initialization of '%s'.", bean.getClass(), initAfter));
                try {
                    String version;
                    String subVersion;
                    ServletContext servletContext;
                    Path path;


                    try {
                        servletContext = _applicationContext.getBean(ServletContext.class);
                    } catch (NoSuchBeanDefinitionException e) {
                        if (_applicationContext instanceof WebApplicationContext) {
                            WebApplicationContext context = (WebApplicationContext) _applicationContext;
                            servletContext = context.getServletContext();
                        } else {
                            _logger.warning("No servletContext found.  Database migration aborted.");
                            return bean;
                        }
                    }

                    version = this.systemInfo.getVersion();
                    subVersion = this.systemInfo.getSubVersion();
                    ServletPathFinder pathFinder = new ServletPathFinder(servletContext);

                    path = pathFinder.getAppPath();
                    DataSource ds = null;
                    if (bean instanceof JpaTransactionManager) {
                        ds = ((JpaTransactionManager) bean).getDataSource();
                    } else {
                        ds = ((DataSource) bean);
                    }
                    migrateDatabase(servletContext, path, ds, version, subVersion);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return bean;
            }
        } catch (ClassNotFoundException e) {
            _logger.error(String.format("DB Migration / '%s' is an invalid value for initAfter. Class not found. Error is %s", initAfter, e.getMessage(), e));
        }
        return bean;
    }

    public final void setMigration(final Map<String, List<String>> migration) {
        this._migration = Callables.returning(migration);
    }

    public final void setMigrationLoader(final Callable<Map<String, List<String>>> migration) {
        this._migration = migration;
    }

    private void migrateDatabase(ServletContext servletContext, Path path, final DataSource dataSource, final String webappVersion,
                                 final String subVersion) throws Exception {
        _logger.info("  - Migration ...");

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {

            this.foundErrors = doMigration(webappVersion, subVersion, servletContext, path, conn, statement);
            conn.commit();
        } catch (Exception e) {
            _logger.warning("  - Migration: Exception running migration for version: " + webappVersion + " subversion: "
                + subVersion + ". " + e.getMessage());
            throw e;
        }
    }

    boolean doMigration(String webappVersion, String subVersion, ServletContext servletContext, Path path, Connection conn,
                        Statement statement) throws Exception {
        // Get db version and subversion
        Pair<String, String> dbVersionInfo = getDatabaseVersion(statement);
        String dbVersion = dbVersionInfo.one();
        String dbSubVersion = dbVersionInfo.two();

        boolean anyMigrationError = false;

        // Migrate db if needed
        _logger.info("      Webapp   version:" + webappVersion + " subversion:" + subVersion);
        _logger.info("      Database version:" + dbVersion + " subversion:" + dbSubVersion);
        if (dbVersion == null) {
            _logger.warning("      Unable to retrieve the current GeoNetwork version from the database. "
                + "If this is an initial run of the software, then the database will be auto-populated. "
                + "Else check that the database is properly configured");
            return true;
        } else if (webappVersion == null) {
            _logger.warning("      Unable to retrieve the GeoNetwork version from the application code.");
            return true;
        }

        Version from = new Version(), to = new Version();

        try {
            from = parseVersionNumber(dbVersion);
        } catch (Exception e) {
            _logger.warning("      Error parsing the GeoNetwork version (" + dbVersion + "." + dbSubVersion + ") from the database: " + e.getMessage());
            _logger.error(e);
        }
        try {
            to = parseVersionNumber(webappVersion);
        } catch (Exception e) {
            _logger.warning("      Error parsing GeoNetwork version (" + webappVersion + "." + subVersion + ") from the application code: " + e.getMessage());
            _logger.error(e);
        }

        switch (from.compareTo(to)) {
            case 1:
                _logger.info("      Running on a newer database version.");
                break;
            case 0:
                _logger.info("      Application version equals the Database version, no migration task to apply.");
                break;
            case -1:
                boolean anyMigrationAction = false;

                // Migrating from 2.0 to 2.5 could be done 2.0 -> 2.3 -> 2.4 -> 2.5
                String dbType = DatabaseType.lookup(conn).toString();
                _logger.debug("      Migrating from " + from + " to " + to + " (dbtype:" + dbType + ")...");

                _logger.info("      Loading SQL migration step configuration from <?xml version=\"1.0\" encoding=\"UTF-8\"?>\n ...");
                for (Map.Entry<String, List<String>> migrationEntry : _migration.call().entrySet()) {
                    Version versionNumber = parseVersionNumber(migrationEntry.getKey());
                    if (versionNumber.compareTo(from) > 0 && versionNumber.compareTo(to) < 1) {
                        _logger.info("       - running tasks for " + versionNumber + "...");
                        for (String file : migrationEntry.getValue()) {
                            if (file.startsWith(JAVA_MIGRATION_PREFIX)) {
                                anyMigrationAction = true;
                                anyMigrationError |= runJavaMigration(conn, file);
                            } else {
                                int lastSep = file.lastIndexOf('/');
                                Assert.isTrue(lastSep > -1, file + " has the wrong format");
                                Path filePath = path.resolve(file.substring(0, lastSep));

                                String filePrefix = file.substring(lastSep + 1);
                                anyMigrationAction = true;
                                _logger.info("         - SQL migration file:" + filePath + " prefix:" + filePrefix + " ...");
                                try {
                                    Lib.db.insertData(servletContext, statement, path, filePath, filePrefix);
                                } catch (Exception e) {
                                    _logger.info("          Errors occurs during SQL migration file: " + e.getMessage());
                                    _logger.error(e);
                                    anyMigrationError = true;
                                }
                            }
                        }
                    }
                }
                if (anyMigrationAction && !anyMigrationError) {
                    _logger.info("      Successfull migration.\n"
                        + "      Catalogue administrator still need to update the catalogue\n"
                        + "      logo and data directory in order to complete the migration process.\n"
                        + "      Lucene index rebuild is also recommended after migration."
                    );
                }

                if (!anyMigrationAction) {
                    _logger.warning("      No migration task found between webapp and database version.\n"
                        + "      The system may be unstable or may failed to start if you try to run \n"
                        + "      the current GeoNetwork " + webappVersion + " with an older database (ie. " + dbVersion
                        + "\n"
                        + "      ). Try to run the migration task manually on the current database\n"
                        + "      before starting the application or start with a new empty database.\n"
                        + "      Sample SQL scripts for migration could be found in WEB-INF/sql/migrate folder.\n"
                    );

                }

                if (anyMigrationError) {
                    _logger.warning("      Error occurs during migration. Check the log file for more details.");
                }
                // TODO : Maybe some migration stuff has to be done in Java ?
                break;
            default:
                throw new Error("Unrecognized value: " + to.compareTo(from) + " when comparing " + to + " -> " + from);
        }

        return anyMigrationError;
    }

    /**
     * Execute a Java database migration.
     *
     * @param conn database connection.
     * @param file Java migration class name prefixed with JAVA_MIGRATION_PREFIX ("java:")
     * @return <code>true</code> if there is any error while executing the migration. <code>false</code> if there are no errors.
     */
    private boolean runJavaMigration(Connection conn, String file) {
        try {
            String className = file.substring(JAVA_MIGRATION_PREFIX.length());
            _logger.info("         - Java migration class:" + className);

            DatabaseMigrationTask task = (DatabaseMigrationTask) Class.forName(className).newInstance();
            task.setContext(_applicationContext);
            task.update(conn);
            return false;
        } catch (SQLException e) {
            StringBuilder error = new StringBuilder();
            formatSqlException(e, error);

            SQLException next = e.getNextException();
            while (next != null) {
                formatSqlException(next, error);
                next = e.getNextException();
            }

            try {
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                PrintStream writer = new PrintStream(byteArrayStream, true, Constants.ENCODING);
                e.printStackTrace(writer);

                error.append("\n    Stack Trace: \n").append(byteArrayStream.toString(Constants.ENCODING));
            } catch (UnsupportedEncodingException e1) {
                // skip
            }
            _logger.error("          Errors occurs during Java migration file: " + error);
            return true;
        } catch (Throwable e) {
            _logger.error("          Errors occurs during Java migration file: " + e.getMessage());
            _logger.error(e);
            return true;
        }
    }

    private void formatSqlException(SQLException e, StringBuilder error) {
        error.append("\n    SQLState: ").append(e.getSQLState());
        error.append("\n    Error Code: ").append(e.getErrorCode());
        error.append("\n    Message: ").append(e.getMessage());
    }

    /**
     * Return database version and subversion number.
     */
    private Pair<String, String> getDatabaseVersion(Statement statement) throws SQLException {
        String version = null;
        String subversion = null;

        try {
            version = newLookup(statement, Settings.SYSTEM_PLATFORM_VERSION);
            subversion = newLookup(statement, Settings.SYSTEM_PLATFORM_SUBVERSION);

            if (version == null) {
                // Before 2.11, settings was a tree. Check using keys
                version = oldLookup(statement, VERSION_NUMBER_ID_BEFORE_2_11);
                subversion = oldLookup(statement, SUBVERSION_NUMBER_ID_BEFORE_2_11);
            }
        } catch (SQLException e) {
            _logger.info("     Error getting database version: " + e.getMessage() +
                ". Probably due to an old version. Trying with new Settings structure.");
        }

        return Pair.read(version, subversion);
    }

    private String newLookup(Statement statement, String key) throws SQLException {
        final String newGetVersion = "SELECT value FROM Settings WHERE name = '" + key + "'";

        try (ResultSet results = statement.executeQuery(newGetVersion)) {
            if (results.next()) {
                return results.getString(1);
            }
            return null;
        }
    }

    private String oldLookup(Statement statement, int key) throws SQLException {
        final String newGetVersion = "SELECT value FROM Settings WHERE id = " + key;

        try (ResultSet results = statement.executeQuery(newGetVersion)) {
            if (results.next()) {
                return results.getString(1);
            }
            return null;
        }
    }

    /**
     * Parses a version number removing extra "-*" element and returning an integer.
     * "2.7.0-SNAPSHOT" is returned as 270.
     *
     * @param number The version number to parse
     * @return The version number as an integer
     */
    private Version parseVersionNumber(String number) throws Exception {
        // Remove extra "-SNAPSHOT" info which may be in version number
        int dashIdx = number.indexOf("-");
        if (dashIdx != -1) {
            number = number.substring(0, number.indexOf("-"));
        }
        switch (numDots(number)) {
            case 0:
                number += ".0.0";
                break;
            case 1:
                number += ".0";
                break;
            default:
                break;
        }

        final String[] parts = number.split("\\.");
        String major = parts[0];
        String minor = parts.length > 1 ? parts[1] : "0";
        String micro = parts.length > 2 ? parts[2] : "0";
        return new Version(major, minor, micro);
    }

    private int numDots(String number) {
        int num = 0;

        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) == '.') {
                num++;
            }
        }

        return num;
    }

    public Map<String, List<String>> getMigrationConfig() throws Exception {
        return _migration.call();
    }

    public boolean isFoundErrors() {
        return foundErrors;
    }

    public String getInitAfter() {
        return initAfter;
    }

    public void setInitAfter(String initAfter) {
        this.initAfter = initAfter;
    }
}
