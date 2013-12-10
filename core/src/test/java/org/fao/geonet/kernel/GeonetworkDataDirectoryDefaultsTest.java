package org.fao.geonet.kernel;

/**
 * Test the default logic of calculating the Geonetwork data directories.
 *
 * User: Jesse
 * Date: 11/14/13
 * Time: 8:36 AM
 */
public class GeonetworkDataDirectoryDefaultsTest extends AbstractGeonetworkDataDirectoryTest {
    @Override
    protected String getDataDir() {
        return  getWebappDir(getClass())+GeonetworkDataDirectory.GEONETWORK_DEFAULT_DATA_DIR;
    }

}
