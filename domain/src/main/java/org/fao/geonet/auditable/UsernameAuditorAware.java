/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.auditable;

import java.util.Optional;
import org.keycloak.KeycloakPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Extracts the current user used, to store the information in the auditable entities.
 */
public class UsernameAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(getUsername(authentication));
    }

    /**
     * Returns the username of the authenticated user.
     * The principal type depends on the authentication provider (database/LDAP, Keycloak, OIDC, JWT...),
     * so it can't be cast to {@link org.fao.geonet.domain.User}.
     */
    public static String getUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof KeycloakPrincipal
            && ((KeycloakPrincipal<?>) principal).getKeycloakSecurityContext().getToken() != null) {
            return ((KeycloakPrincipal<?>) principal).getKeycloakSecurityContext().getToken().getPreferredUsername();
        } else if (principal instanceof OidcUser) {
            return ((OidcUser) principal).getPreferredUsername();
        } else if (principal instanceof OAuth2User) {
            return ((OAuth2User) principal).getAttribute(StandardClaimNames.PREFERRED_USERNAME);
        }
        // Covers JwtAuthenticationToken (returns the principal claim, "sub" by default)
        // and plain principals such as "anonymousUser" (returns principal.toString()).
        return authentication.getName();
    }
}
