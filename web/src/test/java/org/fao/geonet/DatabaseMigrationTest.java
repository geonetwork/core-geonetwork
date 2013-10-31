package org.fao.geonet;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
        String h2URL = "jdbc:h2:" + dbFileContainer.getRoot() + "/geonetwork;LOCK_TIMEOUT=20000;DB_CLOSE_ON_EXIT=FALSE;MVCC=TRUE";

        Class.forName("org.h2.Driver");
        Connection conn = null;
        Statement statement = null;
        try {
            conn = DriverManager.getConnection(h2URL, "admin", "gnos");
            statement = conn.createStatement();
            final File webappDir = findwebappDir(parentFile);
            File migrationConfig = new File(webappDir, DATABASE_MIGRATION_XML);
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(migrationConfig.toURI().toString());
            final DatabaseMigration databaseMigration = context.getBean(DatabaseMigration.class);
            databaseMigration.doMigration(CURRENT_WEB_APP_VERSION, CURRENT_WEB_APP_SUB_VERSION, null, webappDir.getPath()+File.separator, conn,
                    statement);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
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