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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * A useless proxy
 * which inject Elasticsearch credentials using Basic Authentication
 * because sometimes, it looks like no anonymous access are available.
 */
public class EsBasicAuthInjectorProxyServlet
    extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

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
        super.init(servletConfig);
    }
    
    /**
     * Add the basic authentication
     * @param requestConfig the configuration used for the request made by the client.
     */
    @Override
    protected HttpClient createHttpClient(RequestConfig requestConfig) {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
            return HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(requestConfig)
                .useSystemProperties()
                .build();
        } else {
            return super.createHttpClient(requestConfig);
        }
    }
}
