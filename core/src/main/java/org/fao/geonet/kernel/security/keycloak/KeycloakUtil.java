/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.fao.geonet.utils.Log;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import jakarta.annotation.PostConstruct;

public class KeycloakUtil implements SecurityProviderUtil {
    public static String signinPath = null;
    private static LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    @Autowired
    private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint0;

    @Autowired
    private KeycloakUserUtils keycloakUserUtils;

    @PostConstruct
    private void init () {
        loginUrlAuthenticationEntryPoint = this.loginUrlAuthenticationEntryPoint0;
    }

    /**
     * Retrieve sign in path which will be used in the keycloak settings.
     * @return  the sign in path
     */
    public static String getSigninPath() {
        if (signinPath == null) {
            try {
                signinPath = loginUrlAuthenticationEntryPoint.getLoginFormUrl().split("\\?")[0];
            } catch(BeansException e) {
                // If we cannot find the bean then we will just use a default.
                Log.debug(Geonet.SECURITY, "Could not find the bean, using the default instead");
            }
            // If signinPath is null then something may have gone wrong.
            // This should generally not happen - if it does then lets set to what it currently expected and then log a warning.
            if (StringUtils.isEmpty(signinPath)) {
                signinPath = "/signin";
                Log.warning(Geonet.SECURITY,
                    "Could not detect signin path from configuration. Using /signin");
            }
        }
        return signinPath;
    }

    /**
     * Retrieve authentication header value as a bearer token
     * @return  the authentication header value. In most cases it should be a bearer token header value. i.e. "Bearer ....."
     */
    @Override
    public String getSSOAuthenticationHeaderValue() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof KeycloakPrincipal) {
            RefreshableKeycloakSecurityContext refreshableKeycloakSecurityContext =
                (RefreshableKeycloakSecurityContext) ((KeycloakPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getKeycloakSecurityContext();
            return "Bearer " + refreshableKeycloakSecurityContext.getTokenString();
        }
        return null;
    }

    /**
     * Retrieve user details from the keycloak token
     * return the user details information
     * @param auth authentication object to get the user details from
     * @return the user details information based on the keycloak token
     */
    @Override
    public UserDetails getUserDetails(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof KeycloakPrincipal) {
            return keycloakUserUtils.getUserDetails(((KeycloakPrincipal) auth.getPrincipal()).getKeycloakSecurityContext().getToken(), false);
        } else {
            // If unknown auth class then return null.
            // This will occur when it is an anonymous user.
            return null;
        }

    }

    @Override
    public boolean loginServiceAccount() {
        // Todo: returning false to keep old functionality the same.  If needed, this could login as a service account which could be used by the harvester or any other jobs.
        //       Note: It is suggested that openidconnectbearer is used instead of keycloak if this functionality is required as keycloak driver will be deprecated soon.
        return false;
    }
}
