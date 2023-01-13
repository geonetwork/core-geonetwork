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
package org.fao.geonet.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A useless proxy
 * which inject Elasticsearch credentials using Basic Authentication
 * because sometimes, it looks like no anonymous access are available.
 */
public class EsBasicAuthInjectorProxyServlet
    extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

    private static final String P_IS_SECURED = "isSecured";

    protected boolean isSecured = false;

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        this.username = servletConfig.getInitParameter("username");
        this.password = servletConfig.getInitParameter("password");

        String doIsSecured = servletConfig.getInitParameter(P_IS_SECURED);
        if(doIsSecured != null) {
            isSecured = Boolean.parseBoolean(doIsSecured);
        }

        super.init(servletConfig);
    }

    /**
     * Hides the {{@link org.mitre.dsmiley.httpproxy.ProxyServlet#getContentLength(HttpServletRequest)}} private method.
     */
    private long getContentLength(HttpServletRequest request) {
        String contentLengthHeader = request.getHeader("Content-Length");
        return contentLengthHeader != null ? Long.parseLong(contentLengthHeader) : -1L;
    }

    @Override
    protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest servletRequest) throws IOException {
        HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        InputStreamEntity entity = new InputStreamEntity(servletRequest.getInputStream(), this.getContentLength(servletRequest));

        // https://github.com/mitre/HTTP-Proxy-Servlet/issues/67
        if ("GET".equals(method) || !isSecured) {
            eProxyRequest.setEntity(entity);
        } else {
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
            eProxyRequest.setEntity(bufferedHttpEntity);
        }

        return eProxyRequest;
    }

    /**
     * Add the basic authentication
     */
    @Override
    protected HttpClient createHttpClient() {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
            return HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(buildRequestConfig())
                .useSystemProperties()
                .build();
        } else {
            return super.createHttpClient();
        }
    }
}
