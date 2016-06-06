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
