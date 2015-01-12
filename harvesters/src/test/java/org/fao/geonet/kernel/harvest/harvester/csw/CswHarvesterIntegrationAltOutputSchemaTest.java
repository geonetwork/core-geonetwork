package org.fao.geonet.kernel.harvest.harvester.csw;

/**
 * Integration Test for the Csw Harvester class.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:01 PM
 */
public class CswHarvesterIntegrationAltOutputSchemaTest extends CswHarvesterIntegrationTest {

    public static final String ALT_SCHEMA = "http://www.opengis.net/cat/csw/2.0.2";

    @Override
    public String getOutputSchema() {
        return ALT_SCHEMA;
    }
}
