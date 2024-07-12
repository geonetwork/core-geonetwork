/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.utils;

import com.google.common.base.Function;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpClientConnection;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

/**
 * Factory interface for making different kinds of requests.  This is an interface so that tests can
 * mock their own implementations.
 *
 * User: Jesse Date: 10/18/13 Time: 4:16 PM
 */
public class GeonetHttpRequestFactory {
    private int numberOfConcurrentRequests = 20;
    private PoolingHttpClientConnectionManager connectionManager;
    private volatile HttpClientConnectionManager nonShutdownableConnectionManager;

    @PreDestroy
    public synchronized void shutdown() {
        if (connectionManager != null) {
            connectionManager.shutdown();
        }
        connectionManager = null;
    }

    public synchronized void setNumberOfConcurrentRequests(int numberOfConcurrentRequests) {
        shutdown();
        this.numberOfConcurrentRequests = numberOfConcurrentRequests;
    }

    /**
     * Create a default XmlRequest.
     */
    public final XmlRequest createXmlRequest() {
        return createXmlRequest(null, 80, "http");
    }

    /**
     * Create a default XmlRequest.
     */
    public XmlRequest createXmlRequest(String host, int port, String protocol) {
        return new XmlRequest(host, port, protocol, this);
    }

    /**
     * Create a default XmlRequest.
     */
    public final XmlRequest createXmlRequest(String host) {
        return createXmlRequest(host, 80, "http");
    }

    /**
     * Create a default XmlRequest.
     */
    public final XmlRequest createXmlRequest(String host, int port) {
        String protocol = "http";
        if (port == 443) {
            protocol = "https";
        }
        return createXmlRequest(host, port, protocol);
    }

    /**
     * Create an XmlRequest from a url.
     *
     * @param url the url of the request.
     * @return the XmlRequest.
     */
    public final XmlRequest createXmlRequest(URL url) {
        final int port = url.getPort();
        final XmlRequest request = createXmlRequest(url.getHost(), port,
            url.getProtocol());

        request.setAddress(url.getPath());
        request.setQuery(url.getQuery());
        request.setFragment(url.getRef());
        request.setUserInfo(url.getUserInfo());
        request.setCookieStore(new BasicCookieStore());
        return request;
    }

    public ClientHttpResponse execute(HttpUriRequest request) throws IOException {
        final Function<HttpClientBuilder, Void> noop = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable HttpClientBuilder input) {
                return null;
            }
        };
        return execute(request, noop);
    }

    public ClientHttpResponse execute(HttpUriRequest request,
                                      final Credentials credentials,
                                      final AuthScope authScope) throws IOException {
        final Function<HttpClientBuilder, Void> setCredentials = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {

                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(authScope, credentials);
                input.setDefaultCredentialsProvider(credentialsProvider);

                return null;
            }
        };
        return execute(request, setCredentials);
    }

    public ClientHttpResponse execute(HttpUriRequest request,
                                      Function<HttpClientBuilder, Void> configurator) throws IOException {
        final HttpClientBuilder clientBuilder = getDefaultHttpClientBuilder();
        configurator.apply(clientBuilder);
        CloseableHttpClient httpClient = clientBuilder.build();

        return new AdaptingResponse(httpClient, httpClient.execute(request));
    }

    public ClientHttpResponse execute(HttpUriRequest request,
                                      Function<HttpClientBuilder, Void> configurator,
                                      AbstractHttpRequest r) throws IOException {
        final HttpClientBuilder clientBuilder = getDefaultHttpClientBuilder();
        configurator.apply(clientBuilder);
        CloseableHttpClient httpClient = clientBuilder.build();

        if (r.isPreemptiveBasicAuth() || r.getHttpClientContext() != null) {
            return new AdaptingResponse(httpClient, httpClient.execute(request, r.getHttpClientContext()));
        } else {
            return new AdaptingResponse(httpClient, httpClient.execute(request));
        }

    }

    public ClientHttpResponse execute(HttpUriRequest request,
                                      Function<HttpClientBuilder, Void> configurator,
                                      HttpClientContext context) throws IOException {
        final HttpClientBuilder clientBuilder = getDefaultHttpClientBuilder();
        configurator.apply(clientBuilder);
        CloseableHttpClient httpClient = clientBuilder.build();

        return new AdaptingResponse(httpClient, httpClient.execute(request, context));
    }

    public HttpClientBuilder getDefaultHttpClientBuilder() {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.disableContentCompression();
        builder.useSystemProperties();

        synchronized (this) {
            if (connectionManager == null) {
                connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setMaxTotal(this.numberOfConcurrentRequests);
                nonShutdownableConnectionManager = new HttpClientConnectionManager() {
                    public void closeExpiredConnections() {
                        connectionManager.closeExpiredConnections();
                    }

                    public ConnectionRequest requestConnection(HttpRoute route, Object state) {
                        return connectionManager.requestConnection(route, state);
                    }

                    public void releaseConnection(HttpClientConnection managedConn, Object state, long keepalive, TimeUnit tunit) {
                        connectionManager.releaseConnection(managedConn, state, keepalive, tunit);
                    }

                    public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context) throws IOException {
                        connectionManager.connect(managedConn, route, connectTimeout, context);
                    }

                    public void upgrade(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
                        connectionManager.upgrade(managedConn, route, context);
                    }

                    public void routeComplete(HttpClientConnection managedConn, HttpRoute route, HttpContext context) throws IOException {
                        connectionManager.routeComplete(managedConn, route, context);
                    }

                    public void shutdown() {
                        // don't shutdown pool
                    }

                    public void closeIdleConnections(long idleTimeout, TimeUnit tunit) {
                        connectionManager.closeIdleConnections(idleTimeout, tunit);
                    }
                };
            }
            connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout((int) TimeUnit.MINUTES.toMillis(3)).build());
            builder.setConnectionManager(nonShutdownableConnectionManager);
        }


        return builder;
    }

    private static class AdaptingResponse extends AbstractClientHttpResponse {

        private final CloseableHttpResponse response;
        private final CloseableHttpClient client;

        public AdaptingResponse(CloseableHttpClient client, CloseableHttpResponse response) {
            this.response = response;
            this.client = client;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getStatusLine().getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusLine().getReasonPhrase();
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }

        @Override
        public InputStream getBody() throws IOException {
            return response.getEntity().getContent();
        }

        @Override
        public HttpHeaders getHeaders() {
            final HttpHeaders httpHeaders = new HttpHeaders();

            final Header[] headers = response.getAllHeaders();

            for (Header header : headers) {
                final HeaderElement[] elements = header.getElements();
                for (HeaderElement element : elements) {
                    httpHeaders.add(header.getName(), element.getValue());
                }
            }
            return httpHeaders;
        }
    }


}
