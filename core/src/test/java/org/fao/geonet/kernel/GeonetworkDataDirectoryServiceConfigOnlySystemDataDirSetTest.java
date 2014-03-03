package org.fao.geonet.kernel;

import org.jdom.Element;
import org.junit.Before;

import java.io.File;
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
        list.add(createServiceConfigParam(GeonetworkDataDirectory.GEONETWORK_DIR_KEY, getDataDir()));
        return list;
    }

    /**
     * Get The expected data directory
     */
    @Override
    protected String getDataDir() {
        return new File(_dataDirContainer, "nonDefaultDataDir").getAbsolutePath() + File.separator;
    }


}
