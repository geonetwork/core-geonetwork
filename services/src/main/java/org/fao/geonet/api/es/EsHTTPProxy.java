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


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@RequestMapping(value = {
    "/{portal}/api",
    "/{portal}/api/" + API.VERSION_0_1
})
@Tag(name = "search",
    description = "Proxy for ElasticSearch catalog search operations")
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
    @Autowired
    AccessManager accessManager;
    @Autowired
    NodeInfo node;
    @Autowired
    SourceRepository sourceRepository;
    @Value("${es.index.records:gn-records}")
    private String defaultIndex;
    @Autowired
    private EsRestClient client;

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

    private static void addUserInfo(ObjectNode doc, ServiceContext context) throws Exception {
        final Integer owner = getSourceInteger(doc, Geonet.IndexFieldNames.OWNER);
        final Integer groupOwner = getSourceInteger(doc, Geonet.IndexFieldNames.GROUP_OWNER);

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
                                && canEdit == false
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

    @io.swagger.v3.oas.annotations.Operation(summary = "Search proxy for ElasticSearch",
        description = "See https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html")
    @RequestMapping(value = "/search/records/{endPoint}",
        method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void handlePOSTMetadata(
        @RequestParam(defaultValue = SelectionManager.SELECTION_METADATA)
            String bucket,
        @PathVariable String endPoint,
        @Parameter(hidden = true)
            HttpSession httpSession,
        @Parameter(hidden = true)
            HttpServletRequest request,
        @Parameter(hidden = true)
            HttpServletResponse response) throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);

        // Retrieve request body with ElasticSearch query and parse JSON
        String body = IOUtils.toString(request.getReader());
        call(context, httpSession, request, response, endPoint, body, bucket);
    }

    public void call(ServiceContext context, HttpSession httpSession, HttpServletRequest request,
                     HttpServletResponse response,
                     String endPoint, String body, String selectionBucket) throws Exception {
        final String url = client.getServerUrl() + "/" + defaultIndex + "/" + endPoint + "?";
        // Make query on multiple indices
//        final String url = client.getServerUrl() + "/" + defaultIndex + ",gn-features/" + endPoint + "?";
        UserSession session = context.getUserSession();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode nodeQuery = objectMapper.readTree(body);

        // multisearch support
        final MappingIterator<Object> mappingIterator = objectMapper.readerFor(JsonNode.class).readValues(body);
        StringBuffer requestBody = new StringBuffer();
        while (mappingIterator.hasNextValue()) {
            JsonNode node = (JsonNode) mappingIterator.nextValue();
            final JsonNode indexNode = node.get("index");
            if (indexNode != null) {
                ((ObjectNode) node).put("index", defaultIndex);
            } else {
                final JsonNode queryNode = node.get("query");
                if (queryNode != null) {
                    addFilterToQuery(context, objectMapper, node);
                    if (selectionBucket != null) {
                        // Multisearch are not supposed to work with a bucket.
                        // Only one request is store in session
                        session.setProperty(Geonet.Session.SEARCH_REQUEST + selectionBucket, node);
                    }
                }
                final JsonNode sourceNode = node.get("_source");
                if (sourceNode != null) {
                    final JsonNode sourceIncludes = sourceNode.get("includes");
                    if (sourceIncludes != null && sourceIncludes.isArray()) {
                        ((ArrayNode) sourceIncludes).add("op*");
                    }
                }
            }
            requestBody.append(node.toString()).append(System.lineSeparator());
        }

        handleRequest(context, httpSession, request, response, url,
            requestBody.toString(), true, selectionBucket);
    }

    private void addFilterToQuery(ServiceContext context,
                                  ObjectMapper objectMapper,
                                  JsonNode esQuery) throws Exception {

        // Build filter node
        String esFilter = buildQueryFilter(context, "");
        JsonNode nodeFilter = objectMapper.readTree(esFilter);

        JsonNode queryNode = esQuery.get("query");

//        if (queryNode == null) {
//            // Add default filter if no query provided
//            ObjectNode objectNodeQuery = objectMapper.createObjectNode();
//            objectNodeQuery.set("filter", nodeFilter);
//            ((ObjectNode) esQuery).set("query", objectNodeQuery);
//        } else
        if (queryNode.get("function_score") != null) {
            // Add filter node to the bool element of the query if provided
            ObjectNode objectNode = (ObjectNode) queryNode.get("function_score").get("query").get("bool");
            objectNode.set("filter", nodeFilter);
        } else if (queryNode.get("bool") != null) {
            // Add filter node to the bool element of the query if provided
            ObjectNode objectNode = (ObjectNode) queryNode.get("bool");
            objectNode.set("filter", nodeFilter);
        } else {
            // If no bool node in the query, create the bool node and add the query and filter nodes to it
            ObjectNode copy = esQuery.get("query").deepCopy();

            ObjectNode objectNodeBool = objectMapper.createObjectNode();
            objectNodeBool.set("must", copy);
            objectNodeBool.set("filter", nodeFilter);

            ((ObjectNode) queryNode).removeAll();
            ((ObjectNode) queryNode).set("bool", objectNodeBool);
        }
    }

    /**
     * Add search privilege criteria to a query.
     */
    private String buildQueryFilter(ServiceContext context, String type) throws Exception {
        StringBuilder query = new StringBuilder();
        query.append(buildPermissionsFilter(context).trim());

        if (type.equalsIgnoreCase("metadata")) {
            query.append(" AND (isTemplate:n)");
        } else if (type.equalsIgnoreCase("template")) {
            query.append(" AND (isTemplate:y)");
        } else if (type.equalsIgnoreCase("subtemplate")) {
            query.append(" AND (isTemplate:s)");
        }

        final String portalFilter = buildPortalFilter();
        if (!"".equals(portalFilter)) {
            query.append(" ").append(portalFilter);
        }
        return String.format(filterTemplate, query);

    }

    private String buildPortalFilter() {
        // If the requested portal define a filter
        // Add it to the request.
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Source portal = sourceRepository.findById(node.getId()).get();
            if (portal == null) {
                LOGGER.warn("Null portal " + node);
            } else if (StringUtils.isNotEmpty(portal.getFilter())) {
                LOGGER.debug("Applying portal filter: {}", portal.getFilter());
                return portal.getFilter();
            }
        }
        return "";
    }

    private String buildPermissionsFilter(ServiceContext context) throws Exception {
        final UserSession userSession = context.getUserSession();

        // If admin you can see all
        if (Profile.Administrator.equals(userSession.getProfile())) {
            return "*";
        } else {
            // op0 (ie. view operation) contains one of the ids of your groups
            Set<Integer> groups = accessManager.getUserGroups(userSession, context.getIpAddress(), false);
            final String ids = groups.stream().map(Object::toString).map(e -> e.replace("-", "\\\\-"))
                .collect(Collectors.joining(" OR "));
            String operationFilter = String.format("op%d:(%s)", ReservedOperation.view.getId(), ids);


            String ownerFilter = "";
            if (userSession.getUserIdAsInt() > 0) {
                // OR you are owner
                ownerFilter = String.format("owner:%d", userSession.getUserIdAsInt());
                // OR member of groupOwner
                // TODOES
            }
            return String.format("%s %s", operationFilter, ownerFilter).trim();
        }
    }

    private String buildDocTypeFilter(String type) {
        return "documentType:" + type;
    }

    private void handleRequest(ServiceContext context, HttpSession httpSession, HttpServletRequest request,
                               HttpServletResponse response, String sUrl,
                               String requestBody, boolean addPermissions, String selectionBucket) throws Exception {
        try {
            URL url = new URL(sUrl);

            // open communication between proxy and final host
            // all actions before the connection can be taken now
            HttpURLConnection connectionWithFinalHost = (HttpURLConnection) url.openConnection();
            try {
                connectionWithFinalHost.setRequestMethod("POST");

                // copy headers from client's request to request that will be send to the final host
                copyHeadersToConnection(request, connectionWithFinalHost);

                connectionWithFinalHost.setDoOutput(true);
                connectionWithFinalHost.getOutputStream().write(requestBody.getBytes(Constants.ENCODING));

                // connect to remote host
                // interactions with the resource are enabled now
                connectionWithFinalHost.connect();

                int code = connectionWithFinalHost.getResponseCode();
                if (code != 200) {
                    response.sendError(code,
                        String.format(
                            "Error is: %s. Request body: %s",
                            connectionWithFinalHost.getResponseMessage(),
                            requestBody));
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

                // send remote host's response to client
                String contentEncoding = getContentEncoding(connectionWithFinalHost.getHeaderFields());

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
                    processResponse(context, httpSession, streamFromServer, streamToClient, selectionBucket, addPermissions);
                    streamToClient.flush();
                } finally {
                    IOUtils.closeQuietly(streamFromServer);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                connectionWithFinalHost.disconnect();
            }
        } catch (IOException e) {
            // connection problem with the host
            e.printStackTrace();

            throw new Exception(
                String.format("Failed to request Es at URL %s. " +
                        "Check Es configuration.",
                    sUrl),
                e);
        }
    }

    private void processResponse(ServiceContext context, HttpSession httpSession,
                                 InputStream streamFromServer, OutputStream streamToClient,
                                 String bucket, boolean addPermissions) throws Exception {
        JsonParser parser = JsonStreamUtils.jsonFactory.createParser(streamFromServer);
        JsonGenerator generator = JsonStreamUtils.jsonFactory.createGenerator(streamToClient);
        parser.nextToken();  //Go to the first token

        final Set<String> selections = (addPermissions ?
            SelectionManager.getManager(ApiUtils.getUserSession(httpSession)).getSelection(bucket) : new HashSet<>());

        JsonStreamUtils.addInfoToDocs(parser, generator, doc -> {
            if (addPermissions) {
                addUserInfo(doc, context);
                addSelectionInfo(doc, selections);
            }

            // Remove fields with privileges info
            if (doc.has("_source")) {
                ObjectNode sourceNode = (ObjectNode) doc.get("_source");

                for (ReservedOperation o : ReservedOperation.values()) {
                    sourceNode.remove("op" + o.getId());
                }
            }
        });
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

            if (!isInIgnoreList(headerName, ignoreList)) {

                // concatenate all values from the header
                List<String> valuesList = map.get(headerName);
                StringBuilder sBuilder = new StringBuilder();
                valuesList.forEach(sBuilder::append);

                // add header to HttpServletResponse object
                if (headerName != null) {
                    if ("Transfer-Encoding".equalsIgnoreCase(headerName) && "chunked".equalsIgnoreCase(sBuilder.toString())) {
                        // do not write this header because Tomcat already assembled the chunks itself
                        continue;
                    }
                    response.addHeader(headerName, sBuilder.toString());
                }
            }
        }
    }

    /**
     * Helper function to detect if a specific header is in a given ignore list
     *
     * @return true: in, false: not in
     */
    private boolean isInIgnoreList(String headerName, String[] ignoreList) {
        if (headerName == null) return false;

        for (String headerToIgnore : ignoreList) {
            if (headerName.equalsIgnoreCase(headerToIgnore))
                return true;
        }
        return false;
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
                !"X-XSRF-TOKEN".equalsIgnoreCase(headerName)) {
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

}
