package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.junit.Test;
import org.mockito.Mockito;

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

    @Test
    public void testFallback() throws Exception {
        final File file0 = new File(FormatIntegrationTest.class.getResource("template-cache-test/formatter/file0.html").getFile());
        final File file1 = new File(FormatIntegrationTest.class.getResource("template-cache-test/schema1/file1.html").getFile());
        final File file2 = new File(FormatIntegrationTest.class.getResource("template-cache-test/schema2/file2.html").getFile());
        final File file3 = new File(FormatIntegrationTest.class.getResource("template-cache-test/file3.html").getFile());

        final TemplateCache templateCache = new TemplateCache();
        templateCache.systemInfo = new SystemInfo("production", "testing");
        templateCache.schemaManager = Mockito.mock(SchemaManager.class);
        Mockito.when(templateCache.schemaManager.getSchemaDir("schema1")).thenReturn(file1.getParentFile().getAbsolutePath());
        Mockito.when(templateCache.schemaManager.getSchemaDir("schema2")).thenReturn(file2.getParentFile().getAbsolutePath());
        templateCache.init();

        final File schemaAndRootDir = file0.getParentFile().getParentFile();

        FileResult fileResult = templateCache.createFileResult(file0.getParentFile(), schemaAndRootDir,
                schemaAndRootDir, file0.getName(), Collections.<String, Object>emptyMap());
        assertEquals(Files.toString(file0, Constants.CHARSET).length(), fileResult.toString().length());

        fileResult = templateCache.createFileResult(file0.getParentFile(), schemaAndRootDir,
                schemaAndRootDir, file1.getName(), Collections.<String, Object>emptyMap());
        assertEquals(Files.toString(file1, Constants.CHARSET).length(), fileResult.toString().length());


        fileResult = templateCache.createFileResult(file0.getParentFile(), schemaAndRootDir,
                schemaAndRootDir, file2.getName(), Collections.<String, Object>emptyMap());
        assertEquals(Files.toString(file2, Constants.CHARSET).length(), fileResult.toString().length());


        fileResult = templateCache.createFileResult(file0.getParentFile(), schemaAndRootDir,
                schemaAndRootDir, file3.getName(), Collections.<String, Object>emptyMap());
        assertEquals(Files.toString(file3, Constants.CHARSET).length(), fileResult.toString().length());


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