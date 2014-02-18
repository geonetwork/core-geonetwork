package org.fao.geonet.kernel;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;

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
        System.setProperty(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, new File(_dataDirContainer,
                "node1NonDefaultDataDir").getAbsolutePath());
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
    protected String getDataDir() {
        return new File(_dataDirContainer, "node1NonDefaultDataDir").getAbsolutePath() + "_" + getGeonetworkNodeId() + File.separator;
    }


}
