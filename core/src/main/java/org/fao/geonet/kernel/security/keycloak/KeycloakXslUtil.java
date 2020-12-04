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
import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import java.util.*;


public final class KeycloakXslUtil {

    private static KeycloakConfiguration keycloakConfiguration = null;
    private static KeycloakDeployment keycloakDeployment = null;

    private static void loadSecurityProviderConfiguration() {
        if (keycloakConfiguration == null) {
            keycloakConfiguration = ApplicationContextHolder.get().getBean(KeycloakConfiguration.class);
        }

        if (keycloakConfiguration == null) {
            throw new RuntimeException("Keycloak security provider configuration is not found");
        }

        if (keycloakDeployment == null) {
            keycloakDeployment = ApplicationContextHolder.get().getBean(AdapterDeploymentContext.class).resolveDeployment(null);
        }

        if (keycloakDeployment == null) {
            throw new RuntimeException("Cannot locate keycloak resolver to read the keycloak.json file");
        }
    }

	public static String getRealm() {
        loadSecurityProviderConfiguration();

        return keycloakDeployment.getRealm();
	}

    public static String getAuthServerBaseUrl() {
        loadSecurityProviderConfiguration();

        return keycloakDeployment.getAuthServerBaseUrl();
    }

    public static String getClientId() {
        loadSecurityProviderConfiguration();

        if (keycloakDeployment.isPublicClient()) {
            // The client id needs to be a public client.
            // So if this is public client return the client name/resource name.
            return keycloakDeployment.getResourceName();
        } else {
            // Otherwise, the client name will come from a configuration variable that will need to be provided in the configuration.
            return keycloakConfiguration.getPublicClientId();
        }
    }

    public static String getInitOnLoad() {
        loadSecurityProviderConfiguration();
        if (keycloakConfiguration.getLoginType().equals(SecurityProviderConfiguration.LoginType.AUTOLOGIN.toString().toLowerCase())) {
            return "login-required";
        } else {
            return "check-sso";
        }
    }
}
