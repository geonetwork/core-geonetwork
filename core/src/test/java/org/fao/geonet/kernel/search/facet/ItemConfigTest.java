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

import static org.fao.geonet.kernel.search.TranslatorFactory.IDENTITY_TRANSLATOR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.fao.geonet.kernel.search.TranslatorFactory;
import org.fao.geonet.kernel.search.classifier.Split;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ItemConfigTest {
    private static final String TEST_DIMENSION = "keywordToken";
    private Dimension keywordTokenDimension;
    private TranslatorFactory mockFactory;

    @Before
    public void loadTestData() throws IOException, JDOMException {
        keywordTokenDimension = new Dimension(TEST_DIMENSION, "keyword", "Keyword Tokens");
        keywordTokenDimension.setClassifier(new Split("-| *\\| *"));
        mockFactory = mock(TranslatorFactory.class);
        when(mockFactory.getTranslator(null, "eng")).thenReturn(IDENTITY_TRANSLATOR);
    }

    @Test
    public void testItemConfigDefaults() throws JDOMException {
        ItemConfig itemConfig = new ItemConfig(keywordTokenDimension, mockFactory);
        assertEquals(keywordTokenDimension, itemConfig.getDimension());
        assertEquals(ItemConfig.DEFAULT_MAX_KEYS, itemConfig.getMax());
        assertEquals(ItemConfig.DEFAULT_DEPTH, itemConfig.getDepth());
        assertEquals(SortBy.COUNT, itemConfig.getSortBy());
        assertEquals(SortOrder.DESCENDING, itemConfig.getSortOrder());
        assertEquals(itemConfig.getTranslator("eng"), IDENTITY_TRANSLATOR);
    }

    @Test
    public void testItemConfigAll() throws JDOMException {
        ItemConfig itemConfig = new ItemConfig(keywordTokenDimension, mockFactory);
        itemConfig.setDepth(3);
        itemConfig.setMax(17);
        itemConfig.setSortBy(SortBy.NUMVALUE);
        itemConfig.setSortOrder(SortOrder.ASCENDING);
        assertEquals(keywordTokenDimension, itemConfig.getDimension());
        assertEquals(17, itemConfig.getMax());
        assertEquals(3, itemConfig.getDepth());
        assertEquals(SortBy.NUMVALUE, itemConfig.getSortBy());
        assertEquals(SortOrder.ASCENDING, itemConfig.getSortOrder());
    }
}
