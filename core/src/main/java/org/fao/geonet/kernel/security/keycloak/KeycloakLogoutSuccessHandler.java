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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class KeycloakLogoutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    private KeycloakConfiguration keycloakConfiguration;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String url = request.getRequestURL().toString();
        String redirectUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();

        // If the IDP does not support back channel logout then we will redirect the client to the IDP logout url
        if (keycloakConfiguration.getIDPLogoutUrl() != null) {
            // The redirect url should be in the format similar to https://idp.example.com/logout?redirect={RedirecUrl} we need to replace {RedirecUrl}
            redirectUrl=keycloakConfiguration.getIDPLogoutUrl().replace(KeycloakConfiguration.REDIRECT_PLACEHOLDER, URLEncoder.encode(redirectUrl, Constants.ENCODING));
            Log.debug(Geonet.SECURITY, "redirectUrl: " + redirectUrl);
        }
        response.setStatus(HttpStatus.OK.value());
        response.sendRedirect(redirectUrl);
    }
}

