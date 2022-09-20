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


import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * this is a very simple class that computes the FULL url for logout.
 * Since it's difficult to determine the GN host/port, we look at the incoming request to create the post-logout URL.
 * <p>
 * This should be the base geonetwork url - i.e. http://localhost:8080/geonetwork
 */
public class GeonetworkOidcLogoutHandler implements LogoutSuccessHandler {


    @Autowired
    ServletContext servletContext;

    OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;

    public GeonetworkOidcLogoutHandler(OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler) throws URISyntaxException {
        this.oidcClientInitiatedLogoutSuccessHandler = oidcClientInitiatedLogoutSuccessHandler;
    }


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        this.oidcClientInitiatedLogoutSuccessHandler.setPostLogoutRedirectUri(createPostLogoutRedirectUri(request));

        oidcClientInitiatedLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
    }

    private URI createPostLogoutRedirectUri(HttpServletRequest request) {
        String uri = "";
        try {
            String protocol = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String path = servletContext.getContextPath();
            uri = protocol + "://" + host + ":" + port + path;
            return new URI(uri);
        } catch (URISyntaxException e) {
            Log.debug(Geonet.SECURITY,"OIDC Post Logout Redirect Uri is invalid.  Likely you can ignore this -"
                +uri,e);
        }
        return null;
    }
}
