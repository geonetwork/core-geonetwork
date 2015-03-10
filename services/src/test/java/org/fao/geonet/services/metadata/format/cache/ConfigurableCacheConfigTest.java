package org.fao.geonet.services.metadata.format.cache;

import com.google.common.collect.Sets;
import org.fao.geonet.services.metadata.format.FormatType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurableCacheConfigTest {

    @Test
    public void testAllowCachingTypeExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setTypeExceptions(Sets.newHashSet(FormatType.xml));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId", true)));
    }

    @Test
    public void testAllowCachingAllowedTypes() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setAllowedTypes(Sets.newHashSet(FormatType.html));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId", true)));
    }

    @Test
    public void testAllowCachingFormatterExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setFormatterExceptions(Sets.newHashSet("fmtId2"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.xml, "fmtId2", true)));
    }

    @Test
    public void testAllowCachingAllowedFormatters() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setFormatterIds(Sets.newHashSet("fmtId"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId2", true)));
    }

    @Test
    public void testAllowCachingLangExceptions() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setLangExceptions(Sets.newHashSet("fre"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "fre", FormatType.xml, "fmtId2", true)));
    }

    @Test
    public void testAllowCachingAllowedLangs() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setAllowedLanguages(Sets.newHashSet("eng"));
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "fre", FormatType.html, "fmtId", true)));
    }

    @Test
    public void testAllowCachingHideWithheld() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setCacheHideWithheld(false);
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", false)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
    }

    @Test
    public void testAllowCachingHideFullMetadata() throws Exception {
        final ConfigurableCacheConfig cacheConfig = new ConfigurableCacheConfig();
        cacheConfig.setCacheFullMetadata(false);
        assertTrue(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", true)));
        assertFalse(cacheConfig.allowCaching(new Key(1, "eng", FormatType.html, "fmtId", false)));
    }

}