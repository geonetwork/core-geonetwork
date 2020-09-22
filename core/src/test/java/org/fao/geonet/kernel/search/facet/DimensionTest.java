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

package org.fao.geonet.kernel.search.facet;

import com.google.common.collect.Sets;

import org.fao.geonet.ApplicationContextHolder;
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
    public void testDimensionSetClassifier() {
        Dimension dimension = new Dimension("test", "index", "Test");
        dimension.setClassifier(new Split(" *(-|\\|) *"));
        assertEquals(Split.class, dimension.getClassifier().getClass());
    }

    @Test
    public void testDimensionSetLocalized() {
        Dimension dimension = new Dimension("test", "index", "Test");
        ApplicationContextHolder.clear();
        final GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        applicationContext.getBeanFactory().registerSingleton("languages", Sets.newHashSet("eng", "fre", "ger"));
        dimension.setApplicationContext(applicationContext);
        dimension.setLocalized(true);
        assertEquals("test_eng", dimension.getName("eng"));
        assertEquals("test_eng_facet", dimension.getFacetFieldName("eng"));
    }

}
