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

import org.junit.Test;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextContentParserTest {

    public static void assertCorrectRender(TextBlock contents, Map<String, Object> model, String expected) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(out, model);
        contents.render(context);

        assertEquals(expected, out.toString());
    }

    public static TextContentParser createTestTextContentParser() throws InstantiationException, IllegalAccessException {
        final TextContentParser parser = new TextContentParser();
        addFilters(parser, FilterCapitalize.class, FilterEscapeXmlAttrs.class, FilterEscapeXmlContent.class, FilterLowerCase.class,
            FilterUpperCase.class, FilterGenerateUUID.class, FilterLastUUID.class);
        return parser;
    }

    private static void addFilters(TextContentParser parser, Class<? extends TextContentFilter>... filters) throws IllegalAccessException,
        InstantiationException {
        for (Class<? extends TextContentFilter> filter : filters) {
            final Component componentAnnotation = filter.getAnnotation(Component.class);
            parser.filters.put(componentAnnotation.value(), filter.newInstance());
        }
    }

    @Test
    public void testParse() throws Exception {
        final TextContentParser parser = createTestTextContentParser();

        final TextBlock contents = parser.parse("Hi {{name}}\n\n\tThis is a great test\n\nFrom {{from}}");

        Map<String, Object> model = Maps.newHashMap();
        model.put("name", "Name");
        model.put("from", "From");
        assertCorrectRender(contents, model, "Hi Name\n\n\tThis is a great test\n\nFrom From");

        model.remove("from");
        assertCorrectRender(contents, model, "Hi Name\n\n\tThis is a great test\n\nFrom from");

        model.remove("name");
        assertCorrectRender(contents, model, "Hi name\n\n\tThis is a great test\n\nFrom from");

        model.put("name", "Name");
        model.put("from", "From");
        assertCorrectRender(parser.parse("{{name}}{{from}}"), model, "NameFrom");
    }

    @Test
    public void testParseAmp() throws Exception {
        final TextContentParser parser = createTestTextContentParser();

        TextBlock contents = parser.parse("{{name}}");

        Map<String, Object> model = Maps.newHashMap();
        model.put("name", "&Name");
        assertCorrectRender(contents, model, "&Name");

        final Component annotation = FilterEscapeXmlAttrs.class.getAnnotation(Component.class);
        contents = parser.parse("{{name | " + annotation.value() + "}}");
        model.put("name", "&Name");
        assertCorrectRender(contents, model, "&amp;Name");
    }
}
