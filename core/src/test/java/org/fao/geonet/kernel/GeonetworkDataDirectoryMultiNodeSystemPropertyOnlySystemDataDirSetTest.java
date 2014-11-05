package org.fao.geonet.kernel;

import org.fao.geonet.utils.IO;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and the system data directory
 * is set to something other than the default via the general property ({@link org.fao.geonet.kernel
 * .GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectoryMultiNodeSystemPropertyOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {

    @Before
    public void setSystemProperties() {
        final String dataDir = testFixture.getDataDirContainer().resolve("node1NonDefaultDataDir").toAbsolutePath().normalize().toString();
        System.setProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, dataDir);
    }

    @After
    public void resetSystemProperties() {
        System.clearProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY);
    }
    @Override
    protected String getGeonetworkNodeId() {
        return "node1";
    }
    @Override
    protected boolean isDefaultNode() {
        return false;
    }


    /**
     * Get The expected data directory
     */
    @Override
    protected Path getDataDir() {
        return IO.toPath(testFixture.getDataDirContainer().resolve("node1NonDefaultDataDir").toAbsolutePath().normalize() + "_" + getGeonetworkNodeId());
    }


}
