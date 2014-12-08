package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.services.metadata.format.FormatIntegrationTest;
import org.fao.geonet.utils.IO;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TemplateCacheTest {

    @Test
    public void testCreateFileResult() throws Exception {
        final TemplateCache templateCache = new TemplateCache();
        templateCache.systemInfo = SystemInfo.createForTesting(SystemInfo.STAGE_PRODUCTION);
        templateCache.xmlTemplateParser = AbstractTemplateParserTest.createTestParser(SystemInfo.STAGE_TESTING);
        final Path functionFile = IO.toPath(FormatIntegrationTest.class.getResource("functions.xsl").toURI());
        final FileResult fileResult = templateCache.createFileResult(functionFile.getParent(), functionFile.getParent(),
                functionFile.getParent(), functionFile.getFileName().toString(), Collections.<String, Object>emptyMap());

        assertNotNull(fileResult);
        assertNotNull(templateCache.canonicalFileNameToText.get(functionFile.toRealPath()));
    }

    @Test
    public void testFallback() throws Exception {
        String[] stagingProfiles = {SystemInfo.STAGE_TESTING, SystemInfo.STAGE_DEVELOPMENT, SystemInfo.STAGE_PRODUCTION};

        final Path file0 = IO.toPath(FormatIntegrationTest.class.getResource("template-cache-test/formatter/file0.txt").toURI());
        final Path file1 = IO.toPath(FormatIntegrationTest.class.getResource("template-cache-test/schema1/formatter/file1.txt").toURI());
        final Path file2 = IO.toPath(FormatIntegrationTest.class.getResource("template-cache-test/schema2/formatter/file2.txt").toURI());
        final Path file3 = IO.toPath(FormatIntegrationTest.class.getResource("template-cache-test/file3.txt").toURI());

        for (String profile : stagingProfiles) {
            final TemplateCache templateCache = new TemplateCache();
            templateCache.systemInfo = SystemInfo.createForTesting(profile);
            templateCache.schemaManager = Mockito.mock(SchemaManager.class);
            templateCache.xmlTemplateParser = AbstractTemplateParserTest.createTestParser(SystemInfo.STAGE_TESTING);
            Mockito.when(templateCache.schemaManager.getSchemaDir("schema1")).thenReturn(file1.getParent().getParent());
            Mockito.when(templateCache.schemaManager.getSchemaDir("schema2")).thenReturn(file2.getParent().getParent());

            final Path schemaAndRootDir = file0.getParent().getParent();

            final Map<String, Object> emptyMap = Collections.emptyMap();
            FileResult fileResult = templateCache.createFileResult(file0.getParent(), schemaAndRootDir,
                    schemaAndRootDir, file0.getFileName().toString(), emptyMap);
            assertEquals(new String(Files.readAllBytes(file0), Constants.CHARSET).length(), fileResult.toString().length());

            fileResult = templateCache.createFileResult(file0.getParent(), schemaAndRootDir,
                    schemaAndRootDir, file1.getFileName().toString(), emptyMap);
            assertEquals(new String(Files.readAllBytes(file1), Constants.CHARSET).length(), fileResult.toString().length());


            fileResult = templateCache.createFileResult(file0.getParent(), schemaAndRootDir,
                    schemaAndRootDir, file2.getFileName().toString(), emptyMap);
            assertEquals(new String(Files.readAllBytes(file2), Constants.CHARSET).length(), fileResult.toString().length());


            fileResult = templateCache.createFileResult(file0.getParent(), schemaAndRootDir,
                    schemaAndRootDir, file3.getFileName().toString(), emptyMap);
            assertEquals(new String(Files.readAllBytes(file3), Constants.CHARSET).length(), fileResult.toString().length());

        }

    }
}