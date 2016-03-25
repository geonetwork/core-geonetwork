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

package org.fao.geonet.spring.xml;

import static org.junit.Assert.assertEquals;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.search.classifier.Split;
import org.fao.geonet.kernel.search.classifier.Value;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.facet.Facets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FacetsConfigurationTest {

    @Autowired
    private Facets facets;
    @Autowired
    private ConfigurableApplicationContext applicationContext;


    @Before
    public void setUp() throws Exception {
        ApplicationContextHolder.set(applicationContext);

    }

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
