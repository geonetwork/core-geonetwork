package org.fao.geonet.kernel;

import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and the system data directory
 * is set to something other than the default via the general property ({@link GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectorySystemPropertyOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {


    @Before
    public void setSystemProperties() {
        System.setProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getDataDir().toString());
    }

    @After
    public void resetSystemProperties() {
        System.clearProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
    }

    /**
     * Get The expected data directory
     */
    @Override
    protected Path getDataDir() {
        return testFixture.getDataDirContainer().resolve("nonDefaultDataDir").toAbsolutePath().normalize();
    }


}
