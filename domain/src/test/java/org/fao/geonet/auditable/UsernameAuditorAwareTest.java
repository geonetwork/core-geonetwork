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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.fao.geonet.domain.User;
import org.junit.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
    @Test
    public void keycloakPrincipalWithoutPreferredUsername() {
        AccessToken token = new AccessToken();
        token.setName("Keycloak User");
        KeycloakPrincipal<KeycloakSecurityContext> principal =
            new KeycloakPrincipal<>("a-uuid", new KeycloakSecurityContext(null, token, null, null));
        assertEquals("Keycloak User", UsernameAuditorAware.getUsername(new TestingAuthenticationToken(principal, null)));
    }

    @Test
    public void oidcPrincipal() {
        // The name attribute is the configured userNameAttribute (email by default), not the preferred username
        OidcIdToken idToken = OidcIdToken.withTokenValue("token")
            .subject("a-uuid")
            .claim(StandardClaimNames.PREFERRED_USERNAME, "oidcuser")
            .claim(StandardClaimNames.EMAIL, "oidcuser@example.com")
            .build();
        DefaultOidcUser principal = new DefaultOidcUser(Collections.emptyList(), idToken, StandardClaimNames.EMAIL);
        assertEquals("oidcuser@example.com", UsernameAuditorAware.getUsername(new TestingAuthenticationToken(principal, null)));
    }

    @Test
    public void oAuth2Principal() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(StandardClaimNames.SUB, "a-uuid");
        attributes.put(StandardClaimNames.PREFERRED_USERNAME, "oauth2user");
        attributes.put(StandardClaimNames.EMAIL, "oauth2user@example.com");
        DefaultOAuth2User principal = new DefaultOAuth2User(Collections.emptyList(), attributes, StandardClaimNames.EMAIL);
        assertEquals("oauth2user@example.com", UsernameAuditorAware.getUsername(new TestingAuthenticationToken(principal, null)));
    }

    @Test
    public void jwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("jwtuser").build();
        assertEquals("jwtuser", UsernameAuditorAware.getUsername(new JwtAuthenticationToken(jwt)));
    }

    @Test
    public void anonymousAuditor() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "key", "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
        try {
            assertEquals(Optional.of("anonymousUser"), new UsernameAuditorAware().getCurrentAuditor());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
