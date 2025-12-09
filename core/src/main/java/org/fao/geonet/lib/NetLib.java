//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.lib;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.XmlRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import jeeves.server.context.ServiceContext;

public class NetLib {
    private ProxyConfiguration proxyConfiguration;

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }

    public NetLib() {
        boolean isProxyConfiguredInSystemProperties = StringUtils.isNotBlank(System.getProperty(ProxyConfiguration.HTTP_PROXY_HOST)) ||
            StringUtils.isNotBlank(System.getProperty(ProxyConfiguration.HTTPS_PROXY_HOST));

        proxyConfiguration = new ProxyConfiguration(isProxyConfiguredInSystemProperties);
    }

    public void setupProxy(ServiceContext context, XmlRequest req) {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        setupProxy(sm, req);
    }

    //---------------------------------------------------------------------------

    /**
     * Setup proxy for XmlRequest
     */

    public void setupProxy(SettingManager sm, XmlRequest req) {
        proxyConfiguration.refresh(sm);

        boolean enabled = proxyConfiguration.isEnabled();
        String host = proxyConfiguration.getHost();
        String port = proxyConfiguration.getPort();
        String username = proxyConfiguration.getUsername();
        String password = proxyConfiguration.getPassword();
        String ignoreHostList = proxyConfiguration.getIgnoreHostList();

        if (!enabled) {
            req.setUseProxy(false);
        } else {
            if (!Lib.type.isInteger(port))
                Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : " + port);
            else {
                if (!isProxyHostException(req.getHost(), ignoreHostList)) {
                    req.setUseProxy(true);
                    req.setProxyHost(host);
                    req.setProxyPort(Integer.parseInt(port));
                    if (username.trim().length() != 0) {
                        req.setProxyCredentials(username, password);
                    }
                } else {
                    Log.info(Geonet.GEONETWORK, "Proxy configuration ignored, host: " + req.getHost() + " is in proxy ignore list");
                    req.setUseProxy(false);
                }

            }
        }
    }

    //---------------------------------------------------------------------------

    public CredentialsStore setupProxy(ServiceContext context, HttpClientBuilder client, String requestHost) {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        return setupProxy(sm, client, requestHost);
    }

    /**
     * Setup proxy for http client
     */
    public CredentialsStore setupProxy(SettingManager sm, HttpClientBuilder client, String requestHost) {
        proxyConfiguration.refresh(sm);

        boolean enabled = proxyConfiguration.isEnabled();
        String host = proxyConfiguration.getHost();
        String port = proxyConfiguration.getPort();
        String username = proxyConfiguration.getUsername();
        String password = proxyConfiguration.getPassword();
        String ignoreHostList = proxyConfiguration.getIgnoreHostList();

        CredentialsStore provider = new BasicCredentialsProvider();
        if (enabled) {
            if (!Lib.type.isInteger(port)) {
                Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : " + port);
            } else {
                if (!isProxyHostException(requestHost, ignoreHostList)) {
                    final HttpHost proxy = new HttpHost(host, Integer.parseInt(port));
                    client.setProxy(proxy);

                    if (username.trim().length() != 0) {
                        provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(username, password.toCharArray()));
                        client.setDefaultCredentialsProvider(provider);
                    }
                } else {
                    client.setProxy(null);
                }

            }
        }

        return provider;
    }

    //---------------------------------------------------------------------------

    public void setupProxy(ServiceContext context) {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        setupProxy(sm);
    }

    //---------------------------------------------------------------------------

    /**
     * Setup proxy for http client
     */
    public void setupProxy(SettingManager sm) {
        proxyConfiguration.refresh(sm);

        // If the proxy is configured in the system properties,
        // ignore the proxy configuration in the system settings.
        if (proxyConfiguration.isProxyConfiguredInSystemProperties()) return;

        if (proxyConfiguration.isEnabled()) {
            String host = proxyConfiguration.getHost();
            String port = proxyConfiguration.getPort();
            String username = proxyConfiguration.getUsername();
            String ignoreHostList = proxyConfiguration.getIgnoreHostList();

            Properties props = System.getProperties();
            props.put(ProxyConfiguration.HTTP_PROXY_HOST, host);
            props.put(ProxyConfiguration.HTTP_PROXY_PORT, port);
            props.put(ProxyConfiguration.HTTPS_PROXY_HOST, host);
            props.put(ProxyConfiguration.HTTPS_PROXY_PORT, port);
            props.put(ProxyConfiguration.HTTP_NON_PROXY_HOSTS, ignoreHostList);

            if (username.trim().length() > 0) {
                Log.error(Geonet.GEONETWORK, "Proxy credentials cannot be used");
            }
        } else {
            System.clearProperty(ProxyConfiguration.HTTP_PROXY_HOST);
            System.clearProperty(ProxyConfiguration.HTTP_PROXY_PORT);
            System.clearProperty(ProxyConfiguration.HTTPS_PROXY_HOST);
            System.clearProperty(ProxyConfiguration.HTTPS_PROXY_PORT);
            System.clearProperty(ProxyConfiguration.HTTP_NON_PROXY_HOSTS);
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Setups proxy for java.net.URL.
     */
    public URLConnection setupProxy(ServiceContext context, URL url) throws IOException {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        proxyConfiguration.refresh(sm);

        boolean enabled = proxyConfiguration.isEnabled();
        String host = proxyConfiguration.getHost();
        String port = proxyConfiguration.getPort();
        String username = proxyConfiguration.getUsername();
        String password = proxyConfiguration.getPassword();
        String ignoreHostList = proxyConfiguration.getIgnoreHostList();

        URLConnection conn = null;
        if (enabled) {
            if (!Lib.type.isInteger(port)) {
                Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : " + port);
            } else {
                if (!isProxyHostException(url.getHost(), ignoreHostList)) {

                    InetSocketAddress sa = new InetSocketAddress(host, Integer.parseInt(port));
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
                    conn = url.openConnection(proxy);

                    if (username.trim().length() != 0) {
                        String encodedUserPwd = new Base64().encodeAsString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
                        conn.setRequestProperty("Accept-Charset", "UTF-8");
                        conn.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPwd);
                    }

                } else {
                    conn = url.openConnection();
                }
            }
        } else {
            conn = url.openConnection();
        }

        return conn;
    }
    //---------------------------------------------------------------------------

    public boolean isUrlValid(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    //---------------------------------------------------------------------------

    /**
     * Checks if a host matches a ignore host list.
     *
     * Ignore host list format should be: string with host names or ip's separated by | that allows
     * wildcards.
     */
    public boolean isProxyHostException(String requestHost, String ignoreHostList) {
        if (StringUtils.isEmpty(requestHost)) return false;
        if (StringUtils.isEmpty(ignoreHostList)) return false;

        try {
            return (requestHost.matches(ignoreHostList));
        } catch (PatternSyntaxException ex) {
            Log.error(Geonet.GEONETWORK + ".httpproxy", "Proxy ignore host list expression is not valid: " + ex.getMessage());
        }

        return false;
    }
}
