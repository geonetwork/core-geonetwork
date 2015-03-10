package org.fao.geonet.kernel.search.facet;

import com.google.common.collect.Sets;
import org.fao.geonet.kernel.search.classifier.Split;
import org.fao.geonet.kernel.search.classifier.Value;
import org.jdom.JDOMException;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.Assert.assertEquals;

public class DimensionTest {

    @Test
    public void testDimensionConstructor() {
        Dimension dimension = new Dimension("test", "index", "Test");
        assertEquals("test", dimension.getName());
        assertEquals("index", dimension.getIndexKey());
        assertEquals("Test", dimension.getLabel());
        assertEquals("test_facet", dimension.getFacetFieldName("eng"));
        assertEquals(Value.class, dimension.getClassifier().getClass());
    }

    @Test
    public void testDimensionSetClassifier() throws JDOMException {
        Dimension dimension = new Dimension("test", "index", "Test");
        dimension.setClassifier(new Split(" *(-|\\|) *"));
        assertEquals(Split.class, dimension.getClassifier().getClass());
    }

    @Test
    public void testDimensionSetLocalized() throws JDOMException {
        Dimension dimension = new Dimension("test", "index", "Test");
        final GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("languages", Sets.newHashSet("eng", "fre", "ger"));
        dimension.setApplicationContext(applicationContext);
        dimension.setLocalized(true);
        assertEquals("test_eng", dimension.getName("eng"));
        assertEquals("test_eng_facet", dimension.getFacetFieldName("eng"));
    }

}
