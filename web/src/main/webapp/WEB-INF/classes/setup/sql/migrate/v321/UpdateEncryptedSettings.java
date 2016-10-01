package v321;

import jeeves.config.springutil.JeevesApplicationContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class UpdateEncryptedSettings implements DatabaseMigrationTask, ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void update(Connection connection) throws SQLException {
        // Take the StandardPBEStringEncryptor configuration from the properties file
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();

        String path = ((JeevesApplicationContext) applicationContext).getServletContext()
                .getRealPath("WEB-INF/config-security/config-security.properties");
        Properties properties = new Properties();

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
            properties.load(fileReader);

            standardPBEStringEncryptor.setAlgorithm(properties.getProperty("encrypter.algorithm"));
            standardPBEStringEncryptor.setPassword(properties.getProperty("encrypter.password"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileReader);
        }

        try (Statement statement = connection.createStatement()) {
            final String encryptedSettings = "SELECT name, value FROM Settings WHERE encrypted = 'y'";

            ResultSet settingsResultSet = statement.executeQuery(encryptedSettings);
            int numberOfSettings = 0;
            Map<String, String> updates = new HashMap<String, String>();

            try {
                while (settingsResultSet.next()) {
                    String name = settingsResultSet.getString(1);
                    String value = settingsResultSet.getString(2);
                    if (StringUtils.isNotEmpty(value)) {
                        value = standardPBEStringEncryptor.encrypt(value);
                        updates.put(name, value);
                        numberOfSettings++;
                    }
                }
                Log.debug(Geonet.DB, "  Number of settings of type password to update: " + numberOfSettings);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                settingsResultSet.close();
            }

            for(String key : updates.keySet()) {
                statement.execute("UPDATE Settings SET value='" + updates.get(key) + "' WHERE name='" + key + "'");
            }

            final String encryptedHarvesterSettings = "SELECT name, value FROM HarvesterSettings WHERE name = 'password'";
            ResultSet harvesterSettingsResultSet = statement.executeQuery(encryptedHarvesterSettings);
            int numberOfHarvesterSettings = 0;
            updates = new HashMap<String, String>();
            try {
                while (harvesterSettingsResultSet.next()) {
                    String name = harvesterSettingsResultSet.getString(1);
                    String value = harvesterSettingsResultSet.getString(2);
                    if (StringUtils.isNotEmpty(value)) {
                        value = standardPBEStringEncryptor.encrypt(value);
                        updates.put(name, value);

                        numberOfHarvesterSettings++;
                    }
                }

                Log.debug(Geonet.DB, "  Number of harvester settings of type password to update: " + numberOfHarvesterSettings);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                harvesterSettingsResultSet.close();
            }

            for(String key : updates.keySet()) {
                statement.execute("UPDATE HarvesterSettings SET value='" + updates.get(key) + "' WHERE name='" + key + "'");
            }


            final String encryptedMapServerPasswords = "SELECT id, password FROM Mapservers";
            ResultSet mapServerResultSet = statement.executeQuery(encryptedMapServerPasswords);
            int numberOfMapServers = 0;
            Map<Integer, String> updatesMapServers = new HashMap<Integer, String>();
            try {
                while (mapServerResultSet.next()) {
                    Integer id = mapServerResultSet.getInt(1);
                    String password = mapServerResultSet.getString(2);
                    if (StringUtils.isNotEmpty(password)) {
                        password = standardPBEStringEncryptor.encrypt(password);
                        updatesMapServers.put(id, password);
                        numberOfMapServers++;
                    }
                }

                Log.debug(Geonet.DB, "  Number of map server passwords to update: " + numberOfMapServers);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                harvesterSettingsResultSet.close();
            }

            for(Integer key : updatesMapServers.keySet()) {
                statement.execute("UPDATE Mapservers SET password='" + updates.get(key) + "' WHERE id='" + key + "'");
            }

        }
    }
}
