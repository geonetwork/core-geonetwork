/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.fao.geonet.domain.Profile;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Tests that the JwtHeadersTrivialUser is working.
 */
public class JwtHeadersTrivialUserTest {


    /**
     * test #maxProfile
     * Should give the highest profile in the profileGroups
     */
    @Test
    public void testMaxProfile() {
        Map<Profile, List<String>> profileGroups = new HashMap<>();

        //no profileGroups -> JwtHeadersTrivialUser.MIN_PROFILE
        var maxProfile = JwtHeadersTrivialUser.getMaxProfile(profileGroups);
        Assert.assertEquals(JwtHeadersTrivialUser.MIN_PROFILE, maxProfile);


        //admin -> admin
        profileGroups = new HashMap<>();
        profileGroups.put(Profile.Administrator, new ArrayList<>());
        maxProfile = JwtHeadersTrivialUser.getMaxProfile(profileGroups);
        Assert.assertEquals(Profile.Administrator, maxProfile);

        //Reviewer -> Reviewer
        profileGroups = new HashMap<>();
        profileGroups.put(Profile.Reviewer, new ArrayList<>());
        maxProfile = JwtHeadersTrivialUser.getMaxProfile(profileGroups);
        Assert.assertEquals(Profile.Reviewer, maxProfile);

        //Editor -> Editor
        profileGroups = new HashMap<>();
        profileGroups.put(Profile.Editor, new ArrayList<>());
        maxProfile = JwtHeadersTrivialUser.getMaxProfile(profileGroups);
        Assert.assertEquals(Profile.Editor, maxProfile);


        //Editor,Reviewer -> Reviewer
        profileGroups = new HashMap<>();
        profileGroups.put(Profile.Editor, new ArrayList<>());
        profileGroups.put(Profile.Reviewer, new ArrayList<>());
        maxProfile = JwtHeadersTrivialUser.getMaxProfile(profileGroups);
        Assert.assertEquals(Profile.Reviewer, maxProfile);
    }


    /**
     * tests that the extraction of ProfileRoles is correct
     */
    @Test
    public void testExtractProfileRoles() {

        //no roles -> no profileGroups
        List<String> processedRolesFromHeaders = Arrays.asList();
        var profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(0, profileGroups.size());

        // "Administrator"   ->   "Administrator":[]
        processedRolesFromHeaders = Arrays.asList("Administrator");
        profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(1, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());

        // "g1:Reviewer"   ->   "Reviewer":["g1"]
        processedRolesFromHeaders = Arrays.asList("g1:Reviewer");
        profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(1, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Reviewer));
        Assert.assertEquals(1, profileGroups.get(Profile.Reviewer).size());
        Assert.assertEquals("g1", profileGroups.get(Profile.Reviewer).get(0));

        // "g1:Reviewer","g2:Reviewer"   ->   "Reviewer":["g1",g2]
        processedRolesFromHeaders = Arrays.asList("g1:Reviewer", "g2:Reviewer");
        profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(1, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Reviewer));
        Assert.assertEquals(2, profileGroups.get(Profile.Reviewer).size());
        Assert.assertTrue(profileGroups.get(Profile.Reviewer).contains("g1"));
        Assert.assertTrue(profileGroups.get(Profile.Reviewer).contains("g2"));

        // "g1:Reviewer","g2:Editor"   ->   "Reviewer":["g1"], "Editor":["g2"]
        processedRolesFromHeaders = Arrays.asList("g1:Reviewer", "g2:Editor");
        profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(2, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Reviewer));
        Assert.assertTrue(profileGroups.containsKey(Profile.Editor));
        Assert.assertEquals(1, profileGroups.get(Profile.Reviewer).size());
        Assert.assertEquals(1, profileGroups.get(Profile.Editor).size());
        Assert.assertTrue(profileGroups.get(Profile.Reviewer).contains("g1"));
        Assert.assertTrue(profileGroups.get(Profile.Editor).contains("g2"));

        // "Administrator","g2:Editor"   ->   "Administrator":[], "Editor":["g2"]
        processedRolesFromHeaders = Arrays.asList("Administrator", "g2:Editor");
        profileGroups = JwtHeadersTrivialUser.extractProfileRoles(processedRolesFromHeaders);
        Assert.assertEquals(2, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertTrue(profileGroups.containsKey(Profile.Editor));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());
        Assert.assertEquals(1, profileGroups.get(Profile.Editor).size());
        Assert.assertTrue(profileGroups.get(Profile.Editor).contains("g2"));
    }

    /**
     * Method #UpdateUserWithRoles relies on the above methods, so we don't test this too much
     * The method just updates the user (Profile & ProfileGroups), so we test that here.
     */
    @Test
    public void testUpdateUserWithRoles() {
        // "Administrator","g2:Editor"   ->   "Administrator":[], "Editor":["g2"]  AND Profile=Administrator
        var processedRolesFromHeaders = Arrays.asList("Administrator", "g2:Editor");
        var user = new JwtHeadersTrivialUser("testcaseUser");
        JwtHeadersTrivialUser.updateUserWithRoles(user, processedRolesFromHeaders);

        Assert.assertEquals(Profile.Administrator, user.getProfile());
        var profileGroups = user.getProfileGroups();
        Assert.assertEquals(2, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertTrue(profileGroups.containsKey(Profile.Editor));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());
        Assert.assertEquals(1, profileGroups.get(Profile.Editor).size());
        Assert.assertTrue(profileGroups.get(Profile.Editor).contains("g2"));
    }


    /***
     * Method #handleRoles mostly relies on methods tested above and is mostly about extracting the correct headers from the request
     */
    @Test
    public void testHandleRolesJson() {
        var config = JwtHeadersIntegrationTest.getBasicConfig();
        var user = new JwtHeadersTrivialUser("testCaseUser");
        var request = new MockHttpServletRequest();
        request.addHeader("oidc_id_token_payload", "{\"preferred_username\":\"david.blasby2@geocat.net\",\"resource_access\":{\"live-key2\":{\"roles\":[\"GeonetworkAdministrator\",\"group1:Reviewer\"]}}}");

        JwtHeadersTrivialUser.handleRoles(user, config.getJwtConfiguration(), request);

        Assert.assertEquals(Profile.Administrator, user.getProfile());
        var profileGroups = user.getProfileGroups();
        Assert.assertEquals(2, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertTrue(profileGroups.containsKey(Profile.Reviewer));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());
        Assert.assertEquals(1, profileGroups.get(Profile.Reviewer).size());
        Assert.assertTrue(profileGroups.get(Profile.Reviewer).contains("group1"));
    }

    @Test
    public void testHandleRolesJWT() {
        var config = JwtHeadersIntegrationTest.getBasicConfigJWT();
        var user = new JwtHeadersTrivialUser("testCaseUser");
        var request = new MockHttpServletRequest();
        request.addHeader("TOKEN", JwtHeadersIntegrationTest.JWT);

        JwtHeadersTrivialUser.handleRoles(user, config.getJwtConfiguration(), request);

        Assert.assertEquals(Profile.Administrator, user.getProfile());
        var profileGroups = user.getProfileGroups();
        Assert.assertEquals(1, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());
    }

    /**
     * this is dependent on the above methods, so this is just a quick test
     */
    @Test
    public void testCreate() throws Exception {
        var config = JwtHeadersIntegrationTest.getBasicConfigJWT();
        var request = new MockHttpServletRequest();
        request.addHeader("TOKEN", JwtHeadersIntegrationTest.JWT);

        var user = JwtHeadersTrivialUser.create(config.getJwtConfiguration(), request);

        Assert.assertEquals("david.blasby@geocat.net", user.getUsername());

        Assert.assertEquals(Profile.Administrator, user.getProfile());
        var profileGroups = user.getProfileGroups();
        Assert.assertEquals(1, profileGroups.size());
        Assert.assertTrue(profileGroups.containsKey(Profile.Administrator));
        Assert.assertEquals(0, profileGroups.get(Profile.Administrator).size());
    }
}
