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

import org.fao.geonet.SystemInfo;
import org.fao.geonet.utils.IO;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Jesse on 12/3/2014.
 */
public class TemplateParserTest extends AbstractTemplateParserTest {
    @Test(expected = TemplateException.class)
    public void testRepeatNotMapWhenMapRequired() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("repeat-template.html");
        final TNode parseTree = parser.parse(IO.toPath(url.toURI()));

        Map<String, Object> model = Maps.newHashMap();
        model.put("maps", Lists.newArrayList(Lists.newArrayList("elem1")));
        model.put("type", "x");
        String expected = "This doesn't matter because the render should fail";
        assertCorrectRender(parseTree, model, expected);
    }

    @Test(expected = TemplateException.class)
    public void testMultipleDirectivesPerElem() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_TESTING);
        final URL url = TemplateParserTest.class.getResource("multiple-directives-per-el-template.html");
        parser.parse(IO.toPath(url.toURI()));
    }


    @Test
    public void testSpacing() throws Exception {
        final TemplateParser parser = createTestParser(SystemInfo.STAGE_PRODUCTION);
        final URL url = AbstractTemplateParserTest.class.getResource("template-spacing.html");
        final Path path = IO.toPath(url.toURI());
        final TNode parseTree = parser.parse(path);

        Map<String, Object> model = Maps.newHashMap();
        model.put("title", "Title");

        String expected = "<html>\n"
            + "<head>\n"
            + "  <title>Title</title>\n"
            + "</head>\n"
            + "</html>";
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(result, model);

        parseTree.render(context);

        assertEquals(expected + "\n" + result, expected, result.toString());
    }

}
