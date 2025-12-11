//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.webdav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;

import jeeves.server.context.ServiceContext;

import org.apache.http.HttpClientConnection;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;

//import org.apache.hc.client.impl.LaxRedirectStrategy;
//import org.apache.hc.client4.http.impl.classic.HttpClientBuilder;
//import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.fao.geonet.Logger;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class WebDavRetriever implements RemoteRetriever {

    private Logger log;
    private ServiceContext context;
    private WebDavParams params;

    private List<RemoteFile> files = new ArrayList<RemoteFile>();
    private Sardine sardine;
    private AtomicBoolean cancelMonitor;

    //--------------------------------------------------------------------------
    //---
    //--- RemoteRetriever interface
    //---
    //--------------------------------------------------------------------------

    static String calculateBaseURL(AtomicBoolean cancelMonitor, String url, List<DavResource> resources) throws IOException {
        for (Iterator<DavResource> iterator = resources.iterator(); iterator.hasNext(); ) {
            if (cancelMonitor.get()) {
                return "";
            }

            DavResource next = iterator.next();
            if (url.endsWith(next.getPath())) {
                // this is the directory we just searched for so remove it and use it to calculate the base URL.
                iterator.remove();

                return url.substring(0, url.length() - next.getPath().length());
            }
        }
        return url;
    }

    //---------------------------------------------------------------------------

    public void init(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WebDavParams params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
    }

    /**
     * Replacement for {@link GeonetHttpRequestFactory#getDefaultHttpClientBuilder()} so we can
     * make use of class apache http 4 client.
     *
     * return http client4 builder.
     */
    public HttpClientBuilder getDefaultHttpClientBuilder() {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.disableContentCompression();
        builder.useSystemProperties();

            var connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(2);

            HttpClientConnectionManager nonShutdownableConnectionManager = new HttpClientConnectionManager() {
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

        builder.setConnectionManager(nonShutdownableConnectionManager);



        return builder;
    }

    public List<RemoteFile> retrieve() throws Exception {

        final HttpClientBuilder clientBuilder = getDefaultHttpClientBuilder();
        Lib.net.setupProxy(context, clientBuilder, new URL(params.url).getHost());

        if (params.isUseAccount()) {
            this.sardine = new SardineImpl(clientBuilder, params.getUsername(), params.getPassword());
        } else {
            this.sardine = new SardineImpl(clientBuilder);
        }
        files.clear();

        String url = params.url;
        if (!url.endsWith("/")) {
            if (log.isDebugEnabled()) {
                log.debug("URL " + url + "does not end in slash -- will be appended");
            }
            url += "/";
        }

        final List<DavResource> resources = open(url);
        url = calculateBaseURL(cancelMonitor, url, resources);
        for (DavResource resource : resources) {
            if (cancelMonitor.get()) {
                return Collections.emptyList();
            }

            retrieveFile(url, resource);
        }
        return files;
    }

    //---------------------------------------------------------------------------

    public void destroy() {
        try {
            sardine.shutdown();
        } catch (Exception e) {
            log.warning("Cannot close resource : " + e.getMessage());
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private List<DavResource> open(String url) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("opening webdav resource with URL: " + url);
        }

        if (log.isDebugEnabled()) {
            log.debug("Connecting to webdav url for node : " + params.getName() + " URL: " + params.url);
        }

        final List<DavResource> davResources = sardine.list(url, 1, false);
        if (log.isDebugEnabled()) {
            log.debug("# " + davResources.size() + " webdav resources found in: " + url);
        }

        return davResources;
    }

    private void retrieveFile(String baseURL, DavResource davResource) throws IOException {
        if (this.cancelMonitor.get()) {
            files.clear();
            return;
        }

        String path = davResource.getPath();
        int startSize = files.size();

        if (davResource.isDirectory()) {
            // it is a directory
            if (params.recurse) {
                if (log.isDebugEnabled()) {
                    log.debug(path + " is a collection, processed recursively");
                }

                for (DavResource resource : sardine.list(baseURL + path)) {
                    if (!resource.getHref().equals(davResource.getHref())) {
                        retrieveFile(baseURL, resource);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(path + " is a collection. Ignoring because recursion is disabled.");
                }
            }
        } else {
            // it is a file
            if (log.isDebugEnabled()) {
                log.debug(path + " is not a collection");
            }
            final String name = davResource.getName();
            if (name.toLowerCase().endsWith(".xml")) {
                if (log.isDebugEnabled()) {
                    log.debug("found xml file ! " + name.toLowerCase());
                }
                files.add(new WebDavRemoteFile(sardine, baseURL, davResource));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(name.toLowerCase() + " is not an xml file");
                }
            }
        }

        int endSize = files.size();
        int added = endSize - startSize;
        if (added == 0) {
            if (log.isDebugEnabled()) log.debug("No xml files found in path : " + path);
        } else {
            if (log.isDebugEnabled())
                log.debug("Found " + added + " xml file(s) in path : " + path);
        }
    }

}

//=============================================================================
