/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.repository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;


import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class UserRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    UserGroupRepository userGroupRepository;
    @Autowired
    MetadataRepository metadataRepo;
    @Autowired
    GroupRepository groupRepo;
    @Autowired
    UserRepository userRepo;
    @PersistenceContext
    private EntityManager entityManager;

    public static User newUser(AtomicInteger inc) {
        String val = String.format("%04d", inc.incrementAndGet());
        User user = new User().setName("name" + val).setUsername("username" + val);
        user.getSecurity().setPassword("1234567");
        return user;
    }

    @Test
    public void testNodeIdIsSetOnLoad() {
        User user = newUser();
        user.getEmailAddresses().add("test@geonetwork.com");

        assertNull(user.getSecurity().getNodeId());

        userRepo.save(user);
        // save sets the nodeId
        assertNodeId(user);
        // loading should also set nodeid
        assertNodeId(userRepo.findAll().get(0));
        assertNodeId(userRepo.findById(user.getId()).get());
        assertNodeId(userRepo.findOneByUsername(user.getUsername()));
        assertNodeId(userRepo.findOneByEmail(user.getEmail()));
        assertNodeId(userRepo.findAllByProfile(user.getProfile()).get(0));

    }

    private void assertNodeId(User loaded4) {
        String testNodeId = "testNodeId";
        assertEquals(testNodeId, loaded4.getSecurity().getNodeId());
        loaded4.getSecurity().setNodeId(null);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testFindByEmailAddress() {
        User user1 = newUser();
        String add1 = "add1";
        String add1b = "add1b";
        user1.getEmailAddresses().add(add1);
        user1.getEmailAddresses().add(add1b);
        user1 = userRepo.save(user1);

        User user2 = newUser();
        String add2 = "add2";
        String add2b = "add2b";
        user2.getEmailAddresses().add(add2);
        user2.getEmailAddresses().add(add2b);
        user2 = userRepo.save(user2);

        User foundUser = userRepo.findOneByEmail(add1);
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = userRepo.findOneByEmail(add1b);
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = userRepo.findOneByEmail(add2b);
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        // Test case-insensitive
        foundUser = userRepo.findOneByEmail(add2b.toUpperCase());
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        foundUser = userRepo.findOneByEmail("xjkjk");
        assertNull(foundUser);
    }

    @Test
    public void testFindByUsernameAndAuthTypeIsNullOrEmpty() {
        User user1 = newUser();
        user1.getSecurity().setAuthType("");
        user1 = userRepo.save(user1);

        User user2 = newUser();
        user2.getSecurity().setAuthType(null);
        user2 = userRepo.save(user2);

        User user3 = newUser();
        user3.getSecurity().setAuthType("nonull");
        userRepo.save(user3);

        User foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user1.getUsername());
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user2.getUsername());
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user3.getUsername());
        assertNull(foundUser);

        // Test case-insensitive
        foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user3.getUsername().toUpperCase());
        assertNull(foundUser);

        foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty("blarg");
        assertNull(foundUser);
    }


    @Test
    public void testFindOneByEmailAndSecurityAuthTypeIsNullOrEmpty() {
        User user1 = newUser();
        user1.getSecurity().setAuthType("");
        user1.getEmailAddresses().add("user1@geonetwork.com");
        user1 = userRepo.save(user1);

        User user2 = newUser();
        user2.getSecurity().setAuthType(null);
        user2.getEmailAddresses().add("user2@geonetwork.com");
        user2 = userRepo.save(user2);

        User user3 = newUser();
        user3.getSecurity().setAuthType("nonull");
        user3.getEmailAddresses().add("user3@geonetwork.com");
        userRepo.save(user3);

        User foundUser = userRepo.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(user1.getEmail());
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = userRepo.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(user2.getEmail());
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        foundUser = userRepo.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(user3.getEmail());
        assertNull(foundUser);

        // Test case-insensitive
        foundUser = userRepo.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty(user3.getEmail().toUpperCase());
        assertNull(foundUser);

        foundUser = userRepo.findOneByEmailAndSecurityAuthTypeIsNullOrEmpty("blarg");
        assertNull(foundUser);
    }

    @Test
    public void testFindByUsername() {
        User user1 = newUser();
        user1 = userRepo.save(user1);

        User foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user1.getUsername());
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty("blarg");
        assertNull(foundUser);
    }

    @Test
    public void testFindAllByGroupOwnerNameAndProfile() {
        Group group1 = groupRepo.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = groupRepo.save(GroupRepositoryTest.newGroup(_inc));

        User editUser = userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = userRepo.save(newUser().setProfile(Profile.Reviewer));
        User registeredUser = userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        userRepo.save(newUser().setProfile(Profile.Administrator));

        Metadata md1 = MetadataRepositoryTest.newMetadata(_inc);
        md1.getSourceInfo().setGroupOwner(group1.getId());
        md1 = metadataRepo.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(_inc);
        md2.getSourceInfo().setGroupOwner(group1.getId());
        md2 = metadataRepo.save(md2);

        Metadata md3 = MetadataRepositoryTest.newMetadata(_inc);
        md3.getSourceInfo().setGroupOwner(group2.getId());
        metadataRepo.save(md3);

        userGroupRepository.save(new UserGroup().setGroup(group1).setUser(editUser).setProfile(Profile.Editor));
        userGroupRepository.save(new UserGroup().setGroup(group2).setUser(registeredUser).setProfile(Profile.RegisteredUser));
        userGroupRepository.save(new UserGroup().setGroup(group2).setUser(reviewerUser).setProfile(Profile.Editor));
        userGroupRepository.save(new UserGroup().setGroup(group1).setUser(reviewerUser).setProfile(Profile.Reviewer));

        List<Pair<Integer, User>> found = userRepo.findAllByGroupOwnerNameAndProfile(List.of(md1.getId()), null);
        found.sort(Comparator.comparing(s -> s.two().getName()));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(0).two());
        assertEquals(reviewerUser, found.get(1).two());

        found = userRepo.findAllByGroupOwnerNameAndProfile(List.of(md1.getId()), null);
        // Sort by user name descending
        found.sort(Comparator.comparing(s -> s.two().getName(), Comparator.reverseOrder()));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(1).two());
        assertEquals(reviewerUser, found.get(0).two());


        found = userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId(), md2.getId()), null);

        assertEquals(4, found.size());
        int md1Found = 0;
        int md2Found = 0;
        for (Pair<Integer, User> info : found) {
            if (info.one() == md1.getId()) {
                md1Found++;
            } else {
                md2Found++;
            }
        }
        assertEquals(2, md1Found);
        assertEquals(2, md2Found);
    }

    @Test
    public void testFindAllUsersInUserGroups() {
        Group group1 = groupRepo.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = groupRepo.save(GroupRepositoryTest.newGroup(_inc));

        User editUser = userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = userRepo.save(newUser().setProfile(Profile.Reviewer));
        User registeredUser = userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        userRepo.save(newUser().setProfile(Profile.Administrator));

        userGroupRepository.save(new UserGroup().setGroup(group1).setUser(editUser).setProfile(Profile.Editor));
        userGroupRepository.save(new UserGroup().setGroup(group2).setUser(registeredUser).setProfile(Profile.RegisteredUser));
        userGroupRepository.save(new UserGroup().setGroup(group2).setUser(reviewerUser).setProfile(Profile.Editor));
        userGroupRepository.save(new UserGroup().setGroup(group1).setUser(reviewerUser).setProfile(Profile.Reviewer));

        List<Integer> found = Lists.transform(userRepo.findAllUsersInUserGroups(UserGroupSpecs.hasGroupId(group1.getId())),
            new Function<>() {

                @Nullable
                @Override
                public Integer apply(@Nullable User input) {
                    return input.getId();
                }
            });

        assertEquals(2, found.size());
        assertTrue(found.contains(editUser.getId()));
        assertTrue(found.contains(reviewerUser.getId()));

        found = Lists.transform(userRepo.findAllUsersInUserGroups(Specification.not(UserGroupSpecs.hasProfile(Profile.RegisteredUser)
        )), new Function<User, Integer>() {

            @Nullable
            @Override
            public Integer apply(@Nullable User input) {
                return input.getId();
            }
        });

        assertEquals(2, found.size());
        assertTrue(found.contains(editUser.getId()));
        assertTrue(found.contains(reviewerUser.getId()));


    }

    @Test
    public void testFindAllUsersThatOwnMetadata() {
        User editUser = userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = userRepo.save(newUser().setProfile(Profile.Reviewer));
        userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        userRepo.save(newUser().setProfile(Profile.Administrator));

        Metadata md1 = MetadataRepositoryTest.newMetadata(_inc);
        md1.getSourceInfo().setOwner(editUser.getId());
        metadataRepo.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(_inc);
        md2.getSourceInfo().setOwner(reviewerUser.getId());
        metadataRepo.save(md2);

        List<User> found = userRepo.findAllUsersThatOwnMetadata();

        assertEquals(2, found.size());
        boolean editUserFound = false;
        boolean reviewerUserFound = false;

        for (User user : found) {
            if (user.getId() == editUser.getId()) {
                editUserFound = true;
            }
            if (user.getId() == reviewerUser.getId()) {
                reviewerUserFound = true;
            }
        }

        assertTrue(editUserFound);
        assertTrue(reviewerUserFound);
    }

    @Test
    public void testFindDuplicatedUsernamesCaseInsensitive() {
        User usernameDuplicated1 = newUser();
        User usernameDuplicated2 = newUser();
        User userNonDuplicated1 = newUser();
        usernameDuplicated1.setUsername("userNamE1");
        usernameDuplicated2.setUsername("usERNAME1");
        userRepo.save(usernameDuplicated1);
        userRepo.save(usernameDuplicated2);
        userRepo.save(userNonDuplicated1);

        List<String> duplicatedUsernames = userRepo.findDuplicatedUsernamesCaseInsensitive();
        MatcherAssert.assertThat("Duplicated usernames don't match the expected ones",
            duplicatedUsernames, CoreMatchers.is(Lists.newArrayList("username1")));
        assertEquals(1, duplicatedUsernames.size());

    }

    private User newUser() {
        return newUser(_inc);
    }
}
