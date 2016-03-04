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

package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

public class TranslatorCachingWrapperTest {
    private Translator mockTranslator;
    private Translator cachingWrapper;

    @Before
    public void setup() {
        mockTranslator = mockTranslator("testKey", "testValue");
        cachingWrapper = new TranslatorCachingWrapper(mockTranslator);
    }

    @Test
    public void testCacheMiss() {
        String value = cachingWrapper.translate("testKey");
        assertEquals("testValue", value);
        verify(mockTranslator, times(1)).translate("testKey");
    }

    @Test
    public void testCacheHit() {
        String value = cachingWrapper.translate("testKey");
        value = cachingWrapper.translate("testKey");
        assertEquals("testValue", value);
        verify(mockTranslator, times(1)).translate("testKey");
    }

    private Translator mockTranslator(String testKey, String testValue) {
        Translator mockTranslator = mock(Translator.class);
        when(mockTranslator.translate(testKey)).thenReturn(testValue);
        return mockTranslator;
    }

}
