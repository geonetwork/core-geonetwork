/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.api.site;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Env;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;

import javax.sql.DataSource;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SiteInformation {
    final Properties properties = System.getProperties();
    private Map<String, String> catProperties = new LinkedHashMap<>();
    private Map<String, String> indexProperties = new LinkedHashMap<>();
    private Map<String, String> systemProperties = new LinkedHashMap<>();
    private Map<String, String> envProperties = new LinkedHashMap<>();
    private Map<String, String> databaseProperties = new LinkedHashMap<>();
    private Map<String, String> versionProperties = new LinkedHashMap<>();

    public SiteInformation(final ServiceContext context, final GeonetContext gc) {
        if (context.getUserSession().isAuthenticated()) {
            loadCatalogueInfo(gc);
            try {
                loadDatabaseInfo(context);
            } catch (SQLException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage(), e);
            }
            try {
                loadIndexInfo(context);
            } catch (IOException | ElasticsearchException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage(), e);
            }
            loadEnvInfo();
            loadVersionInfo(context);
            loadSystemInfo();
        }
    }

    @JsonProperty(value = "catalogue")
    public Map<String, String> getCatProperties() {
        return catProperties;
    }

    public void setCatProperties(Map<String, String> catProperties) {
        this.catProperties = catProperties;
    }

    @JsonProperty(value = "index")
    public Map<String, String> getIndexProperties() {
        return indexProperties;
    }

    public void setIndexProperties(Map<String, String> indexProperties) {
        this.indexProperties = indexProperties;
    }

    @JsonProperty(value = "main")
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    @JsonProperty(value = "env")
    public Map<String, String> getEnvProperties() {
        return envProperties;
    }

    public void setEnvProperties(Map<String, String> envProperties) {
        this.envProperties = envProperties;
    }

    @JsonProperty(value = "database")
    public Map<String, String> getDatabaseProperties() {
        return databaseProperties;
    }

    public void setDatabaseProperties(Map<String, String> databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @JsonProperty(value = "version")
    public Map<String, String> getVersionProperties() {
        return versionProperties;
    }

    public void setVersionProperties(Map<String, String> versionProperties) {
        this.versionProperties = versionProperties;
    }

    /**
     * Load catalogue properties.
     */
    private void loadCatalogueInfo(final GeonetContext gc) {
        ServiceConfig sc = gc.getBean(ServiceConfig.class);

        String[] props = {
            Geonet.Config.SYSTEM_DATA_DIR,
            Geonet.Config.DATA_DIR,
            Geonet.Config.BACKUP_DIR,
            Geonet.Config.CODELIST_DIR,
            Geonet.Config.RESOURCES_DIR,
            Geonet.Config.SCHEMAPLUGINS_DIR,
            Geonet.Config.SCHEMAPUBLICATION_DIR,
            Geonet.Config.CONFIG_DIR,
            Geonet.Config.INDEX_CONFIG_DIR,
            Geonet.Config.FORMATTER_PATH,
            Geonet.Config.HTMLCACHE_DIR,
            Geonet.Config.SUBVERSION_PATH
        };

        for (String prop : props) {
            catProperties.put("data." + prop, sc.getValue(prop));
        }
    }

    /**
     * Compute information about the current system.
     */
    private void loadSystemInfo() {
        systemProperties.put("java.version", properties.getProperty("java.version"));
        systemProperties.put("java.vm.name", properties.getProperty("java.vm.name"));
        systemProperties.put("java.vm.vendor", properties.getProperty("java.vm.vendor"));

        systemProperties.put("os.name", properties.getProperty("os.name"));
        systemProperties.put("os.arch", properties.getProperty("os.arch"));

        try {
            TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();
            systemProperties.put("xslt.factory", transFact.getClass().getName());
        } catch (Exception e) {
            systemProperties.put("xslt.factory", "Exception:" + e.getMessage());
        }

        long freeMem = Runtime.getRuntime().freeMemory() / 1024;
        long totMem = Runtime.getRuntime().totalMemory() / 1024;
        systemProperties.put("mem.free", "" + freeMem);
        systemProperties.put("mem.total", "" + totMem);


        // hostname could be present in various places, not guaranteed to be there
        Set<String> hostNames = new HashSet<>();
        try {
            hostNames.add(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignored) {
        }
        hostNames.add(System.getenv("HOSTNAME"));
        hostNames.add(System.getenv("COMPUTERNAME"));
        systemProperties.put("host.name",
            hostNames.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(", ")));

    }

    /**
     * Compute information about index.
     */
    private void loadIndexInfo(ServiceContext context) throws IOException {
        EsSearchManager esSearchManager = context.getBean(EsSearchManager.class);

        indexProperties.put("es.url", esSearchManager.getClient().getServerUrl());
        indexProperties.put("es.version", esSearchManager.getClient().getServerVersion());
        indexProperties.put("es.index", esSearchManager.getDefaultIndex());
    }

    private void loadEnvInfo(){
        String[] variables = {
            "harvester.scheduler.enabled",
            "db.migration_onstartup"
        };
        for (String variable : variables) {
            String value = Env.getPropertyFromEnv(variable, "");
            if (StringUtils.isNotEmpty(value)) {
                envProperties.put(variable, value);
            }
        }
    }

    /**
     * Compute information about database.
     */
    private void loadDatabaseInfo(ServiceContext context) throws SQLException {
        String dbURL = null;

        Connection connection = null;
        try {
            connection = context.getBean(DataSource.class).getConnection();
            dbURL = connection.getMetaData().getURL();
            databaseProperties.put("db.openattempt", "Database Opened Successfully");
            try {
                databaseProperties.put("db.type", connection.getMetaData().getDatabaseProductName());
                databaseProperties.put("db.version", connection.getMetaData().getDatabaseProductVersion());
                databaseProperties.put("db.driver", connection.getMetaData().getDriverName());
                databaseProperties.put("db.driverVersion", connection.getMetaData().getDriverVersion());
                databaseProperties.put("db.username", connection.getMetaData().getUserName());
                databaseProperties.put("db.name", connection.getCatalog());
                // Put "db.schema" field last as getSchema() has a known issues with the jetty jndi h2 drivers which is most likely related to a driver mismatch issue.
                //    Receiver class org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper does not define or inherit an implementation of the resolved method 'abstract java.lang.String getSchema()' of interface java.sql.Connection.
                databaseProperties.put("db.schema", connection.getSchema());
            } catch (AbstractMethodError e) {
                // Most likely driver mismatch
                //    https://stackoverflow.com/questions/17969365/why-i-am-getting-java-lang-abstractmethoderror-errors
                Log.warning(Geonet.GEONETWORK, "Failed to get db properties. " + e.getMessage());
            }

            if (connection instanceof BasicDataSource) {
                BasicDataSource basicDataSource = (BasicDataSource) connection;
                try {
                    databaseProperties.put("db.numactive", "" + basicDataSource.getNumActive());
                    databaseProperties.put("db.numidle", "" + basicDataSource.getNumIdle());
                    databaseProperties.put("db.maxactive", "" + basicDataSource.getMaxTotal());
                } catch (Exception e) {
                    databaseProperties.put("db.statserror", "Failed to get stats on database connections. Error is: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            databaseProperties.put("db.openattempt",
                "Failed to open database connection, Check config.xml db file configuration. Error" + " is: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        databaseProperties.put("db.url", dbURL);
    }

    /**
     * Compute information about the application and git commit.
     */
    private void loadVersionInfo(ServiceContext context) {
        Properties prop = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
            SettingManager settingManager = context.getBean(SettingManager.class);

            versionProperties.put("app.version", settingManager.getValue(Settings.SYSTEM_PLATFORM_VERSION));
            versionProperties.put("app.subVersion", settingManager.getValue(Settings.SYSTEM_PLATFORM_SUBVERSION));

            prop.load(input);

            Enumeration<?> e = prop.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = prop.getProperty(key);
                versionProperties.put(key, value);
            }

        } catch (Exception ex) {
            Log.error(Geonet.GEONETWORK, ex.getMessage(), ex);
        }
    }
}
