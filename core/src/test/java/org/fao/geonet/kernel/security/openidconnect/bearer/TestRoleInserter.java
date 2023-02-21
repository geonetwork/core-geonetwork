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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.junit.Test;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRoleInserter {

    @Test
    public void testRoleInserter1() {
        Map m = new HashMap();
        m.put("email", "dave@geocat.net");

        OidcUserInfo result = RoleInserter.insertRoles("resource_access.gn-key.roles",
            new OidcUserInfo(m),
            Arrays.asList("a", "b", "c"));

        assertNotNull(result.getClaims().get("resource_access"));
        Map resource_access = (Map) result.getClaims().get("resource_access");
        assertNotNull(resource_access.get("gn-key"));
        Map gn_key = (Map) resource_access.get("gn-key");
        assertNotNull(gn_key.get("roles"));
        List roles = (List) gn_key.get("roles");

        assertEquals(3, roles.size());
        assertTrue(roles.contains("a"));
        assertTrue(roles.contains("b"));
        assertTrue(roles.contains("c"));
    }

    @Test
    public void testRoleInserter2() {
        Map m = new HashMap();
        m.put("resource_access", new HashMap());

        OidcUserInfo result = RoleInserter.insertRoles("resource_access.gn-key.roles",
            new OidcUserInfo(m),
            Arrays.asList("a", "b", "c"));

        assertNotNull(result.getClaims().get("resource_access"));
        Map resource_access = (Map) result.getClaims().get("resource_access");
        assertNotNull(resource_access.get("gn-key"));
        Map gn_key = (Map) resource_access.get("gn-key");
        assertNotNull(gn_key.get("roles"));
        List roles = (List) gn_key.get("roles");

        assertEquals(3, roles.size());
        assertTrue(roles.contains("a"));
        assertTrue(roles.contains("b"));
        assertTrue(roles.contains("c"));
    }

    @Test
    public void testRoleInserter3() {
        Map m = new HashMap();
        Map _resource_access = new HashMap();
        _resource_access.put("gn-key", new HashMap());
        m.put("resource_access", _resource_access);

        OidcUserInfo result = RoleInserter.insertRoles("resource_access.gn-key.roles",
            new OidcUserInfo(m),
            Arrays.asList("a", "b", "c"));

        assertNotNull(result.getClaims().get("resource_access"));
        Map resource_access = (Map) result.getClaims().get("resource_access");
        assertNotNull(resource_access.get("gn-key"));
        Map gn_key = (Map) resource_access.get("gn-key");
        assertNotNull(gn_key.get("roles"));
        List roles = (List) gn_key.get("roles");

        assertEquals(3, roles.size());
        assertTrue(roles.contains("a"));
        assertTrue(roles.contains("b"));
        assertTrue(roles.contains("c"));
    }

    @Test
    public void testRoleInserter4() {
        Map m = new HashMap();
        Map _resource_access = new HashMap();
        Map _gn_key = new HashMap();
        _gn_key.put("roles", new ArrayList());

        _resource_access.put("gn-key", _gn_key);
        m.put("resource_access", _resource_access);

        OidcUserInfo result = RoleInserter.insertRoles("resource_access.gn-key.roles",
            new OidcUserInfo(m),
            Arrays.asList("a", "b", "c"));

        assertNotNull(result.getClaims().get("resource_access"));
        Map resource_access = (Map) result.getClaims().get("resource_access");
        assertNotNull(resource_access.get("gn-key"));
        Map gn_key = (Map) resource_access.get("gn-key");
        assertNotNull(gn_key.get("roles"));
        List roles = (List) gn_key.get("roles");

        assertEquals(3, roles.size());
        assertTrue(roles.contains("a"));
        assertTrue(roles.contains("b"));
        assertTrue(roles.contains("c"));
    }

}
