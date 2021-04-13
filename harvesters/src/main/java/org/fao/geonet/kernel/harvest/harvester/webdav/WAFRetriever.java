//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class connects to a remote WAF server and traverse the links gathering the URLs of all
 * the metadata documents available from the URL passed.
 */
class WAFRetriever implements RemoteRetriever {
    public static final String type_GetCapabilities = "GetCapabilities";
    public static final String type_xml = "xml";
    public static final String type_dir = "directory";
    private final List<RemoteFile> files = new ArrayList<>();
    private final GeonetHttpRequestFactory requestFactory;
    private final SettingManager settingManager;
    private AtomicBoolean cancelMonitor;
    private Logger log;
    private WebDavParams params;
    private CloseableHttpClient httpClient;

    public WAFRetriever(SettingManager settingManager, GeonetHttpRequestFactory requestFactory) {
        this.settingManager = settingManager;
        this.requestFactory = requestFactory;
    }

    public static String getFileType(String path) {
        if (path.toUpperCase().contains("REQUEST=GETCAPABILITIES")) {
            return type_GetCapabilities;
        } else if (path.toUpperCase().endsWith(".XML")) {
            return type_xml;
        } else if (path.toUpperCase().endsWith("/")) {
            return type_dir;
        } else {
            return null;
        }
    }

    public void init(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WebDavParams params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.params = params;

        String host;
        try {
            URL url = new URL(StringUtils.trim(params.url));
            host = url.getHost();
        } catch (MalformedURLException e) {
            log.error("Cannot parse URL " + params.url);
            log.error(e);
            throw new IllegalArgumentException("Cannot parse URL " + params.url, e);
        }
        HttpClientBuilder clientBuilder = requestFactory.getDefaultHttpClientBuilder().setUserAgent(
                "GeoNetwork/" + settingManager.getValue(Settings.SYSTEM_PLATFORM_VERSION) + "-" + settingManager
                        .getValue(Settings.SYSTEM_PLATFORM_SUBVERSION));
        CredentialsProvider provider = Lib.net.setupProxy(settingManager, clientBuilder, host);

        if (params.isUseAccount() && StringUtils.isNotBlank(params.getUsername())) {
            String username = params.getUsername();
            String password = params.getPassword();
            provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthPolicy.BASIC),
                    new UsernamePasswordCredentials(username, password));
            provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthPolicy.DIGEST),
                    new UsernamePasswordCredentials(username, password));
            provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthPolicy.SPNEGO),
                    new UsernamePasswordCredentials(username, password));
            provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthPolicy.KERBEROS),
                    new UsernamePasswordCredentials(username, password));
        }
        clientBuilder.setDefaultCredentialsProvider(provider);
        this.httpClient = clientBuilder.build();

    }

    public List<RemoteFile> retrieve() throws Exception {

        files.clear();
        String url = params.url;
        if (!StringUtils.contains(url, "?") && !StringUtils.endsWith(url, "/")) {
            url += "/";
        }
        retrieveFiles(url);
        return files;
    }

    public void destroy() {
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (IOException e) {
                log.warning("Error closing WAF http client");
                log.error(e);
            }
        }
    }

    private void retrieveFiles(String wafUrl) throws IOException {

        if (log.isDebugEnabled()) {
            log.debug("Scanning resource : " + wafUrl);
        }

        Document doc;
        try (CloseableHttpResponse response = connect(wafUrl)) {
            if (response.getStatusLine().getStatusCode() / 100 == 2) {
                try (InputStream responseStream = response.getEntity().getContent()) {
                    doc = Jsoup.parse(responseStream, null, wafUrl);
                }
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        }

        //Jsoup.parse(new URL(wafUrl), 3000);
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            if (cancelMonitor.get()) {
                files.clear();
                return;
            }

            String url = link.attr("abs:href");

            String fileType = getFileType(url);

            if (StringUtils.equals(fileType, type_dir)) {
                if (params.recurse) {
                    // Parent directory or same directory links, ignore
                    if (!url.contains(wafUrl) || url.equals(wafUrl))
                        continue;

                    // Try as a directory
                    retrieveFiles(url);
                }
            } else if (StringUtils.equals(fileType, type_xml)) {
                files.add(new WAFRemoteFile(url, httpClient));
            } else {
                // Skip link
            }
        }
    }

    private CloseableHttpResponse connect(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        return response;
    }

}
