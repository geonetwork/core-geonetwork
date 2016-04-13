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

package org.fao.geonet.solr;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.fao.geonet.api.API;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SelectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * TODO: Add multinode support at some point
 * Created by fgravin on 11/4/15.
 */
@RequestMapping(value = {
        "/api/search",
        "/api/" + API.VERSION_0_1 + "/search"
})
@Api(value = "search",
        tags = "search",
        description = "Catalog search operations")
@Controller
public class SolrHTTPProxy {
    public static final String[] _validContentTypes = {
            "application/json", "text/plain"
    };

    @Autowired
    private SolrConfig config;

    @ApiOperation(value = "Search metadata",
            notes = "See https://cwiki.apache.org/confluence/display/solr/Common+Query+Parameters for parameters.")
    @RequestMapping(value = "/records",
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void handleGETMetadata(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String url = config.getSolrServerUrl() + "/" +
                config.getSolrServerCore() + "/select?" + addPermissions(addDocType(request.getQueryString(), "metadata"));
        handleRequest(request, response, url, true);
    }

    @ApiOperation(value = "Search suggestion on metadata",
            notes = "See https://cwiki.apache.org/confluence/display/solr/Spell+Checking for parameters.")
    @RequestMapping(value = "/records/spell",
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void metadataSpellChecker(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String url = config.getSolrServerUrl() + "/" +
                config.getSolrServerCore() + "/spell?" + addPermissions(addDocType(request.getQueryString(), "metadata"));
        handleRequest(request, response, url, false);
    }

    @ApiOperation(value = "Search features",
            notes = "See https://cwiki.apache.org/confluence/display/solr/Common+Query+Parameters for parameters.")
    @RequestMapping(value = "/features",
            method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public void handleGETFeatures(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String url = config.getSolrServerUrl() + "/" +
                config.getSolrServerCore() + "/select?" + addDocType(request.getQueryString(), "feature");
        handleRequest(request, response, url, false);
    }

    private String addPermissions(String queryString) throws Exception {
        ServiceContext context = ServiceContext.get();
        AccessManager accessManager = context.getBean(AccessManager.class);
        Set<Integer> groups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
        final int viewId = ReservedOperation.view.getId();
        final String ids = groups.stream().map(Object::toString)
                .collect(Collectors.joining("\" \"", "(\"", "\")"));
        return queryString + String.format("&fq=_op%d:%s", viewId, URIUtil.encodeQuery(ids));
    }

    private String addDocType(String queryString, String type) {
        return queryString + "&fq=docType:" + type;
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, String sUrl,
                               boolean addPermissions) throws Exception {
        try {
            URL url = new URL(sUrl);

            // open communication between proxy and final host
            // all actions before the connection can be taken now
            HttpURLConnection connectionWithFinalHost = (HttpURLConnection) url.openConnection();
            try {
                connectionWithFinalHost.setRequestMethod("GET");

                // copy headers from client's request to request that will be send to the final host
                copyHeadersToConnection(request, connectionWithFinalHost);

                // connect to remote host
                // interactions with the resource are enabled now
                connectionWithFinalHost.connect();

                int code = connectionWithFinalHost.getResponseCode();
                if (code != 200) {
                    response.sendError(code,
                            connectionWithFinalHost.getResponseMessage());
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
                copyHeadersFromConnectionToResponse(response, connectionWithFinalHost);

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
                    if (!addPermissions) {
                        IOUtils.copy(connectionWithFinalHost.getInputStream(), response.getOutputStream());
                    } else {
                        addUserInfoToJson(streamFromServer, streamToClient);
                    }
                } finally {
                    streamFromServer.close();
                    streamToClient.close();
                }
            } finally {
                connectionWithFinalHost.disconnect();
            }
        } catch (IOException e) {
            // connection problem with the host
            e.printStackTrace();

            throw new Exception(
                    String.format("Failed to request Solr at URL %s. " +
                                    "Check Solr configuration.",
                            sUrl),
                    e);
        }
    }

    private void addUserInfoToJson(InputStream streamFromServer, OutputStream streamToClient) throws Exception {
        final ServiceContext context = ServiceContext.get();
        JsonParser parser = JsonStreamUtils.jsonFactory.createJsonParser(streamFromServer);
        JsonGenerator generator = JsonStreamUtils.jsonFactory.createJsonGenerator(streamToClient);
        parser.nextToken();  //Go to the first token

        final SelectionManager manager = SelectionManager.getManager(context.getUserSession());
        final Set<String> selections = manager.getSelection(SelectionManager.SELECTION_METADATA);

        JsonStreamUtils.addInfoToDocs(parser, generator, doc -> {
            addUserInfo(doc, context);
            addSelectionInfo(doc, selections);
        });

        generator.close();
    }

    private static Integer getInteger(ObjectNode node, String name) {
        final JsonNode sub = node.get(name);
        return sub != null ? sub.asInt() : null;
    }

    private static String getString(ObjectNode node, String name) {
        final JsonNode sub = node.get(name);
        return sub != null ? sub.asText() : null;
    }

    private static void addSelectionInfo(ObjectNode doc, Set<String> selections) {
        final String uuid = getString(doc, Geonet.IndexFieldNames.UUID);
        doc.put(Edit.Info.Elem.SELECTED, selections.contains(uuid));
    }

    private static void addUserInfo(ObjectNode doc, ServiceContext context) throws Exception {
        final Integer owner = getInteger(doc, Geonet.IndexFieldNames.OWNER);
        final Integer groupOwner = getInteger(doc, Geonet.IndexFieldNames.GROUP_OWNER);

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
                ArrayNode opFields = (ArrayNode) doc.get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
                if (opFields != null) {
                    for (JsonNode field : opFields) {
                        final int groupId = field.asInt();
                        if (operation == ReservedOperation.editing && editingGroups.contains(groupId)) {
                            canEdit = true;
                            break;
                        }

                        if (groups.contains(groupId)) {
                            operations.add(operation);
                            break;
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
        int groupId = group.getId();
        ArrayNode opFields = (ArrayNode) doc.get(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
        if (opFields != null) {
            for (JsonNode field : opFields) {
                if (groupId == field.asInt()) {
                    return true;
                }
            }
        }
        return false;
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
            if (!"host".equalsIgnoreCase(headerName)) {
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
        for (String validTypeContent : SolrHTTPProxy._validContentTypes) {
            if (validTypeContent.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
