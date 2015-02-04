package org.fao.geonet.kernel.harvest.harvester.csw;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.jdom.Element;

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

    protected void customizeParams(Element params) {
        addCswSpecificParams(params, CswHarvesterIntegrationTest.OUTPUT_SCHEMA);
        params.getChild("site").getChild("ownerId").detach();
    }

    @Override
    protected void performExtraAssertions(AbstractHarvester harvester) {
        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);
        assertEquals(""+admin.getId(), harvester.getParams().getOwnerId());
    }
}
