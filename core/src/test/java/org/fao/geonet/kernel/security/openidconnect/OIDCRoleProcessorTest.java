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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * test the OIDCRoleProcessor
 */
public class OIDCRoleProcessorTest {

//    @Test
//    public void t1() throws Exception {
//        String a = "oaLLT9hkcSj2tGfZsjbu7Xz1Krs0qEicXPmEsJKOBQHauZ_kRM1HdEkgOJbUznUspE6xOuOSXjlzErqBxXAu4SCvcvVOCYG2v9G3-uIrLF5dstD0sYHBo1VomtKxzF90Vslrkn6rNQgUGIWgvuQTxm1uRklYFPEcTIRw0LnYknzJ06GC9ljKR617wABVrZNkBuDgQKj37qcyxoaxIGdxEcmVFZXJyrxDgdXh9owRmZn6LIJlGjZ9m59emfuwnBnsIQG7DirJwe9SXrLXnexRQWqyzCdkYaOqkpKrsjuxUj2-MHX31FqsdpJJsOAvYXGOYBKJRjhGrGdONVrZdUdTBQ";
//        String b = "sfsXMXWuO-dniLaIELa3Pyqz9Y_rWff_AVrCAnFSdPHa8__Pmkbt_yq-6Z3u1o4gjRpKWnrjxIh8zDn1Z1RS26nkKcNg5xfWxR2K8CPbSbY8gMrp_4pZn7tgrEmoLMkwfgYaVC-4MiFEo1P2gd9mCdgIICaNeYkG1bIPTnaqquTM5KfT971MpuOVOdM1ysiejdcNDvEb7v284PYZkw2imwqiBY3FR0sVG7jgKUotFvhd7TR5WsA20GS_6ZIkUUlLUbG_rXWGl0YjZLS_Uf4q8Hbo7u-7MaFn8B69F6YaFdDlXm_A0SpedVFWQFGzMsp43_6vEzjfrFDJVAYkwb6xUQ";
//        String c = "yr3v1uETrFfT17zvOiy01w8nO-1t67cmiZLZxq2ISDdte9dw-IxCR7lPV2wezczIRgcWmYgFnsk2j6m10H4tKzcqZM0JJ_NigY29pFimxlL7_qXMB1PorFJdlAKvp5SgjSTwLrXjkr1AqWwbpzG2yZUNN3GE8GvmTeo4yweQbNCd-yO_Zpozx0J34wHBEMuaw-ZfCUk7mdKKsg-EcE4Zv0Xgl9wP2MpKPx0V8gLazxe6UQ9ShzNuruSOncpLYJN_oQ4aKf5ptOp1rsfDY2IK9frtmRTKOdQ-MEmSdjGL_88IQcvCs7jqVz53XKoXRlXB8tMIGOcg-ICer6yxe2itIQ";
//        String d = "spvQcXWqYrMcvcqQmfSMYnbUC8U03YctnXyLIBe148OzhBrgdAOmPfMfJi_tUW8L9svVGpk5qG6dN0n669cRHKqU52GnG0tlyYXmzFC1hzHVgQz9ehve4tlJ7uw936XIUOAOxx3X20zdpx7gm4zHx4j2ZBlXskAj6U3adpHQNuwUE6kmngJWR-deWlEigMpRsvUVQ2O5h0-RSq8Wr_x7ud3K6GTtrzARamz9uk2IXatKYdnj5Jrk2jLY6nWt-GtxlA_l9XwIrOl6Sqa_pOGIpS01JKdxKvpBC9VdS8oXB-7P5qLksmv7tq-SbbiOec0cvU7WP7vURv104V4FiI_qoQ";
//        String e = "wEMMJtj9yMQd8QS6Vnm538K5GN1Pr_I31_LUl9-OCYu-9_DrDvPGjViQK9kOiCjBfyqoAL-pBecn9-XXaS-C4xZTn1ZRw--GELabuo0u-U6r3TKj42xFDEP-_R5RpOGshoC95lrKiU5teuhn4fBM3XfR2GB0dVMcpzN3h4-0OMvBK__Zr9tkQCU_KzXTbNCjyA7ybtbr83NF9k3KjpTyOyY2S-qvFbY-AoqMhL9Rp8r2HBj_vrsr6RX6GeiSxxjbEzDFA2VIcSKbSHvbNBEeW2KjLXkz6QG2LjKz5XsYLp6kv_-k9lPQBy_V7Ci4ZkhAN-6j1S1Kcq58aLbp0wDNKQ";
//        String f = "1n7-nWSLeuWQzBRlYSbS8RjvWvkQeD7QL9fOWaGXbW73VNGH0YipZisPClFv6GzwfWECTWQp19WFe_lASka5-KEWkQVzCbEMaaafOIs7hC61P5cGgw7dhuW4s7f6ZYGZEzQ4F5rHE-YNRbvD51qirPNzKHk3nji1wrh0YtbPPIf--NbI98bCwLLh9avedOmqESzWOGECEMXv8LSM-B9SKg_4QuBtyBwwIakTuqo84swTBM5w8PdhpWZZDtPgH87Wz-_WjWvk99AjXl7l8pWPQJiKNujt_ck3NDFpzaLEppodhUsID0ptRA008eCU6l8T-ux19wZmb_yBnHcV3pFWhQ";
//        String g = "01re9a2BUTtNtdFzLNI-QEHW8XhDiDMDbGMkxHRIYXH41zBccsXwH9vMi0HuxXHpXOzwtUYKwl93ZR37tp6lpvwlU1HePNmZpJ9D-XAvU73x03YKoZEdaFB39VsVyLih3fuPv6DPE2qT-TNE3X5YdIWOGFrcMkcXLsjO-BCq4qcSdBH2lBgEQUuD6nqreLZsg-gPzSDhjVScIUZGiD8M2sKxADiIHo5KlaZIyu32t8JkavP9jM7ItSAjzig1W2yvVQzUQZA-xZqJo2jxB3g_fygdPUHK6UN-_cqkrfxn2-VWH1wMhlm90SpxTMD4HoYOViz1ggH8GCX2aBiX5OzQ6Q";
//
//       String t = "eyJ0eXAiOiJKV1QiLCJub25jZSI6ImM4bDlGQm9KYkhoM2RkRmp1dkpqYWZCeUxFT1VwYmNOeFJEb21oeUVkRDAiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC84N2Y5MTQ5NC1jMGRjLTQ5M2UtODNjMy05MjI2YzExMTg1MGEvIiwiaWF0IjoxNjU1ODMyNzcyLCJuYmYiOjE2NTU4MzI3NzIsImV4cCI6MTY1NTgzNzE3MCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFWUUFxLzhUQUFBQStPZEhCUnIxbytUYWNtUkFySXhDSFJFMWxXOFhpRUMwRG5MTE1ZV3VPb3duOUhjWUlFVmRycEJ5T3B4ZUVFMHVEMWlnc3ZmNXZXTHhFY1JScVdWaVBOa1ZWaFhBNGZwQkZJWE5kOUxTSTN3PSIsImFtciI6WyJwd2QiLCJtZmEiXSwiYXBwX2Rpc3BsYXluYW1lIjoiZm9ya2V5Y2xvYWsiLCJhcHBpZCI6ImI5ZThkMDVhLTA4YjYtNDhhNS04MWM4LTk1OTBhMGY1NTBmMyIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiYmxhc2J5IiwiZ2l2ZW5fbmFtZSI6ImRhdmlkIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiOTYuNTQuODguNjkiLCJuYW1lIjoiZGF2aWQgYmxhc2J5Iiwib2lkIjoiNmFjNjgyYjYtNjA0OC00ZWI2LWI0Y2EtMjUzOGUzM2NjMDlhIiwicGxhdGYiOiI1IiwicHVpZCI6IjEwMDMyMDAxQTUxMUU2QzEiLCJyaCI6IjAuQVY4QWxCVDVoOXpBUGttRHc1SW13UkdGQ2dNQUFBQUFBQUFBd0FBQUFBQUFBQUJmQUhZLiIsInNjcCI6Ikdyb3VwTWVtYmVyLlJlYWQuQWxsIG9wZW5pZCBVc2VyLlJlYWQgcHJvZmlsZSBlbWFpbCIsInNpZ25pbl9zdGF0ZSI6WyJrbXNpIl0sInN1YiI6Im40N3NkYUJDU3FHNnF4NjRISnBvcWpvbHZkV3N5cW5tUmhCLWYydEJfN0EiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiRVUiLCJ0aWQiOiI4N2Y5MTQ5NC1jMGRjLTQ5M2UtODNjMy05MjI2YzExMTg1MGEiLCJ1bmlxdWVfbmFtZSI6ImRhdmlkLmJsYXNieUBnZW9jYXQubmV0IiwidXBuIjoiZGF2aWQuYmxhc2J5QGdlb2NhdC5uZXQiLCJ1dGkiOiJsZ21FaEstdmUwT1Rzc25UOWZOaUFBIiwidmVyIjoiMS4wIiwid2lkcyI6WyI2MmU5MDM5NC02OWY1LTQyMzctOTE5MC0wMTIxNzcxNDVlMTAiLCJiNzlmYmY0ZC0zZWY5LTQ2ODktODE0My03NmIxOTRlODU1MDkiXSwieG1zX3N0Ijp7InN1YiI6IjNvenJ4UUVRd3pwNXIwc1J0WjhOUGtZdkoxeGQyVy1EOGc4eXZjbV9FSW8ifSwieG1zX3RjZHQiOjE1ODczNzQxNjF9.Y2G-Sgg1HAYLrZbIpVfbZ8hTJvHUFL3TJ1ZkWL_1t8NT3QdgVoQekl2OqcnHaRWJK8-0mX-hTgmkBCJ2hadp62w0DmmHr2U_5iCH7nrG4qjp5SEtsn-Q_XQU5HxJ787Aog98CXjr4I0tWDhXBEn8uGIoWIXv7b4uHNIfFa3R7LuMj74WMb9uJu1fGrN5GTM88Gd1riA6sjDpGlK145lou2xtJ54AmVc3e6OSliLXTdglX2-zoCNOkGroc8kJ3BBoaT0YcH6HXhHVNOoa_8WRAxw7L0Fio3l1gpnqTWFD7Tskoden4QEEFNkZM9ISsU3ZP0KvrrEpHDTbsGutvlu5HA";
////        JwtDecoderFactory factory = new JwtDecoderFactory();
////        JwtDecoder decoder = factory.createJwtDecoder(g,"AQAB");
////        decoder.decode(t);
//
////        JwtDecoder decoder2 = NimbusJwtDecoder
////            .withJwkSetUri("https://login.microsoftonline.com/87f91494-c0dc-493e-83c3-9226c111850a/discovery/keys")
////            .build();
////
////        decoder2.decode(t);
//
//        JWT jj = JWTParser.parse(t);
//        ((SignedJWT jj = JWTParser.parse(t);
//        ((SignedJWT) jj).getPayload().toJSONObject()JWT) jj).getPayload().toJSONObject()
//
//    }

    //creates the OIDCRoleProcessor
    public OIDCRoleProcessor getOIDCRoleProcessor() {
        OIDCRoleProcessor result = new OIDCRoleProcessor();

        result.oidcConfiguration = new OIDCConfiguration();
        result.oidcConfiguration.roleConverter = new HashMap<String, String>() {{
            put("CHANGEME", "CHANGED");
        }};
        result.oidcConfiguration.idTokenRoleLocation = "resource_access.gn-key.roles";
        result.oidcConfiguration.minimumProfile = "RegisteredUser";

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

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","blah"]
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

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","blah"]
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

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","blah"]
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
        resource_access.add("blah");
        resource_access.add("GROUP1:UserAdmin");
        resource_access.add("GROUP2:Editor");


        claims.put("resource_access", resource_access);
        return claims;
    }

    // utility - create a simple user claims for processing
    public Map<String, Object> createSimpleClaims() {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","blah"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();
        List roles = new ArrayList();
        roles.add("Reviewer");
        roles.add("Administrator");
        roles.add("blah");
        gn_key.put("roles", roles);
        resource_access.put("gn-key", gn_key);
        claims.put("resource_access", resource_access);
        return claims;
    }

    // utility - create a simple user claims for processing (with a given set of roles)
    public Map<String, Object> createSimpleClaims(List<String> roles) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("abc", "ABC");

        //resource_access.gn-key.roles -> ["Reviewer","Administrator","blah"]
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

        //resource_access.gn-key.roles -> ["Reviewer","blah","GROUP1:UserAdmin","GROUP2:Editor"]
        Map<String, Map> resource_access = new HashMap<>();
        Map<String, List> gn_key = new HashMap<>();
        List roles = new ArrayList();
        roles.add("Reviewer");
        roles.add("blah");
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
        assertEquals("blah", roles.get(2));
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
        assertEquals("blah", roles.get(1));
        assertEquals("GROUP1:UserAdmin", roles.get(2));
        assertEquals("GROUP2:Editor", roles.get(3));
    }

    // there should be no groups associated with these profiles
    @Test
    public void testGetProfileGroupsNone() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        //blah will be removed (its not a profile)
        List<String> roles = Arrays.asList("Reviewer", "blah", "Administrator");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        assertEquals(2, profileGroups.size());
        assertEquals(0, profileGroups.get(Profile.Reviewer).size());
        assertEquals(0, profileGroups.get(Profile.Administrator).size());
    }

    //these have simple profiles and profile-groups
    @Test
    public void testGetProfileGroups() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        //blah will be removed (its not a profile)
        List<String> roles = Arrays.asList("Reviewer", "blah", "Administrator", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
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

        //blah will be removed (its not a profile)
        List<String> roles = Arrays.asList("Reviewer", "blah", "Administrator", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
        Map<Profile, List<String>> profileGroups = oidcRoleProcessor.getProfileGroups(roles);

        Profile profile = oidcRoleProcessor.getMaxProfile(profileGroups);
        assertEquals(Profile.Administrator, profile);
    }

    //test that max-profile is working (with profiles and profile-groups)
    @Test
    public void testMaxProfile2() {
        OIDCRoleProcessor oidcRoleProcessor = getOIDCRoleProcessor();

        //blah will be removed (its not a profile)
        List<String> roles = Arrays.asList("Reviewer", "blah", "GROUP1:UserAdmin", "GROUP2:Editor", "GROUP1:Editor");
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

        //blah will be removed (its not a profile)
        List<String> roles = Arrays.asList("blah");
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
}
