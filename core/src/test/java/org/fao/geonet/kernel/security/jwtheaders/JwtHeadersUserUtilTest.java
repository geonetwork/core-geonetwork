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

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.security.GeonetworkAuthenticationProvider;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * tests that JwtHeadersUserUtil works.
 *
 * Because JwtHeadersUserUtil uses the Group/User/UserGroup repositories, this uses a lot
 * of Mockito to setup different scenarios.
 *
 * The main scenarios we are testing;
 *   1. user is correctly saved (if new) or loaded (if existing)
 *   2. user's profile and profileGroups are correctly updated (or not updated), depending on the
 *      filter's configuration.
 */
public class JwtHeadersUserUtilTest {

    JwtHeadersUserUtil jwtHeadersUserUtil; //spy()-ed

    @Before
    public void setUp() throws Exception {
        jwtHeadersUserUtil = new JwtHeadersUserUtil();
        jwtHeadersUserUtil = spy(jwtHeadersUserUtil);

        jwtHeadersUserUtil.userRepository = Mockito.mock(UserRepository.class);
        jwtHeadersUserUtil.groupRepository = Mockito.mock(GroupRepository.class);
        jwtHeadersUserUtil.userGroupRepository = Mockito.mock(UserGroupRepository.class);
        jwtHeadersUserUtil.authProvider = Mockito.mock(GeonetworkAuthenticationProvider.class);
        jwtHeadersUserUtil.languageRepository = Mockito.mock(LanguageRepository.class);
    }


    /**
     * we have the config setup so it doesn't get any write access from the database
     * + no user in DB
     * + new user created
     */
    @Test
    public void testSimplestCase() {
        doThrow(new UsernameNotFoundException(""))
            .when(jwtHeadersUserUtil.authProvider).loadUserByUsername(any());

        JwtHeadersConfiguration basicConfig = JwtHeadersIntegrationTest.getBasicConfig();
        basicConfig.setUpdateGroup(false);
        basicConfig.setUpdateProfile(false);

        var trivialUser = new JwtHeadersTrivialUser("testcaseUser@example.com");
        trivialUser = spy(trivialUser);

        User userDetails = (User) jwtHeadersUserUtil.getUser(trivialUser, basicConfig);

        Assert.assertEquals("testcaseUser@example.com", userDetails.getUsername());
        Assert.assertEquals("testcaseUser", userDetails.getName());

        //verify helper methods called
        verify(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");
        verify(jwtHeadersUserUtil).createUser(trivialUser, basicConfig);

        // these shouldn't ever be looked at
        verify(jwtHeadersUserUtil, never()).updateGroups(any(), any());
        verify(trivialUser, never()).getProfile();
        verify(trivialUser, never()).getProfileGroups();

        //db should not have been saved to
        verify(jwtHeadersUserUtil.groupRepository, never()).save(any());
        verify(jwtHeadersUserUtil.userGroupRepository, never()).save(any());
        verify(jwtHeadersUserUtil.languageRepository, never()).save(any());

        //user was saved
        verify(jwtHeadersUserUtil.userRepository).save(userDetails);
    }

    /**
     * we have the config setup so it doesn't get any write access from the database
     * + user IS in DB
     */
    @Test
    public void testSimplestCaseAlreadyExists() {
        User user = new User();
        user.setUsername("testcaseUser@example.com");
        user.setName("testcaseUser");
        user.setId(666);

        doReturn(user)
            .when(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");

        JwtHeadersConfiguration basicConfig = JwtHeadersIntegrationTest.getBasicConfig();
        basicConfig.setUpdateGroup(false);
        basicConfig.setUpdateProfile(false);

        var trivialUser = new JwtHeadersTrivialUser("testcaseUser@example.com");
        trivialUser = spy(trivialUser);

        User userDetails = (User) jwtHeadersUserUtil.getUser(trivialUser, basicConfig);

        Assert.assertEquals("testcaseUser@example.com", userDetails.getUsername());
        Assert.assertEquals("testcaseUser", userDetails.getName());

        //verify helper methods called
        verify(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");


        // these shouldn't ever be looked at
        verify(jwtHeadersUserUtil, never()).createUser(trivialUser, basicConfig);
        verify(jwtHeadersUserUtil, never()).updateGroups(any(), any());
        verify(trivialUser, never()).getProfile();
        verify(trivialUser, never()).getProfileGroups();

        //db should not have been saved to
        verify(jwtHeadersUserUtil.groupRepository, never()).save(any());
        verify(jwtHeadersUserUtil.userGroupRepository, never()).save(any());
        verify(jwtHeadersUserUtil.languageRepository, never()).save(any());

        //user wasn't saved (no modification)
        verify(jwtHeadersUserUtil.userRepository, never()).save(userDetails);
    }


    /**
     * we have the config setup so it writes user data to DB
     * + no user in DB
     * + new user created
     * + validate that profile is set
     * + validate that user groups (in db) are updated
     */
    @Test
    public void testNewUserWithGroups() {
        doThrow(new UsernameNotFoundException(""))
            .when(jwtHeadersUserUtil.authProvider).loadUserByUsername(any());

        //make sure that the group ID is set when saved.  GN uses the ID in Set<> operations, so we must SET it.
        when(jwtHeadersUserUtil.groupRepository.save(any())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                ((Group) invocation.getArguments()[0]).setId(new Random().nextInt());
                return ((Group) invocation.getArguments()[0]);
            }
        });

        JwtHeadersConfiguration basicConfig = JwtHeadersIntegrationTest.getBasicConfig();
        basicConfig.setUpdateGroup(true);
        basicConfig.setUpdateProfile(true);

        var trivialUser = new JwtHeadersTrivialUser("testcaseUser@example.com");
        trivialUser.setProfile(Profile.Administrator);

        Map<Profile, List<String>> profileGroups = new HashMap<>();
        profileGroups.put(Profile.Reviewer, Arrays.asList("group1", "group2"));
        trivialUser.setProfileGroups(profileGroups);

        trivialUser = spy(trivialUser);

        User userDetails = (User) jwtHeadersUserUtil.getUser(trivialUser, basicConfig);

        Assert.assertEquals("testcaseUser@example.com", userDetails.getUsername());
        Assert.assertEquals("testcaseUser", userDetails.getName());

        //verify helper methods called
        verify(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");
        verify(jwtHeadersUserUtil).createUser(trivialUser, basicConfig);

        //user should be saved with the Profile (admin)
        verify(jwtHeadersUserUtil.userRepository).save(userDetails); //user was saved
        Assert.assertEquals(Profile.Administrator, userDetails.getProfile());


        //update groups method was called
        verify(jwtHeadersUserUtil).updateGroups(profileGroups, userDetails);

        //group1 and group2 saved to db
        //attempted to find them in DB
        verify(jwtHeadersUserUtil.groupRepository).findByName("group1");
        verify(jwtHeadersUserUtil.groupRepository).findByName("group2");

        //saved
        ArgumentCaptor<Group> groupsCaptor = ArgumentCaptor.forClass(Group.class);
        verify(jwtHeadersUserUtil.groupRepository, times(2)).save(groupsCaptor.capture());

        Assert.assertEquals("group1", groupsCaptor.getAllValues().get(0).getName());
        Assert.assertEquals("group2", groupsCaptor.getAllValues().get(1).getName());


        //user connected to group and role
        ArgumentCaptor<Set> setUserGroupCaptor = ArgumentCaptor.forClass(Set.class);

        verify(jwtHeadersUserUtil.userGroupRepository).updateUserGroups(eq(userDetails.getId()), setUserGroupCaptor.capture());
        Assert.assertEquals(1, setUserGroupCaptor.getAllValues().size());
        List<UserGroup> userGroups = (List<UserGroup>) setUserGroupCaptor.getAllValues().get(0).stream().collect(Collectors.toList());
        Collections.sort(userGroups,
            (o1, o2) -> ((o1).getGroup().getName() + "-" + o1.getProfile()).compareTo((o2).getGroup().getName() + "-" + o2.getProfile()));
        Assert.assertEquals(4, userGroups.size());

        Assert.assertEquals(Profile.Editor, userGroups.get(0).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(0).getUser());
        Assert.assertEquals("group1", userGroups.get(0).getGroup().getName());

        Assert.assertEquals(Profile.Reviewer, userGroups.get(1).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(1).getUser());
        Assert.assertEquals("group1", userGroups.get(1).getGroup().getName());

        Assert.assertEquals(Profile.Editor, userGroups.get(2).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(2).getUser());
        Assert.assertEquals("group2", userGroups.get(2).getGroup().getName());

        Assert.assertEquals(Profile.Reviewer, userGroups.get(3).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(3).getUser());
        Assert.assertEquals("group2", userGroups.get(3).getGroup().getName());
    }


    /**
     * we have the config setup so it writes user data to DB
     * + user IS in DB
     * + validate that profile is set
     * + validate that user groups (in db) are updated
     */
    @Test
    public void testOldUserWithGroups() {
        User user = new User();
        user.setUsername("testcaseUser@example.com");
        user.setName("testcaseUser");
        user.setId(666);

        doReturn(user)
            .when(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");


        //make sure that the group ID is set when saved.  GN uses the ID in Set<> operations, so we must SET it.
        when(jwtHeadersUserUtil.groupRepository.save(any())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                ((Group) invocation.getArguments()[0]).setId(new Random().nextInt());
                return ((Group) invocation.getArguments()[0]);
            }
        });

        JwtHeadersConfiguration basicConfig = JwtHeadersIntegrationTest.getBasicConfig();
        basicConfig.setUpdateGroup(true);
        basicConfig.setUpdateProfile(true);

        var trivialUser = new JwtHeadersTrivialUser("testcaseUser@example.com");
        trivialUser.setProfile(Profile.Administrator);

        Map<Profile, List<String>> profileGroups = new HashMap<>();
        profileGroups.put(Profile.Reviewer, Arrays.asList("group1", "group2"));
        trivialUser.setProfileGroups(profileGroups);

        trivialUser = spy(trivialUser);

        User userDetails = (User) jwtHeadersUserUtil.getUser(trivialUser, basicConfig);

        Assert.assertEquals("testcaseUser@example.com", userDetails.getUsername());
        Assert.assertEquals("testcaseUser", userDetails.getName());

        //verify helper methods called
        verify(jwtHeadersUserUtil.authProvider).loadUserByUsername("testcaseUser@example.com");
        verify(jwtHeadersUserUtil, never()).createUser(trivialUser, basicConfig);

        //user should be saved with the Profile (admin)
        verify(jwtHeadersUserUtil.userRepository).save(userDetails); //user was saved
        Assert.assertEquals(Profile.Administrator, userDetails.getProfile());


        //update groups method was called
        verify(jwtHeadersUserUtil).updateGroups(profileGroups, userDetails);

        //group1 and group2 saved to db
        //attempted to find them in DB
        verify(jwtHeadersUserUtil.groupRepository).findByName("group1");
        verify(jwtHeadersUserUtil.groupRepository).findByName("group2");

        //saved
        ArgumentCaptor<Group> groupsCaptor = ArgumentCaptor.forClass(Group.class);
        verify(jwtHeadersUserUtil.groupRepository, times(2)).save(groupsCaptor.capture());

        Assert.assertEquals("group1", groupsCaptor.getAllValues().get(0).getName());
        Assert.assertEquals("group2", groupsCaptor.getAllValues().get(1).getName());

        //user connected to group and role
        ArgumentCaptor<Set> setUserGroupCaptor = ArgumentCaptor.forClass(Set.class);

        verify(jwtHeadersUserUtil.userGroupRepository).updateUserGroups(eq(userDetails.getId()), setUserGroupCaptor.capture());
        Assert.assertEquals(1, setUserGroupCaptor.getAllValues().size());
        List<UserGroup> userGroups = (List<UserGroup>) setUserGroupCaptor.getAllValues().get(0).stream().collect(Collectors.toList());
        Collections.sort(userGroups,
            (o1, o2) -> ((o1).getGroup().getName() + "-" + o1.getProfile()).compareTo((o2).getGroup().getName() + "-" + o2.getProfile()));
        Assert.assertEquals(4, userGroups.size());

        Assert.assertEquals(Profile.Editor, userGroups.get(0).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(0).getUser());
        Assert.assertEquals("group1", userGroups.get(0).getGroup().getName());

        Assert.assertEquals(Profile.Reviewer, userGroups.get(1).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(1).getUser());
        Assert.assertEquals("group1", userGroups.get(1).getGroup().getName());

        Assert.assertEquals(Profile.Editor, userGroups.get(2).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(2).getUser());
        Assert.assertEquals("group2", userGroups.get(2).getGroup().getName());

        Assert.assertEquals(Profile.Reviewer, userGroups.get(3).getProfile());
        Assert.assertEquals(userDetails, userGroups.get(3).getUser());
        Assert.assertEquals("group2", userGroups.get(3).getGroup().getName());
    }


}
