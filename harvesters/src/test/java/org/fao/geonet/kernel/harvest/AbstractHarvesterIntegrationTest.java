package org.fao.geonet.kernel.harvest;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.repository.HarvestHistoryRepository;
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


    private final String _harvesterType;

    public AbstractHarvesterIntegrationTest(String harvesterType) {
        this._harvesterType = harvesterType;
    }
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

        Element params = createHarvesterParams(_harvesterType);
        customizeParams(params);
        final String harvesterUuid = _harvestManager.addHarvesterReturnUUID(params);
        AbstractHarvester _harvester = _harvestManager.getHarvester(harvesterUuid);
        _harvester.init(params, context);

        _harvester.invoke();
        final Element result = _harvester.getResult();
        assertEqualsText(""+getExpectedAdded(), result, "added");
        assertEqualsText(""+ getExpectedTotalFound(), result, "total");
        assertEqualsText(""+ getExpectedBadFormat(), result, "badFormat");
        assertEqualsText(""+ getExpectedDoesNotValidate(), result, "doesNotValidate");
        assertEqualsText(""+ getExpectedUnknownSchema(), result, "unknownSchema");
        assertEqualsText(""+ getExpectedUpdated(), result, "updated");
        assertEqualsText(""+ getExpectedRemoved(), result, "removed");

        assertExpectedErrors(_harvester.getErrors());

        _requestFactory.assertAllRequestsCalled();

        assertEquals(1, _harvestHistoryRepository.count());

        performExtraAssertions(_harvester);
    }

    protected void performExtraAssertions(AbstractHarvester harvester) {
        // no extras by default
    }

    protected int getExpectedTotalFound() { return 0; }
    protected int getExpectedAdded() { return 0; }
    protected int getExpectedBadFormat() { return 0; }
    protected int getExpectedDoesNotValidate() { return 0; }
    protected int getExpectedUnknownSchema() { return 0; }
    protected int getExpectedUpdated() { return 0; }
    protected int getExpectedRemoved() { return 0; }

    protected void assertExpectedErrors(List errors) {
        assertEquals (0, errors.size());
    }

    protected abstract void mockHttpRequests(MockRequestFactoryGeonet bean) throws Exception;
    protected abstract void customizeParams(Element params);


}
