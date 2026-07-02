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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.es.ObjectNodeUtils;
import org.fao.geonet.constants.Geonet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EsQueryProcessorTest {

    @InjectMocks
    private EsQueryProcessor esQueryProcessor;

    @Mock
    private EsQueryFilterBuilder queryFilterBuilder;

    @Mock
    private ServiceContext context;

    @Mock
    private UserSession userSession;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String defaultIndex = "gn-records-default";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(esQueryProcessor, "defaultIndex", defaultIndex);
        when(context.getUserSession()).thenReturn(userSession);
    }

    @Test
    public void testProcessSingleQuery() throws Exception {
        String body = "{\"query\": {\"match_all\": {}}}";
        String result = esQueryProcessor.process(context, body, null);

        verify(queryFilterBuilder).addFilterToQuery(eq(context), any(ObjectMapper.class), any(JsonNode.class));
        JsonNode resultNode = objectMapper.readTree(result);
        assertTrue(resultNode.has("query"));
    }

    @Test
    public void testProcessQueryWithIndexNode() throws Exception {
        String body = "{\"index\": \"some-index\"}";
        String result = esQueryProcessor.process(context, body, null);

        // Should NOT call filter builder if index node is present
        verify(queryFilterBuilder, never()).addFilterToQuery(any(), any(), any());

        JsonNode resultNode = objectMapper.readTree(result);
        assertEquals(defaultIndex, resultNode.get("index").asText());
    }

    @Test
    public void testProcessMultiSearch() throws Exception {
        String body = "{\"index\": \"index1\"}\n{\"query\": {\"match_all\": {}}}";
        String result = esQueryProcessor.process(context, body, null);

        String[] lines = result.split(System.lineSeparator());
        assertEquals(2, lines.length);

        JsonNode line1 = objectMapper.readTree(lines[0]);
        assertEquals(defaultIndex, line1.get("index").asText());

        JsonNode line2 = objectMapper.readTree(lines[1]);
        assertTrue(line2.has("query"));
        verify(queryFilterBuilder, times(1)).addFilterToQuery(eq(context), any(ObjectMapper.class), any(JsonNode.class));
    }

    @Test
    public void testProcessWithSelectionBucket() throws Exception {
        String body = "{\"query\": {\"match_all\": {}}}";
        String bucket = "myBucket";
        esQueryProcessor.process(context, body, bucket);

        verify(userSession).setProperty(eq(Geonet.Session.SEARCH_REQUEST + bucket), any(JsonNode.class));
    }

    @Test
    public void testProcessWithSourceArray() throws Exception {
        String body = "{\"_source\": [\"title\", \"abstract\"]}";
        String result = esQueryProcessor.process(context, body, null);

        JsonNode resultNode = objectMapper.readTree(result);
        ArrayNode source = (ArrayNode) resultNode.get(ObjectNodeUtils.SOURCE_NODE);

        assertRequiredFields(source);
    }

    @Test
    public void testProcessWithSourceIncludes() throws Exception {
        String body = "{\"_source\": {\"includes\": [\"title\"], \"excludes\": [\"extra\"]}}";
        String result = esQueryProcessor.process(context, body, null);

        JsonNode resultNode = objectMapper.readTree(result);
        ArrayNode includes = (ArrayNode) resultNode.get(ObjectNodeUtils.SOURCE_NODE).get("includes");

        assertRequiredFields(includes);
    }

    @Test
    public void testProcessWithSourceNoIncludes() throws Exception {
        String body = "{\"_source\": {\"excludes\": [\"extra\"]}}";
        String result = esQueryProcessor.process(context, body, null);

        JsonNode resultNode = objectMapper.readTree(result);
        ObjectNode source = (ObjectNode) resultNode.get(ObjectNodeUtils.SOURCE_NODE);

        assertEquals(1, source.size());
        assertTrue(source.has("excludes"));
    }

    private void assertRequiredFields(ArrayNode array) {
        assertTrue(contains(array, "op*"));
        assertTrue(contains(array, Geonet.IndexFieldNames.SCHEMA));
        assertTrue(contains(array, Geonet.IndexFieldNames.GROUP_OWNER));
        assertTrue(contains(array, Geonet.IndexFieldNames.OWNER));
        assertTrue(contains(array, Geonet.IndexFieldNames.ID));
    }

    private boolean contains(ArrayNode array, String value) {
        for (JsonNode node : array) {
            if (node.asText().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
