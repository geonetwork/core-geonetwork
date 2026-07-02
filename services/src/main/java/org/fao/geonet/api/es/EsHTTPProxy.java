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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.es.processors.response.EsResponseProcessor;
import org.fao.geonet.api.es.processors.query.EsQueryProcessor;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.SelectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Proxy from GeoNetwork {@code /{portal}}/api} to Elasticsearch service.
 * The portal and privileges are included the search provided by the user.
 */
@RequestMapping(value = {
    "/{portal}/api"
})
@Tag(name = "search",
    description = "Proxy for Elasticsearch catalog search operations")
@Controller
public class EsHTTPProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.INDEX_ENGINE);

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
    private EsResponseProcessor responseProcessor;

    @Autowired
    private EsQueryProcessor queryPreprocessor;

    @Autowired
    private EsResponseContentTypeValidator contentTypeValidator;

    public EsHTTPProxy() {
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
        @Parameter(description = "Type of related resource. If none, no associated resource returned."
        )
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
        call(context, httpSession, request, response, EsSearchEndpoints.SEARCH_ENDPOINT.toString(), body, bucket, relatedTypes);
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
        call(context, httpSession, request, response, EsSearchEndpoints.MULTISEARCH_ENDPOINT.toString(), body, bucket, relatedTypes);
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
        String body) throws Exception {

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
        if (EsSearchEndpoints.SEARCH_ENDPOINT.toString().equals(endPoint) ||
            EsSearchEndpoints.MULTISEARCH_ENDPOINT.toString().equals(endPoint)) {
            String requestBody = queryPreprocessor.process(context, body, selectionBucket);

            handleRequest(context, httpSession, request, response, url, endPoint,
                requestBody, true, selectionBucket, relatedTypes);
        } else {
            handleRequest(context, httpSession, request, response, url, endPoint,
                body, true, selectionBucket, relatedTypes);
        }
    }

    protected HttpURLConnection openConnection(String sUrl) throws IOException {
        return (HttpURLConnection) new URL(sUrl).openConnection();
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
            // open communication between proxy and final host
            // all actions before the connection can be taken now
            HttpURLConnection connectionWithFinalHost = openConnection(sUrl);
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
                            IOUtils.toString(errorDetails, Charset.defaultCharset())
                        ));
                    return;
                }

                // get content type
                String contentType = connectionWithFinalHost.getContentType();
                contentTypeValidator.validateContentType(connectionWithFinalHost, response, contentType);

                // copy headers from the remote server's response to the response to send to the client
                copyHeadersFromConnectionToResponse(response, connectionWithFinalHost, proxyHeadersIgnoreList);

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
                    responseProcessor.processResponse(context, httpSession, streamFromServer, streamToClient, endPoint, selectionBucket, addPermissions, relatedTypes);
                    streamToClient.flush();
                } finally {
                    IOUtils.closeQuietly(streamFromServer);
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            } finally {
                connectionWithFinalHost.disconnect();
            }
        } catch (IOException e) {
            // connection problem with the host
            LOGGER.error(e.getMessage(), e);

            throw new Exception(
                String.format("Failed to request Es at URL %s. " +
                        "Check Es configuration.",
                    sUrl),
                e);
        }
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
     * Copy client's headers in the request to send to the final host.
     * Trick the host by hiding the proxy indirection and keep useful headers information.
     *
     * @param uc Contains now headers from client request except Host
     */
    protected void copyHeadersToConnection(HttpServletRequest request, HttpURLConnection uc) {

        for (Enumeration<String> enumHeader = request.getHeaderNames(); enumHeader.hasMoreElements(); ) {
            String headerName = enumHeader.nextElement();
            String headerValue = request.getHeader(headerName);

            // copy every header except host
            if (!"host".equalsIgnoreCase(headerName) &&
                !"X-XSRF-TOKEN".equalsIgnoreCase(headerName) &&
                !"Cookie".equalsIgnoreCase(headerName)) {
                uc.setRequestProperty(headerName, headerValue);
            }
        }
    }
}
