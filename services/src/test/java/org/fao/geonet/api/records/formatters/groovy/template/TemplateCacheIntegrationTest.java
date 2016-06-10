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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.fao.geonet.api.records.formatters.AbstractFormatterTest;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.api.records.formatters.FormatterConstants.SCHEMA_PLUGIN_FORMATTER_DIR;
import static org.junit.Assert.assertTrue;

public class TemplateCacheIntegrationTest extends AbstractServiceIntegrationTest {
    @Autowired
    private TemplateCache templateCache;
    @Autowired
    private SchemaManager schemaManager;

    @Test
    public void testOnlineResource() throws Exception {
        final Path schemaDir = schemaManager.getSchemaDir(ISO19139SchemaPlugin.IDENTIFIER);
        Path rootFormatterDir = schemaDir.resolve(SCHEMA_PLUGIN_FORMATTER_DIR);
        Path formatterDir = Paths.get(AbstractFormatterTest.class.getResource("template-cache-test").toURI());

        Map<String, Object> model = Maps.newHashMap();
        List<Map<String, Object>> links = Lists.newArrayList();
        links.add(newLink("href&1", "name&1", "desc&1"));
        links.add(newLink("href2", null, "desc2"));
        links.add(newLink(null, "name3", "desc3"));
        links.add(newLink(null, "name4", null));
        model.put("links", links);

        FileResult fileResult = templateCache.createFileResult(formatterDir, schemaDir, rootFormatterDir,
            "html/online-resource.html", model);

        final String finalResult = fileResult.toString();
        assertTrue(finalResult, finalResult.contains("href&amp;1"));
        assertTrue(finalResult, finalResult.contains("name&amp;1"));
        assertTrue(finalResult, finalResult.contains("desc&amp;1"));
        assertTrue(finalResult, finalResult.contains("href2"));
        assertTrue(finalResult, finalResult.contains("desc2"));
        assertTrue(finalResult, finalResult.contains("name3"));
        assertTrue(finalResult, finalResult.contains("desc3"));
        assertTrue(finalResult, finalResult.contains("name4"));
    }

    private Map<String, Object> newLink(String href, String name, String desc) {
        final HashMap<String, Object> model = Maps.newHashMap();
        model.put("href", href);
        model.put("name", name);
        model.put("desc", desc);
        return model;
    }
}
