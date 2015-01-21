package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class DimensionFormatterTest {

    private DimensionFormatter formatter; 

    @Before
    public void loadTestData() throws IOException, JDOMException {
        Dimension mockDimension = mock(Dimension.class);
        when(mockDimension.getName()).thenReturn("keyword");
        when(mockDimension.getLabel()).thenReturn("Keywords");

        formatter = new DimensionFormatter(mockDimension);
    }

    @Test
    public void testBuildDimensionTag() throws JDOMException {
        Element dimensionTag = formatter.buildDimensionTag(6);

        assertEquals("dimension", dimensionTag.getName());
        assertEquals(0, dimensionTag.getContent().size());
        assertEquals(2, dimensionTag.getAttributes().size());
        assertEquals("keyword", dimensionTag.getAttributeValue("name"));
        assertEquals("Keywords", dimensionTag.getAttributeValue("label"));
    }

    @Test
    public void testBuildCategoryTag() {
        CategorySummary result = new CategorySummary();
        result.value = "oceans";
        result.label = "Oceans";
        result.count = 3;
        
        Element categoryTag = formatter.buildCategoryTag(result);

        assertEquals("category", categoryTag.getName());
        assertEquals(0, categoryTag.getContent().size());
        assertEquals(3, categoryTag.getAttributes().size());
        assertEquals("oceans", categoryTag.getAttributeValue("value"));
        assertEquals("Oceans", categoryTag.getAttributeValue("label"));
        assertEquals("3", categoryTag.getAttributeValue("count"));
    }

}
