package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * test base class for testing harvesters.
 * User: Jesse
 * Date: 10/18/13
 * Time: 4:02 PM
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:harvesters-repository-test-context.xml")
public abstract class AbstractHarvesterIntegrationTest extends AbstractHarvesterServiceIntegrationTest {
    @Autowired
    protected MockRequestFactoryGeonet _requestFactory;
    @Autowired
    protected HarvestHistoryRepository _harvestHistoryRepository;
    @Before
    public void clearRequestFactory() {
        _requestFactory.clear();
    }

    @Test
    public void testHarvest() throws Exception {
        assertEquals(0, _harvestHistoryRepository.count());
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        mockHttpRequests(_requestFactory);

        Element params = createHarvesterParams("csw");
        customizeParams(params);
        AbstractHarvester _harvester = getHarvesterUnderTest();
        _harvester.init(params, context);

        _harvester.invoke();
        assertExpectedResult(_harvester.getResult());
        assertExpectedErrors(_harvester.getErrors());

        _requestFactory.assertAllRequestsCalled();

        assertEquals(1, _harvestHistoryRepository.count());
    }

    protected void assertExpectedErrors(List errors) {
        assertEquals (0, errors.size());
    }

    protected abstract void assertExpectedResult(Element result);
    protected abstract void mockHttpRequests(MockRequestFactoryGeonet bean);
    protected abstract void customizeParams(Element params);

    protected abstract AbstractHarvester getHarvesterUnderTest();

}
