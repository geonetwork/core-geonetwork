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

package org.fao.geonet.api.site;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.dbcp2.BasicDataSource;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.TransformerFactoryFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Created by francois on 04/06/16.
 */
public class SiteInformation {
    final Properties properties = System.getProperties();

    @JsonProperty(value = "catalogue")
    public HashMap<String, String> getCatProperties() {
        return catProperties;
    }

    public void setCatProperties(HashMap<String, String> catProperties) {
        this.catProperties = catProperties;
    }

    @JsonProperty(value = "index")
    public HashMap<String, String> getIndexProperties() {
        return indexProperties;
    }

    public void setIndexProperties(HashMap<String, String> indexProperties) {
        this.indexProperties = indexProperties;
    }

    @JsonProperty(value = "main")
    public HashMap<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(HashMap<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    @JsonProperty(value = "database")
    public HashMap<String, String> getDatabaseProperties() {
        return databaseProperties;
    }

    public void setDatabaseProperties(HashMap<String, String> databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @JsonProperty(value = "version")
    public Map<String, String> getVersionProperties() {
        return versionProperties;
    }

    public void setVersionProperties(HashMap<String, String> versionProperties) {
        this.versionProperties = versionProperties;
    }

    private HashMap<String, String> catProperties = new HashMap<String, String>();
    private HashMap<String, String> indexProperties = new HashMap<String, String>();
    private HashMap<String, String> systemProperties = new HashMap<String, String>();
    private HashMap<String, String> databaseProperties = new HashMap<String, String>();
    private HashMap<String, String> versionProperties = new HashMap<String, String>();

    public SiteInformation(final ServiceContext context, final GeonetContext gc) {
        if(context.getUserSession().isAuthenticated()) {
            loadCatalogueInfo(gc);
            try {
                loadDatabaseInfo(context);
            } catch (SQLException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage(), e);
            }
            try {
                loadIndexInfo(context);
            } catch (IOException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage(), e);
            }
            loadVersionInfo(context);
            loadSystemInfo();
        }
    }

    /**
     * Load catalogue properties.
     */
    private void loadCatalogueInfo(final GeonetContext gc) {
        ServiceConfig sc = gc.getBean(ServiceConfig.class);

        String[] props = { Geonet.Config.DATA_DIR, Geonet.Config.CODELIST_DIR, Geonet.Config.CONFIG_DIR, Geonet.Config.SCHEMAPLUGINS_DIR,
                Geonet.Config.SUBVERSION_PATH, Geonet.Config.RESOURCES_DIR, Geonet.Config.FORMATTER_PATH, Geonet.Config.BACKUP_DIR };

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

    }

    /**
     * Compute information about Lucene index.
     */
    private void loadIndexInfo(ServiceContext context) throws IOException {
        final GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        Path luceneDir = dataDirectory.getLuceneDir();
        indexProperties.put("index.path", luceneDir.toAbsolutePath().normalize().toString());
        if (Files.exists(luceneDir)) {
            long size = ApiUtils.sizeOfDirectory(luceneDir);
            indexProperties.put("index.size", "" + size); // lucene + Shapefile
            // if exist
        }
        indexProperties.put("index.lucene.config", context.getBean(LuceneConfig.class).toString());
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
     * Compute information about git commit.
     */
    private void loadVersionInfo(ServiceContext context) {
        Properties prop = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/git.properties")) {
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
