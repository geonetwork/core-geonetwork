package org.fao.geonet.kernel;

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and the system data directory
 * is set to something other than the default via the general property ({@link GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectoryMultiNodeServiceConfigOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {

    @Override
    protected ArrayList<Element> getServiceConfigParameterElements() {
        ArrayList<Element> list = super.getServiceConfigParameterElements();
        list.add(createServiceConfigParam(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getBaseDir().toString()));
        return list;
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
        final Path baseDir = getBaseDir();
        return baseDir.getParent().resolve(baseDir.getFileName() + "_"+getGeonetworkNodeId());
    }

    private Path getBaseDir() {
        return testFixture.getDataDirContainer().toAbsolutePath().normalize();
    }


}
