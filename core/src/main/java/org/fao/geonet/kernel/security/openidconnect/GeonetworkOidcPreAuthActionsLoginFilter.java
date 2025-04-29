/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Filter implementation for handling pre-authentication actions for OpenID Connect (OIDC) login.
 * This filter checks if the user is authenticated and redirects unauthenticated users to the login page.
 */
public class GeonetworkOidcPreAuthActionsLoginFilter  implements Filter {

    /**
     * Repository for managing client registrations for OpenID Connect.
     * This is used to retrieve client registration details such as registration ID.
     */
    @Autowired
    private  ClientRegistrationRepository clientRegistrationRepository;



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

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
            !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);

        boolean isPublicEndpoint =
            requestUri.endsWith("/.well-known/jwks.json");

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
