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

package org.fao.geonet.kernel.security.keycloak;

import org.fao.geonet.Constants;
import org.fao.geonet.security.web.csrf.GeonetworkCsrfSecurityRequestMatcher;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.keycloak.adapters.springsecurity.filter.KeycloakCsrfRequestMatcher;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.constants.AdapterConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class KeycloakPreAuthActionsLoginFilter extends KeycloakPreAuthActionsFilter {

    @Autowired
    LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    public KeycloakPreAuthActionsLoginFilter(UserSessionManagement userSessionManagement) {
        super(userSessionManagement);
    }

    public KeycloakPreAuthActionsLoginFilter(UserSessionManagement userSessionManagement, CsrfFilter csrfFilter,
                                             GeonetworkCsrfSecurityRequestMatcher csrfRequestMatcher) {
        super(userSessionManagement);
        // Set the csrf filter request matcher for Keycloak so that it allows k_* endpoints to be reached without CSRF.
        // Without this fix, the back-channel logout was not working due to CSRF failures.
        csrfRequestMatcher.addRequestMatcher(new KeycloakCsrfRequestMatcher());
        csrfFilter.setRequireCsrfProtectionMatcher(csrfRequestMatcher);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        // Lets redirect the user to the signin page if
        //     - The session is not authenticate
        //     - This is not a request for the signin page (we don't want endless loop to sign in page)
        //     - It does not match the default request matcher (which is mostly used to validate bearer tokens request for api's)
        //       No sign in page required for api calls.
        //     - and it is not an internal k_* request which should be processed by keycloak adapter and also don't required login page.
        if (servletRequest.getPathInfo() != null &&
            !KeycloakAuthenticationProcessingFilter.DEFAULT_REQUEST_MATCHER.matches(servletRequest) &&
            !isAuthenticated() &&
            !(servletRequest.getContextPath() + KeycloakUtil.getSigninPath()).equals(servletRequest.getRequestURI()) &&
            !servletRequest.getRequestURI().endsWith(AdapterConstants.K_LOGOUT) &&
            !servletRequest.getRequestURI().endsWith(AdapterConstants.K_PUSH_NOT_BEFORE) &&
            !servletRequest.getRequestURI().endsWith(AdapterConstants.K_QUERY_BEARER_TOKEN) &&
            !servletRequest.getRequestURI().endsWith(AdapterConstants.K_TEST_AVAILABLE) &&
            !servletRequest.getRequestURI().endsWith(AdapterConstants.K_JWKS)) {

            // Get request uri which is a relative path. Absolute paths will be ignored if they are received as returning url.
            String returningUrl = servletRequest.getRequestURI() +
                (servletRequest.getQueryString() == null ? "" : "?" + servletRequest.getQueryString());

            String encodedRedirectURL = ((HttpServletResponse) response).encodeRedirectURL(
                servletRequest.getContextPath() + KeycloakUtil.getSigninPath() + "?redirectUrl=" + URLEncoder.encode(returningUrl, Constants.ENCODING));

            servletResponse.sendRedirect(encodedRedirectURL);

            // No further action required as we are redirecting to new page
            return;
        }

        super.doFilter(servletRequest, servletResponse, chain);
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
            isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
