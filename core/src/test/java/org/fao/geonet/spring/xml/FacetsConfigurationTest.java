package org.fao.geonet.spring.xml;

import static org.junit.Assert.assertEquals;

import org.fao.geonet.kernel.search.classifier.Split;
import org.fao.geonet.kernel.search.classifier.Value;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.facet.Facets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FacetsConfigurationTest {

    @Autowired
    private Facets facets;

    @Test 
    public void testMandatoryParametersSet() {
        Dimension keyword = facets.getDimensions().get(0);
        assertEquals("keyword", keyword.getName());
        assertEquals("keyword_eng", keyword.getName("eng"));
        assertEquals("keyword", keyword.getIndexKey());
        assertEquals("Keywords", keyword.getLabel());
    }

    @Test
    public void testDefaultClassifierSet() throws Exception {
        Dimension keyword = facets.getDimensions().get(0);
        assertEquals(Value.class, keyword.getClassifier().getClass());
    }

    @Test
    public void testSetClassifier() throws Exception {
        Dimension keywordToken = facets.getDimensions().get(1);
        assertEquals(Split.class, keywordToken.getClassifier().getClass());
    }

    @Test
    public void testGetFacetFieldName() throws Exception {
        Dimension keywordToken = facets.getDimensions().get(1);
        assertEquals("keywordToken_facet", keywordToken.getFacetFieldName("eng"));
    }
}
