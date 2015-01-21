package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;

import org.fao.geonet.kernel.search.classifier.Split;
import org.fao.geonet.kernel.search.classifier.Value;
import org.jdom.JDOMException;
import org.junit.Test;

public class DimensionTest {

    @Test
    public void testDimensionConstructor() {
        Dimension dimension = new Dimension("test", "index", "Test");
        assertEquals("test", dimension.getName());
        assertEquals("index", dimension.getIndexKey());
        assertEquals("Test", dimension.getLabel());
        assertEquals("test_facet", dimension.getFacetFieldName());
        assertEquals(Value.class, dimension.getClassifier().getClass());
    }

    @Test
    public void testDimensionSetClassifier() throws JDOMException {
        Dimension dimension = new Dimension("test", "index", "Test");
        dimension.setClassifier(new Split(" *(-|\\|) *"));
        assertEquals(Split.class, dimension.getClassifier().getClass());
    }

}
