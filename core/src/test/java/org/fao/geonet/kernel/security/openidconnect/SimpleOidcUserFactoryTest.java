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

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.openidconnect.bearer.JwtDecoderFactory;
import org.junit.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * tests the SimpleOidcUserFactory
 */
public class SimpleOidcUserFactoryTest {


    //create the factory
    public SimpleOidcUserFactory createFactory() {
        SimpleOidcUserFactory factory = new SimpleOidcUserFactory();
        factory.oidcConfiguration = new OIDCConfiguration();
        factory.oidcConfiguration.setIdTokenRoleLocation("resource_access.gn-key.roles");
        factory.oidcRoleProcessor = new OIDCRoleProcessor();
        factory.oidcRoleProcessor.oidcConfiguration = factory.oidcConfiguration;
        return factory;
    }

    //make sure that user created has the correct information in it.
    @Test
    public void testCreation() throws Exception {
        SimpleOidcUserFactory factory = createFactory();
        OidcIdToken idToken = createToken();
        SimpleOidcUser user = factory.create(idToken, null);

        assertNotNull(user);
        assertEquals("user@example.com", user.getUsername());
        assertEquals("ngiven", user.getFirstname());
        assertEquals("nfamily", user.getSurname());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("geocat", user.getOrganisation());

        assertEquals(Profile.Administrator.toString(), user.getProfile());
    }

    //example ID token
    public OidcIdToken createToken() throws Exception {

        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "user@example.com");
        claims.put("family_name", "nfamily");
        claims.put("given_name", "ngiven");
        claims.put("email", "user@example.com");
        claims.put("organization", "geocat");
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();
        List roles = new ArrayList();
        roles.add("Administrator");
        gn_key.put("roles", roles);
        resource_access.put("gn-key", gn_key);

        claims.put("resource_access", resource_access);


        OidcIdToken idToken = new OidcIdToken("d", Instant.now(), Instant.now().plusSeconds(3600), claims);
        return idToken;
    }

    @Test
    public void testUpdateUser() throws Exception {
        SimpleOidcUserFactory factory = createFactory();
        OidcIdToken idToken = createToken();
        SimpleOidcUser user = factory.create(idToken, null);
        User userGN = new User();
        userGN.setUsername(user.getUsername());
        user.updateUser(userGN);

        assertEquals("user@example.com", userGN.getUsername());
        assertEquals("ngiven", userGN.getName());
        assertEquals("nfamily", userGN.getSurname());
        assertEquals("user@example.com", userGN.getEmail());
        assertEquals("geocat", userGN.getOrganisation());

        assertEquals(Profile.Administrator, userGN.getProfile());
    }
}
