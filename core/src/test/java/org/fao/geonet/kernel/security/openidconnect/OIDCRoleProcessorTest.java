/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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
import org.junit.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * test the OIDCRoleProcessor
 */
public class OIDCRoleProcessorTest {


    //creates the OIDCRoleProcessor
    public OIDCRoleProcessor getOIDCRoleProcessor() {
        OIDCRoleProcessor result = new OIDCRoleProcessor();

        result.oidcConfiguration = new OIDCConfiguration();
        result.oidcConfiguration.setRoleConverter(new HashMap<String, String>() {{
            put("CHANGEME", "CHANGED");
        }});
        result.oidcConfiguration.setIdTokenRoleLocation("resource_access.gn-key.roles");
        result.oidcConfiguration.setMinimumProfile("RegisteredUser");

        return result;
    }

    //test that the setRoleConverterString properly parses the serialized form
    @Test
    public void testRoleConverterParser() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();
        oidcRoleProcessor.oidcConfiguration.setRoleConverterString("A=B");
        assertEquals(1, oidcRoleProcessor.oidcConfiguration.getRoleConverter().size());
        assertEquals("A", oidcRoleProcessor.oidcConfiguration.getRoleConverter().keySet().iterator().next());
        assertEquals("B", oidcRoleProcessor.oidcConfiguration.getRoleConverter().values().iterator().next());

        oidcRoleProcessor.oidcConfiguration.setRoleConverterString("A=B:C");
        assertEquals(1, oidcRoleProcessor.oidcConfiguration.getRoleConverter().size());
        assertEquals("A", oidcRoleProcessor.oidcConfiguration.getRoleConverter().keySet().iterator().next());
        assertEquals("B:C", oidcRoleProcessor.oidcConfiguration.getRoleConverter().values().iterator().next());
    }

    //simple test - just make sure the change map works
    @Test
    public void testSimpleConvert() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = Arrays.asList("DO-NOT-CHANGE", "CHANGEME", "DO-NOT-CHANGE2");
        List<String> xformedRoles = oidcRoleProcessor.simpleConvertRoles(roles);

        assertSame(3, xformedRoles.size());
        assertSame("DO-NOT-CHANGE", xformedRoles.get(0));
        assertSame("CHANGED", xformedRoles.get(1));
        assertSame("DO-NOT-CHANGE2", xformedRoles.get(2));
    }

    //change map should remove redundant roles
    @Test
    public void testSimpleConvertUnique() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        //after xform there will be 2 "CHANGED" -- remove one
        List<String> roles = Arrays.asList("DO-NOT-CHANGE", "CHANGEME", "DO-NOT-CHANGE2", "CHANGED");
        List<String> xformedRoles = oidcRoleProcessor.simpleConvertRoles(roles);

        assertSame(3, xformedRoles.size());
        assertSame("DO-NOT-CHANGE", xformedRoles.get(0));
        assertSame("CHANGED", xformedRoles.get(1));
        assertSame("DO-NOT-CHANGE2", xformedRoles.get(2));
    }

    //no "roles" component
    public Map<String, Object> createBadSimpleClaims1() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","SystemGroup1"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();


        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }

    //roles isn't a list - its a map
    public Map<String, Object> createBadSimpleClaims2() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","SystemGroup1"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, Map> gn_key = new HashMap<>();

        gn_key.put("roles", new HashMap<String, Object>());

        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }

    //no gn-key
    public Map<String, Object> createBadSimpleClaims3() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","SystemGroup1"]
        Map<String, Map> resource_access = new HashMap<>();


        claims.put("resource_access", resource_access);
        return claims;
    }

    //no resource_access
    public Map<String, Object> createBadSimpleClaims4() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        return claims;
    }

    //no resource_access is empty
    public Map<String, Object> createBadSimpleClaims5() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");
        Map<String, List> resource_access = new HashMap<>();


        claims.put("resource_access", resource_access);
        return claims;
    }

    //no resource_access is list
    public Map<String, Object> createBadSimpleClaims6() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");
        List<String> resource_access = new ArrayList();
        resource_access.add("Reviewer");
        resource_access.add("SystemGroup1");
        resource_access.add("GROUP1:UserAdmin");
        resource_access.add("GROUP2:Editor");


        claims.put("resource_access", resource_access);
        return claims;
    }

    // utility - create a simple user claims for processing
    public Map<String, Object> createSimpleClaims() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","SystemGroup1"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();
        List roles = new ArrayList();
        roles.add("Reviewer");
        roles.add("Administrator");
        roles.add("SystemGroup1");
        gn_key.put("roles", roles);
        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }

    // utility - create a simple user claims for processing (with a given set of roles)
    public Map<String, Object> createSimpleClaims(List<String> roles) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","SystemGroup1"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();

        gn_key.put("roles", roles);
        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }

    // utility - create a more complex user claims for processing (i.e. roles with GN-group:GN-profile format)
    public Map<String, Object> createComplexClaims() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","SystemGroup1","GROUP1:UserAdmin","GROUP2:Editor"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();
        List roles = new ArrayList();
        roles.add("Reviewer");
        roles.add("SystemGroup1");
        roles.add("GROUP1:UserAdmin");
        roles.add("GROUP2:Editor");
        gn_key.put("roles", roles);
        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }


    // simple test - easiest example
    @Test
    public void testGetTokenRolesSimple() {
        Map<String, Object> claims = createSimpleClaims();
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = oidcRoleProcessor.getTokenRoles(claims);

        assertEquals(3, roles.size());
        assertEquals("Reviewer", roles.get(0));
        assertEquals("Administrator", roles.get(1));
        assertEquals("SystemGroup1", roles.get(2));
    }


    //all should produce no roles (and not throw)
    // these are all, technically, errors but they should not throw
    @Test
    public void testGetTokenRolesBad() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        Map<String, Object> claims = createBadSimpleClaims1();
        List<String> roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

        claims = createBadSimpleClaims2();
        roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

        claims = createBadSimpleClaims3();
        roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

        claims = createBadSimpleClaims4();
        roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

        claims = createBadSimpleClaims5();
        roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

        claims = createBadSimpleClaims6();
        roles = oidcRoleProcessor.getTokenRoles(claims);
        assertEquals(0, roles.size());

    }

    //test with profile-groups
    @Test
    public void testGetTokenRolesComplex() {
        Map<String, Object> claims = createComplexClaims();
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = oidcRoleProcessor.getTokenRoles(claims);

        assertEquals(4, roles.size());
        assertEquals("Reviewer", roles.get(0));
        assertEquals("SystemGroup1", roles.get(1));
        assertEquals("GROUP1:UserAdmin", roles.get(2));
        assertEquals("GROUP2:Editor", roles.get(3));
    }

    // there should be no groups associated with these profiles
    @Test
    public void testGetProfileGroupsNone() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        // SystemGroup1 will be removed as system groups are excluded
        List<String> roles = Arrays.asList("Reviewer", "SystemGroup1", "Administrator");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        assertEquals(2, profileGroups.size());
        assertEquals(0, profileGroups.get(Profile.Reviewer).size());
        assertEquals(0, profileGroups.get(Profile.Administrator).size());
    }

    //these have simple profiles and profile-groups
    @Test
    public void testGetProfileGroups() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        // SystemGroup1 will be removed as system groups are excluded
        List<String> roles = Arrays.asList("Reviewer", "SystemGroup1", "Administrator", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        assertEquals(4, profileGroups.size());
        assertEquals(0, profileGroups.get(Profile.Reviewer).size());
        assertEquals(0, profileGroups.get(Profile.Administrator).size());
        assertEquals(1, profileGroups.get(Profile.UserAdmin).size());
        assertEquals(2, profileGroups.get(Profile.Editor).size());

        assertTrue(profileGroups.get(Profile.UserAdmin).contains("GROUP1"));
        assertTrue(profileGroups.get(Profile.Editor).contains("GROUP1"));
        assertTrue(profileGroups.get(Profile.Editor).contains("GROUP2"));
    }

    //test that max-profile is working (with profiles and profile-groups)
    @Test
    public void testMaxProfile1() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        // SystemGroup1 will be removed as system groups are excluded
        List<String> roles = Arrays.asList("Reviewer", "SystemGroup1", "Administrator", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.Administrator, profile);
    }

    //test that max-profile is working (with profiles and profile-groups)
    @Test
    public void testMaxProfile2() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        // SystemGroup1 will be removed as system groups are excluded
        List<String> roles = Arrays.asList("Reviewer", "SystemGroup1", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.UserAdmin, profile);
    }

    //test that max-profile is working (with profile only)
    @Test
    public void testMaxProfile3() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = Arrays.asList("Reviewer");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.Reviewer, profile);
    }

    //test that max-profile is working (with profile-group only)
    @Test
    public void testMaxProfile4() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = Arrays.asList("GROUP1:Editor");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.Editor, profile);
    }

    //test that max-profile is working (no valid profiles - should be the minimumProfile)
    @Test
    public void testMaxProfile5() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        // SystemGroup1 will be removed as system groups are excluded
        List<String> roles = Arrays.asList("SystemGroup1");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.Guest, profile); // min
    }

    //test that getProfile is working
    @Test
    public void testGetProfile() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();
        Map<String, Object> claims = createSimpleClaims();

        //claims contains Administrator profile
        Profile profile = oidcRoleProcessor.getProfile(claims);
        assertEquals(Profile.Administrator, profile);

        claims = new HashMap<>(); //no claims

        profile = oidcRoleProcessor.getProfile(claims);
        assertEquals(Profile.RegisteredUser, profile); // no claims -> get minimum
    }

    // from standard GN configuration
    private RoleHierarchyImpl getRoleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("  Administrator > UserAdmin\n" +
            "        UserAdmin > Reviewer\n" +
            "        Reviewer > Editor\n" +
            "        Editor > RegisteredUser\n" +
            "        RegisteredUser > Guest");
        return roleHierarchy;
    }

    //an editor profile has authorities Editor, RegisteredUser, and Guest
    @Test
    public void testCreateAuthorities1() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();
        RoleHierarchyImpl roleHierarchy = getRoleHierarchy();

        Map<String, Object> claims = createSimpleClaims(Arrays.asList("Editor"));

        Collection<? extends GrantedAuthority> authorities = oidcRoleProcessor.createAuthorities(roleHierarchy, claims);

        assertEquals(3, authorities.size());

        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.Editor.toString())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.RegisteredUser.toString())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.Guest.toString())));
    }

    //a user with no configured roles will default to RegisteredUser, their authorities will be RegisteredUser and Guest
    @Test
    public void testCreateAuthorities2() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();
        RoleHierarchyImpl roleHierarchy = getRoleHierarchy();

        //no roles -> get min (RegisterdUser)
        Map<String, Object> claims = createSimpleClaims(Arrays.asList());

        Collection<? extends GrantedAuthority> authorities = oidcRoleProcessor.createAuthorities(roleHierarchy, claims);

        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.RegisteredUser.toString())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.Guest.toString())));
    }

    //a user with a profile-group of "group1:Editor" is an Editor and will have authorities as
    // Editor, RegisteredUser, Guest
    @Test
    public void testCreateAuthorities3() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();
        RoleHierarchyImpl roleHierarchy = getRoleHierarchy();

        Map<String, Object> claims = createSimpleClaims(Arrays.asList("group1:Editor"));

        Collection<? extends GrantedAuthority> authorities = oidcRoleProcessor.createAuthorities(roleHierarchy, claims);

        assertEquals(3, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.Editor.toString())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.RegisteredUser.toString())));
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Profile.Guest.toString())));
    }

    @Test
    public void getSystemGroups_withValidSystemGroups_returnsSystemGroups() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = Arrays.asList("externalPublicationRequester", "GroupB:Editor", "Administrator");
        List<String> systemGroups = oidcRoleProcessor.getSystemGroups(roles);

        assertFalse(systemGroups.contains("GroupB:Editor"));
        assertFalse(systemGroups.contains("Administrator"));

        assertEquals(1, systemGroups.size());
        assertTrue(systemGroups.contains("externalPublicationRequester"));
    }

    @Test
    public void getSystemGroups_withNoSystemGroups_returnsEmptyList() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        List<String> roles = Arrays.asList("GroupB:Editor", "Administrator");
        List<String> systemGroups = oidcRoleProcessor.getSystemGroups(roles);

        assertTrue(systemGroups.isEmpty());

        List<String> emptyRoles = new ArrayList<>();
        List<String> emptySystemGroups = oidcRoleProcessor.getSystemGroups(emptyRoles);

        assertTrue(emptySystemGroups.isEmpty());
    }
}
