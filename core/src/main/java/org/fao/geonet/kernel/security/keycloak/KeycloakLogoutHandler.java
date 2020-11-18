/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.facade.SimpleHttpFacade;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class KeycloakLogoutHandler extends org.keycloak.adapters.springsecurity.authentication.KeycloakLogoutHandler {

    private AdapterDeploymentContext adapterDeploymentContext;
    public KeycloakLogoutHandler(AdapterDeploymentContext adapterDeploymentContext) {
        super(adapterDeploymentContext);
        this.adapterDeploymentContext = adapterDeploymentContext;
    }


    /**
     *
     * @param request
     * @param response
     * @param authentication
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            this.handleSingleSignOut(request, response, authentication);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @param authenticationToken
     */
    private void handleSingleSignOut(HttpServletRequest request, HttpServletResponse response,Authentication authenticationToken) {
        HttpFacade facade = new SimpleHttpFacade(request, response);
        KeycloakDeployment deployment = adapterDeploymentContext.resolveDeployment(facade);
        RefreshableKeycloakSecurityContext session = (RefreshableKeycloakSecurityContext)authenticationToken.getDetails();
        session.logout(deployment);
    }
}

