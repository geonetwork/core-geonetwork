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
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.MetadataOperationFilterType;
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

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EsHTTPProxyTest {

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private SchemaManager schemaManager;

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
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(List.of(1));

        // 2. Call a private method using reflection
        Method method = getProcessMetadataSchemaFiltersMethod();
        method.invoke(esHTTPProxy, context, mds, doc, new EsHTTPProxy.UserEditorGroups(userSession));

        // 3. Assertions for user in a group
        assertTrue("someField should exist when user is in groupOwner", doc.get("_source").has("someField"));

        // --- Test case where the user is NOT in a group ---
        // When the user is NOT in group 1 (e.g. in group 2)
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(List.of(2));

        // re-create doc
        doc = mapper.createObjectNode();
        source = mapper.createObjectNode();
        source.put(Geonet.IndexFieldNames.GROUP_OWNER, 1);
        source.put("someField", "someValue");
        doc.set("_source", source);
        doc.put("edit", false);
        doc.put("download", false);
        doc.put("dynamic", false);

        method.invoke(esHTTPProxy, context, mds, doc, new EsHTTPProxy.UserEditorGroups(userSession));

        assertFalse("someField should be filtered when user is not in groupOwner", doc.get("_source").has("someField"));
    }

    /**
     * The edit, download and dynamic flags are only added to the document when permissions are
     * requested. When they are missing, the schema filters must fall back to the restrictive
     * behaviour instead of failing.
     */
    @Test
    public void testProcessMetadataSchemaFiltersAppliesEditFilterWhenPermissionFlagsMissing() throws Exception {
        ServiceContext context = createServiceContext(new UserSession());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put("someField", "someValue");
        source.put("otherField", "keep");
        doc.set("_source", source);
        // No edit/download/dynamic flags on the doc (addPermissions=false case)

        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter editFilter = new MetadataSchemaOperationFilter(null, "$.someField", null);
        when(mds.getOperationFilter(ReservedOperation.editing)).thenReturn(editFilter);

        Method method = getProcessMetadataSchemaFiltersMethod();
        method.invoke(esHTTPProxy, context, mds, doc, new EsHTTPProxy.UserEditorGroups(context.getUserSession()));

        assertFalse("someField should be filtered when the edit flag is missing", doc.get("_source").has("someField"));
        assertTrue("otherField should be preserved", doc.get("_source").has("otherField"));
    }

    /**
     * When no filter applies, the source node must be left untouched: no JsonPath
     * serialize/deserialize round trip per document.
     */
    @Test
    public void testProcessMetadataSchemaFiltersKeepsSourceNodeWhenNoFilterApplies() throws Exception {
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(true);
        ServiceContext context = createServiceContext(userSession);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doc = mapper.createObjectNode();
        ObjectNode source = mapper.createObjectNode();
        source.put("someField", "someValue");
        doc.set("_source", source);

        MetadataSchema mds = mock(MetadataSchema.class);

        Method method = getProcessMetadataSchemaFiltersMethod();
        method.invoke(esHTTPProxy, context, mds, doc, new EsHTTPProxy.UserEditorGroups(userSession));

        assertSame("_source must be kept as-is when no filter applies", source, doc.get("_source"));
    }

    /**
     * The _source node returned to the client must only contain the expected fields,
     * also when the schema filters replace the _source node.
     */
    @Test
    public void testProcessResponseSearchProcessesSourceNode() throws Exception {
        ServiceContext context = createServiceContext(anonymousUserSession());
        mockSchemaWithAuthenticatedFilter();

        String esResponse = "{\"took\":1,\"hits\":{\"total\":{\"value\":1},\"hits\":[" + searchHit("uuid-1") + "]}}";

        JsonNode result = invokeProcessResponse(context, "_search", esResponse);

        JsonNode sourceNode = result.path("hits").path("hits").path(0).path("_source");
        assertSourceNodeProcessed(sourceNode);
    }

    @Test
    public void testProcessResponseMSearchProcessesSourceNode() throws Exception {
        ServiceContext context = createServiceContext(anonymousUserSession());
        mockSchemaWithAuthenticatedFilter();

        String esResponse = "{\"responses\":[{\"took\":1,\"hits\":{\"total\":{\"value\":1},\"hits\":["
            + searchHit("uuid-1") + "]}}]}";

        JsonNode result = invokeProcessResponse(context, "_msearch", esResponse);

        JsonNode sourceNode = result.path("responses").path(0).path("hits").path("hits").path(0).path("_source");
        assertSourceNodeProcessed(sourceNode);
    }

    /**
     * The editor groups of the user must be fetched at most once per response,
     * not once per returned document.
     */
    @Test
    public void testProcessResponseFetchesEditorGroupsOnce() throws Exception {
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(true);
        when(userSession.getUserIdAsInt()).thenReturn(42);
        ServiceContext context = createServiceContext(userSession);

        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter groupOwnerFilter = new MetadataSchemaOperationFilter(null, "$.protectedField", null);
        when(mds.getOperationFilter(MetadataOperationFilterType.groupOwner.name())).thenReturn(groupOwnerFilter);
        when(schemaManager.getSchema("iso19139")).thenReturn(mds);

        // The user is not in the groupOwner group of the documents
        when(userGroupRepository.findGroupIds(any(Specification.class))).thenReturn(List.of(2));

        String esResponse = "{\"took\":1,\"hits\":{\"total\":{\"value\":2},\"hits\":["
            + searchHit("uuid-1") + "," + searchHit("uuid-2") + "]}}";

        JsonNode result = invokeProcessResponse(context, "_search", esResponse);

        assertEquals(2, result.path("hits").path("hits").size());
        verify(userGroupRepository, times(1)).findGroupIds(any(Specification.class));
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

    @Test
    public void testMSearchAppliesDefaultIndexOnlyToHeaderAndFilterOnlyToSearchRequest() throws Exception {
        setPrivateField("defaultIndex", "gn-records");
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.getProfile()).thenReturn(Profile.Administrator);
        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        String msearchBody = "{\"index\":\"external-index\"}\n"
            + "{\"query\":{\"match_all\":{}}}\n";
        String rewrittenBody = invokeBuildSearchRequestBody(context, userSession, mapper, msearchBody, "_msearch", null);

        String[] lines = rewrittenBody.split("\\R");
        assertTrue("Expected header + request NDJSON lines", lines.length >= 2);

        JsonNode headerNode = mapper.readTree(lines[0]);
        JsonNode searchNode = mapper.readTree(lines[1]);

        assertEquals("gn-records", headerNode.path("index").asText());
        assertFalse("Header must not be rewritten into a query payload", headerNode.has("query"));
        assertFalse("Header must not receive ACL filter", headerNode.toString().contains("*:*"));

        assertTrue("Search request must receive ACL filter",
            searchNode.path("query").path("bool").path("filter").toString().contains("*:*"));
    }

    /**
     * Reproduces the reported bug: third-party clients may send a _msearch header that omits
     * the "index" field entirely (relying on the URL path's default index instead). The old
     * content-sniffing implementation (checking for an "index" key to decide header vs. body)
     * misclassified this as a search body and corrupted it with an ACL filter, producing an
     * invalid request. The position-based implementation must still recognise it as the header.
     */
    @Test
    public void testMSearchAppliesDefaultIndexToHeaderMissingIndexField() throws Exception {
        setPrivateField("defaultIndex", "gn-records");
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.getProfile()).thenReturn(Profile.Administrator);
        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        String msearchBody = "{\"search_type\":\"query_then_fetch\"}\n"
            + "{\"query\":{\"match_all\":{}}}\n";
        String rewrittenBody = invokeBuildSearchRequestBody(context, userSession, mapper, msearchBody, "_msearch", null);

        String[] lines = rewrittenBody.split("\\R");
        assertEquals("Expected exactly one header + one request line", 2, lines.length);

        JsonNode headerNode = mapper.readTree(lines[0]);
        JsonNode searchNode = mapper.readTree(lines[1]);

        assertEquals("Header without an index field must still get the default index",
            "gn-records", headerNode.path("index").asText());
        assertEquals("Other header fields must be preserved",
            "query_then_fetch", headerNode.path("search_type").asText());
        assertFalse("Header must not receive ACL filter", headerNode.toString().contains("*:*"));

        assertTrue("Search request must receive ACL filter",
            searchNode.path("query").path("bool").path("filter").toString().contains("*:*"));
    }

    /**
     * A _msearch body can contain several header/body pairs. The header/body alternation must
     * be tracked by line position across the whole request, not just for the first pair.
     */
    @Test
    public void testMSearchHandlesMultipleRequestPairs() throws Exception {
        setPrivateField("defaultIndex", "gn-records");
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.getProfile()).thenReturn(Profile.Administrator);
        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        String msearchBody = "{}\n"
            + "{\"query\":{\"match_all\":{}}}\n"
            + "{\"index\":\"external-index\"}\n"
            + "{\"query\":{\"term\":{\"field\":\"value\"}}}\n";
        String rewrittenBody = invokeBuildSearchRequestBody(context, userSession, mapper, msearchBody, "_msearch", null);

        String[] lines = rewrittenBody.split("\\R");
        assertEquals("Expected two header + request pairs", 4, lines.length);

        JsonNode header1 = mapper.readTree(lines[0]);
        JsonNode search1 = mapper.readTree(lines[1]);
        JsonNode header2 = mapper.readTree(lines[2]);
        JsonNode search2 = mapper.readTree(lines[3]);

        assertEquals("gn-records", header1.path("index").asText());
        assertTrue("First search request must receive ACL filter",
            search1.path("query").path("bool").path("filter").toString().contains("*:*"));

        assertEquals("Second header's index must also be forced to the default index, " +
            "overriding any client-supplied value", "gn-records", header2.path("index").asText());
        assertTrue("Second search request must receive ACL filter",
            search2.path("query").path("bool").path("filter").toString().contains("*:*"));
    }

    /**
     * A _msearch header line must be a JSON object. A malformed header (e.g. a bare JSON null)
     * cannot carry an "index" override, so it can't be used to bypass the default index, but it
     * must still be rejected clearly by the proxy rather than silently forwarded to Elasticsearch.
     *
     * Note: the header must be a JSON scalar such as "null" here, not "[]". A top-level JSON
     * array is special-cased by Jackson's ObjectReader#readValues(String): it is treated as the
     * single root value to unwrap into a sequence of its own elements, rather than as one NDJSON
     * line among several root-level values. An empty array therefore makes hasNextValue() return
     * false immediately, silently skipping the rest of the request body (and this test) instead
     * of reaching the code under test.
     */
    @Test
    public void testMSearchRejectsNonObjectHeader() throws Exception {
        setPrivateField("defaultIndex", "gn-records");
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        UserSession userSession = spy(new UserSession());
        when(userSession.getProfile()).thenReturn(Profile.Administrator);
        context.setUserSession(userSession);

        ObjectMapper mapper = new ObjectMapper();
        String msearchBody = "null\n"
            + "{\"query\":{\"match_all\":{}}}\n";

        try {
            invokeBuildSearchRequestBody(context, userSession, mapper, msearchBody, "_msearch", null);
            fail("Expected an exception for a non-object _msearch header");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue("Cause must be an IllegalArgumentException",
                e.getCause() instanceof IllegalArgumentException);
        }
    }

    private ServiceContext createServiceContext(UserSession userSession) {
        ServiceContext context = new ServiceContext("default", applicationContext, new HashMap<>(), null);
        context.setUserSession(userSession);
        return context;
    }

    /**
     * A bare UserSession reports itself as authenticated when there is no security context,
     * so the anonymous case must be stubbed explicitly.
     */
    private UserSession anonymousUserSession() {
        UserSession userSession = spy(new UserSession());
        when(userSession.isAuthenticated()).thenReturn(false);
        return userSession;
    }

    private Method getProcessMetadataSchemaFiltersMethod() throws NoSuchMethodException {
        Method method = EsHTTPProxy.class.getDeclaredMethod("processMetadataSchemaFilters",
            ServiceContext.class, MetadataSchema.class, ObjectNode.class, EsHTTPProxy.UserEditorGroups.class);
        method.setAccessible(true);
        return method;
    }

    private void mockSchemaWithAuthenticatedFilter() {
        MetadataSchema mds = mock(MetadataSchema.class);
        MetadataSchemaOperationFilter authenticatedFilter = new MetadataSchemaOperationFilter(null, "$.protectedField", null);
        when(mds.getOperationFilter(MetadataOperationFilterType.authenticated.name())).thenReturn(authenticatedFilter);
        when(schemaManager.getSchema("iso19139")).thenReturn(mds);
    }

    /**
     * A search hit with the internal index fields, a field covered by the schema filters
     * and a regular field.
     */
    private String searchHit(String uuid) {
        StringBuilder ops = new StringBuilder();
        for (ReservedOperation o : ReservedOperation.values()) {
            ops.append("\"op").append(o.getId()).append("\":[1],");
        }
        return "{\"_id\":\"" + uuid + "\",\"_source\":{"
            + "\"" + Geonet.IndexFieldNames.SCHEMA + "\":\"iso19139\","
            + "\"" + Geonet.IndexFieldNames.UUID + "\":\"" + uuid + "\","
            + "\"" + Geonet.IndexFieldNames.GROUP_OWNER + "\":1,"
            + ops
            + "\"protectedField\":\"secret\","
            + "\"resourceTitle\":\"Title\"}}";
    }

    private void assertSourceNodeProcessed(JsonNode sourceNode) {
        assertTrue("_source must be present in the response", sourceNode.isObject());
        for (ReservedOperation o : ReservedOperation.values()) {
            assertFalse("op" + o.getId() + " must not be present in _source", sourceNode.has("op" + o.getId()));
        }
        assertFalse("filtered element must not be present for anonymous users", sourceNode.has("protectedField"));
        assertTrue("regular fields must be preserved", sourceNode.has("resourceTitle"));
    }

    private JsonNode invokeProcessResponse(ServiceContext context, String endPoint, String esResponse) throws Exception {
        Method method = EsHTTPProxy.class.getDeclaredMethod("processResponse",
            ServiceContext.class, HttpSession.class, InputStream.class, OutputStream.class,
            String.class, String.class, boolean.class, RelatedItemType[].class);
        method.setAccessible(true);

        InputStream streamFromServer = new ByteArrayInputStream(esResponse.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream streamToClient = new ByteArrayOutputStream();

        method.invoke(esHTTPProxy, context, null, streamFromServer, streamToClient, endPoint, null, false, null);

        return new ObjectMapper().readTree(streamToClient.toString(StandardCharsets.UTF_8.name()));
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

    private String invokeBuildSearchRequestBody(ServiceContext context,
                                                UserSession session,
                                                ObjectMapper mapper,
                                                String body,
                                                String endPoint,
                                                String selectionBucket) throws Exception {
        Method method = EsHTTPProxy.class.getDeclaredMethod("buildSearchRequestBody",
            ServiceContext.class,
            UserSession.class,
            ObjectMapper.class,
            String.class,
            String.class,
            String.class);
        method.setAccessible(true);
        return (String) method.invoke(esHTTPProxy, context, session, mapper, body, endPoint, selectionBucket);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = EsHTTPProxy.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(esHTTPProxy, value);
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
