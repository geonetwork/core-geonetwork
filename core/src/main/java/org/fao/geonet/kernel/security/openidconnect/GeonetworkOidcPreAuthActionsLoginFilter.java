/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect;

import org.fao.geonet.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.ServletContextAware;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Filter implementation for handling pre-authentication actions for OpenID Connect (OIDC) login.
 * This filter checks if the user is authenticated and redirects unauthenticated users to the login page.
 */
public class GeonetworkOidcPreAuthActionsLoginFilter  implements Filter, ServletContextAware {

    /**
     * Repository for managing client registrations for OpenID Connect.
     * This is used to retrieve client registration details such as registration ID.
     */
    @Autowired
    private  ClientRegistrationRepository clientRegistrationRepository;


    /**
     * The servlet context parameter name that contains the excluded URL paths.
     * This is used to configure which paths should be ignored by the filter.
     * Based on the GeoNetworkPortalFilter configured in web.xml
     */
    private static final String EXCLUDED_URL_PATHS = "excludedPaths";

    /**
     * RequestMatchers for the ignored application paths.
     */
    private List<AntPathRequestMatcher> excludedPathsMatchers = new ArrayList<>();

    /**
     * The Method to set the servlet context from the web.xml.
     * It also sets the request matchers for the excluded paths.
     */
    public void setServletContext(ServletContext servletContext) {
        //get excluded paths from servlet context in web.xml
        String excludedPathsValue = servletContext.getInitParameter(EXCLUDED_URL_PATHS);

        if (StringUtils.isNotEmpty(excludedPathsValue)) {
            excludedPathsMatchers = Arrays.stream(excludedPathsValue.split(","))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotEmpty)
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        }

        // add the jwks endpoint to the excluded paths
        excludedPathsMatchers.add(new AntPathRequestMatcher("/.well-known/jwks.json"));
    }

    @Override
    public void init(FilterConfig config) {
    }

    /**
     * Filter implementation for handling pre-authentication actions for OpenID Connect login.
     * This filter checks if the user is authenticated and redirects unauthenticated users to the login page.
     * It ensures secure access to protected resources by handling login redirection and bypassing public endpoints.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        final HttpSession httpSession = servletRequest.getSession(false);

        String requestUri = servletRequest.getRequestURI();
        String contextPath = servletRequest.getContextPath();
        String clientRegistrationId = GeonetworkClientRegistrationProvider.CLIENTREGISTRATION_NAME;
        // Grab the first (or default) registrationId from repository
        String registrationId = clientRegistrationRepository.findByRegistrationId(clientRegistrationId).getRegistrationId();
        String loginPath = contextPath + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + registrationId;

        // Avoid infinite loop and skip API or OIDC system endpoints or bearer token access
        boolean isLoginRequest = requestUri.equals(loginPath);
        boolean isBearerTokenAccess = servletRequest.getHeader("Authorization") != null &&
            servletRequest.getHeader("Authorization").startsWith("Bearer ");
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) && httpSession != null;

        boolean isPublicEndpoint = excludedPathsMatchers.stream()
            .anyMatch(matcher -> matcher.matches(servletRequest));

        if (!isAuthenticated && !isLoginRequest && !isPublicEndpoint && !isBearerTokenAccess) {
            String returningUrl = requestUri +
                (servletRequest.getQueryString() == null ? "" : "?" + servletRequest.getQueryString());

            String redirectUrl = loginPath + "?redirectUrl=" + URLEncoder.encode(returningUrl, Constants.ENCODING);
            servletResponse.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }



}
