package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;

import static java.io.File.separator;
import static org.junit.Assert.assertEquals;

/**
 * Abstract class for GeonetworkDataDirectory tests where the data directory layout is a default layout but the
 * location of the root data directory is configurable by implementing {@link #getDataDir()}.
 *
 * User: Jesse
 * Date: 11/14/13
 * Time: 9:07 AM
 */
public abstract class AbstractGeonetworkDataDirectoryTest extends AbstractCoreIntegrationTest {
    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    protected abstract String getDataDir();

    @Test
    public void testInit() throws Exception {
        // make sure it exists
        new File(getDataDir()).mkdirs();

        // reinitialize data directory so that it uses the defaults
        dataDirectory.setSystemDataDir(null);
        final ArrayList<Element> serviceConfigParameterElements = getServiceConfigParameterElements();
        final ServiceConfig handlerConfig = new ServiceConfig(serviceConfigParameterElements);
        final String webappDir = getWebappDir(getClass());
        dataDirectory.init("geonetwork", webappDir, handlerConfig, null);

        assertEquals(getGeonetworkNodeId(), dataDirectory.getNodeId());
        final String expectedDataDir = getDataDir();
        assertEquals(expectedDataDir, dataDirectory.getSystemDataDir());
        assertEquals(new File(webappDir).getAbsoluteFile(), new File(dataDirectory.getWebappDir()).getAbsoluteFile());
        assertSystemDirSubFolders(expectedDataDir);
    }

    private void assertSystemDirSubFolders(String expectedDataDir) {
        final String expectedConfigDir = expectedDataDir + separator + "config";
        assertEquals(new File(expectedConfigDir), dataDirectory.getConfigDir());
        assertEquals(new File(expectedDataDir, "index"), dataDirectory.getLuceneDir());
        assertEquals(new File(expectedDataDir, "spatialindex"), dataDirectory.getSpatialIndexPath());
        assertEquals(new File(expectedDataDir, "data" + separator + "metadata_data"), dataDirectory.getMetadataDataDir());
        assertEquals(new File(expectedDataDir, "data" + separator + "metadata_subversion"), dataDirectory.getMetadataRevisionDir());
        final File expectedResourcesDir = new File(expectedDataDir, "data" + separator + "resources");
        assertEquals(expectedResourcesDir, dataDirectory.getResourcesDir());
        assertEquals(new File(expectedResourcesDir, "htmlcache"), dataDirectory.getHtmlCacheDir());
        assertEquals(new File(expectedConfigDir, "schema_plugins"), dataDirectory.getSchemaPluginsDir());
        assertEquals(new File(expectedConfigDir, "codelist"), dataDirectory.getThesauriDir());
    }
}
