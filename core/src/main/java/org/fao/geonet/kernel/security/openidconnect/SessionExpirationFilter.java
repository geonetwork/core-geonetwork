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
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import java.io.IOException;
import java.time.Instant;

/**
 * A fix for the token life span issue based on
 *      https://stackoverflow.com/questions/77438484/solved-keycloak-spring-security-oidc-backchannel-logout-unable-to-trigger-re
 *
 * This resolved the issue where keycloak or OIDC session is killed therefor when detected, we also need to kill the spring sessions as well.
 */

public class SessionExpirationFilter implements Filter {
    /**
     * Service to retrieve OAuth2 authorized clients inorder to get access token.
     */
    @Autowired(required = false)
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SessionExpirationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * Token filter to check and invalidate the expired token
     *
     * @param request     http request object
     * @param response    http response
     * @param filterChain Servlet filterChain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Get the session without creating a new one.
        final HttpSession httpSession = httpRequest.getSession(false);
        // Only proceed if
        //      the current session is not null - otherwise there is nothing to expire
        //      the response is not committed  - otherwise we may get an error indicating that the response is already committed when attempting to expire the session.
        if (httpSession != null && !response.isCommitted()) {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OidcUser principal = (OidcUser) oauth2Token.getPrincipal();

                if (principal.getExpiresAt() != null && principal.getExpiresAt().isBefore(Instant.now())) {
                    try {
                        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(
                            OAuth2AuthorizeRequest.withClientRegistrationId(oauth2Token.getAuthorizedClientRegistrationId())
                                .principal(oauth2Token)
                                .build()
                        );

                        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
                            log.warn("Session '{}' for subject '{}', user '{}' is expired due to terminated/expired authentication server session.", httpSession.getId(), principal.getSubject(), principal.getPreferredUsername());
                            httpRequest.logout();
                            SecurityContextHolder.getContext().setAuthentication(null);
                            httpSession.invalidate();
                        }
                    } catch (ClientAuthorizationException e) {
                        log.warn("Authorization exception occurred for session '{}' for user '{}'.", httpSession.getId(), principal.getPreferredUsername(), e);
                        httpRequest.logout();
                        SecurityContextHolder.getContext().setAuthentication(null);
                        httpSession.invalidate();
                    }
                }
            }
        }
        filterChain.doFilter(httpRequest, httpResponse);
    }

    @Override
    public void destroy() {

    }
}
