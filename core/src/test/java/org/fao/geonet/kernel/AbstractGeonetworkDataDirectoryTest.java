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

package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Abstract class for GeonetworkDataDirectory tests where the data directory layout is a default
 * layout but the location of the root data directory is configurable by implementing {@link
 * #getDataDir()}.
 *
 * User: Jesse Date: 11/14/13 Time: 9:07 AM
 */
public abstract class AbstractGeonetworkDataDirectoryTest extends AbstractCoreIntegrationTest {
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    protected abstract Path getDataDir();

    @Test
    public void testInit() throws Exception {
        // make sure it exists
        Files.createDirectories(getDataDir());

        // reinitialize data directory so that it uses the defaults
        dataDirectory.setSystemDataDir(null);
        dataDirectory.setConfigDir(null);
        dataDirectory.setIndexConfigDir(null);
        dataDirectory.setMetadataDataDir(null);
        dataDirectory.setMetadataRevisionDir(null);
        dataDirectory.setResourcesDir(null);
        dataDirectory.setHtmlCacheDir(null);
        dataDirectory.setSchemaPluginsDir(null);
        dataDirectory.setThesauriDir(null);
        final ArrayList<Element> serviceConfigParameterElements = getServiceConfigParameterElements();
        final ServiceConfig handlerConfig = new ServiceConfig(serviceConfigParameterElements);
        final Path webappDir = getWebappDir(getClass());
        dataDirectory.init("geonetwork", webappDir, handlerConfig, null);

        final Path expectedDataDir = getDataDir();
        assertEquals(expectedDataDir, dataDirectory.getSystemDataDir());
        assertEquals(webappDir.toAbsolutePath().normalize(), dataDirectory.getWebappDir().toAbsolutePath().normalize());
        assertSystemDirSubFolders(expectedDataDir);
    }

    private void assertSystemDirSubFolders(Path expectedDataDir) {
        final Path expectedConfigDir = expectedDataDir.resolve("config");
        assertEquals(expectedConfigDir, dataDirectory.getConfigDir());
        assertEquals(expectedDataDir.resolve("data").resolve("metadata_data"), dataDirectory.getMetadataDataDir());
        assertEquals(expectedDataDir.resolve("data").resolve("metadata_subversion"), dataDirectory.getMetadataRevisionDir());
        final Path expectedResourcesDir = expectedDataDir.resolve("data").resolve("resources");
        assertEquals(expectedResourcesDir, dataDirectory.getResourcesDir());
        assertEquals(expectedResourcesDir.resolve("htmlcache"), dataDirectory.getHtmlCacheDir());
        assertEquals(expectedResourcesDir.resolve("schemapublication"), dataDirectory.getSchemaPublicationDir());
        assertEquals(expectedConfigDir.resolve("schema_plugins"), dataDirectory.getSchemaPluginsDir());
        assertEquals(expectedConfigDir.resolve("codelist"), dataDirectory.getThesauriDir());
    }
}
