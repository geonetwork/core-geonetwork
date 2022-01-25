/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.proxy;

import jeeves.server.UserSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.mapservices.MapService;
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.specification.LinkSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * This is a class extending the real proxy to make sure we can tweak specifics like removing the CSRF token on requests
 *
 * @author delawen
 */
public class URITemplateProxyServlet extends org.mitre.dsmiley.httpproxy.URITemplateProxyServlet {

    private static final long serialVersionUID = 4847856943273604410L;
    private static final String P_SECURITY_MODE = "securityMode";
    public static final String P_FORWARDEDHOST = "forwardHost";
    public static final String P_FORWARDEDHOSTPREFIXPATH = "forwardHostPrefixPath";

    protected boolean doForwardHost = false;
    protected String doForwardHostPrefixPath = "";

    private enum SECURITY_MODE {
        NONE,
        /**
         * Check if the host of the requested URL is registered in
         * at least one analyzed link in a metadata record.
         */
        DB_LINK_CHECK;

        public static SECURITY_MODE parse(String value) {
            if ("DB_LINK_CHECK".equals(value)) {
                return DB_LINK_CHECK;
            }
            return NONE;
        }
    }

    protected SECURITY_MODE securityMode;

    @Autowired
    MetadataLinkRepository metadataLinkRepository;

    /**
     * These are the "hop-by-hop" headers that should not be copied.
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html Overriding
     * parent
     */
    static {
        String[] headers = new String[]{
            "X-XSRF-TOKEN",
            "Access-Control-Allow-Origin",
            "Vary",
            "Access-Control-Allow-Credentials",
            "Strict-Transport-Security",
            "Etag"};
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

    protected void initTarget() throws ServletException {
        securityMode = SECURITY_MODE.parse(getConfigParam(P_SECURITY_MODE));
        String doForwadHostString = getConfigParam(P_FORWARDEDHOST);
        if (doForwadHostString != null) {
            String doForwadHostPrefixPathString = getConfigParam(P_FORWARDEDHOSTPREFIXPATH);
            this.doForwardHost = Boolean.parseBoolean(doForwadHostString);
            this.doForwardHostPrefixPath =
                doForwadHostPrefixPathString != null ? doForwadHostPrefixPathString : "";
        }
        super.initTarget();
    }

    /**
     * Creates the HttpClient used to make the proxied requests.
     * It configures the client to use system properties like
     * <code>http.proxyHost</code> and <code>http.httpPort</code>.
     * <p>
     * Called from {@link #init(ServletConfig)}.
     *
     * @param requestConfig the configuration used for the request made by the client.
     */
    @Override
    protected HttpClient createHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .useSystemProperties()
            .build();
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        super.copyRequestHeaders(servletRequest, proxyRequest);
        if (doForwardHost) {
            StringBuffer url = servletRequest.getRequestURL();
            String uri = servletRequest.getRequestURI();
            String host = url.substring(servletRequest.getScheme().length() + 3, url.indexOf(uri));

            proxyRequest.setHeader("X-Forwarded-Host", host);
            proxyRequest.setHeader("X-Forwarded-Proto", servletRequest.getScheme());
            proxyRequest.setHeader("X-Forwarded-Prefix", servletRequest.getContextPath() + this.doForwardHostPrefixPath);
        }

        // Only attempt this logic is the Authorization is currently not used.
        if (StringUtils.isEmpty(servletRequest.getHeader("Authorization"))) {

            // List of authentication url to apply the logic.
            List<MapService> mapServiceList = ApplicationContextHolder.get().getBean("securedMapServices", List.class);

            // Only continue if the current request matches one of our list of authentication url patterns
            Optional<MapService> result = mapServiceList.stream().filter(u ->
                (MapService.UrlType.valueOf(u.getUrlType()).equals(MapService.UrlType.TEXT) && proxyRequest.getRequestLine().getUri().contains(u.getUrl())) ||
                    (MapService.UrlType.valueOf(u.getUrlType()).equals(MapService.UrlType.REGEXP) && proxyRequest.getRequestLine().getUri().matches(u.getUrl()))
            ).findFirst();
            if (result.isPresent()) {
                if (MapService.AuthType.valueOf(result.get().getAuthType()).equals(MapService.AuthType.BASIC)) {
                    proxyRequest.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((result.get().getUsername() + ":" + result.get().getPassword()).getBytes()));
                } else {
                    if (MapService.AuthType.valueOf(result.get().getAuthType()).equals(MapService.AuthType.BEARER)) {
                        // In order to get a bearer token the user needs to be authenticated. - If not authenticated then we skip and the request will be made as anonymous.
                        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                            SecurityProviderUtil securityProviderUtil = SecurityProviderConfiguration.getSecurityProviderUtil();

                            if (securityProviderUtil != null) {
                                String authenticationHeaderValue = securityProviderUtil.getSSOAuthenticationHeaderValue();
                                if (!StringUtils.isEmpty(authenticationHeaderValue)) {
                                    proxyRequest.setHeader("Authorization", authenticationHeaderValue);
                                }
                            } else {
                                throw new IllegalArgumentException("Invalid or Unsupported authentication type " + result.get().getAuthType() + " for current security provider");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown authentication type " + result.get().getAuthType());
                    }
                }
            }
        }
    }

    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException {

        switch (securityMode) {
            case NONE:
                super.service(servletRequest, servletResponse);
                break;
            case DB_LINK_CHECK:
                boolean proxyCallAllowed = false;

                // Check if user is authenticated
                try {
                    UserSession userSession = ApiUtils.getUserSession(servletRequest.getSession());
                    if (userSession.isAuthenticated()) {
                        proxyCallAllowed = true;
                    }
                } catch (SecurityException securityException) {
                    servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        securityException.getMessage());
                }

                // Check if the link requested is in database link list
                if (proxyCallAllowed == false
                    && securityMode == SECURITY_MODE.DB_LINK_CHECK) {
                    try {
                        URI uri = new URI(servletRequest.getParameter("url"));
                        String host = uri.getHost();
                        LinkRepository linkRepository =
                            ApplicationContextHolder.get().getBean(LinkRepository.class);
                        long linksFound = linkRepository.count(
                            LinkSpecs.filter(host, null, null,
                                null, null, null));
                        if (linksFound == 0) {
                            String message = String.format(
                                "The proxy does not allow to access '%s' " +
                                    "because the URL host was not registered in any metadata records.",
                                uri
                            );
                            if (linkRepository.count() == 0) {
                                servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "The proxy is configured with DB_LINK_CHECK mode " +
                                        "but the MetadataLink table is empty. " +
                                        "Administrator may need to analyze record links from the admin console " +
                                        "in order to register URL allowed by the proxy. " + message);
                            }
                            servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, message);
                        }
                        proxyCallAllowed = linksFound > 0;
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(String.format(
                            "'%s' is invalid. Error is: '%s'",
                            e.getMessage()
                        ));
                    }
                }

                if (proxyCallAllowed) {
                    super.service(servletRequest, servletResponse);
                }
                break;
        }
    }
}
