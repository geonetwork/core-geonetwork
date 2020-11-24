/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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
import org.fao.geonet.kernel.setting.SettingManager;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;

public class keycloakPreAuthActionsLoginFilter extends KeycloakPreAuthActionsFilter {
    public static String signinPath = null;

    @Autowired
    LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    @Autowired
    SettingManager settingManager;

    public keycloakPreAuthActionsLoginFilter(UserSessionManagement userSessionManagement) {
        super(userSessionManagement);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if (servletRequest.getPathInfo() != null &&
                !(servletRequest.getContextPath() + KeycloakUtil.getSigninPath()).equals(servletRequest.getRequestURI())  &&
                !(servletRequest.getPathInfo()).equals("/k_logout")  &&
                !isAuthenticated() ) {

            String returningUrl = servletRequest.getRequestURL().toString();
            // If the application is behind a proxy, it is possible that it will get an http request instead of https
            // As this redirect goes back to the client, we need to tell the client to use https
            if (settingManager.getServerURL().startsWith("https://") && returningUrl.startsWith("http://")) {
                returningUrl = returningUrl.replaceFirst("(?i)^http://", "https://");
            }

            // Append query string
            if (servletRequest.getQueryString() != null) {
                returningUrl =  returningUrl + "?" + servletRequest.getQueryString();
            }

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
