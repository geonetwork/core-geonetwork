/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es.processors.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class EsQueryFilterBuilderTest {

    @Mock
    private ServiceContext context;

    @Mock
    private NodeInfo node;

    @InjectMocks
    private EsQueryFilterBuilder esQueryFilterBuilder;

    private ObjectMapper objectMapper = new ObjectMapper();
    private MockedStatic<EsFilterBuilder> esFilterBuilderMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        esFilterBuilderMockedStatic = mockStatic(EsFilterBuilder.class);
        // Default mock for EsFilterBuilder.build
        esFilterBuilderMockedStatic.when(() -> EsFilterBuilder.build(any(), anyString(), anyBoolean(), any()))
                .thenReturn("group:1");
    }

    @After
    public void tearDown() {
        esFilterBuilderMockedStatic.close();
    }

    @Test
    public void testAddFilterToQuery_NoQuery() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        assertQueryWrappedWithFilter(esQuery);
    }

    @Test
    public void testAddFilterToQuery_NullQuery() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putNull("query");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        assertQueryWrappedWithFilter(esQuery);
    }

    @Test
    public void testAddFilterToQuery_EmptyObjectQuery() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        assertQueryWrappedWithFilter(esQuery);
    }

    private void assertQueryWrappedWithFilter(ObjectNode esQuery) {
        assertTrue(esQuery.has("query"));
        assertTrue(esQuery.get("query").has("bool"));
        JsonNode bool = esQuery.get("query").get("bool");
        assertTrue(bool.has("must"));
        assertTrue(bool.get("must").has("match_all"));
        assertTrue(bool.has("filter"));
        assertEquals("group:1", bool.get("filter").get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_TopLevelBool() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        ObjectNode boolNode = esQuery.putObject("query").putObject("bool");
        boolNode.putObject("must").putObject("match_all");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode bool = esQuery.get("query").get("bool");
        assertTrue(bool.has("filter"));
        assertEquals("group:1", bool.get("filter").get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_FunctionScore_NoInnerQuery() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query").putObject("function_score");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode functionScore = esQuery.get("query").get("function_score");
        assertTrue(functionScore.has("query"));
        JsonNode innerBool = functionScore.get("query").get("bool");
        assertNotNull(innerBool);
        assertTrue(innerBool.has("filter"));
        assertEquals("group:1", innerBool.get("filter").get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_FunctionScore_InnerBool() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        ObjectNode functionScore = esQuery.putObject("query").putObject("function_score");
        functionScore.putObject("query").putObject("bool").putObject("must").putObject("match_all");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode innerBool = esQuery.get("query").get("function_score").get("query").get("bool");
        assertTrue(innerBool.has("filter"));
        assertEquals("group:1", innerBool.get("filter").get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_FunctionScore_InnerOther() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        ObjectNode functionScore = esQuery.putObject("query").putObject("function_score");
        functionScore.putObject("query").putObject("match_all");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode innerQuery = esQuery.get("query").get("function_score").get("query");
        assertTrue(innerQuery.has("bool"));
        JsonNode innerBool = innerQuery.get("bool");
        assertTrue(innerBool.has("must"));
        assertTrue(innerBool.get("must").has("match_all"));
        assertTrue(innerBool.has("filter"));
    }

    @Test
    public void testAddFilterToQuery_OtherQueryType() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query").putObject("match_all");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode queryNode = esQuery.get("query");
        assertTrue(queryNode.has("bool"));
        JsonNode bool = queryNode.get("bool");
        assertTrue(bool.has("must"));
        assertTrue(bool.get("must").has("match_all"));
        assertTrue(bool.has("filter"));
    }

    @Test
    public void testAddFilterToQuery_ExistingFilterArray() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        ObjectNode boolNode = esQuery.putObject("query").putObject("bool");
        boolNode.putArray("filter").addObject().putObject("term").put("field", "value");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode filter = esQuery.get("query").get("bool").get("filter");
        assertTrue(filter.isArray());
        assertEquals(2, filter.size());
        assertEquals("group:1", filter.get(1).get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_ExistingFilterObject() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        ObjectNode boolNode = esQuery.putObject("query").putObject("bool");
        boolNode.putObject("filter").putObject("term").put("field", "value");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode filter = esQuery.get("query").get("bool").get("filter");
        assertTrue(filter.isArray());
        assertEquals(2, filter.size());
        assertEquals("group:1", filter.get(1).get("query_string").get("query").asText());
    }

    @Test
    public void testAddFilterToQuery_DraftDetection() throws Exception {
        // Use a more realistic query structure
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query").putObject("term").put("draft", true);

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        esFilterBuilderMockedStatic.verify(() -> EsFilterBuilder.build(any(), anyString(), eq(true), any()));

        ObjectNode esQuery2 = objectMapper.createObjectNode();
        esQuery2.putObject("query").putObject("match_all");
        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery2);

        esFilterBuilderMockedStatic.verify(() -> EsFilterBuilder.build(any(), anyString(), eq(false), any()));
    }

    @Test
    public void testAddFilterToQuery_GlobalAggregations() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query").putObject("match_all");
        ObjectNode aggs = esQuery.putObject("aggs");
        ObjectNode globalAgg = aggs.putObject("all_docs");
        globalAgg.putObject("global");
        globalAgg.putObject("aggs").putObject("avg_size").putObject("avg").put("field", "size");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode allDocs = esQuery.get("aggs").get("all_docs");
        assertFalse("global should be removed", allDocs.has("global"));
        assertTrue("filter should be added", allDocs.has("filter"));
        assertEquals("group:1", allDocs.get("filter").get("query_string").get("query").asText());
        assertTrue("sub-aggs should be preserved", allDocs.has("aggs"));
        assertTrue(allDocs.get("aggs").has("avg_size"));
    }

    @Test
    public void testAddFilterToQuery_NestedGlobalAggregations() throws Exception {
        ObjectNode esQuery = objectMapper.createObjectNode();
        esQuery.putObject("query").putObject("match_all");
        ObjectNode aggregations = esQuery.putObject("aggregations");
        ObjectNode topAgg = aggregations.putObject("by_type");
        topAgg.putObject("terms").put("field", "type");
        ObjectNode subAggs = topAgg.putObject("aggregations");
        ObjectNode nestedGlobal = subAggs.putObject("global_data");
        nestedGlobal.putObject("global");

        esQueryFilterBuilder.addFilterToQuery(context, objectMapper, esQuery);

        JsonNode globalData = esQuery.get("aggregations").get("by_type").get("aggregations").get("global_data");
        assertFalse(globalData.has("global"));
        assertTrue(globalData.has("filter"));
        assertEquals("group:1", globalData.get("filter").get("query_string").get("query").asText());
    }
}
