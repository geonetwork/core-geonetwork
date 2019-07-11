//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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
package org.fao.geonet.services.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.TransformerFactoryFactory;
import org.jdom.Element;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.transform.TransformerFactory;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Retrieve various type of information about the system (eg. Java version, XSLT transformer
 * factory, Lucene index properties). Usually these properties could not be set from the web
 * interface and some of them could be updated in configuration file.
 *
 * @author francois
 */
@Deprecated
public class GetInfo implements Service {
    final Properties properties = System.getProperties();
    private HashMap<String, String> catProperties = new HashMap<String, String>();
    private HashMap<String, String> indexProperties = new HashMap<String, String>();
    private HashMap<String, String> systemProperties = new HashMap<String, String>();
    private HashMap<String, String> databaseProperties = new HashMap<String, String>();

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);

        loadSystemInfo();
        loadCatalogueInfo(gc);
        loadIndexInfo(context);
        loadDatabaseInfo(context);

        Element system = gc.getBean(SettingManager.class).getAllAsXML(true);

        Element main = new Element("main");
        addToElement(main, systemProperties);

        Element index = new Element("index");
        addToElement(index, indexProperties);

        Element cat = new Element("catalogue");
        addToElement(cat, catProperties);

        Element db = new Element("database");
        addToElement(db, databaseProperties);

        Element info = new Element("info");
        info.addContent(system);
        info.addContent(cat);
        info.addContent(main);
        info.addContent(index);
        info.addContent(db);

        return info;
    }

    /**
     * Load catalogue properties.
     */
    private void loadCatalogueInfo(final GeonetContext gc) {
        ServiceConfig sc = gc.getBean(ServiceConfig.class);

        String[] props = {Geonet.Config.DATA_DIR, Geonet.Config.CODELIST_DIR,
            Geonet.Config.CONFIG_DIR, Geonet.Config.SCHEMAPLUGINS_DIR,
            Geonet.Config.SUBVERSION_PATH, Geonet.Config.RESOURCES_DIR,
            Geonet.Config.FORMATTER_PATH, Geonet.Config.BACKUP_DIR};

        for (String prop : props) {
            catProperties.put("data." + prop, sc.getValue(prop));
        }
    }


    /**
     * Compute information about the current system.
     */
    private void loadSystemInfo() {
        systemProperties.put("java.version",
            properties.getProperty("java.version"));
        systemProperties.put("java.vm.name",
            properties.getProperty("java.vm.name"));
        systemProperties.put("java.vm.vendor",
            properties.getProperty("java.vm.vendor"));

        systemProperties.put("os.name", properties.getProperty("os.name"));
        systemProperties.put("os.arch", properties.getProperty("os.arch"));

        try {
            TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();
            systemProperties.put("xslt.factory", transFact.getClass().getName());
            systemProperties.put("system.xslt.factory", System.getProperty("javax.xml.transform.TransformerFactory"));
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
            long size = sizeOfDirectory(luceneDir) / 1024;
            indexProperties.put("index.size", "" + size); // lucene + Shapefile
            // if exist
        }

        indexProperties.put("index.lucene.config", context.getBean(LuceneConfig.class).toString());
    }

    private long sizeOfDirectory(Path lDir) throws IOException {
        final long[] size = new long[]{0};
        Files.walkFileTree(lDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                size[0] += Files.size(file);
                return FileVisitResult.CONTINUE;
            }
        });

        return size[0];
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
            databaseProperties.put("db.openattempt", "Failed to open database connection, Check config.xml db file configuration. Error" +
                " is: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        databaseProperties.put("db.url", dbURL);

    }


    /**
     * Add HashMap content to an Element.
     */
    private void addToElement(Element el, HashMap<String, String> h) {
        for (Map.Entry<String, String> entry : h.entrySet()) {
            el.addContent(new Element(entry.getKey()).setText(entry.getValue()));
        }
    }
}
