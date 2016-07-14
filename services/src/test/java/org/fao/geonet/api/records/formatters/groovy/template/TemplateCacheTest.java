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

package org.fao.geonet.api.records.formatters.groovy.template;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.FormatterApiIntegrationTest;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.IO;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TemplateCacheTest {

    @Test
    public void testCreateFileResult() throws Exception {
        ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
        ApplicationContextHolder.set(context);
        Mockito.when(context.getBean(SystemInfo.class)).thenReturn(SystemInfo.createForTesting(SystemInfo.STAGE_PRODUCTION));

        final TemplateCache templateCache = new TemplateCache();
        templateCache.xmlTemplateParser = AbstractTemplateParserTest.createTestParser(SystemInfo.STAGE_TESTING);
        final Path functionFile = IO.toPath(FormatterApiIntegrationTest.class.getResource("functions.xsl").toURI());
        final FileResult fileResult = templateCache.createFileResult(functionFile.getParent(), functionFile.getParent(),
            functionFile.getParent(), functionFile.getFileName().toString(), Collections.<String, Object>emptyMap());

        assertNotNull(fileResult);
        assertNotNull(templateCache.canonicalFileNameToText.get(functionFile.toRealPath()));
    }

    @Test
    public void testFallback() throws Exception {
        String[] stagingProfiles = {SystemInfo.STAGE_TESTING, SystemInfo.STAGE_DEVELOPMENT, SystemInfo.STAGE_PRODUCTION};

        final Path file0 = IO.toPath(FormatterApiIntegrationTest.class.getResource("template-cache-test/formatter/file0.txt").toURI());
        final Path file1 = IO.toPath(FormatterApiIntegrationTest.class.getResource("template-cache-test/schema1/formatter/file1.txt").toURI());
        final Path file2 = IO.toPath(FormatterApiIntegrationTest.class.getResource("template-cache-test/schema2/formatter/file2.txt").toURI());
        final Path file3 = IO.toPath(FormatterApiIntegrationTest.class.getResource("template-cache-test/file3.txt").toURI());

        for (String profile : stagingProfiles) {
            SchemaManager schemaManager = Mockito.mock(SchemaManager.class);
            Mockito.when(schemaManager.getSchemaDir("schema1")).thenReturn(file1.getParent().getParent());
            Mockito.when(schemaManager.getSchemaDir("schema2")).thenReturn(file2.getParent().getParent());

            ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
            ApplicationContextHolder.set(context);
            Mockito.when(context.getBean(SystemInfo.class)).thenReturn(SystemInfo.createForTesting(SystemInfo.STAGE_PRODUCTION));
            Mockito.when(context.getBean(SchemaManager.class)).thenReturn(schemaManager);

            final TemplateCache templateCache = new TemplateCache();
            templateCache.xmlTemplateParser = AbstractTemplateParserTest.createTestParser(SystemInfo.STAGE_TESTING);
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
