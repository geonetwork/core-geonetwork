package org.fao.geonet;

import com.google.common.util.concurrent.Callables;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.sources.http.ServletPathFinder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.lib.DatabaseType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * Postprocessor that runs after the jdbcDataSource bean has been initialized and migrates the
 * database as soon as it is.
 * <p/>
 * User: Jesse
 * Date: 9/2/13
 * Time: 8:01 PM
 */
public class DatabaseMigration implements BeanPostProcessor {
    private static final int VERSION_NUMBER_ID_BEFORE_2_11 = 15;
    private static final int SUBVERSION_NUMBER_ID_BEFORE_2_11 = 16;
    private static final String JAVA_MIGRATION_PREFIX = "java:";

    @Autowired
    private SystemInfo systemInfo;
    @Autowired
    private ApplicationContext _applicationContext;

    private Callable<LinkedHashMap<String, List<String>>> _migration;

    private Logger _logger = Log.createLogger(Geonet.GEONETWORK);
    private boolean foundErrors;

    @Override
    public final Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        return bean;  // Do nothing
    }

    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof DataSource) {
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
                migrateDatabase(servletContext, path, (DataSource) bean, version, subVersion);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return bean;
        }
        return bean;
    }

    public final void setMigration(final LinkedHashMap<String, List<String>> migration) {
        this._migration = Callables.returning(migration);
    }

    public final void setMigrationLoader(final Callable<LinkedHashMap<String, List<String>>> migration) {
        this._migration = migration;
    }

    private void migrateDatabase(ServletContext servletContext, Path path, final DataSource dataSource, final String webappVersion,
                                 final String subVersion) throws Exception {
        _logger.info("  - Migration ...");

        Connection conn = null;
        Statement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
            this.foundErrors = doMigration(webappVersion, subVersion, servletContext, path, conn, statement);

        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
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
        if (dbVersion == null || webappVersion == null) {
            _logger.warning("      Database does not contain any version information. Check that the database is a GeoNetwork "
                            + "database with data.  Migration step aborted.");
            return true;
        }

        Version from = new Version(), to = new Version();

        try {
            from = parseVersionNumber(dbVersion);
            to = parseVersionNumber(webappVersion);
        } catch (Exception e) {
            _logger.warning("      Error parsing version numbers: " + e.getMessage());
            e.printStackTrace();
        }

        switch (to.compareTo(from)) {
            case -1:
                _logger.info("      Running on a newer database version.");
                break;
            case 0:
                _logger.info("      Webapp version = Database version, no migration task to apply.");
                break;
            case 1:
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
                                    e.printStackTrace();
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
                throw new Error("Unrecognized value: " + to.compareTo(from));
        }

        return anyMigrationError;
    }

    private boolean runJavaMigration(Connection conn, String file) {
        try {
            String className = file.substring(JAVA_MIGRATION_PREFIX.length());
            _logger.info("         - Java migration class:" + className);

            DatabaseMigrationTask task = (DatabaseMigrationTask) Class.forName(className).newInstance();
            task.update(conn);
            return false;
        } catch (Throwable e) {
            _logger.info("          Errors occurs during Java migration file: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Return database version and subversion number.
     *
     * @return
     */
    private Pair<String, String> getDatabaseVersion(Statement statement) throws SQLException {
        String version = null;
        String subversion = null;

        try {
            version = newLookup(statement, Geonet.Settings.VERSION);
            subversion = newLookup(statement, Geonet.Settings.SUBVERSION);

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
        ResultSet results = null;
        try {
            final String newGetVersion = "SELECT value FROM Settings WHERE name = '" + key + "'";
            results = statement.executeQuery(newGetVersion);
            if (results.next()) {
                return results.getString(1);
            }
            return null;
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    private String oldLookup(Statement statement, int key) throws SQLException {
        ResultSet results = null;
        try {
            final String newGetVersion = "SELECT value FROM Settings WHERE id = " + key;
            results = statement.executeQuery(newGetVersion);
            if (results.next()) {
                return results.getString(1);
            }
            return null;
        } finally {
            if (results != null) {
                results.close();
            }
        }
    }

    /**
     * Parses a version number removing extra "-*" element and returning an integer. "2.7.0-SNAPSHOT" is returned as 270.
     *
     * @param number The version number to parse
     * @return The version number as an integer
     * @throws Exception
     */
    private Version parseVersionNumber(String number) throws Exception {
        // Remove extra "-SNAPSHOT" info which may be in version number
        int dashIdx = number.indexOf("-");
        if (dashIdx != -1) {
            number = number.substring(0, number.indexOf("-"));
        }
        switch (numDots(number)) {
            case 0 :
                number += ".0.0";
                break;
            case 1 :
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

    public LinkedHashMap<String, List<String>> getMigrationConfig() throws Exception {
        return _migration.call();
    }

    public boolean isFoundErrors() {
        return foundErrors;
    }

    public static class Version implements Comparable<Version> {
        private final int major, minor, micro;

        public Version(String major, String minor, String micro) {
            this.major = Integer.parseInt(major);
            this.minor = Integer.parseInt(minor);
            this.micro = Integer.parseInt(micro);
        }

        public Version() {
            this("0","0","0");
        }

        @Override
        public int compareTo(Version o) {
            if (major != o.major) {
                return major - o.major;
            }
            if (minor != o.minor) {
                return minor - o.minor;
            }
            if (micro != o.micro) {
                return micro - o.micro;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Version version = (Version) o;

            if (major != version.major) return false;
            if (micro != version.micro) return false;
            if (minor != version.minor) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = major;
            result = 31 * result + minor;
            result = 31 * result + micro;
            return result;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + micro;
        }
    }
}
