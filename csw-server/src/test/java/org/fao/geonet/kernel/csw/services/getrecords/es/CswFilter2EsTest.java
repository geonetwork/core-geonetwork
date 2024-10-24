/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fao.geonet.kernel.csw.services.getrecords.FilterParser;
import org.fao.geonet.kernel.csw.services.getrecords.IFieldMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.StringReader;

import static org.fao.geonet.kernel.csw.services.getrecords.es.EsJsonHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CswFilter2Es converts (XML-based) CSW queries into Elasticsearch queries.
 * These ES-queries are in JSON-notation. We do not want to test the resulting
 * JSON-String char-by-char as this is error-prone.<br>
 * <p>
 * Instead, we deserialize the output string back to a JSON-tree. We then
 * compare this output tree with an expected tree we built up using an
 * Elasticsearch oriented DSL.
 *
 * @author bhoefling
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CswFilter2EsTestConfiguration.class)
class CswFilter2EsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    IFieldMapper fieldMapper;

    /**
     * Builds up the following sub-tree:
     *
     * <pre>
     * {
     *   "query_string": {
     *     "query": "%s"
     * }
     * or
     * {
     *   "query_string": {
     *     "fields": ["field"]
     *     "query": "queryString"
     * }
     * </pre>
     *
     * @param field
     * @param queryString
     * @return see description.
     */
    private static ObjectNode queryStringPart(String field, String queryString) {
        final ObjectNode query = MAPPER.createObjectNode();
        final ObjectNode queryStringNode = MAPPER.createObjectNode();

        if (field != null) {
            final ArrayNode fieldValues = MAPPER.createArrayNode();
            fieldValues.add(field);
            query.set("fields", fieldValues);
        }
        query.put("query", queryString == null ? "%s" : queryString);

        queryStringNode.set("query_string", query);

        return queryStringNode;
    }

    /**
     * Builds up the following sub-tree:
     *
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
        return queryStringPart(null, null);
    }

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
    void testPropertyIsEqualTo() throws IOException {
        // TODO: When we use Java 15, convert these to nice multiline-strings with
        // triple-quotes.
        // INPUT:
        final String input =
            "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "    <PropertyIsEqualTo>\n" //
                + "          <PropertyName>Title</PropertyName>\n" //
                + "          <Literal>Hydrological</Literal>\n" //
                + "    </PropertyIsEqualTo>\n" //
                + "      </Filter>";

        // EXPECTED:
        final ObjectNode expected = EsJsonHelper.boolbdr(). //
            must(array(queryStringPart("Title", "Hydrological"))). //
            filter(queryStringPart()). //
            bld();

        assertFilterEquals(expected, input);
    }

    @Test
    void testPropertyIsEqualToSpecialChars() throws IOException {
        final String input =
            "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "    <PropertyIsEqualTo>\n" //
                + "          <PropertyName>OnlineResourceType</PropertyName>\n" //
                + "          <Literal>OGC:WMS</Literal>\n" //
                + "    </PropertyIsEqualTo>\n" //
                + "      </Filter>";

        // EXPECTED:
        final ObjectNode expected = EsJsonHelper.boolbdr(). //
            must(array(queryStringPart("OnlineResourceType", "OGC\\:WMS"))). //
            filter(queryStringPart()). //
            bld();

        assertFilterEquals(expected, input);
    }

    @Test
    void testPropertyIsLike() throws IOException {

        final String input =
            "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "    <PropertyIsLike wildCard=\"%\" singleChar=\"_\" escapeChar=\"\\\">\n" //
                + "          <PropertyName>AnyText</PropertyName>\n" //
                + "          <Literal>s\\_rvice\\%</Literal>\n" //
                + "    </PropertyIsLike>\n" //
                + "      </Filter>";

        // EXPECTED:
        final ObjectNode expected = EsJsonHelper.boolbdr(). //
            must(array(queryStringPart("AnyText", "s?rvice*"))). //
            filter(queryStringPart()). //
            bld();

        assertFilterEquals(expected, input);
    }

    @Test
    void testPropertyIsLikeSpecialChars() throws IOException {

        final String input =
            "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "    <PropertyIsLike wildCard=\"%\" singleChar=\"_\" escapeChar=\"\\\">\n" //
                + "          <PropertyName>AnyText</PropertyName>\n" //
                + "          <Literal>\"service\"</Literal>\n" //
                + "    </PropertyIsLike>\n" //
                + "      </Filter>";

        // EXPECTED:
        final ObjectNode expected = EsJsonHelper.boolbdr(). //
            must(array(queryStringPart("AnyText", "\\\"service\\\""))). //
            filter(queryStringPart()). //
            bld();

        assertFilterEquals(expected, input);


        final String input2 =
            "<Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "    <PropertyIsLike wildCard=\"%\" singleChar=\"_\" escapeChar=\"\\\">\n" //
                + "          <PropertyName>AnyText</PropertyName>\n" //
                + "          <Literal>OGC:WMS\\%</Literal>\n" //
                + "    </PropertyIsLike>\n" //
                + "      </Filter>";

        // EXPECTED:
        final ObjectNode expected2 = EsJsonHelper.boolbdr(). //
            must(array(queryStringPart("AnyText", "OGC\\:WMS*"))). //
            filter(queryStringPart()). //
            bld();

        assertFilterEquals(expected2, input2);
    }

    @Test
    void testLogicalAnd() throws IOException {

        // INPUT:
        final String input =
            "      <Filter xmlns=\"http://www.opengis.net/ogc\">\n" //
                + "        <And>\n" //
                + "      <PropertyIsEqualTo>\n" //
                + "        <PropertyName>Title</PropertyName>\n" //
                + "            <Literal>Hydrological</Literal>\n" //
                + "      </PropertyIsEqualTo>\n" //
                + "      <PropertyIsEqualTo>\n" //
                + "        <PropertyName>Title</PropertyName>\n" //
                + "            <Literal>Africa</Literal>\n" //
                + "      </PropertyIsEqualTo>\n" //
                + "    </And>\n" //
                + "      </Filter>\n";

        // EXPECTED:
        final ObjectNode expected = EsJsonHelper.boolbdr(). //
            must(
            array(
                queryStringPart("Title", "Africa"),
                queryStringPart("Title", "Hydrological"))) //
            . //
                filter(queryStringPart()). //
                bld();

        assertFilterEquals(expected, input);
    }

    @Test
    void testSpatialBBox() throws IOException {

        // INPUT:
        final String input = //
            "      <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n" //
                + "          <ogc:BBOX>\n" //
                + "            <gml:Envelope xmlns:gml=\"http://www.opengis.net/gml\">\n" //
                + "              <gml:lowerCorner>-180 -90</gml:lowerCorner>\n" //
                + "              <gml:upperCorner>180 90</gml:upperCorner>\n" //
                + "            </gml:Envelope>\n" //
                + "          </ogc:BBOX>\n" //
                + "      </ogc:Filter>\n";

        // EXPECTED:
        final ObjectNode expected = boolbdr(). //
            must(array(geoShape("geom", //
            envelope(-180d, 90d, 180d, -90d), //
            "intersects"))) //
            . //
                filter(queryStringPart()). //
                bld();

        assertFilterEquals(expected, input);
    }

    /**
     * A more complex example with AND, OR and BBox filter.
     *
     * @throws IOException
     */
    @Test
    void testFilterWithAndOrAndSpatialBBox() throws IOException {

        // INPUT:
        final String input = //
            "      <ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">\n" //
                + "        <ogc:And>\n" //
                + "          <ogc:Or>\n" //
                + "            <ogc:PropertyIsEqualTo matchCase=\"true\">\n" //
                + "              <ogc:PropertyName>Type</ogc:PropertyName>\n" //
                + "              <ogc:Literal>data</ogc:Literal>\n" //
                + "            </ogc:PropertyIsEqualTo>\n" //
                + "            <ogc:PropertyIsEqualTo matchCase=\"true\">\n" //
                + "              <ogc:PropertyName>Type</ogc:PropertyName>\n" //
                + "              <ogc:Literal>dataset</ogc:Literal>\n" //
                + "            </ogc:PropertyIsEqualTo>\n" //
                + "            <ogc:PropertyIsEqualTo matchCase=\"true\">\n" //
                + "              <ogc:PropertyName>Type</ogc:PropertyName>\n" //
                + "              <ogc:Literal>datasetcollection</ogc:Literal>\n" //
                + "            </ogc:PropertyIsEqualTo>\n" //
                + "            <ogc:PropertyIsEqualTo matchCase=\"true\">\n" //
                + "              <ogc:PropertyName>Type</ogc:PropertyName>\n" //
                + "              <ogc:Literal>series</ogc:Literal>\n" //
                + "            </ogc:PropertyIsEqualTo>\n" //
                + "          </ogc:Or>\n" //
                + "          <ogc:BBOX>\n" //
                + "            <gml:Envelope xmlns:gml=\"http://www.opengis.net/gml\">\n" //
                + "              <gml:lowerCorner>-180 -90</gml:lowerCorner>\n" //
                + "              <gml:upperCorner>180 90</gml:upperCorner>\n" //
                + "            </gml:Envelope>\n" //
                + "          </ogc:BBOX>\n" //
                + "        </ogc:And>\n" //
                + "      </ogc:Filter>";

        final ObjectNode propertiesPart = boolbdr().should(array( //
                queryStringPart("Type", "series"), //
                queryStringPart("Type", "datasetcollection"), //
                queryStringPart("Type", "dataset"), //
                queryStringPart("Type", "data")
            )) //
            .bld();

        final ObjectNode geoShapePart = geoShape("geom", //
            envelope(-180d, 90d, 180d, -90d), //
            "intersects");

        // EXPECTED:
        final ObjectNode expected = boolbdr(). //
            must(array(geoShapePart, //
            propertiesPart)) //
            . //
                filter(queryStringPart()). //
                bld();

        assertFilterEquals(expected, input);
    }

    /**
     * {@see #assertFilterEquals(JsonNode, String, String)} with
     * FilterCapabilities.VERSION_110 in use.
     *
     * @param expected
     * @param actual
     * @throws IOException
     */
    void assertFilterEquals(JsonNode expected, String actual) throws IOException {
        assertFilterEquals(expected, actual, FilterCapabilities.VERSION_110);
    }

    /**
     * Converts xml-string into OGC Filter expression using a specific filter
     * version. This Filter is then finally converted to an Elasticsearch expression
     * and checked against the expected output.
     *
     * @param expected          JsonNode representing the expected Elasticsearch
     *                          query.
     * @param actual            XML text of the OGC Filter.
     * @param filterSpecVersion see {@link FilterCapabilities}
     * @throws IOException
     */
    void assertFilterEquals(JsonNode expected, String actual, String filterSpecVersion) throws IOException {
        final Filter filter = FilterParser.parseFilter(actual, filterSpecVersion);
        final String result = CswFilter2Es.translate(filter, fieldMapper);
        assertNotNull(result);

        assertEquals(expected, MAPPER.readTree(new StringReader(result)));
    }
}
