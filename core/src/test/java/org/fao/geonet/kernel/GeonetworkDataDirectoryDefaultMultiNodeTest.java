package org.fao.geonet.kernel;

import java.io.File;

import static java.io.File.separator;
import static org.fao.geonet.kernel.GeonetworkDataDirectory.GEONETWORK_DEFAULT_DATA_DIR;

/**
 * Test the default logic of calculating the Geonetwork data directories when the node id is not the default node id.
 * <p/>
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectoryDefaultMultiNodeTest extends AbstractGeonetworkDataDirectoryTest {

    @Override
    protected String getGeonetworkNodeId() {
        return "node1";
    }

    @Override
    protected boolean isDefaultNode() {
        return false;
    }

    @Override
    protected String getDataDir() {
        return new File(getWebappDir(getClass()) + GEONETWORK_DEFAULT_DATA_DIR).getAbsolutePath() + "_" + getGeonetworkNodeId() + separator;
    }
}
