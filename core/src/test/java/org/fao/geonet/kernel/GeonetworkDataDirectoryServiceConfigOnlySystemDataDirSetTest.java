package org.fao.geonet.kernel;

import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Test the logic of calculating the Geonetwork data directories when the node id is the default and the system data directory
 * is set to something other than the default via the general property ({@link org.fao.geonet.kernel.GeonetworkDataDirectory#GEONETWORK_DIR_KEY}.
 * <p/>
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectoryServiceConfigOnlySystemDataDirSetTest extends AbstractGeonetworkDataDirectoryTest {

    @Override
    protected ArrayList<Element> getServiceConfigParameterElements() {
        ArrayList<Element> list = super.getServiceConfigParameterElements();
        list.add(createServiceConfigParam(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getDataDir().toString()));
        return list;
    }

    /**
     * Get The expected data directory
     */
    @Override
    protected Path getDataDir() {
        return testFixture.getDataDirContainer().resolve("nonDefaultDataDir").toAbsolutePath().normalize();
    }


}
