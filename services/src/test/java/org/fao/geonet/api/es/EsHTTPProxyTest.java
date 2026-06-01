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

package org.fao.geonet.api.es;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.repository.UserGroupRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EsHTTPProxyTest {

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private UserGroupRepository userGroupRepository;

    @InjectMocks
    private EsHTTPProxy esHTTPProxy = new EsHTTPProxy();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ApplicationContextHolder.set(applicationContext);
        when(applicationContext.getBean(UserGroupRepository.class)).thenReturn(userGroupRepository);
    }

    @Test
    public void testProcessMetadataSchemaFiltersAuthenticated() throws Exception {
        // 1. Setup
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(true);

        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        // Mock MetadataSchema
        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter authenticatedFilter = new MetadataSchemaOperationFilter(null, "$.someField", null);
        when(mds.getOperationFilter("authenticated")).thenReturn(authenticatedFilter);

        // 2. Call a private method using reflection
        Method method = EsHTTPProxy.class.getDeclaredMethod("processMetadataSchemaFilters", ServiceContext.class, MetadataSchema.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(esHTTPProxy, context, mds, doc);

        // 3. Assertions for authenticated user
        assertTrue("someField should exist when user is authenticated", doc.get("_source").has("someField"));

        // --- Test case where the user is NOT authenticated ---
        when(userSession.isAuthenticated()).thenReturn(false);

        // re-create doc
        doc = mapper.createObjectNode();
        source = mapper.createObjectNode();
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        method.invoke(esHTTPProxy, context, mds, doc);

        assertFalse("someField should be filtered when user is not authenticated", doc.get("_source").has("someField"));
    }

    /**
     * When the search body omits the "query" field, addFilterToQuery must still inject the ACL filter into a
     * freshly synthesised bool query so that authorization is enforced.
     */
    @Test
    public void testAddFilterToQueryInjectsAclWhenQueryFieldIsMissing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("size", 0);

        invokeAddFilterToQuery(body, mapper);

        assertAclFilterPresent(body, "missing query");
        assertTrue("Unrelated fields must be preserved", body.has("size"));
    }

    @Test
    public void testAddFilterToQueryInjectsAclWhenQueryIsExplicitNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.putNull("query");

        invokeAddFilterToQuery(body, mapper);

        assertAclFilterPresent(body, "query: null");
    }

    @Test
    public void testAddFilterToQueryInjectsAclWhenQueryIsEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.set("query", mapper.createObjectNode());

        invokeAddFilterToQuery(body, mapper);

        assertAclFilterPresent(body, "query: {}");
    }

    @Test
    public void testAddFilterToQueryPreservesExistingBoolQuery() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = (ObjectNode) mapper.readTree(
            "{\"query\":{\"bool\":{\"must\":[{\"match\":{\"any\":\"foo\"}}]}}}");

        invokeAddFilterToQuery(body, mapper);

        JsonNode boolNode = body.path("query").path("bool");
        assertTrue("must clause preserved", boolNode.path("must").isArray());
        assertEquals(1, boolNode.path("must").size());
        assertNotNull("filter clause must be added", boolNode.get("filter"));
        assertTrue("filter must reference *:* permission",
            boolNode.get("filter").toString().contains("*:*"));
    }

    @Test
    public void testAddFilterToQueryWithEmptyFilterObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = (ObjectNode) mapper.readTree(
            "{\"size\":0,\"track_total_hits\":true,\"query\":{\"bool\":{\"must\":{\"query_string\":{\"query\":\"+isTemplate:n\"}},\"filter\":{}}}," +
                "\"aggs\":{\"cl_topic.key\":{\"terms\":{\"field\":\"cl_topic.key\",\"size\":20}}}}");
        invokeAddFilterToQuery(body, mapper);
        JsonNode boolNode = body.path("query").path("bool");
        JsonNode filterNode = boolNode.get("filter");
        assertNotNull("filter clause must be present", filterNode);
        // {} is not a valid ES query clause; it must not appear anywhere in the filter
        assertFalse("filter must not be an empty object", filterNode.isObject() && filterNode.isEmpty());
        if (filterNode.isArray()) {
            for (JsonNode elem : filterNode) {
                assertFalse("filter array must not contain empty objects", elem.isObject() && elem.isEmpty());
            }
        }
        assertTrue("filter must reference *:* permission", filterNode.toString().contains("*:*"));
    }

    @Test
    public void testAddFilterToQueryReplacesGlobalAggregation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = (ObjectNode) mapper.readTree(
            "{\"size\":0,\"aggs\":{\"leak\":{\"global\":{},\"aggs\":{\"titles\":{\"terms\":{\"field\":\"resourceTitle.keyword\"}}}}}}");

        invokeAddFilterToQuery(body, mapper);

        assertAclFilterPresent(body, "global agg: query must still be injected");

        JsonNode leakAgg = body.path("aggs").path("leak");
        assertFalse("global key must be removed", leakAgg.has("global"));
        assertNotNull("filter key must replace global", leakAgg.get("filter"));
        assertTrue("replacement filter must reference *:* permission",
            leakAgg.get("filter").toString().contains("*:*"));
        assertNotNull("nested sub-aggs must be preserved", leakAgg.get("aggs"));
    }

    @Test
    public void testAddFilterToQueryReplacesGlobalAggregationUsingAggregationsKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = (ObjectNode) mapper.readTree(
            "{\"size\":0,\"aggregations\":{\"g\":{\"global\":{}}}}");

        invokeAddFilterToQuery(body, mapper);

        JsonNode gAgg = body.path("aggregations").path("g");
        assertFalse("global key must be removed (aggregations key variant)", gAgg.has("global"));
        assertNotNull("filter key must replace global (aggregations key variant)", gAgg.get("filter"));
    }

    private void invokeAddFilterToQuery(ObjectNode body, ObjectMapper mapper) throws Exception {
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        // Administrator profile short-circuits EsFilterBuilder.buildPermissionsFilter
        // to "*:*" without touching the AccessManager static fields.
        when(userSession.getProfile()).thenReturn(Profile.Administrator);
        context.setUserSession(userSession);

        Method method = EsHTTPProxy.class.getDeclaredMethod("addFilterToQuery",
            ServiceContext.class, ObjectMapper.class, JsonNode.class);
        method.setAccessible(true);
        method.invoke(esHTTPProxy, context, mapper, body);
    }

    private void assertAclFilterPresent(ObjectNode body, String caseLabel) {
        JsonNode queryNode = body.get("query");
        assertNotNull(caseLabel + ": query field must be present after filter injection", queryNode);
        JsonNode boolNode = queryNode.get("bool");
        assertNotNull(caseLabel + ": query.bool wrapper must be present", boolNode);
        JsonNode filterNode = boolNode.get("filter");
        assertNotNull(caseLabel + ": query.bool.filter clause must be present", filterNode);
        assertTrue(caseLabel + ": filter must reference *:* permission for admin profile",
            filterNode.toString().contains("*:*"));
    }
}
