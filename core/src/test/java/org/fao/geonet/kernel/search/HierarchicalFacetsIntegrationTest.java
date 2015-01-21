package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test hierarchical facets
 *
 */
public class HierarchicalFacetsIntegrationTest extends AbstractCoreIntegrationTest {

    final private static String[] METADATA_KEYWORD = {
        "Australia",
        "Zimbabwe", 
        "All fishing areas", 
        "Australia",
        "France"
    };

    final private static List<String> TEST_METADATA = new ArrayList<String>();

    @Autowired
    private SearchManager searchManager;

    private ServiceContext serviceContext;
    private MetaSearcher luceneSearcher;

    @BeforeClass
    public static void loadTestMetadata() throws IOException, JDOMException {
        String metadataTemplate = loadTemplate();

        for (String keyword: METADATA_KEYWORD) {
            TEST_METADATA.add(metadataTemplate.replace("{keyword}", keyword));
        }
    }

    private static String loadTemplate() throws IOException {
        URL url = AbstractLanguageSearchOrderIntegrationTest.class.getResource("templated-keyword.iso19139.xml");
        return IOUtils.toString(url, "UTF-8");
    }

    @Before
    public void setup() throws Exception {
        super.setup();
        serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        for (String element : TEST_METADATA) {
            byte[] bytes = element.getBytes("UTF-8");
            importMetadataXML(serviceContext, null, new ByteArrayInputStream(bytes),
                MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);
        }

        luceneSearcher = searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
    }

    @Test
    public void searchReturnsHierarchicalFacetCounts() throws Exception {
        Element request = new Element("request")
        .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
        .addContent(new Element("from").setText("1"))
        .addContent(new Element("to").setText("50"));
        ServiceConfig config = createServiceConfig("region_keyword");

        luceneSearcher.search(serviceContext, request, config);
        Element result = luceneSearcher.present(serviceContext, request, config);

        assertEquals(loadExpectedResult("search-returns-hierarchical-facet-counts.xml"), getSummary(result));
    }

    @Test
    public void searchReturnsInternationalisedHierarchicalFacetCounts() throws Exception {
        Element request = new Element("request")
        .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
        .addContent(new Element("from").setText("1"))
        .addContent(new Element("to").setText("50"))
        .addContent(new Element("requestedLanguage").setText("fre"));
        ServiceConfig config = createServiceConfig("region_keyword");

        luceneSearcher.search(serviceContext, request, config);
        Element result = luceneSearcher.present(serviceContext, request, config);

        assertEquals(loadExpectedResult("search-returns-internationalised-hierarchical-facet-counts.xml"), getSummary(result));
    }

    @Test
    public void drilldownReturnsFilteredResults() throws Exception {
        Element request = new Element("request")
        .addContent(new Element(Geonet.SearchResult.FAST).setText("true"))
        .addContent(new Element(SearchParameter.FACET_QUERY).setText("regionKeyword/http%3A%2F%2Fgeonetwork-opensource.org%2Fregions%23country/http%3A%2F%2Fgeonetwork-opensource.org%2Fregions%231220"))
        .addContent(new Element("from").setText("1"))
        .addContent(new Element("to").setText("50"));
        ServiceConfig config = createServiceConfig("region_keyword");

        luceneSearcher.search(serviceContext, request, config);
        Element result = luceneSearcher.present(serviceContext, request, config);

        assertEquals(loadExpectedResult("drilldown-returns-filtered-results.xml"), getSummary(result));
    }

    private ServiceConfig createServiceConfig(String resultType) {
        ServiceConfig config = new ServiceConfig();
        config.setValue(Geonet.SearchResult.RESULT_TYPE, resultType);
        return config;
    }

    private String loadExpectedResult(String expectedResultFileName) throws IOException, JDOMException {
        URL url = this.getClass().getResource("expected_result/" + expectedResultFileName);
        Element expectedResult = Xml.loadFile(url);
        return Xml.getString(expectedResult);
    }

    private String getSummary(Element result) {
        return Xml.getString(result.getChild("summary"));
    }

}
