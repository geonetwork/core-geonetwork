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

import static org.junit.Assert.assertEquals;

import org.fao.geonet.domain.User;
import org.junit.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.TestingAuthenticationToken;

public class UsernameAuditorAwareTest {

    @Test
    public void userPrincipal() {
        User user = new User().setUsername("admin");
        assertEquals("admin", UsernameAuditorAware.getUsername(new TestingAuthenticationToken(user, null)));
    }

    @Test
    public void keycloakPrincipal() {
        AccessToken token = new AccessToken();
        token.setPreferredUsername("kcuser");
        KeycloakPrincipal<KeycloakSecurityContext> principal =
            new KeycloakPrincipal<>("a-uuid", new KeycloakSecurityContext(null, token, null, null));
        assertEquals("kcuser", UsernameAuditorAware.getUsername(new TestingAuthenticationToken(principal, null)));
    }

    @Test
    public void stringPrincipal() {
        assertEquals("anonymousUser", UsernameAuditorAware.getUsername(new TestingAuthenticationToken("anonymousUser", null)));
    }
}
