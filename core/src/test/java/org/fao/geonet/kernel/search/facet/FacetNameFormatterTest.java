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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class FacetNameFormatterTest {

    private FacetNameFormatter formatter;

    @Before
    public void loadTestData() throws IOException, JDOMException {
        Dimension mockDimension = mock(Dimension.class);
        when(mockDimension.getName()).thenReturn("keyword");
        when(mockDimension.getLabel()).thenReturn("keywords");

        formatter = new FacetNameFormatter(mockDimension);
    }

    @Test
    public void testBuildDimensionTag() throws JDOMException {
        Element dimensionTag = formatter.buildDimensionTag(6);

        assertEquals("keywords", dimensionTag.getName());
        assertEquals(0, dimensionTag.getContent().size());
        assertEquals(0, dimensionTag.getAttributes().size());
    }

    @Test
    public void testBuildCategoryTag() {
        CategorySummary result = new CategorySummary();
        result.value = "oceans";
        result.label = "Oceans";
        result.count = 3;

        Element categoryTag = formatter.buildCategoryTag(result);

        assertEquals("keyword", categoryTag.getName());
        assertEquals(0, categoryTag.getContent().size());
        assertEquals(3, categoryTag.getAttributes().size());
        assertEquals("oceans", categoryTag.getAttributeValue("name"));
        assertEquals("Oceans", categoryTag.getAttributeValue("label"));
        assertEquals("3", categoryTag.getAttributeValue("count"));
    }
}
