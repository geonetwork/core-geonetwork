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

package org.fao.geonet.api.records.formatters.cache;

import com.google.common.collect.Sets;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurableCacheConfigTest {

    @Before
    public void setUp() throws Exception {
        ConfigurableApplicationContext applicationContext = Mockito.mock(ConfigurableApplicationContext.class);
        ApplicationContextHolder.set(applicationContext);
        Mockito.when(applicationContext.getBean(SystemInfo.class)).thenReturn(SystemInfo.createForTesting(SystemInfo.STAGE_PRODUCTION));
    }

    @Test
    public void testAllowCachingTypeExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setTypeExceptions(Sets.newHashSet(FormatType.xml));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingAllowedTypes() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setAllowedTypes(Sets.newHashSet(FormatType.html));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingFormatterExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setFormatterExceptions(Sets.newHashSet("fmtId2"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId2", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingAllowedFormatters() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setFormatterIds(Sets.newHashSet("fmtId"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId2", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingLangExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setLangExceptions(Sets.newHashSet("fre"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "fre", FormatType.xml, "fmtId2", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingAllowedLangs() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setAllowedLanguages(Sets.newHashSet("eng"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "fre", FormatType.html, "fmtId", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingHideWithheld() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setCacheHideWithheld(false);
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", false, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
    }

    @Test
    public void testAllowCachingHideFullMetadata() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setCacheFullMetadata(false);
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true, FormatterWidth._100)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", false, FormatterWidth._100)));
    }

}
