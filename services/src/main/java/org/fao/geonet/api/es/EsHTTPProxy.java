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


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.MetadataUtils;
import org.fao.geonet.api.records.model.related.AssociatedRecord;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.MetadataSchemaOperationFilter;
import org.fao.geonet.kernel.search.EsFilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Proxy from GeoNetwork {@code /{portal}}/api} to Elasticsearch service.
 *
 * The portal and privileges are included the search provided by the user.
 */
@RequestMapping(value = {
    "/{portal}/api"
})
@Tag(name = "search",
    description = "Proxy for Elasticsearch catalog search operations")
@Controller
public class EsHTTPProxy {
    public static final String[] _validContentTypes = {
        "application/json", "text/plain"
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);
    /**
     * Privileges filter only allows
     * * op0 (ie. view operation) contains one of the ids of your groups
     */
    private static final String filterTemplate = " {\n" +
        "       \t\"query_string\": {\n" +
        "       \t\t\"query\": \"%s\"\n" +
        "       \t}\n" +
        "}";

    private static final String SEARCH_ENDPOINT = "_search";
    private static final String MULTISEARCH_ENDPOINT = "_msearch";

    @Autowired
    NodeInfo node;

    @Value("${es.index.records:gn-records}")
    private String defaultIndex;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;

    @Value("${es.proxy.headers:content-type,content-encoding,transfer-encoding}")
    private String[] proxyHeadersAllowedList;

    /**
     * Ignore list of headers handled by proxy implementation directly.
     */
    private final String[] proxyHeadersIgnoreList =  {"Content-Length"};

    @Autowired
    private EsRestClient client;

    @Autowired
    private SchemaManager schemaManager;

    public EsHTTPProxy() {
    }

    private static Integer getInteger(ObjectNode node, String name) {
        final JsonNode sub = node.get(name);
        return sub != null ? sub.asInt() : null;
    }

    private static String getString(ObjectNode node, String name) {
        final JsonNode sub = node.get(name);
        return sub != null ? sub.asText() : null;
    }

    private static String getSourceString(ObjectNode node, String name) {
        final JsonNode sub = node.get("_source").get(name);
        return sub != null ? sub.asText() : null;
    }

    private static Integer getSourceInteger(ObjectNode node, String name) {
        final JsonNode sub = node.get("_source").get(name);
        return sub != null ? sub.asInt() : null;
    }

    private static void addSelectionInfo(ObjectNode doc, Set<String> selections) {
        final String uuid = getSourceString(doc, Geonet.IndexFieldNames.UUID);
        doc.put(Edit.Info.Elem.SELECTED, selections.contains(uuid));
    }

    private static void addRelatedTypes(ObjectNode doc,
                                        RelatedItemType[] relatedTypes,
                                        ServiceContext context) {
        Map<RelatedItemType, List<AssociatedRecord>> related = null;
        try {
            related = MetadataUtils.getAssociated(
                context,
                context.getBean(IMetadataUtils.class)
                    .findOne(doc.get("_source").get("id").asText()),
                relatedTypes, 0, 1000);
        } catch (Exception e) {
            LOGGER.warn("Failed to load related types for {}. Error is: {}",
                getSourceString(doc, Geonet.IndexFieldNames.UUID),
                e.getMessage()
                );
        }
        doc.putPOJO("related", related);
    }

    public static void addUserInfo(ObjectNode doc, ServiceContext context) throws Exception {
        final Integer owner = getSourceInteger(doc, Geonet.IndexFieldNames.OWNER);
        final Integer groupOwner = getSourceInteger(doc, Geonet.IndexFieldNames.GROUP_OWNER);
        final String id = getSourceString(doc, Geonet.IndexFieldNames.ID);

        ObjectMapper objectMapper = new ObjectMapper();

        final MetadataSourceInfo sourceInfo = new MetadataSourceInfo();
        sourceInfo.setOwner(owner);
        if (groupOwner != null) {
            sourceInfo.setGroupOwner(groupOwner);
        }
        final AccessManager accessManager = context.getBean(AccessManager.class);
        final boolean isOwner = accessManager.isOwner(context, sourceInfo);
        final HashSet<ReservedOperation> operations;
        boolean canEdit = false;
        if (isOwner) {
            operations = Sets.newHashSet(Arrays.asList(ReservedOperation.values()));
            if (owner != null) {
                doc.put("ownerId", owner.intValue());
            }
        } else {
            final Collection<Integer> groups =
                accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
            final Collection<Integer> editingGroups =
                accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), true);
            operations = Sets.newHashSet();
            for (ReservedOperation operation : ReservedOperation.values()) {
                final JsonNode operationNodes = doc.get("_source").get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
                if (operationNodes != null) {
                    ArrayNode opFields = operationNodes.isArray() ? (ArrayNode) operationNodes : objectMapper.createArrayNode().add(operationNodes);
                    if (opFields != null) {
                        for (JsonNode field : opFields) {
                            final int groupId = field.asInt();
                            if (operation == ReservedOperation.editing
                                && !canEdit
                                && editingGroups.contains(groupId)) {
                                canEdit = true;
                            }

                            if (groups.contains(groupId)) {
                                operations.add(operation);
                            }
                        }
                    }
                }
            }
        }
        doc.put(Edit.Info.Elem.EDIT, isOwner || canEdit);
        doc.put(Edit.Info.Elem.REVIEW,
            id != null && accessManager.hasReviewPermission(context, id));
        doc.put(Edit.Info.Elem.OWNER, isOwner);
        doc.put(Edit.Info.Elem.IS_PUBLISHED_TO_ALL, hasOperation(doc, ReservedGroup.all, ReservedOperation.view));
        addReservedOperation(doc, operations, ReservedOperation.view);
        addReservedOperation(doc, operations, ReservedOperation.notify);
        addReservedOperation(doc, operations, ReservedOperation.download);
        addReservedOperation(doc, operations, ReservedOperation.dynamic);
        addReservedOperation(doc, operations, ReservedOperation.featured);

        if (!operations.contains(ReservedOperation.download)) {
            doc.put(Edit.Info.Elem.GUEST_DOWNLOAD, hasOperation(doc, ReservedGroup.guest, ReservedOperation.download));
        }
    }

    private static void addReservedOperation(ObjectNode doc, HashSet<ReservedOperation> operations,
                                             ReservedOperation kind) {
        doc.put(kind.name(), operations.contains(kind));
    }

    private static boolean hasOperation(ObjectNode doc, ReservedGroup group, ReservedOperation operation) {
        ObjectMapper objectMapper = new ObjectMapper();
        int groupId = group.getId();
        final JsonNode operationNodes = doc.get("_source").get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
        if (operationNodes != null) {
            ArrayNode opFields = operationNodes.isArray() ? (ArrayNode) operationNodes : objectMapper.createArrayNode().add(operationNodes);
            if (opFields != null) {
                for (JsonNode field : opFields) {
                    if (groupId == field.asInt()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Execute a search query and get back search hits that match the query.",
        description = "The search API execute a search query with a JSON request body. For more information see https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html for search parameters, and https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html JSON Query DSL.")
    @RequestMapping(value = "/search/records/_search",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")))
    })
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void search(
        @RequestParam(defaultValue = SelectionManager.SELECTION_BUCKET)
        String bucket,
        @Parameter(description = "Type of related resource. If none, no associated resource returned.")
        @RequestParam(name = "relatedType", defaultValue = "")
            RelatedItemType[] relatedTypes,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
        HttpServletResponse response,
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON request based on Elasticsearch API.",
            content = @Content(examples = {
                @ExampleObject(value = "{\"query\":{\"match\":{\"_id\":\"catalogue_uuid\"}}}")
            }))
        String body,
        @Parameter(hidden = true)
        HttpEntity<String> httpEntity) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        call(context, httpSession, request, response, SEARCH_ENDPOINT, body, bucket, relatedTypes);
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Executes several searches with a Elasticsearch API request.",
        description = "The multi search API executes several searches from a single API request. See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html for search parameters, and https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html Query DSL.")
    @RequestMapping(value = "/search/records/_msearch",
        method = RequestMethod.POST,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE},
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results.",
            content = {
                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")),
                @Content(mediaType = MediaType.APPLICATION_NDJSON_VALUE, schema = @Schema(type = "string"))
            }
        )
    })
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void msearch(
        @RequestParam(defaultValue = SelectionManager.SELECTION_METADATA)
        String bucket,
        @Parameter(description = "Type of related resource. If none, no associated resource returned.")
        @RequestParam(name = "relatedType", defaultValue = "")
            RelatedItemType[] relatedTypes,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
        HttpServletResponse response,
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON request based on Elasticsearch API.",
            content = @Content(examples = {
                @ExampleObject(value = "{\"query\":{\"match\":{\"_id\":\"catalogue_uuid\"}}}")
            }))
        String body) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        call(context, httpSession, request, response, MULTISEARCH_ENDPOINT, body, bucket, relatedTypes);
    }


    @Hidden
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Elasticsearch proxy endpoint",
        description = "Endpoint to allow access to more ES API" +
            " only allowed to Administrator. Currently not" +
            " used by the user interface. Needs improvements in the proxy call.")
    @RequestMapping(value = "/search/records/{endPoint}",
        method = {
            RequestMethod.POST, RequestMethod.GET
        },
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")))
    })
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ResponseBody
    public void call(
        @RequestParam(defaultValue = SelectionManager.SELECTION_BUCKET)
        String bucket,
        @Parameter(description = "'_search' for search service.")
        @PathVariable String endPoint,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
        HttpServletResponse response,
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON request based on Elasticsearch API.",
            content = @Content(examples = {
                @ExampleObject(value = "{\"query\":{\"match\":{\"_id\":\"catalogue_uuid\"}}}")
            }))
        String body,
        @Parameter(hidden = true)
        HttpEntity<String> httpEntity) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);
        call(context, httpSession, request, response, endPoint, body, bucket, null);
    }

    private void call(ServiceContext context, HttpSession httpSession, HttpServletRequest request,
                      HttpServletResponse response,
                      String endPoint, String body,
                      String selectionBucket,
                      RelatedItemType[] relatedTypes) throws Exception {
        final String url = client.getServerUrl() + "/" + defaultIndex + "/" + endPoint + "?";
        // Make query on multiple indices
//        final String url = client.getServerUrl() + "/" + defaultIndex + ",gn-features/" + endPoint + "?";
        if (SEARCH_ENDPOINT.equals(endPoint) || MULTISEARCH_ENDPOINT.equals(endPoint)) {
            UserSession session = context.getUserSession();
            ObjectMapper objectMapper = new ObjectMapper();

            // multisearch support
            final MappingIterator<Object> mappingIterator = objectMapper.readerFor(JsonNode.class).readValues(body);
            StringBuilder requestBody = new StringBuilder();
            while (mappingIterator.hasNextValue()) {
                JsonNode node = (JsonNode) mappingIterator.nextValue();
                final JsonNode indexNode = node.get("index");
                if (indexNode != null) {
                    ((ObjectNode) node).put("index", defaultIndex);
                } else {
                    addFilterToQuery(context, objectMapper, node);
                    if (selectionBucket != null) {
                        // Multisearch are not supposed to work with a bucket.
                        // Only one request is store in session
                        session.setProperty(Geonet.Session.SEARCH_REQUEST + selectionBucket, node);
                    }
                    final JsonNode sourceNode = node.get("_source");
                    if (sourceNode != null) {
                        if (sourceNode.isArray()) {
                            addRequiredField((ArrayNode) sourceNode);
                        } else {
                            final JsonNode sourceIncludes = sourceNode.get("includes");
                            if (sourceIncludes != null && sourceIncludes.isArray()) {
                                addRequiredField((ArrayNode) sourceIncludes);
                            }
                        }
                    }
                }
                requestBody.append(node).append(System.lineSeparator());
            }
            handleRequest(context, httpSession, request, response, url, endPoint,
                requestBody.toString(), true, selectionBucket, relatedTypes);
        } else {
            handleRequest(context, httpSession, request, response, url, endPoint,
                body, true, selectionBucket, relatedTypes);
        }
    }

    /**
     * {@link #addUserInfo(ObjectNode, ServiceContext)}
     * rely on fields from the index. Add them to the source.
     */
    private void addRequiredField(ArrayNode source) {
        source.add("op*");
        source.add(Geonet.IndexFieldNames.SCHEMA);
        source.add(Geonet.IndexFieldNames.GROUP_OWNER);
        source.add(Geonet.IndexFieldNames.OWNER);
        source.add(Geonet.IndexFieldNames.ID);
    }

    private void addFilterToQuery(ServiceContext context,
                                  ObjectMapper objectMapper,
                                  JsonNode esQuery) throws Exception {

        // Build filter node
        String esFilter = buildQueryFilter(context,
            "",
            esQuery.toString().contains("\"draft\":"));
        JsonNode nodeFilter = objectMapper.readTree(esFilter);

        JsonNode queryNode = esQuery.get("query");


        // Replace any "global" aggregation with a "filter" aggregation scoped to
        // the ACL filter.
        // Must run after nodeFilter is built, before any branch exits early via return.
        for (String aggsKey : new String[]{"aggs", "aggregations"}) {
            JsonNode aggsNode = esQuery.get(aggsKey);
            if (aggsNode != null && aggsNode.isObject()) {
                replaceGlobalAggregations((ObjectNode) aggsNode, nodeFilter);
            }
        }
        // Defensive: if no "query", create a bool { must: match_all, filter: nodeFilter }
        if (queryNode == null || queryNode.isNull()
            || (queryNode.isObject() && queryNode.isEmpty())) {
            ObjectNode boolNode = objectMapper.createObjectNode();
            // prefer must = match_all object (same shape as existing code)
            ObjectNode matchAll = objectMapper.createObjectNode();
            matchAll.putObject("match_all");
            boolNode.set("must", matchAll);
            boolNode.set("filter", nodeFilter);
            ((ObjectNode) esQuery).set("query", objectMapper.createObjectNode().set("bool", boolNode));
            return;
        }

        // Try to find the boolean node where to insert the filter.
        // Prefer function_score.query.bool if present because function_score
        // needs the filter to be applied to the inner query used for scoring.
        JsonNode functionScoreNode = queryNode.get("function_score");
        if (functionScoreNode != null && functionScoreNode.isObject()) {
            JsonNode innerQuery = functionScoreNode.get("query");
            if (innerQuery == null || innerQuery.isNull()) {
                // create function_score.query.bool with only the filter
                ObjectNode boolNode = objectMapper.createObjectNode();
                boolNode.set("filter", nodeFilter);
                ((ObjectNode) functionScoreNode).set("query", objectMapper.createObjectNode().set("bool", boolNode));
                return;
            }

            JsonNode innerBool = innerQuery.get("bool");
            if (innerBool != null && innerBool.isObject()) {
                insertFilter((ObjectNode) innerBool, nodeFilter);
                return;
            }

            // innerQuery exists but isn't a bool -> wrap it into a bool { must: <old>, filter: <new> }
            ObjectNode newBool = objectMapper.createObjectNode();
            newBool.set("must", innerQuery.deepCopy());
            newBool.set("filter", nodeFilter);
            ((ObjectNode) functionScoreNode).set("query", objectMapper.createObjectNode().set("bool", newBool));
            return;
        }

        // Top-level bool
        JsonNode boolNode = queryNode.get("bool");
        if (boolNode != null && boolNode.isObject()) {
            insertFilter((ObjectNode) boolNode, nodeFilter);
            return;
        }

        // Other query shapes: wrap existing query into a bool { must: <existing query>, filter: nodeFilter }
        ObjectNode copy = queryNode.deepCopy();
        ObjectNode objectNodeBool = objectMapper.createObjectNode();
        objectNodeBool.set("must", copy);
        objectNodeBool.set("filter", nodeFilter);

        // Replace the existing "query" content with the new bool
        ((ObjectNode) queryNode).removeAll();
        ((ObjectNode) queryNode).set("bool", objectNodeBool);
    }

    private void replaceGlobalAggregations(ObjectNode aggsNode, JsonNode aclFilter) {
        aggsNode.fields().forEachRemaining(entry -> {
            JsonNode aggDef = entry.getValue();
            if (!aggDef.isObject()) {
                return;
            }
            ObjectNode aggDefObj = (ObjectNode) aggDef;
            if (aggDefObj.has("global")) {
                // "global" ignores the query scope; swap it for a filter-scoped bucket.
                aggDefObj.remove("global");
                aggDefObj.set("filter", aclFilter);
            }
            // Recurse into nested sub-aggregations.
            for (String subKey : new String[]{"aggs", "aggregations"}) {
                JsonNode sub = aggDefObj.get(subKey);
                if (sub != null && sub.isObject()) {
                    replaceGlobalAggregations((ObjectNode) sub, aclFilter);
                }
            }
        });
    }



    private void insertFilter(ObjectNode objectNode, JsonNode nodeFilter) {
        JsonNode filter = objectNode.get("filter");
        if (filter == null || filter.isNull() || (filter.isObject() && filter.isEmpty())) {
            objectNode.set("filter", nodeFilter);
        } else if (filter.isArray()) {
            ((ArrayNode) filter).add(nodeFilter);
        } else {
            // existing filter is an object (or other non-array) -> convert to array preserving both
            ArrayNode arr = JsonNodeFactory.instance.arrayNode();
            arr.add(filter);
            arr.add(nodeFilter);
            objectNode.set("filter", arr);
        }
    }

    /**
     * Add search privilege criteria to a query.
     */
    protected String buildQueryFilter(ServiceContext context, String type, boolean isSearchingForDraft) throws Exception {
        return String.format(filterTemplate,
            EsFilterBuilder.build(context, type, isSearchingForDraft, node));

    }

    private void handleRequest(ServiceContext context,
                               HttpSession httpSession,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               String sUrl,
                               String endPoint,
                               String requestBody,
                               boolean addPermissions,
                               String selectionBucket,
                               RelatedItemType[] relatedTypes) throws Exception {
        try {
            URL url = new URL(sUrl);

            // open communication between proxy and final host
            // all actions before the connection can be taken now
            HttpURLConnection connectionWithFinalHost = (HttpURLConnection) url.openConnection();
            try {
                connectionWithFinalHost.setRequestMethod(request.getMethod());

                // copy headers from client's request to request that will be send to the final host
                copyHeadersToConnection(request, connectionWithFinalHost);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    String auth = username + ":" + password;
                    byte[] encodedAuth = Base64.getEncoder().encode(
                        auth.getBytes(StandardCharsets.UTF_8));
                    String authHeaderValue = "Basic " + new String(encodedAuth);
                    connectionWithFinalHost.setRequestProperty("Authorization", authHeaderValue);
                }

                connectionWithFinalHost.setDoOutput(true);
                connectionWithFinalHost.getOutputStream().write(requestBody.getBytes(Constants.ENCODING));

                // connect to remote host
                // interactions with the resource are enabled now
                connectionWithFinalHost.connect();

                // send remote host's response to client
                String contentEncoding = getContentEncoding(connectionWithFinalHost.getHeaderFields());

                int code = connectionWithFinalHost.getResponseCode();
                if (code != 200) {
                    InputStream errorDetails = "gzip".equalsIgnoreCase(contentEncoding) ?
                        new GZIPInputStream(connectionWithFinalHost.getErrorStream()) :
                        connectionWithFinalHost.getErrorStream();

                    response.sendError(code,
                        String.format(
                            "Error is: %s.\nRequest:\n%s.\nError:\n%s.",
                            connectionWithFinalHost.getResponseMessage(),
                            requestBody,
                            IOUtils.toString(errorDetails, StandardCharsets.UTF_8)
                        ));
                    return;
                }

                // get content type
                String contentType = connectionWithFinalHost.getContentType();
                if (contentType == null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Host url has been validated by proxy but content type given by remote host is null");
                    return;
                }

                // content type has to be valid
                if (!isContentTypeValid(contentType)) {
                    if (connectionWithFinalHost.getResponseMessage() != null) {
                        if (connectionWithFinalHost.getResponseMessage().equalsIgnoreCase("Not Found")) {
                            // content type was not valid because it was a not found page (text/html)
                            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Remote host not found");
                            return;
                        }
                    }

                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "The content type of the remote host's response \"" + contentType
                            + "\" is not allowed by the proxy rules");
                    return;
                }

                // copy headers from the remote server's response to the response to send to the client
                copyHeadersFromConnectionToResponse(response, connectionWithFinalHost, "Content-Length");

                if (!contentType.split(";")[0].equals("application/json")) {
                    addPermissions = false;
                }

                final InputStream streamFromServer;
                final OutputStream streamToClient;

                if (contentEncoding == null || !addPermissions) {
                    // A simple stream can do the job for data that is not in content encoded
                    // but also for data content encoded with a known charset
                    streamFromServer = connectionWithFinalHost.getInputStream();
                    streamToClient = response.getOutputStream();
                } else if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    // the charset is unknown and the data are compressed in gzip
                    // we add the gzip wrapper to be able to read/write the stream content
                    streamFromServer = new GZIPInputStream(connectionWithFinalHost.getInputStream());
                    streamToClient = new GZIPOutputStream(response.getOutputStream());
                } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                    // same but with deflate
                    streamFromServer = new DeflaterInputStream(connectionWithFinalHost.getInputStream());
                    streamToClient = new DeflaterOutputStream(response.getOutputStream());
                } else {
                    throw new UnsupportedOperationException("Please handle the stream when it is encoded in " + contentEncoding);
                }

                try {
                    processResponse(context, httpSession, streamFromServer, streamToClient, endPoint, selectionBucket, addPermissions, relatedTypes);
                    streamToClient.flush();
                } finally {
                    IOUtils.closeQuietly(streamFromServer);
                }
            } catch (Exception ex) {
                LOGGER.error("Error processing request", ex);
            } finally {
                connectionWithFinalHost.disconnect();
            }
        } catch (IOException e) {
            // connection problem with the host
            LOGGER.error("Error processing request", e);

            throw new Exception(
                String.format("Failed to request Es at URL %s. " +
                        "Check Es configuration.",
                    sUrl),
                e);
        }
    }

    private void processResponse(ServiceContext context, HttpSession httpSession,
                                 InputStream streamFromServer, OutputStream streamToClient,
                                 String endPoint,
                                 String bucket,
                                 boolean addPermissions,
                                 RelatedItemType[] relatedTypes) throws Exception {
        JsonParser parser = JsonStreamUtils.jsonFactory.createParser(streamFromServer);
        JsonGenerator generator = JsonStreamUtils.jsonFactory.createGenerator(streamToClient);
        parser.nextToken();  //Go to the first token

        final Set<String> selections = (addPermissions ?
            SelectionManager.getManager(ApiUtils.getUserSession(httpSession)).getSelection(bucket) : new HashSet<>());

        if (endPoint.equals(SEARCH_ENDPOINT)) {
            JsonStreamUtils.addInfoToDocs(parser, generator, doc -> {
                if (addPermissions) {
                    addUserInfo(doc, context);
                    addSelectionInfo(doc, selections);
                }

                if ((relatedTypes != null ) && (relatedTypes.length > 0)) {
                    addRelatedTypes(doc, relatedTypes, context);
                }

                if (doc.has("_source")) {
                    ObjectNode sourceNode = (ObjectNode) doc.get("_source");

                    if (sourceNode.has(Geonet.IndexFieldNames.SCHEMA)) {
                        String metadataSchema = sourceNode.get(Geonet.IndexFieldNames.SCHEMA).asText();
                        try {
                            MetadataSchema mds = schemaManager.getSchema(metadataSchema);

                            // Apply metadata schema filters to remove non-allowed fields
                            processMetadataSchemaFilters(context, mds, doc);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Failed to load metadata schema for {}. Error is: {}",
                                getSourceString(doc, Geonet.IndexFieldNames.UUID),
                                e.getMessage()
                            );
                        }
                    }

                    // Remove fields with privileges info
                    for (ReservedOperation o : ReservedOperation.values()) {
                        sourceNode.remove("op" + o.getId());
                    }

                }
            });
        } else {
            JsonStreamUtils.addInfoToDocsMSearch(parser, generator, doc -> {
                if (addPermissions) {
                    addUserInfo(doc, context);
                    addSelectionInfo(doc, selections);
                }

                if ((relatedTypes != null ) && (relatedTypes.length > 0)) {
                    addRelatedTypes(doc, relatedTypes, context);
                }

                // Remove fields with privileges info
                if (doc.has("_source")) {
                    ObjectNode sourceNode = (ObjectNode) doc.get("_source");

                    for (ReservedOperation o : ReservedOperation.values()) {
                        sourceNode.remove("op" + o.getId());
                    }
                }
            });
        }

        generator.flush();
        generator.close();
    }

    /**
     * Gets the encoding of the content sent by the remote host: extracts the
     * content-encoding header
     *
     * @param headerFields headers of the HttpURLConnection
     * @return null if not exists otherwise name of the encoding (gzip, deflate...)
     */
    private String getContentEncoding(Map<String, List<String>> headerFields) {
        for (String headerName : headerFields.keySet()) {
            if (headerName != null) {
                if ("Content-Encoding".equalsIgnoreCase(headerName)) {
                    List<String> valuesList = headerFields.get(headerName);
                    StringBuilder sBuilder = new StringBuilder();
                    valuesList.forEach(sBuilder::append);
                    return sBuilder.toString().toLowerCase();
                }
            }
        }
        return null;
    }

    /**
     * Copy headers from the connection to the response
     *
     * @param response   to copy headers in
     * @param uc         contains headers to copy
     * @param ignoreList list of headers that mustn't be copied
     */
    private void copyHeadersFromConnectionToResponse(HttpServletResponse response, HttpURLConnection uc, String... ignoreList) {
        Map<String, List<String>> map = uc.getHeaderFields();
        for (String headerName : map.keySet()) {
            if (headerName == null) {
                continue;
            }
            if (Arrays.stream(ignoreList).anyMatch(headerName::equalsIgnoreCase)) {
                // Ignore list reflects headers that are handled by ESHTTPProxy directly
                continue;
            }
            if (Arrays.stream(proxyHeadersAllowedList).noneMatch(headerName::equalsIgnoreCase)) {
                // Allow list is provided as a configuration option and may need to be adjusted
                // as Elasticsearch API changes over time.
                continue;
            }
            // concatenate all values from the header
            List<String> valuesList = map.get(headerName);
            StringBuilder sBuilder = new StringBuilder();
            valuesList.forEach(sBuilder::append);

            if ("Transfer-Encoding".equalsIgnoreCase(headerName) && "chunked".equalsIgnoreCase(sBuilder.toString())) {
                // do not write this header + value because Tomcat already assembled the chunks itself
                continue;
            }
            // add header to HttpServletResponse object
            response.addHeader(headerName, sBuilder.toString());
        }
    }

    /**
     * Copy client's headers in the request to send to the final host
     * Trick the host by hiding the proxy indirection and keep useful headers information
     *
     * @param uc Contains now headers from client request except Host
     */
    protected void copyHeadersToConnection(HttpServletRequest request, HttpURLConnection uc) {

        for (Enumeration enumHeader = request.getHeaderNames(); enumHeader.hasMoreElements(); ) {
            String headerName = (String) enumHeader.nextElement();
            String headerValue = request.getHeader(headerName);

            // copy every header except host
            if (!"host".equalsIgnoreCase(headerName) &&
                !"X-XSRF-TOKEN".equalsIgnoreCase(headerName) &&
                !"Cookie".equalsIgnoreCase(headerName)) {
                uc.setRequestProperty(headerName, headerValue);
            }
        }
    }

    /**
     * Check if the content type is accepted by the proxy
     *
     * @return true: valid; false: not valid
     */
    protected boolean isContentTypeValid(final String contentType) {

        // focus only on type, not on the text encoding
        String type = contentType.split(";")[0];
        for (String validTypeContent : EsHTTPProxy._validContentTypes) {
            if (validTypeContent.equals(type)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Process the metadata schema filters to filter out from the ElasticSearch response
     * the elements defined in the metadata schema filters.
     *
     * It uses a jsonpath to filter the elements, typically is configured with the following jsonpath, to
     * filter the ES object elements with an attribute nilReason = 'withheld'.
     *
     *  $.*[?(@.nilReason == 'withheld')]
     *
     * The metadata index process, has to define this attribute. Any element that requires to be filtered, should be
     * defined as an object in ElasticSearch.
     *
     * Example for contacts:
     *
     *  <xsl:template mode="index-contact" match="*[cit:CI_Responsibility]">
     *      ...
     *      <!-- Check if the contact has an attribute @gco:nilReason = 'withheld', added by update-fixed-info.xsl process -->
     *      <xsl:variable name="hasWithheld" select="@gco:nilReason = 'withheld'" as="xs:boolean" />
     *
     *      <xsl:element name="contact{$fieldSuffix}">
     *        <xsl:attribute name="type" select="'object'"/>{
     *        ...
     *        "address":"<xsl:value-of select="gn-fn-index:json-escape($address)"/>"
     *        <xsl:if test="$hasWithheld">
     *         ,"nilReason": "withheld"
     *        </xsl:if>
     *
     * @param mds
     * @param doc
     * @throws JsonProcessingException
     */
    private void processMetadataSchemaFilters(ServiceContext context, MetadataSchema mds, ObjectNode doc) throws JsonProcessingException {
        if (!doc.has("_source")) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode sourceNode = (ObjectNode) doc.get("_source");

        MetadataSchemaOperationFilter authenticatedFilter = mds.getOperationFilter("authenticated");

        List<String> jsonpathFilters = new ArrayList<>();

        if (authenticatedFilter != null && !context.getUserSession().isAuthenticated()) {
            jsonpathFilters.add(authenticatedFilter.getJsonpath());
        }

        MetadataSchemaOperationFilter editFilter = mds.getOperationFilter(ReservedOperation.editing);

        if (editFilter != null) {
            boolean canEdit = doc.get("edit").asBoolean();

            if (!canEdit) {
                jsonpathFilters.add(editFilter.getJsonpath());
            }
        }

        MetadataSchemaOperationFilter downloadFilter = mds.getOperationFilter(ReservedOperation.download);
        if (downloadFilter != null) {
            boolean canDownload = doc.get("download").asBoolean();

            if (!canDownload) {
                jsonpathFilters.add(downloadFilter.getJsonpath());
            }
        }

        MetadataSchemaOperationFilter dynamicFilter = mds.getOperationFilter(ReservedOperation.dynamic);
        if (dynamicFilter != null) {
            boolean canDynamic = doc.get("dynamic").asBoolean();

            if (!canDynamic) {
                jsonpathFilters.add(dynamicFilter.getJsonpath());
            }
        }

        JsonNode actualObj = filterResponseElements(mapper, sourceNode, jsonpathFilters);
        if (actualObj != null) {
            doc.set("_source", actualObj);
        }
    }
    private JsonNode filterResponseElements(ObjectMapper mapper, ObjectNode sourceNode, List<String> jsonPathFilters) throws JsonProcessingException {
        DocumentContext jsonContext = JsonPath.parse(sourceNode.toPrettyString());

        for(String jsonPath : jsonPathFilters) {
            if (StringUtils.isNotBlank(jsonPath)) {
                jsonContext = jsonContext.delete(jsonPath);
            }
        }

        return mapper.readTree(jsonContext.jsonString());
    }
}
