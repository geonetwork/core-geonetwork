package org.fao.geonet.services.main;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.*;

/**
 * Test the SearchSuggestion Service
 * Created by Jesse on 2/4/14.
 */
public class SearchSuggestionIntegrationTest extends AbstractServiceIntegrationTest {
    private static final SearchSuggestion searchSuggestionService = new SearchSuggestion();
    private ServiceContext context;

    @BeforeClass
    public static void initService() throws Exception {
        searchSuggestionService.init("", new ServiceConfig(Lists.newArrayList(
                createServiceConfigParam(SearchSuggestion.CONFIG_PARAM_DEFAULT_SEARCH_FIELD, "any"),
                createServiceConfigParam(SearchSuggestion.CONFIG_PARAM_MAX_NUMBER_OF_TERMS, "10000000"),
                createServiceConfigParam(SearchSuggestion.CONFIG_PARAM_THRESHOLD, "1000")
        )));

    }

    @Before
    public void importSampleMetadata() throws Exception {
        this.context = createServiceContext();
        loginAsAdmin(context);

        final MEFLibIntegrationTest.ImportMetadata importMetadata = new MEFLibIntegrationTest.ImportMetadata(this, context);
        importMetadata.getMefFilesToLoad().add("mef2-example-2md.zip");
        importMetadata.invoke();
    }

    @Test
    public void testExec_INDEX_TERM_VALUES_sortBy_Frequency() throws Exception {
        final Element params = createParams(
                read(SearchSuggestion.PARAM_FIELD, "any"),
                read(SearchSuggestion.PARAM_Q, "Aus"),
                read(SearchSuggestion.PARAM_MAX_NUMBER_OF_TERMS, 100000),
                read(SearchSuggestion.PARAM_ORIGIN, SearchSuggestion.INDEX_TERM_VALUES),
                read(SearchSuggestion.PARAM_THRESHOLD, 1),
                read(SearchSuggestion.PARAM_SORT_BY, SearchSuggestion.SORT_BY_OPTION.FREQUENCY)
        );


        List<Element> items = performQuery(params);
        assertEquals(Lists.transform(items, new Function<Element, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Element input) {
                return input.getAttributeValue(SearchSuggestion.ATT_TERM);
            }
        }).toString() + "Has different values than expected", 3, items.size());
        assertDecreasingFrequency(items);

        params.getChild(SearchSuggestion.PARAM_Q).setText("vic");
        items = performQuery(params);
        assertEquals(8, items.size());
        assertDecreasingFrequency(items);

        params.getChild(SearchSuggestion.PARAM_Q).setText("vic*");
        items = performQuery(params);
        assertEquals(4, items.size());
        for (Element item : items) {
            assertTrue(item.getAttributeValue(SearchSuggestion.ATT_TERM).startsWith("vic"));
        }
        assertDecreasingFrequency(items);

    }

    @Test
    public void testExec_INDEX_TERM_VALUES_sortBy_STARTSWITHFIRST() throws Exception {
        final Element params = createParams(
                read(SearchSuggestion.PARAM_FIELD, "any"),
                read(SearchSuggestion.PARAM_Q, "Aus"),
                read(SearchSuggestion.PARAM_MAX_NUMBER_OF_TERMS, 100000),
                read(SearchSuggestion.PARAM_ORIGIN, SearchSuggestion.INDEX_TERM_VALUES),
                read(SearchSuggestion.PARAM_THRESHOLD, 1),
                read(SearchSuggestion.PARAM_SORT_BY, SearchSuggestion.SORT_BY_OPTION.STARTSWITHFIRST)
        );

        final String searchTerm = "vic";
        params.getChild(SearchSuggestion.PARAM_Q).setText(searchTerm);
        List<Element> items = performQuery(params);
        assertEquals(8, items.size());

        boolean startsWith = true;
        for (Element item : items) {
            String term = item.getAttributeValue(SearchSuggestion.ATT_TERM);
            if (!startsWith) {
                assertFalse(term.startsWith(searchTerm));
            } else {
                startsWith = term.startsWith(searchTerm);
            }
        }
    }

    @Test
    public void testExec_INDEX_TERM_VALUES_sortBy_ALPHA() throws Exception {
        final Element params = createParams(
                read(SearchSuggestion.PARAM_FIELD, "any"),
                read(SearchSuggestion.PARAM_Q, "Aus"),
                read(SearchSuggestion.PARAM_MAX_NUMBER_OF_TERMS, 100000),
                read(SearchSuggestion.PARAM_ORIGIN, SearchSuggestion.INDEX_TERM_VALUES),
                read(SearchSuggestion.PARAM_THRESHOLD, 1),
                read(SearchSuggestion.PARAM_SORT_BY, SearchSuggestion.SORT_BY_OPTION.ALPHA)
        );

        final String searchTerm = "vic";
        params.getChild(SearchSuggestion.PARAM_Q).setText(searchTerm);
        List<Element> items = performQuery(params);
        assertEquals(8, items.size());

        String lastTerm = null;
        for (Element item : items) {
            String term = item.getAttributeValue(SearchSuggestion.ATT_TERM);
            if (lastTerm != null) {
                assertTrue(lastTerm.compareTo(term) < 1);
            }
            lastTerm = term;
        }
    }

    @Test
    public void testExec_RECORDS_FIELD_VALUES_sortBy_FREQUENCY() throws Exception {
        final String searchTerm = "*vic*";
        final Element params = createParams(
                read(SearchSuggestion.PARAM_FIELD, "title"),
                read(SearchSuggestion.PARAM_Q, searchTerm),
                read(SearchSuggestion.PARAM_MAX_NUMBER_OF_TERMS, 100000),
                read(SearchSuggestion.PARAM_ORIGIN, SearchSuggestion.RECORDS_FIELD_VALUES),
                read(SearchSuggestion.PARAM_THRESHOLD, 1),
                read(SearchSuggestion.PARAM_SORT_BY, SearchSuggestion.SORT_BY_OPTION.FREQUENCY)
        );

        List<Element> items = performQuery(params);
        assertDecreasingFrequency(items);
    }

    private List<Element> performQuery(Element params) throws Exception {
        Element result;
        List<Element> items;
        result = searchSuggestionService.exec(params, context);

        items = result.getChildren(SearchSuggestion.ELEM_ITEM);
        return items;
    }

    private void assertDecreasingFrequency(List<Element> items) {
        int lastFrequency = Integer.MAX_VALUE;

        for (Element item : items) {
            int frequency = Integer.parseInt(item.getAttributeValue(SearchSuggestion.ATT_FREQ));
            String term = item.getAttributeValue(SearchSuggestion.ATT_TERM);

            assertTrue(frequency <= lastFrequency);
            assertNotNull(term);
            lastFrequency = frequency;
        }
    }
}
