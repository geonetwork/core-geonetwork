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

import com.google.common.collect.Maps;

import org.fao.geonet.SystemInfo;
import org.fao.geonet.api.records.formatters.groovy.Environment;
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.api.records.formatters.groovy.Handlers;
import org.fao.geonet.api.records.formatters.groovy.TransformationContext;
import org.fao.geonet.utils.IO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.util.Map;

/**
 * @author Jesse on 12/3/2014.
 */
public class TNodeIncludeTest extends AbstractTemplateParserTest {
    Handlers handlers;
    Functions functions;
    Environment env;

    @Before
    public void setUp() throws Exception {
        handlers = Mockito.mock(Handlers.class);
        functions = Mockito.mock(Functions.class);
        env = Mockito.mock(Environment.class);

        FileResult result = Mockito.mock(FileResult.class);
        Mockito.when(result.toString()).thenReturn("<div>included</div>");
        Mockito.when(handlers.fileResult(Mockito.anyString(), Mockito.anyMap())).thenReturn(result);

        new TransformationContext(handlers, functions, env).setThreadLocal();

    }

    @Test
    public void testInclude() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("include-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        String expected = "<html>\n"
            + "    <div><div>included</div></div>\n"
            + "</html>";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test
    public void testIncludeReplace() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TNodeRepeatTest.class.getResource("include-template-replace.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        String expected = "<html>\n"
            + "    <div>included</div>\n"
            + "</html>";
        assertCorrectRender(parseTree, model, expected);
    }
}
