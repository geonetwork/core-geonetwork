package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TemplateCacheTest {

    @Test
    public void testCreateFileResult() throws Exception {
        final TemplateCache templateCache = new TemplateCache();
        templateCache.systemInfo = new SystemInfo("production", "testing");
        templateCache.init();
        final File functionFile = new File(FormatIntegrationTest.class.getResource("functions.xsl").getFile());
        final FileResult fileResult = templateCache.createFileResult(functionFile.getParentFile(), functionFile.getParentFile(),
                functionFile.getParentFile(),
                functionFile.getName(), Collections.<String, Object>emptyMap());

        assertEquals(Files.toString(functionFile, Constants.CHARSET).length(), fileResult.toString().length());
        assertNotNull(templateCache.canonicalFileNameToText.getIfPresent(functionFile.getCanonicalPath()));
    }

    @Test (expected = AssertionError.class)
    public void testTooLarge() throws Exception {
        final TemplateCache templateCache = new TemplateCache();
        templateCache.setMaxSizeKB(Integer.MAX_VALUE);
        templateCache.init();
    }
    @Test (expected = AssertionError.class)
    public void testTooSMall() throws Exception {
        final TemplateCache templateCache = new TemplateCache();
        templateCache.setMaxSizeKB(0);
        templateCache.init();
    }
}