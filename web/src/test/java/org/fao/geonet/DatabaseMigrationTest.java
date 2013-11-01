package org.fao.geonet;

import static org.junit.Assert.*;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.sql.*;

public class DatabaseMigrationTest {

    private static final String CURRENT_WEB_APP_VERSION = "2.11.0";
    private static final String CURRENT_WEB_APP_SUB_VERSION = "SNAPSHOT";
    public static final String DATABASE_MIGRATION_XML = "WEB-INF/config-db/database_migration.xml";

    @Rule
    public TemporaryFolder dbFileContainer = new TemporaryFolder();

    @Test
    public void testMigrateFrom2_10() throws Exception {
        performMigration("2.10");
    }

    private void performMigration(String databaseContainmentDir) throws Exception {
        final String pathToDbFile = databaseContainmentDir + "/geonetwork.h2.db";
        final String resource = DatabaseMigrationTest.class.getClassLoader().getResource(pathToDbFile).getFile();
        String parentFile = new File(resource).getParent();
        FileUtils.copyDirectory(new File(parentFile), dbFileContainer.getRoot());
        String h2URL = "jdbc:h2:" + dbFileContainer.getRoot() + "/geonetwork;LOCK_TIMEOUT=20000;DB_CLOSE_ON_EXIT=FALSE;MVCC=TRUE;MODE=MySQL";

        Class.forName("org.h2.Driver");
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(h2URL, "admin", "gnos");
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            conn.commit();
            final File webappDir = findwebappDir(parentFile);
            File migrationConfig = new File(webappDir, DATABASE_MIGRATION_XML);
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(migrationConfig.toURI().toString());
            final DatabaseMigration databaseMigration = context.getBean(DatabaseMigration.class);
            boolean errors = databaseMigration.doMigration(CURRENT_WEB_APP_VERSION, CURRENT_WEB_APP_SUB_VERSION, null, webappDir.getPath()+File.separator, conn,
                    statement);

            assertFalse(errors);

            assertThatDBIsUpToDate(statement);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void assertThatDBIsUpToDate(Statement statement) throws SQLException {
        assertRowCount(statement, "Settings", 80);
        assertSettingsTableIsCorrect(statement);
        assertRowCount(statement, "Users", 2);
        assertUserHasUpdatedProfile(statement);
        assertMetadataHasUpdatedIsTemplate(statement);
        assertRowCount(statement, "Address", 2);
    }

    private void assertMetadataHasUpdatedIsTemplate(Statement statement) throws SQLException {
        ResultSet resultSet = select(statement, "SELECT isTemplate from Metadata");
        assertEquals(resultSet.getMetaData().getColumnTypeName(1), Types.INTEGER, resultSet.getMetaData().getColumnType(1));
        resultSet.close();
    }

    private void assertUserHasUpdatedProfile(Statement statement) throws SQLException {
        ResultSet resultSet = select(statement, "SELECT profile from Users");
        assertEquals(resultSet.getMetaData().getColumnTypeName(1), Types.INTEGER, resultSet.getMetaData().getColumnType(1));
        resultSet.close();
    }

    private void assertRowCount(Statement statement, String tableName, int expectedCount) throws SQLException {
        final String sql = "SELECT count(*) from " + tableName;
        ResultSet resultSet = select(statement, sql);
        assertEquals(expectedCount, resultSet.getInt(1));
        resultSet.close();
    }

    private ResultSet select(Statement statement, String sql) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        assertTrue(resultSet.next());
        return resultSet;
    }

    private void assertSettingsTableIsCorrect(Statement statement) throws SQLException {
        ResultSet resultSet = select(statement, "SELECT name, value, datatype, " +
                                                "position from Settings WHERE name='system/server/port'");
        assertEquals("system/server/port", resultSet.getString(1));
        assertEquals("8080", resultSet.getString(2));
        assertEquals(1, resultSet.getInt(3));
        assertEquals(220, resultSet.getInt(4));

        assertEquals(resultSet.getMetaData().getColumnTypeName(1), Types.VARCHAR, resultSet.getMetaData().getColumnType(1));
        assertEquals(resultSet.getMetaData().getColumnTypeName(2), Types.VARCHAR, resultSet.getMetaData().getColumnType(2));
        assertEquals(resultSet.getMetaData().getColumnTypeName(3), Types.INTEGER, resultSet.getMetaData().getColumnType(3));
        assertEquals(resultSet.getMetaData().getColumnTypeName(4), Types.INTEGER, resultSet.getMetaData().getColumnType(3));
        resultSet.close();
    }

    private File findwebappDir(String parentFile) {
        File current = new File(parentFile);
        final String pathToFile = "src/main/webapp/" + DATABASE_MIGRATION_XML;
        final String pathToFile2 = "web/src/main/webapp/" + DATABASE_MIGRATION_XML;
        while (!new File(current, pathToFile).exists() && !new File(current, pathToFile2).exists()) {
            current = current.getParentFile();
        }

        if (new File(current, pathToFile).exists()) {
            return new File(current, pathToFile).getParentFile().getParentFile().getParentFile();
        } else {
            return new File(current, pathToFile2).getParentFile().getParentFile().getParentFile();
        }
    }
}