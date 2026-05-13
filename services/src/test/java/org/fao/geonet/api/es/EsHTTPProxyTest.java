/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataOperationFilterType;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.repository.UserGroupRepository;
import org.junit.Assert;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EsHTTPProxyTest {

    @Mock
    private SchemaManager schemaManager;

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
    public void testProcessMetadataSchemaFiltersGroupOwner() throws Exception {
        // 1. Setup
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getUserIdAsInt()).thenReturn(42);

        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 1);
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        // Mock MetadataSchema
        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter groupOwnerFilter = new MetadataSchemaOperationFilter(null, "$.someField", null);
        when(mds.getOperationFilter(MetadataOperationFilterType.groupOwner.name())).thenReturn(groupOwnerFilter);

        // Mock AccessManager.getGroups via UserGroupRepository
        // When user is in group 1
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(Arrays.asList(1));

        // 2. Call private method using reflection
        Method method = EsHTTPProxy.class.getDeclaredMethod("processMetadataSchemaFilters", ServiceContext.class, MetadataSchema.class, ObjectNode.class);
        method.setAccessible(true);
        method.invoke(esHTTPProxy, context, mds, doc);

        // 3. Assertions for user in group
        assertTrue("someField should exist when user is in groupOwner", doc.get("_source").has("someField"));

        // --- Test case where user is NOT in group ---
        // When user is NOT in group 1 (e.g. in group 2)
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(Arrays.asList(2));

        // re-create doc
        doc = mapper.createObjectNode();
        source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 1);
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        method.invoke(esHTTPProxy, context, mds, doc);

        assertFalse("someField should be filtered when user is not in groupOwner", doc.get("_source").has("someField"));
    }

    private static final String QUERY_FILTER = "{\"query_string\":{\"query\":\"(op0:(1)) AND (draft:n OR draft:e)\"}}";

    private static class TestableEsHTTPProxy extends EsHTTPProxy {
        @Override
        protected String buildQueryFilter(ServiceContext context, String type, boolean isSearchingForDraft) {
            return QUERY_FILTER;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private Method getAddFilterToQueryMethod() throws Exception {
        Method m = EsHTTPProxy.class.getDeclaredMethod("addFilterToQuery", ServiceContext.class, com.fasterxml.jackson.databind.ObjectMapper.class, com.fasterxml.jackson.databind.JsonNode.class);
        m.setAccessible(true);
        return m;
    }

    private JsonNode buildExpectedFilterNode() throws Exception {
        return mapper.readTree(QUERY_FILTER);
    }

    private void invokeAddFilter(EsHTTPProxy proxy, ObjectNode root) throws Exception {
        getAddFilterToQueryMethod().invoke(proxy, null, mapper, root);
    }

    private void assertAppendedFilter(ArrayNode filters, JsonNode expectedFilter) {
        Assert.assertEquals("Expected original filter + injected access filter", 2, filters.size());
        Assert.assertEquals("Injected filter should be appended as second filter", expectedFilter, filters.get(1));
    }

    @Test
    public void shouldCreateBoolQueryWithMatchAllAndAccessFilterWhenQueryIsMissing() throws Exception {
        EsHTTPProxy proxy = new TestableEsHTTPProxy();
        JsonNode expectedFilter = buildExpectedFilterNode();

        ObjectNode root = mapper.createObjectNode();

        invokeAddFilter(proxy, root);

        JsonNode query = root.get("query");
        Assert.assertNotNull("A query node should be created", query);
        JsonNode bool = query.get("bool");
        Assert.assertNotNull(bool);
        Assert.assertTrue("The bool query should contain a must clause", bool.has("must"));
        Assert.assertTrue("The bool query should contain a filter clause", bool.has("filter"));

        JsonNode must = bool.get("must");
        Assert.assertTrue("Missing query should be replaced by match_all", must.has("match_all"));
        Assert.assertEquals("Expected access filter was not injected", expectedFilter, bool.get("filter"));
    }

    @Test
    public void shouldConvertSingleBoolFilterToArrayAndAppendAccessFilter() throws Exception {
        EsHTTPProxy proxy = new TestableEsHTTPProxy();
        JsonNode expectedFilter = buildExpectedFilterNode();

        ObjectNode filterObj = mapper.createObjectNode();
        filterObj.putObject("term").put("a", "b");

        ObjectNode boolNode = mapper.createObjectNode();
        boolNode.set("must", mapper.createObjectNode().putObject("match").put("field", "value"));
        boolNode.set("filter", filterObj);

        ObjectNode root = mapper.createObjectNode();
        root.set("query", mapper.createObjectNode().set("bool", boolNode));

        invokeAddFilter(proxy, root);

        JsonNode resultingFilter = root.get("query").get("bool").get("filter");
        Assert.assertTrue("Existing object filter should be converted to array", resultingFilter.isArray());
        ArrayNode arr = (ArrayNode) resultingFilter;
        Assert.assertEquals("Original filter should stay first", filterObj, arr.get(0));
        assertAppendedFilter(arr, expectedFilter);
    }

    @Test
    public void shouldAppendAccessFilterToExistingBoolFilterArray() throws Exception {
        EsHTTPProxy proxy = new TestableEsHTTPProxy();
        JsonNode expectedFilter = buildExpectedFilterNode();

        ObjectNode existingFilter = mapper.createObjectNode();
        existingFilter.putObject("term").put("c", "d");

        ArrayNode filterArray = mapper.createArrayNode();
        filterArray.add(existingFilter);

        ObjectNode boolNode = mapper.createObjectNode();
        boolNode.set("filter", filterArray);

        ObjectNode root = mapper.createObjectNode();
        root.set("query", mapper.createObjectNode().set("bool", boolNode));

        invokeAddFilter(proxy, root);

        JsonNode resultingFilter = root.get("query").get("bool").get("filter");
        Assert.assertTrue("Filter should remain an array", resultingFilter.isArray());
        ArrayNode arr = (ArrayNode) resultingFilter;
        Assert.assertEquals(existingFilter, arr.get(0));
        assertAppendedFilter(arr, expectedFilter);
    }

    @Test
    public void shouldWrapFunctionScoreInnerQueryIntoBoolAndInjectAccessFilter() throws Exception {
        EsHTTPProxy proxy = new TestableEsHTTPProxy();
        JsonNode expectedFilter = buildExpectedFilterNode();

        ObjectNode innerQuery = mapper.createObjectNode();
        innerQuery.putObject("match").put("title", "abc");

        ObjectNode functionScore = mapper.createObjectNode();
        functionScore.set("query", innerQuery);
        functionScore.putArray("functions"); // keep valid shape

        ObjectNode root = mapper.createObjectNode();
        root.set("query", mapper.createObjectNode().set("function_score", functionScore));

        invokeAddFilter(proxy, root);

        JsonNode newFunctionQuery = root.get("query").get("function_score").get("query");
        Assert.assertTrue("function_score.query should become a bool query", newFunctionQuery.has("bool"));
        JsonNode bool = newFunctionQuery.get("bool");
        Assert.assertTrue(bool.has("must"));
        Assert.assertTrue(bool.has("filter"));
        Assert.assertEquals(innerQuery, bool.get("must"));
        Assert.assertEquals(expectedFilter, bool.get("filter"));
    }

    @Test
    public void shouldAppendAccessFilterWhenFunctionScoreInnerBoolAlreadyHasFilter() throws Exception {
        EsHTTPProxy proxy = new TestableEsHTTPProxy();
        JsonNode expectedFilter = buildExpectedFilterNode();

        ObjectNode innerBool = mapper.createObjectNode();
        innerBool.set("must", mapper.createObjectNode().putObject("match").put("f", "v"));
        innerBool.set("filter", mapper.createObjectNode().putObject("term").put("x", "y"));

        ObjectNode functionScore = mapper.createObjectNode();
        functionScore.set("query", mapper.createObjectNode().set("bool", innerBool));

        ObjectNode root = mapper.createObjectNode();
        root.set("query", mapper.createObjectNode().set("function_score", functionScore));

        invokeAddFilter(proxy, root);

        JsonNode resultingFilter = root.get("query").get("function_score").get("query").get("bool").get("filter");
        Assert.assertTrue("Inner bool filter should be converted to array", resultingFilter.isArray());
        ArrayNode arr = (ArrayNode) resultingFilter;
        assertAppendedFilter(arr, expectedFilter);
    }
}
