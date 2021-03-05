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
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class EncryptorInitializer {
    private final static String LOG_MODULE = Geonet.GEONETWORK + ".encryptor";

    @Autowired
    StandardPBEStringEncryptor encryptor;

    @Autowired
    DataSource dataSource;

    public void init(GeonetworkDataDirectory dataDirectory) throws Exception {
        String securityPropsPath = dataDirectory.getConfigDir().resolve("encryptor")
            .resolve("encryptor.properties").toString();

        PropertiesConfiguration conf = new PropertiesConfiguration(securityPropsPath);
        String encryptorAlgorithm = (String) conf.getProperty("encryptor.algorithm");
        String encryptorPassword = (String) conf.getProperty("encryptor.password");

        boolean updateDatabase = false;

        // Creates a random encryptor password if the password has the value 'default'
        if (StringUtils.isEmpty(encryptorPassword)) {
            Log.info(LOG_MODULE, "Generating a random password for the database password encryptor");
            encryptorPassword = RandomStringUtils.randomAlphanumeric(10);

            conf.setProperty("encryptor.password", encryptorPassword);
            conf.save();

            updateDatabase = true;
        }

        Log.info(LOG_MODULE, "Password database encryptor initialized - Keep the file " + securityPropsPath +
            " safe and make a backup. When upgrading to a newer version of GeoNetwork the file must be restored, " +
            "otherwise GeoNetwork will not be able to decrypt passwords already stored in the database.");

        encryptor.setAlgorithm(encryptorAlgorithm);
        encryptor.setPassword(encryptorPassword);

        /**
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
        if (updateDatabase) {
            Log.info(LOG_MODULE, "Password database encryptor - encrypting passwords stored in the database");
            updateDb();
        }

        // Register encryptor in HibernatePBEEncryptorRegistry class
        HibernatePBEEncryptorRegistry registry =
            HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("STRING_ENCRYPTOR", encryptor);

    }

    private void updateDb() throws SQLException {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            final String encryptedSettings = "SELECT name, value FROM Settings WHERE encrypted = 'y'";

            ResultSet settingsResultSet = statement.executeQuery(encryptedSettings);
            int numberOfSettings = 0;
            Map<String, String> updates = new HashMap<String, String>();

            try {
                while (settingsResultSet.next()) {
                    String name = settingsResultSet.getString(1);
                    String value = settingsResultSet.getString(2);
                    if (StringUtils.isNotEmpty(value)) {
                        value = encryptor.encrypt(value);
                        updates.put(name, value);
                        numberOfSettings++;
                    }
                }
                Log.debug(LOG_MODULE, "  Number of settings of type password to update: " + numberOfSettings);
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
                        value = encryptor.encrypt(value);
                        updates.put(name, value);

                        numberOfHarvesterSettings++;
                    }
                }

                Log.debug(LOG_MODULE, "  Number of harvester settings of type password to update: " + numberOfHarvesterSettings);
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
                        password = encryptor.encrypt(password);
                        updatesMapServers.put(id, password);
                        numberOfMapServers++;
                    }
                }

                Log.debug(LOG_MODULE, "  Number of map server passwords to update: " + numberOfMapServers);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                harvesterSettingsResultSet.close();
            }

            for(Integer key : updatesMapServers.keySet()) {
                statement.execute("UPDATE Mapservers SET password='" + updatesMapServers.get(key) + "' WHERE id='" + key + "'");
            }

            connection.commit();
        }
    }
}
