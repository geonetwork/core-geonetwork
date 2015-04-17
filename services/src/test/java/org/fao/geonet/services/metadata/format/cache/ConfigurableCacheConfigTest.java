package org.fao.geonet.services.metadata.format.cache;

import com.google.common.collect.Sets;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterWidth;
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