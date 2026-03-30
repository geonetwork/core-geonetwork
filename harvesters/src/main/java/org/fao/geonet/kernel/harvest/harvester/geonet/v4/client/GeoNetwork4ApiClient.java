//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet.v4.client;

import javax.annotation.Nullable;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * This class acts as a client for interacting with GeoNetwork version 4 REST API. It provides functionalities
 * for executing HTTP requests to retrieve and manipulate data from a GeoNetwork server.
 * <p>
 * It can use authentication credentials for accessing secured endpoints of the GeoNetwork server.
 * The class interacts with sources, groups, records, and metadata stored on the server, retrieving data in
 * JSON or MEF formats as applicable.
 */
@Component
public class GeoNetwork4ApiClient {
    @Autowired
    private GeonetHttpRequestFactory requestFactory;

    @Autowired
    private SettingManager settingManager;

    /**
     * Retrieves a map of sources from the GeoNetwork 4.x server. The sources are identified by their UUIDs.
     *
     * @param serverUrl the URL of the GeoNetwork server.
     * @param user the username for authentication with the GeoNetwork server.
     * @param password the password for authentication with the GeoNetwork server.
     * @return a map where the keys are UUIDs of the sources, and the values are Source objects.
     * @throws URISyntaxException if the server URL is malformed.
     * @throws IOException if an error occurs during communication with the GeoNetwork server.
     */
    public Map<String, Source> retrieveSources(String serverUrl, String user, String password) throws URISyntaxException, IOException {
        String sourcesJson = retrieveUrl(addUrlSlash(serverUrl) + "api/sources", user, password);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Source> sourceList
            = objectMapper.readValue(sourcesJson, new TypeReference<>() { });

        Map<String, Source> sourceMap = new HashMap<>();
        sourceList.forEach(s -> sourceMap.put(s.getUuid(), s));
        return sourceMap;
    }


    /**
     * Retrieves a list of groups from the GeoNetwork 4.x server.
     *
     * @param serverUrl the URL of the GeoNetwork server.
     * @param user the username for authentication with the GeoNetwork server.
     * @param password the password for authentication with the GeoNetwork server.
     * @return a list of groups retrieved from the GeoNetwork server.
     * @throws URISyntaxException if the server URL is malformed.
     * @throws IOException if an error occurs during communication with the GeoNetwork server.
     */
    public List<Group> retrieveGroups(String serverUrl, String user, String password) throws URISyntaxException, IOException {
        String groupsJson = retrieveUrl(addUrlSlash(serverUrl) + "api/groups", user, password);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(groupsJson, new TypeReference<>() { });
    }

    /**
     * Executes a search query against a GeoNetwork server and retrieves the search results.
     *
     * @param serverUrl the URL of the GeoNetwork server.
     * @param query the search query in JSON format.
     * @param user the username for authentication with the GeoNetwork server.
     * @param password the password for authentication with the GeoNetwork server.
     * @return a {@code SearchResponse} object containing the total number of results and the set of result hits.
     * @throws URISyntaxException if the server URL is malformed.
     * @throws IOException if an error occurs during communication with the GeoNetwork server.
     */
    public SearchResponse query(String serverUrl, String query, String user, String password) throws URISyntaxException, IOException {
        HttpPost httpMethod = new HttpPost(createUrl(addUrlSlash(serverUrl) + "api/search/records/_search"));
        final Header headerContentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        final Header header = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        httpMethod.addHeader(headerContentType);
        httpMethod.addHeader(header);
        final StringEntity entity = new StringEntity(query);
        httpMethod.setEntity(entity);

        try (ClientHttpResponse httpResponse = doExecute(httpMethod, user, password)) {
            String jsonResponse = CharStreams.toString(new InputStreamReader(httpResponse.getBody()));

            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module =
                new SimpleModule("CustomSearchResponseDeserializer", new Version(1, 0, 0, null, null, null));
            module.addDeserializer(SearchResponse.class, new SearchResponseDeserializer());
            objectMapper.registerModule(module);
            return objectMapper.readValue(jsonResponse, SearchResponse.class);

        }
    }

    /**
     * Downloads a Metadata Exchange Format (MEF) file from a GeoNetwork server for a specific record
     * identified by its UUID. The MEF file is stored in a temporary location on the filesystem.
     *
     * @param serverUrl the URL of the GeoNetwork server
     * @param uuid      the unique identifier (UUID) of the record to retrieve
     * @param user      the username for authentication with the GeoNetwork server
     * @param password  the password for authentication with the GeoNetwork server
     * @return a {@code Path} representing the location of the downloaded MEF file
     * @throws URISyntaxException if the URL of the GeoNetwork server is malformed
     * @throws IOException        if any network or file operation fails
     */
    public Path retrieveMEF(String serverUrl, String uuid, String user, String password) throws URISyntaxException, IOException {
        if (!Lib.net.isUrlValid(serverUrl)) {
            throw new BadParameterEx("Invalid URL", serverUrl);
        }

        Path tempFile = Files.createTempFile("temp-", ".dat");

        String url = addUrlSlash(serverUrl) +
            "/api/records/" + uuid + "/formatters/zip?withRelated=false";

        HttpGet httpMethod = new HttpGet(createUrl(url));
        final Header header = new BasicHeader(HttpHeaders.ACCEPT, "application/x-gn-mef-2-zip");
        httpMethod.addHeader(header);

        try (ClientHttpResponse httpResponse = doExecute(httpMethod, user, password)) {
            Files.copy(httpResponse.getBody(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }

    private URI createUrl(String jsonUrl) throws URISyntaxException {
        return new URI(jsonUrl);
    }

    private String retrieveUrl(String serverUrl, String user, String password) throws URISyntaxException, IOException {
        if (!Lib.net.isUrlValid(serverUrl)) {
            throw new BadParameterEx("Invalid URL", serverUrl);
        }

        HttpGet httpMethod = new HttpGet(createUrl(serverUrl));
        final Header header = new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpMethod.addHeader(header);

        try (ClientHttpResponse httpResponse = doExecute(httpMethod, user, password)) {
            return CharStreams.toString(new InputStreamReader(httpResponse.getBody()));
        }
    }

    private String addUrlSlash(String url) {
        return url + (!url.endsWith("/") ? "/" : "");
    }

    protected ClientHttpResponse doExecute(HttpUriRequest method, String username, String password) throws IOException {
        final String requestHost = method.getURI().getHost();
        HttpClientContext httpClientContext = HttpClientContext.create();

        final Function<HttpClientBuilder, Void> requestConfiguration = new Function<>() {
            @Nullable
            @Override
            public Void apply(HttpClientBuilder input) {
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    HttpHost targetHost = new HttpHost(
                        method.getURI().getHost(),
                        method.getURI().getPort(),
                        method.getURI().getScheme());

                    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                        new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                        new UsernamePasswordCredentials(username, password));

                    final RequestConfig.Builder builder = RequestConfig.custom();
                    builder.setAuthenticationEnabled(true);
                    builder.setRedirectsEnabled(true);
                    builder.setRelativeRedirectsAllowed(true);
                    builder.setCircularRedirectsAllowed(true);
                    builder.setMaxRedirects(3);

                    input.setDefaultRequestConfig(builder.build());

                    // Preemptive authentication
                    // Create AuthCache instance
                    AuthCache authCache = new BasicAuthCache();
                    // Generate BASIC scheme object and add it to the local auth cache
                    BasicScheme basicAuth = new BasicScheme();
                    authCache.put(targetHost, basicAuth);

                    // Add AuthCache to the execution context
                    httpClientContext.setCredentialsProvider(credentialsProvider);
                    httpClientContext.setAuthCache(authCache);
                }

                Lib.net.setupProxy(settingManager, input, requestHost);
                input.useSystemProperties();

                return null;
            }
        };

        return requestFactory.execute(method, requestConfiguration, httpClientContext);
    }
}
