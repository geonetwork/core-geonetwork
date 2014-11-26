package org.fao.geonet;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GeonetTestFixtureTest extends AbstractCoreIntegrationTest {

    @Autowired
    GeonetTestFixture testFixture;

    @Test
    public void testSetup() throws Exception {
        testFixture.setup(this);
        testFixture.assertCorrectDataDir();
    }
}