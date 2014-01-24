package org.fao.geonet.kernel.harvest.harvester.csw;

import org.fao.geonet.kernel.harvest.MockRequestFactoryGeonet;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Integration Test for the Csw Harvester class.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:01 PM
 */
public class CswHarvesterIntegrationNoOwnerTest extends CswHarvesterIntegrationTest {

    @Autowired
    private CswHarvester _harvester;

    protected void customizeParams(Element params) {
        addCswSpecificParams(params);
        params.getChild("site").getChild("ownerId").detach();
    }

    @Override
    protected int getExpectedAdded() {
        return 0;
    }

    @Override
    protected void mockHttpRequests(MockRequestFactoryGeonet bean) {
        // no requests will be made
    }

    @Override
    protected void assertExpectedErrors(List errors) {
        assertFalse(errors.isEmpty());
    }
}
