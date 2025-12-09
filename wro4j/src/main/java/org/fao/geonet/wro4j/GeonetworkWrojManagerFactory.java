//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.wro4j;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.web.context.support.XmlWebApplicationContext;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.cache.impl.LruMemoryCacheStrategy;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * User: Jesse Date: 11/25/13 Time: 8:35 AM
 */
public class GeonetworkWrojManagerFactory extends ConfigurableWroManagerFactory {
    public static final String WRO4J_LOG = "geonetwork.wro4j";
    private static final String CACHE_PROP_KEY = "cacheStrategy";
    private static final String SIZE_PROP_KEY = "lruSize";

    private CacheStrategy<CacheKey, CacheValue> cacheStrategy = null;
    private ServletContext servletContext;


    @Override
    protected WroModelFactory newModelFactory() {
        return new GeonetWroModelFactory() {
            @Override
            protected Properties getConfigProperties() {
                return newConfigProperties();
            }
        };
    }

    @Override
    protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
        Properties properties = newConfigProperties();
        int lruSize = Integer.parseInt(properties.getProperty(SIZE_PROP_KEY, "128"));
        switch (properties.getProperty(CACHE_PROP_KEY, "lru")) {
            case DiskbackedCache.NAME:
                if (cacheStrategy == null) {
                    cacheStrategy = initDiskbackedCache(lruSize);
                }
                break;
            default:
                cacheStrategy = new LruMemoryCacheStrategy<>(lruSize);
        }
        return cacheStrategy;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.cacheStrategy != null) {
            cacheStrategy.destroy();
        }
    }

    private DiskbackedCache initDiskbackedCache(int lruSize) {
        Properties bundledProperties = readPropertiesFile("/git.properties");
        String bundledGitRevision = bundledProperties.getProperty("git.commit.id", "");
        Path htmlCacheDir = null;
        boolean gitVersionMatch = false;

        try {
            if (ApplicationContextHolder.get() != null) {
                GeonetworkDataDirectory geonetworkDataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
                if (ApplicationContextHolder.get() instanceof XmlWebApplicationContext) {
                    servletContext = ((XmlWebApplicationContext) ApplicationContextHolder.get()).getServletContext();
                }
                htmlCacheDir = geonetworkDataDirectory.getHtmlCacheDir();
            }
            if (htmlCacheDir != null) {
                Properties propInDataDir = new Properties();
                Path propFileInDataDir = htmlCacheDir.resolve("git.properties");
                if (Files.exists(propFileInDataDir)) {
                    try (InputStream propertiesInputStream = Files.newInputStream(propFileInDataDir)) {
                        propInDataDir.load(propertiesInputStream);
                    }
                } else {
                    try (OutputStream propertiesOutputStream = Files.newOutputStream(propFileInDataDir)) {
                        bundledProperties.store(propertiesOutputStream,
                            "Copied from GN git.properties on " + DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now()));
                    }
                }
                String dataDirGitVersion = propInDataDir.getProperty("git.commit.id");
                gitVersionMatch = bundledGitRevision.equals(dataDirGitVersion);

                if (servletContext != null) {
                    // getResource() returns null if no resource with this name is found
                    try (InputStream bundledPrebuiltCacheStream = servletContext.getResourceAsStream("/WEB-INF/prebuilt/wro4j-cache.mv.db")) {
                        if (bundledPrebuiltCacheStream != null) {
                            // The prebuilt cache exists
                            Path dataWroCache = htmlCacheDir.resolve("wro4j-cache.mv.db");
                            // Copy the prebuilt cache if it doesn't exist in the data directory or if the version in data
                            // directory doesn't match the current GN git version
                            if (!gitVersionMatch || Files.notExists(dataWroCache)) {
                                Files.copy(bundledPrebuiltCacheStream, dataWroCache, StandardCopyOption.REPLACE_EXISTING);
                                Path customLessFiles = htmlCacheDir.resolve(Geonet.Config.NODE_LESS_DIR).resolve("gn_dynamic_style.json");
                                if (Files.exists(customLessFiles)) {
                                    removeCSSFromCache(htmlCacheDir);
                                }
                            }


                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.error(WRO4J_LOG, "Error trying to initialize pre-populated wro4j-cache", e);
        }


        return new DiskbackedCache(lruSize, null);
    }

    private void removeCSSFromCache(Path dataDirPath) {

        try (DiskbackedCache tempCache = new DiskbackedCache(50, dataDirPath.resolve("wro4j-cache").toString())) {
            tempCache.deleteCSSItems();
        } catch (IOException e) {
            throw new WroRuntimeException("Error removing CSS files from the wro4j cache", e);
        }
    }

    /**
     * Reads a .properties file and load its content in a {@link Properties} object. Uses the classloader to retrieve
     * the file.
     *
     * @param propertiesFile the file to load from the classpath.
     * @return a {@link Properties} object with the contents of the file or empty if it can't be read.
     */
    private Properties readPropertiesFile(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream(propertiesFile)) {
            properties.load(input);
        } catch (IOException e) {
            Log.error(WRO4J_LOG, "Cannot read " + propertiesFile + " file", e);
        }
        return properties;
    }
}
