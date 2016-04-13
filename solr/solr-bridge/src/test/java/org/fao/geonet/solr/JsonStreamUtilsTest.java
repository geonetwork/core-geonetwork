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

package org.fao.geonet.solr;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class JsonStreamUtilsTest {
    private static String transform(String orig, JsonStreamUtils.JsonFilter callback) throws Exception {
        final JsonParser parser = JsonStreamUtils.jsonFactory.createJsonParser(orig);
        final StringWriter writer = new StringWriter();
        final JsonGenerator generator = JsonStreamUtils.jsonFactory.createJsonGenerator(writer);
        parser.nextToken();

        callback.apply(parser, generator);
        generator.close();
        return writer.toString();
    }

    @Test
    public void testFilterArrayElements() throws Exception {
        final String orig = "[1,3]";
        final String actual = transform(orig, (parser, generator) ->
                JsonStreamUtils.filterArrayElements(parser, generator, (par, gen) ->
                        gen.writeNumber(par.getIntValue() * 2)));

        assertEquals("[2,6]", actual);
    }

    @Test
    public void testFilterObjectInPath() throws Exception {
        final String orig = "{\"a\":{\"b\":[1,2,3],\"c\": 4}}";
        final String actual = transform(orig, (parser, generator) ->
                JsonStreamUtils.filterObjectInPath(parser, generator, (par, gen) ->
                        gen.writeNumber(par.getIntValue() * 2), "a", "c"));

        assertEquals("{\"a\":{\"b\":[1,2,3],\"c\":8}}", actual);
    }

    @Test
    public void testAddInfoToDocs() throws Exception {
        final String orig = "{\"x\":12,\"response\":{\"docs\":[{\"id\":12,\"other\":\"OTHER\"}],\"y\":13}}";
        final String actual = transform(orig, (parser, generator) ->
                JsonStreamUtils.addInfoToDocs(parser, generator, (tree) -> {
                    assertEquals(12, tree.get("id").asInt());
                    tree.put("test", "yep");
                }));

        assertEquals(
                "{\"x\":12,\"response\":{\"docs\":[{\"id\":12,\"other\":\"OTHER\",\"test\":\"yep\"}],\"y\":13}}",
                actual);
    }
}
