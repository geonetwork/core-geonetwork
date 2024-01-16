//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Env;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class initializes the encryptor used to encrypt/decrypt info stored in the database and performs a migration
 * in case it detects that the info in the database is not encrypted yet.
 * It should run after the datasource has been initialized.
 */
public class EncryptorInitializer {
    private final static String LOG_MODULE = Geonet.GEONETWORK + ".encryptor";
    private final static String ALGORITHM_KEY = "encryptor.algorithm";
    private final static String PASSWORD_KEY = "encryptor.password";
    private final static String DEFAULT_ALGORITHM = "PBEWithMD5AndDES";

    @Autowired
    StandardPBEStringEncryptor encryptor;

    @Autowired
    DataSource dataSource;

    private boolean firstInitialSetupFlag;

    /**
     * Set flag to indicate that a encrypt should be done during initialization
     * This will cause all fields flag as encrypted to be encrypted.
     * This should generally only be set during initial setup or migration script.
     *
     * @param firstInitialSetupFlag indicate that a encrypt should be done during initialization - default false
     */
    public void setFirstInitialSetupFlag(boolean firstInitialSetupFlag) {
        this.firstInitialSetupFlag = firstInitialSetupFlag;
    }


    public void init(GeonetworkDataDirectory dataDirectory) throws Exception {
        PropertiesConfiguration conf = getEncryptorPropertiesFile(dataDirectory);

        // Values in the current properties file
        String encryptorAlgorithmPropFile = (String) conf.getProperty(ALGORITHM_KEY);
        String encryptorPasswordPropFile = (String) conf.getProperty(PASSWORD_KEY);

        // Values to track changes (if values are provided in environment variables)
        String encryptorAlgorithm;
        String encryptorPassword;

        // Has the configuration change?
        boolean updateConfiguration;

        if (StringUtils.isEmpty(encryptorAlgorithmPropFile)) {
            encryptorAlgorithmPropFile = Env.getPropertyFromEnv(ALGORITHM_KEY, DEFAULT_ALGORITHM);
            encryptorAlgorithm = encryptorAlgorithmPropFile;
            // No algorithm configured yet
            updateConfiguration = true;
        } else {
            String encryptorAlgorithmFromEnv = Env.getPropertyFromEnv(ALGORITHM_KEY, "");
            if (StringUtils.isNotEmpty(encryptorAlgorithmFromEnv)) {
                encryptorAlgorithm = encryptorAlgorithmFromEnv;
            } else {
                encryptorAlgorithm = encryptorAlgorithmPropFile;
            }

            // Different algorithm provided in environment variables
            updateConfiguration = !encryptorAlgorithm.equals(encryptorAlgorithmPropFile);
        }


        if (StringUtils.isEmpty(encryptorPasswordPropFile)) {
            encryptorPasswordPropFile = Env.getPropertyFromEnv(PASSWORD_KEY, "");
            // Creates a random encryptor password if the password is empty
            if (StringUtils.isEmpty(encryptorPasswordPropFile)) {
                if (!firstInitialSetupFlag) {
                    Log.error(LOG_MODULE, String.format(
                        "Password database encryptor initialization error - could not locate encryptor password via encryption " +
                        "file %s or supplied properties/environment variable (%s). " +
                        "GeoNetwork can not decrypt passwords already stored in the database. " +
                        "Either recover the previous password and restart the application or manually null all existing encrypted " +
                        "passwords in the database and re-enter passwords via the application", conf.getPath(), PASSWORD_KEY));
                }
                Log.info(LOG_MODULE, "Generating a new random password for the database password encryptor");
                encryptorPasswordPropFile = RandomStringUtils.randomAlphanumeric(10);
            }
            encryptorPassword = encryptorPasswordPropFile;
            // No password configured yet
            updateConfiguration = true;
        } else {
            String encryptorPasswordFromEnv = Env.getPropertyFromEnv(PASSWORD_KEY, "");
            if (StringUtils.isNotEmpty(encryptorPasswordFromEnv)) {
                encryptorPassword = encryptorPasswordFromEnv;
            } else {
                encryptorPassword = encryptorPasswordPropFile;
            }

            // Different password provided in environment variables
            updateConfiguration = updateConfiguration || !encryptorPassword.equals(encryptorPasswordPropFile);
        }

        Log.info(LOG_MODULE, String.format("Password database encryptor initialized - Keep the file %s safe and make " +
            "a backup. When upgrading to a newer version of GeoNetwork the file must be restored, otherwise " +
            "GeoNetwork will not be able to decrypt passwords already stored in the database.", conf.getPath()));

        encryptor.setAlgorithm(encryptorAlgorithm);
        encryptor.setPassword(encryptorPassword);

        try {
            encryptor.initialize();
        } catch (EncryptionInitializationException ex) {
            if (ex.getCause() instanceof NoSuchAlgorithmException) {
                Log.error(LOG_MODULE, String.format("Encryptor algorithm %s is not supported", encryptorAlgorithm));
            } else {
                Log.error(LOG_MODULE, ex.getMessage(), ex);
            }

            throw new RuntimeException(String.format("Password encryptor could not be initialised, review the " +
                "configuration in %s or the environment variables if provided.", conf.getPath()));
        }


        /*
         * Updates the database rows with passwords. Uses SQL updates to avoid interfere with the Hibernate TypeDef
         * to encrypt database fields. For example in {@link org.fao.geonet.domain.MapServer}.
         *
         * Can't be done in a database upgrade as when the {@link javax.sql.DataSource} and
         * {@link org.springframework.orm.jpa.JpaTransactionManager} beans are initialized the data directory bean is
         * not yet ready.
         *
         * Adding a migration upgrade that depends on this bean to be initialized doesn't work as the database version
         * has been already updated in the previous upgrades.
         */
        if (updateConfiguration) {
            conf.setProperty(ALGORITHM_KEY, encryptorAlgorithm);
            conf.setProperty(PASSWORD_KEY, encryptorPassword);
            String headerComment = "Generated at %s.";
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            conf.setHeader(String.format(headerComment, date));


            // Save the encryptor.properties file
            conf.save();

            // Encryptor configuration change: should be unencrypted the passwords with the previous configuration
            // and encrypted with the new one.
            if (!encryptorPassword.equals(encryptorPasswordPropFile) ||
                !encryptorAlgorithm.equals(encryptorAlgorithmPropFile)) {

                Log.info(LOG_MODULE, "Password database encryptor - re-encrypting passwords stored in the database");

                StandardPBEStringEncryptor previousEncryptor = new StandardPBEStringEncryptor();
                previousEncryptor.setAlgorithm(encryptorAlgorithmPropFile);
                previousEncryptor.setPassword(encryptorPasswordPropFile);
                previousEncryptor.initialize();
                updateDb(previousEncryptor);
            } else {
                if (firstInitialSetupFlag) {
                    Log.info(LOG_MODULE, "Password database encryptor - encrypting passwords stored in the database");
                    updateDb(null);
                }
            }
        }

        // Register encryptor in HibernatePBEEncryptorRegistry class
        HibernatePBEEncryptorRegistry registry =
            HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("STRING_ENCRYPTOR", encryptor);

    }

    private void updateDb(StandardPBEStringEncryptor previousEncryptor) throws SQLException {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            final String encryptedSettings = "SELECT name, value FROM Settings WHERE encrypted = 'y'";

            Map<String, String> updates = new HashMap<>();

            try (ResultSet settingsResultSet = statement.executeQuery(encryptedSettings)) {
                int numberOfSettings = encryptDatabaseValuesStringId(previousEncryptor, updates, settingsResultSet);

                Log.debug(LOG_MODULE, "  Number of settings of type password to update: " + numberOfSettings);
            } catch (Exception ex) {
                Log.error(LOG_MODULE, "Error getting the settings' passwords for encrypting them");
                Log.error(LOG_MODULE, ex);
                ex.printStackTrace();
            }

            for (String key : updates.keySet()) {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE Settings SET value=? WHERE name=?");
                pstmt.setString(1, updates.get(key));
                pstmt.setString(2, key);
                pstmt.executeUpdate();
            }

            final String encryptedHarvesterSettings = "SELECT name, value FROM HarvesterSettings WHERE name = 'password'";
            updates = new HashMap<>();
            try (ResultSet harvesterSettingsResultSet = statement.executeQuery(encryptedHarvesterSettings)) {
                int numberOfHarvesterSettings = encryptDatabaseValuesStringId(previousEncryptor, updates, harvesterSettingsResultSet);

                Log.debug(LOG_MODULE, "  Number of harvester settings of type password to update: " + numberOfHarvesterSettings);
            } catch (Exception ex) {
                Log.error(LOG_MODULE, "Error getting the harvesters' passwords for encrypting them");
                Log.error(LOG_MODULE, ex);
                ex.printStackTrace();
            }

            for (String key : updates.keySet()) {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE HarvesterSettings SET value=? WHERE name=?");
                pstmt.setString(1, updates.get(key));
                pstmt.setString(2, key);
                pstmt.executeUpdate();
            }


            final String encryptedMapServerPasswords = "SELECT id, password FROM Mapservers";
            int numberOfMapServers;
            Map<Integer, String> updatesMapServers = new HashMap<>();
            try (ResultSet mapServerResultSet = statement.executeQuery(encryptedMapServerPasswords)) {
                numberOfMapServers = encryptDatabaseValuesIntegerId(previousEncryptor, updatesMapServers, mapServerResultSet);

                Log.debug(LOG_MODULE, "  Number of map server passwords to update: " + numberOfMapServers);
            } catch (Exception ex) {
                Log.error(LOG_MODULE, "Error getting the map servers' passwords for encrypting them");
                Log.error(LOG_MODULE, ex);
                ex.printStackTrace();
            }

            for (Integer key : updatesMapServers.keySet()) {
                PreparedStatement pstmt = connection.prepareStatement("UPDATE Mapservers SET password=? WHERE id=?");
                pstmt.setString(1, updatesMapServers.get(key));
                pstmt.setInt(2, key);
                pstmt.executeUpdate();
            }

            connection.commit();
        }
    }

    private int encryptDatabaseValuesIntegerId(StandardPBEStringEncryptor previousEncryptor, Map<Integer, String> updatedRows, ResultSet mapServerResultSet) throws SQLException {
        int numberOfRowsUpdated = 0;
        while (mapServerResultSet.next()) {
            Integer id = mapServerResultSet.getInt(1);
            String password = mapServerResultSet.getString(2);
            if (StringUtils.isNotEmpty(password)) {
                if (previousEncryptor != null) {
                    password = previousEncryptor.decrypt(password);
                }

                password = encryptor.encrypt(password);
                updatedRows.put(id, password);
                numberOfRowsUpdated++;
            }
        }
        return numberOfRowsUpdated;
    }

    private int encryptDatabaseValuesStringId(StandardPBEStringEncryptor previousEncryptor, Map<String, String> updates, ResultSet settingsResultSet) throws SQLException {
        int numberOfRowsUpdated = 0;
        while (settingsResultSet.next()) {
            String name = settingsResultSet.getString(1);
            String value = settingsResultSet.getString(2);
            if (StringUtils.isNotEmpty(value)) {
                if (previousEncryptor != null) {
                    value = previousEncryptor.decrypt(value);
                }

                value = encryptor.encrypt(value);
                updates.put(name, value);
                numberOfRowsUpdated++;
            }
        }
        return numberOfRowsUpdated;
    }


    /**
     * Retrieves the encryptor properties file from the data directory. If the file doesn't exists, it's created.
     *
     * @param dataDirectory
     * @return
     * @throws Exception
     */
    private PropertiesConfiguration getEncryptorPropertiesFile(GeonetworkDataDirectory dataDirectory)
        throws Exception {
        Path securityPropsPath = dataDirectory.getConfigDir().resolve(Geonet.File.ENCRYPTOR_CONFIGURATION);

        // Create the file if doesn't exists
        if (!Files.exists(securityPropsPath)) {
            Files.createFile(securityPropsPath);
        }

        return new PropertiesConfiguration(securityPropsPath.toFile());
    }
}
