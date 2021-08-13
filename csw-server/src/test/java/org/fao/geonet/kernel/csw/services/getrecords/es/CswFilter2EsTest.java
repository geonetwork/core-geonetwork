/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.csw.services.getrecords.es;

import static org.fao.geonet.kernel.csw.services.getrecords.es.EsJsonHelper.array;
import static org.fao.geonet.kernel.csw.services.getrecords.es.EsJsonHelper.boolbdr;
import static org.fao.geonet.kernel.csw.services.getrecords.es.EsJsonHelper.match;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.fao.geonet.kernel.csw.services.getrecords.FilterParser;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * CswFilter2Es converts (XML-based) CSW queries into ElasticSearch queries.
 * These ES-queries are in JSON-notation. We do not want to test the resulting
 * JSON-String char-by-char. Instead, we create an expected object-tree,
 * deserialize the result-String and then compare them on a tree-level.
 *
 * We use Jackson to build the JSON trees.
 *
 * @author bhoefling
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CswFilter2EsTestConfiguration.class)
class CswFilter2EsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    IFieldMapper fieldMapper;

    /**
     * Example test on how to use Jackson to compare two JSON Objects.
     *
     * @throws IOException
     */
    @Test
    void testJSONParser() throws IOException {

        final JsonNode root1 = MAPPER.readTree(new StringReader("{ \"query\": 23, \"query2\": 42}"));
        final JsonNode root2 = MAPPER.readTree(new StringReader("{ \"query2\": 42, \"query\": 23 }"));
        assertEquals(root1, root2);

        final ObjectNode root3 = (ObjectNode) MAPPER.createObjectNode();
        root3.put("query", 23);
        root3.put("query2", 42);
        assertEquals(root1, root3);
    }

    @Test
    void firstSimpleTest() throws IOException {
        final String myXML = "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" + "    <PropertyIsEqualTo>\n"
                + "          <PropertyName>Title</PropertyName>\n" + "          <Literal>Hydrological</Literal>\n"
                + "    </PropertyIsEqualTo>\n" + "      </Filter>";

        final Filter filter = FilterParser.parseFilter(myXML, FilterCapabilities.VERSION_110);
        final String result = CswFilter2Es.translate(filter, fieldMapper);
        assertNotNull(result);

        // EXPECTED:
        final ObjectNode expected = boolbdr().must(array(match("Title", "Hydrological"))).filter(queryStringPart())
                .bld();

        assertEquals(expected, MAPPER.readTree(new StringReader(result)));
    }

    /**
     * <pre>
     * {
     *   "query_string": {
     *     "query": "%s"
     * }
     * </pre>
     *
     * @return see description.
     */
    private static ObjectNode queryStringPart() {
        // build the "query_string" part:
        final ObjectNode query = MAPPER.createObjectNode();
        query.put("query", "%s");

        final ObjectNode queryString = MAPPER.createObjectNode();
        queryString.set("query_string", query);
        return queryString;
    }
}
