package org.fao.geonet.kernel.harvest.harvester.webdav;

/**
 * Test Webdav harvesting when no validation is done.
 *
 * Created by Jesse on 1/25/14.
 */
public class WebDavHarvester_NoValidation_NonRecursive_IntegrationTest extends WebDavHarvester_Validation_NonRecursive_IntegrationTest {

    @Override
    protected boolean onlyValid() {
        return false;
    }

    @Override
    protected int getExpectedAdded() {
        return 2;
    }

    @Override
    protected int getExpectedDoesNotValidate() {
        return 0;
    }
}
