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

package org.fao.geonet.api.es;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;

public class JsonStreamUtils {
    public static final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    public static void filterArrayElements(JsonParser parser, JsonGenerator generator,
                                           JsonFilter callback) throws Exception {
        if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
            throw new RuntimeException("Expecting an array");
        }
        generator.writeStartArray();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            callback.apply(parser, generator);
        }
        generator.writeEndArray();
    }

    public static void filterObjectInPath(JsonParser parser, JsonGenerator generator,
                                          JsonFilter callback,
                                          String... path) throws Exception {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new RuntimeException("Expecting an object");
        }
        generator.writeStartObject();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final String name = parser.getCurrentName();
            if (name.equals(path[0])) {
                generator.writeFieldName(name);
                parser.nextToken();
                if (path.length == 1) {
                    callback.apply(parser, generator);
                } else {
                    final String[] sub = Arrays.copyOfRange(path, 1, path.length);
                    filterObjectInPath(parser, generator, callback, sub);
                }
            } else {
                generator.copyCurrentStructure(parser);
            }
        }
        generator.writeEndObject();
    }

    public static void addInfoToDocs(JsonParser parser, JsonGenerator generator, TreeFilter callback) throws Exception {
        JsonStreamUtils.filterObjectInPath(parser, generator,
            (par, gen) ->
                JsonStreamUtils.filterArrayElements(par, gen, (par1, gen1) ->
                    filterTree(parser, generator, callback)),
            "hits", "hits");
    }

    private static void filterTree(JsonParser parser, JsonGenerator generator, TreeFilter callback) throws Exception {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new RuntimeException("Expecting an object");
        }
        final JsonNode tree = parser.readValueAsTree();
        callback.apply((ObjectNode) tree);
        generator.writeTree(tree);
    }

    public interface JsonFilter {
        void apply(JsonParser parser, JsonGenerator generator) throws Exception;
    }

    public interface TreeFilter {
        void apply(ObjectNode doc) throws Exception;
    }
}
