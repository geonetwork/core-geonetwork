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

package org.fao.geonet.repository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

public class UserRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    UserGroupRepository _userGroupRepository;
    @Autowired
    MetadataRepository _metadataRepo;
    @Autowired
    GroupRepository _groupRepo;
    @Autowired
    UserRepository _userRepo;
    @PersistenceContext
    private EntityManager _entityManager;

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

        _userRepo.save(user);
        // save sets the nodeId
        assertNodeId(user);
        // loading should also set nodeid
        assertNodeId(_userRepo.findAll().get(0));
        assertNodeId(_userRepo.findOne(user.getId()));
        assertNodeId(_userRepo.findOneByUsername(user.getUsername()));
        assertNodeId(_userRepo.findOneByEmail(user.getEmail()));
        assertNodeId(_userRepo.findAllByProfile(user.getProfile()).get(0));

    }

    private void assertNodeId(User loaded4) {
        String testNodeId = "testNodeId";
        assertEquals(testNodeId, loaded4.getSecurity().getNodeId());
        loaded4.getSecurity().setNodeId(null);
        _entityManager.flush();
        _entityManager.clear();
    }

    @Test
    public void testFindByEmailAddress() {
        User user1 = newUser();
        String add1 = "add1";
        String add1b = "add1b";
        user1.getEmailAddresses().add(add1);
        user1.getEmailAddresses().add(add1b);
        user1 = _userRepo.save(user1);

        User user2 = newUser();
        String add2 = "add2";
        String add2b = "add2b";
        user2.getEmailAddresses().add(add2);
        user2.getEmailAddresses().add(add2b);
        user2 = _userRepo.save(user2);

        User foundUser = _userRepo.findOneByEmail(add1);
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByEmail(add1b);
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByEmail(add2b);
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByEmail("xjkjk");
        assertNull(foundUser);
    }

    @Test
    public void testFindByUsernameAndAuthTypeIsNullOrEmpty() {
        User user1 = newUser();
        user1.getSecurity().setAuthType("");
        user1 = _userRepo.save(user1);

        User user2 = newUser();
        user2.getSecurity().setAuthType(null);
        user2 = _userRepo.save(user2);

        User user3 = newUser();
        user3.getSecurity().setAuthType("nonull");
        _userRepo.save(user3);

        User foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user1.getUsername());
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user2.getUsername());
        assertNotNull(foundUser);
        assertEquals(user2.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user3.getUsername());
        assertNull(foundUser);

        foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty("blarg");
        assertNull(foundUser);
    }

    @Test
    public void testFindByUsername() {
        User user1 = newUser();
        user1 = _userRepo.save(user1);

        User foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty(user1.getUsername());
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());

        foundUser = _userRepo.findOneByUsernameAndSecurityAuthTypeIsNullOrEmpty("blarg");
        assertNull(foundUser);
    }

    @Test
    public void testFindAllByGroupOwnerNameAndProfile() {
        Group group1 = _groupRepo.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepo.save(GroupRepositoryTest.newGroup(_inc));

        User editUser = _userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = _userRepo.save(newUser().setProfile(Profile.Reviewer));
        User registeredUser = _userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        _userRepo.save(newUser().setProfile(Profile.Administrator));

        Metadata md1 = MetadataRepositoryTest.newMetadata(_inc);
        md1.getSourceInfo().setGroupOwner(group1.getId());
        md1 = _metadataRepo.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(_inc);
        md2.getSourceInfo().setGroupOwner(group1.getId());
        md2 = _metadataRepo.save(md2);

        Metadata md3 = MetadataRepositoryTest.newMetadata(_inc);
        md3.getSourceInfo().setGroupOwner(group2.getId());
        _metadataRepo.save(md3);

        _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(editUser).setProfile(Profile.Editor));
        _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(registeredUser).setProfile(Profile.RegisteredUser));
        _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(reviewerUser).setProfile(Profile.Editor));
        _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(reviewerUser).setProfile(Profile.Reviewer));

        List<Pair<Integer, User>> found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId()), null,
            SortUtils.createSort(User_.name));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(0).two());
        assertEquals(reviewerUser, found.get(1).two());

        found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId()), null,
            new Sort(new Sort.Order(Sort.Direction.DESC, User_.name.getName())));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).one().intValue());
        assertEquals(md1.getId(), found.get(1).one().intValue());
        assertEquals(editUser, found.get(1).two());
        assertEquals(reviewerUser, found.get(0).two());


        found = _userRepo.findAllByGroupOwnerNameAndProfile(Arrays.asList(md1.getId(), md2.getId()), null, null);

        assertEquals(4, found.size());
        int md1Found = 0;
        int md2Found = 0;
        for (Pair<Integer, User> record : found) {
            if (record.one() == md1.getId()) {
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
        Group group1 = _groupRepo.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = _groupRepo.save(GroupRepositoryTest.newGroup(_inc));

        User editUser = _userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = _userRepo.save(newUser().setProfile(Profile.Reviewer));
        User registeredUser = _userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        _userRepo.save(newUser().setProfile(Profile.Administrator));

        _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(editUser).setProfile(Profile.Editor));
        _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(registeredUser).setProfile(Profile.RegisteredUser));
        _userGroupRepository.save(new UserGroup().setGroup(group2).setUser(reviewerUser).setProfile(Profile.Editor));
        _userGroupRepository.save(new UserGroup().setGroup(group1).setUser(reviewerUser).setProfile(Profile.Reviewer));

        List<Integer> found = Lists.transform(_userRepo.findAllUsersInUserGroups(UserGroupSpecs.hasGroupId(group1.getId())),
            new Function<User, Integer>() {

                @Nullable
                @Override
                public Integer apply(@Nullable User input) {
                    return input.getId();
                }
            });

        assertEquals(2, found.size());
        assertTrue(found.contains(editUser.getId()));
        assertTrue(found.contains(reviewerUser.getId()));

        found = Lists.transform(_userRepo.findAllUsersInUserGroups(Specifications.not(UserGroupSpecs.hasProfile(Profile.RegisteredUser)
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

        User editUser = _userRepo.save(newUser().setProfile(Profile.Editor));
        User reviewerUser = _userRepo.save(newUser().setProfile(Profile.Reviewer));
        _userRepo.save(newUser().setProfile(Profile.RegisteredUser));
        _userRepo.save(newUser().setProfile(Profile.Administrator));

        Metadata md1 = MetadataRepositoryTest.newMetadata(_inc);
        md1.getSourceInfo().setOwner(editUser.getId());
        _metadataRepo.save(md1);

        Metadata md2 = MetadataRepositoryTest.newMetadata(_inc);
        md2.getSourceInfo().setOwner(reviewerUser.getId());
        _metadataRepo.save(md2);

        List<User> found = _userRepo.findAllUsersThatOwnMetadata();

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
        _userRepo.save(usernameDuplicated1);
        _userRepo.save(usernameDuplicated2);
        _userRepo.save(userNonDuplicated1);

        List<String> duplicatedUsernames = _userRepo.findDuplicatedUsernamesCaseInsensitive();
        assertThat("Duplicated usernames don't match the expected ones",
            duplicatedUsernames, CoreMatchers.is(Lists.newArrayList("username1")));
        assertEquals(1, duplicatedUsernames.size());

    }

    private User newUser() {
        User user = newUser(_inc);
        return user;
    }

}
