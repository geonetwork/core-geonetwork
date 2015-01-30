package org.fao.geonet.spring.xml;

import static org.fao.geonet.kernel.search.facet.ItemConfig.DEFAULT_DEPTH;
import static org.fao.geonet.kernel.search.facet.ItemConfig.DEFAULT_MAX_KEYS;
import static org.junit.Assert.assertEquals;

import org.fao.geonet.kernel.search.TranslatorFactory;
import org.fao.geonet.kernel.search.facet.Format;
import org.fao.geonet.kernel.search.facet.ItemConfig;
import org.fao.geonet.kernel.search.facet.SortBy;
import org.fao.geonet.kernel.search.facet.SortOrder;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.fao.geonet.kernel.search.facet.SummaryTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SummaryTypesConfigurationTest {
    
    @Autowired
    private SummaryTypes summaryTypes;
    
    @Autowired
    private TranslatorFactory factory;

    @Test
    public void testMandatoryParametersSet() {
        SummaryType hits = summaryTypes.get("hits");
        assertEquals("hits", hits.getName());
        ItemConfig serviceType = hits.getItems().get(6);
        assertEquals("serviceType", serviceType.getDimension().getName());
    }

    @Test
    public void testDefaultParametersSet() {
        SummaryType hits = summaryTypes.get("hits");
        assertEquals(Format.FACET_NAME, hits.getFormat());
        ItemConfig serviceType = hits.getItems().get(6);
        assertEquals(DEFAULT_MAX_KEYS, serviceType.getMax());
        assertEquals(DEFAULT_DEPTH, serviceType.getDepth());
        assertEquals(SortBy.COUNT, serviceType.getSortBy());
        assertEquals(SortOrder.DESCENDING, serviceType.getSortOrder());
        assertEquals(DEFAULT_DEPTH, serviceType.getDepth());
    }

    @Test
    public void testDefaultParametersOverridden() {
        SummaryType hits = summaryTypes.get("hits_dimension");
        assertEquals(Format.DIMENSION, hits.getFormat());
        ItemConfig inspireTheme = hits.getItems().get(1);
        assertEquals(35, inspireTheme.getMax());
        assertEquals(SortBy.VALUE, inspireTheme.getSortBy());
        assertEquals(SortOrder.ASCENDING, inspireTheme.getSortOrder());
        assertEquals(10, inspireTheme.getDepth());
    }

}
