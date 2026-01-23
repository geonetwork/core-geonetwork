/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.shibboleth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.List;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class ShibbolethUserUtilsTest extends AbstractCoreIntegrationTest {

    private ShibbolethUserUtils utils;
    private ShibbolethUserConfiguration config;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private UserGroupRepository userGroupRepo;

    // Default values
    private String surname = "Sur Name";
    private String username = "shibbolethtest";
    private String email = "blabla@bleble.bli";
    private String firstname = "First of her name";
    private String groupname = "ShibTestGroup";
    private String organisation = "Organisation";

    @Before
    public void setUp() {
        utils = new ShibbolethUserUtils();
        utils = spy(utils);
        config = new ShibbolethUserConfiguration();

        config.setArraySeparator(";");
        config.setDefaultGroup(groupname + "1");
        config.setEmailKey("EMAIL_KEY");
        config.setFirstnameKey("FIRSTNAME_KEY");
        config.setGroupKey("GROUP_KEY");
        config.setProfileKey("PROFILE_KEY");
        config.setSurnameKey("SURNAME_KEY");
        config.setUpdateGroup(true);
        config.setUpdateProfile(true);
        config.setUsernameKey("USERNAME_KEY");
        config.setOrganisationKey("ORGANISATION_KEY");
        config.setRoleGroupSeparator(",");

        for (int i = 1; i < 5; i++) {
            Group group = new Group();
            group.setName(groupname + i);
            groupRepo.save(group);
        }
    }

    @After
    public void cleanUp() {
        User user = userRepo.findOneByUsername(username);
        userRepo.deleteById(user.getId());

        for (int i = 1; i < 5; i++) {
            Group group = groupRepo.findByName(groupname + i);
            groupRepo.delete(group);
        }
    }

    @Test
    public void twoConsecutiveLogins() throws Exception {
        User user = userRepo.findOneByUsername(username);
        assertNull("User already exists", user);

        String group = groupname + "1";
        String groups = group + config.getArraySeparator() + group;
        String profile = Profile.UserAdmin.name() + config.getArraySeparator() + Profile.Administrator.name();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), groups);
        request.addHeader(this.config.getProfileKey(), profile);
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);

        doAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class);
            return groupRepo.findByName(name);
        }).when(utils).getOrCreateGroup(anyString(), any());

        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was not created", user);
        assertSame("The profile should be the highest in the list", Profile.Administrator, user.getProfile());

        List<Integer> idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("Groups size is wrong", idGroups.size(), 1);
        assertEquals("The group assigned is wrong", Integer.valueOf(groupRepo.findByName(group).getId()),
                idGroups.get(0));

        // Second round, same user different authorization
        group = groupname + "3";
        groups = group + config.getArraySeparator() + group;
        profile = Profile.Guest.name() + config.getArraySeparator() + Profile.Editor.name();
        request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), groups);
        request.addHeader(this.config.getProfileKey(), profile);
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);
        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was removed", user);

        idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("The profile should be the highest in the list", Profile.Editor, user.getProfile());
        assertSame("Groups size is wrong", idGroups.size(), 1);
        assertEquals("The group assigned is wrong", Integer.valueOf(groupRepo.findByName(group).getId()),
                idGroups.get(0));
    }

    @Test
    public void twoConsecutiveLoginsNoAuthorization() throws Exception {

        config.setUpdateGroup(false);
        config.setUpdateProfile(false);

        User user = userRepo.findOneByUsername(username);
        assertNull("User already exists", user);

        String group = groupname + "1";
        String groups = group + config.getArraySeparator() + group ;
        String profile = Profile.UserAdmin.name() + config.getArraySeparator() + Profile.Administrator.name();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), groups);
        request.addHeader(this.config.getProfileKey(), profile);
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);

        doAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class);
            return groupRepo.findByName(name);
        }).when(utils).getOrCreateGroup(anyString(), any());

        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was not created", user);
        assertSame("The profile should be the highest in the list", Profile.Administrator, user.getProfile());

        List<Integer> idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("Groups size is wrong", idGroups.size(), 1);
        assertEquals("The group assigned is wrong", Integer.valueOf(groupRepo.findByName(group).getId()),
                idGroups.get(0));

        // Second round, same user different authorization but the original
        // authorization should be kept (no updateProfile, updateGroups)
        String groupNew = groupname + "3";
        String groupsgroupNew = groupNew + config.getArraySeparator() + groupNew ;
        request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), groupsgroupNew);
        request.addHeader(this.config.getProfileKey(),
                Profile.Guest.name() + config.getArraySeparator() + Profile.Editor.name());
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);
        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was removed", user);

        idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("The profile should be the highest in the list", Profile.Administrator, user.getProfile());
        assertSame("Groups size is wrong", idGroups.size(), 1);
        assertEquals("The group assigned is wrong", Integer.valueOf(groupRepo.findByName(group).getId()),
                idGroups.get(0));
    }

    @Test
    public void groupLengthNotMatchProfileLength() throws Exception {

        User user = userRepo.findOneByUsername(username);
        assertNull("User already exists", user);

        String group = groupname + "1" + config.getArraySeparator() + groupname + "2" + config.getArraySeparator()
                + groupname + "3";
        String profile = Profile.Editor.name();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), group);
        request.addHeader(this.config.getProfileKey(), profile);
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);
        request.addHeader(this.config.getOrganisationKey(), organisation);

        doAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class);
            return groupRepo.findByName(name);
        }).when(utils).getOrCreateGroup(anyString(), any());

        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was not created", user);
        assertSame("The profile should be the highest in the list", Profile.Editor, user.getProfile());
        List<Integer> idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("Groups size is wrong", idGroups.size(), 3);

        List<UserGroup> groups = userGroupRepo.findAll(UserGroupSpecs.hasUserId(user.getId()));
        for (UserGroup ug : groups) {
            if (ug.getProfile().equals(Profile.Editor)) {
                assertTrue(ug.getGroup().getName().equalsIgnoreCase(groupname + "1"));
            } else if (ug.getProfile().equals(Profile.Guest)) {
                assertTrue(ug.getGroup().getName().equalsIgnoreCase(groupname + "2")
                        || ug.getGroup().getName().equalsIgnoreCase(groupname + "3"));
            } else {
                assertTrue("We have a usergroup we shouldn't have", false);
            }
        }

    }

    @Test
    public void severalGroups() throws Exception {

        User user = userRepo.findOneByUsername(username);
        assertNull("User already exists", user);

        String group = groupname + "1";
        String profile = Profile.Reviewer.name();

        for (int i = 2; i < 5; i++) {
            group = group + config.getArraySeparator() + groupname + i;
            profile = profile + config.getArraySeparator() + Profile.Editor.name();
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(this.config.getEmailKey(), email);
        request.addHeader(this.config.getFirstnameKey(), firstname);
        request.addHeader(this.config.getGroupKey(), group);
        request.addHeader(this.config.getProfileKey(), profile);
        request.addHeader(this.config.getSurnameKey(), surname);
        request.addHeader(this.config.getUsernameKey(), username);

        doAnswer(invocation -> {
            String name = invocation.getArgument(0, String.class);
            return groupRepo.findByName(name);
        }).when(utils).getOrCreateGroup(anyString(), any());

        utils.setupUser(request, this.config);

        // Checks
        user = userRepo.findOneByUsername(username);
        assertNotNull("User was not created", user);
        assertSame("The profile should be the highest in the list", Profile.Reviewer, user.getProfile());

        List<Integer> idGroups = userGroupRepo.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        assertSame("Groups size is wrong", idGroups.size(), 4);

        List<UserGroup> groups = userGroupRepo.findAll(UserGroupSpecs.hasUserId(user.getId()));
        for (UserGroup ug : groups) {
            assertNotSame("No profile can be guest as we have defined a role for all groups.", Profile.Guest,
                    ug.getProfile());
        }
    }

}
