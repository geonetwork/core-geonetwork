package org.fao.geonet.kernel.harvest.harvester.webdav;

/**
 * Test Webdav harvesting when no validation is done.
 *
 * Created by Jesse on 1/25/14.
 */
public class WebDavHarvester_Validation_Recursive_IntegrationTest extends WebDavHarvester_Validation_NonRecursive_IntegrationTest {
    @Override
    protected boolean isRecursive() {
        return true;
    }

    @Override
    protected int getExpectedTotalFound() {
        return 5;
    }

    @Override
    protected int getExpectedAdded() {
        return 2;
    }

    @Override
    protected int getExpectedDoesNotValidate() {
        return 2;
    }
}
