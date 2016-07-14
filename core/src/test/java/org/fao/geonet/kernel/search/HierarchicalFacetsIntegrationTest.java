/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search;

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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test hierarchical facets
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

        for (String keyword : METADATA_KEYWORD) {
            TEST_METADATA.add(metadataTemplate.replace("{keyword}", keyword));
        }
    }

    private static String loadTemplate() throws IOException {
        URL url = AbstractLanguageSearchOrderIntegrationTest.class.getResource("templated-keyword.iso19139.xml");
        return IOUtils.toString(url, "UTF-8");
    }

    @After
    public void closeSearcher() throws Exception {
        if (this.luceneSearcher != null) {
            this.luceneSearcher.close();
        }
    }

    @Before
    public void setup2() throws Exception {
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
    @Ignore("Test fails about 50% of the time in a multi-threaded maven build.  The facets are somehow not completely cleared between tests.")
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
    @Ignore("Test fails about 50% of the time in a multi-threaded maven build.  The facets are somehow not completely cleared between tests.")
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
    @Ignore("Test fails about 50% of the time in a multi-threaded maven build.  The facets are somehow not completely cleared between tests.")
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
