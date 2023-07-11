package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Objects;
import static org.junit.Assert.*;

public class ElasticSearchIndexingTest extends AbstractIntegrationTestWithMockedSingletons {

    @Autowired
    private EsSearchManager searchManager;

    @Autowired
    private SettingManager settingManager;

    private ServiceContext serviceContext;

    @Before
    public void setUp() throws Exception {
        serviceContext = createServiceContext();
        settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);
    }

    @Test
    public void complexDatesAreIndexedCheck() throws Exception {
        // GIVEN
        AbstractMetadata dbInsertedSimpleDataMetadata = loadMetadataWithTemporalExtentUsingSimpleDates();
        validateIndexedExpectedData(dbInsertedSimpleDataMetadata, "forest", 1);
        validateIndexedExpectedData(dbInsertedSimpleDataMetadata, "holocene", 0);
        URL dateResource = AbstractCoreIntegrationTest.class.getResource("kernel/holocene.xml");
        Element dateElement = Xml.loadStream(Objects.requireNonNull(dateResource).openStream());

        // WHEN
        AbstractMetadata dbInsertedMetadata = insertTemplateResourceInDb(serviceContext, dateElement);

        //THEN
        SearchResponse response = this.searchManager.query("_id:" + dbInsertedMetadata.getUuid() + " AND resourceTitleObject.default:holocene", null, 0, 10);
        long actualHitNbr = response.getHits().getTotalHits().value;
        assertEquals(String.format("Incorrect indexation of Holocene data with complex date due to: %s and %s", response, dbInsertedMetadata), 1, actualHitNbr);
    }

    private AbstractMetadata loadMetadataWithTemporalExtentUsingSimpleDates() throws Exception {
        URL dateResource = AbstractCoreIntegrationTest.class.getResource("kernel/forest.xml");
        Element dateElement = Xml.loadStream(Objects.requireNonNull(dateResource).openStream());
        return insertTemplateResourceInDb(serviceContext, dateElement);
    }

    private void validateIndexedExpectedData(AbstractMetadata dbInsertedSimpleDataMetadata, String resourceTitle, long expectedHitNbr) throws Exception {
        SearchResponse searchResponse = this.searchManager.query("resourceTitleObject.default:" + resourceTitle, null, 0, 10);
        long actualHitNbr = searchResponse.getHits().getTotalHits().value;
        String assertionErrorMessage = "The %s data was not indexed the expected number of times due to: %s and %s";
        assertEquals(String.format(assertionErrorMessage, resourceTitle, searchResponse, dbInsertedSimpleDataMetadata), expectedHitNbr, actualHitNbr);
    }
}
