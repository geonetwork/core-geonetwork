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

package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.UserRepositoryTest.newUser;
import static org.fao.geonet.repository.specification.UserSpecs.*;
import static org.fao.geonet.repository.specification.UserSpecs.hasUserIdIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test UserSpecs class User: Jesse Date: 9/12/13 Time: 7:32 PM
 */
public class UserSpecsTest extends AbstractSpringDataTest {
    @Autowired
    UserRepository _userRepo;

    @Test
    public void testHasUserId() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        _userRepo.save(newUser(_inc));

        final List<User> found = _userRepo.findAll(hasUserId(user1.getId()));
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());
    }

    @Test
    public void testHasUserIdIn() throws Exception {
        final User user1 = _userRepo.save(newUser(_inc));
        final User user2 = _userRepo.save(newUser(_inc));
        final User user3 = _userRepo.save(newUser(_inc));

        final List<Integer> ids = Arrays.asList(user1.getId(), user2.getId());
        final List<User> found = _userRepo.findAll(hasUserIdIn(ids));

        assertEquals(2, found.size());
        List<Integer> foundIds = Lists.transform(found, new Function<User, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nullable User input) {
                return input.getId();
            }
        });

        assertTrue(foundIds.contains(user1.getId()));
        assertTrue(foundIds.contains(user2.getId()));
        assertFalse(foundIds.contains(user3.getId()));
    }

    @Test
    public void testHasUserName() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        _userRepo.save(newUser(_inc));

        final List<User> found = _userRepo.findAll(hasUserName(user1.getUsername()));
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());
    }

    @Test
    public void testHasNullAuthType() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        user1.getSecurity().setAuthType(null);
        final User user2 = newUser(_inc);
        user2.getSecurity().setAuthType("authtype");
        _userRepo.save(user2);

        final List<User> found = _userRepo.findAll(hasNullAuthType());
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());
    }

    @Test
    public void testHasProfile() throws Exception {
        final User user1 = newUser(_inc);
        user1.setProfile(Profile.RegisteredUser);
        _userRepo.save(user1);
        final User user2 = newUser(_inc);
        user2.setProfile(Profile.Reviewer);
        _userRepo.save(user2);

        List<User> found = _userRepo.findAll(hasProfile(Profile.RegisteredUser));
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());

        found = _userRepo.findAll(hasProfile(Profile.Reviewer));
        assertEquals(1, found.size());
        assertEquals(user2.getId(), found.get(0).getId());
    }

    @Test
    public void testHasAuthType() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        String expectedType = "expected";
        user1.getSecurity().setAuthType(expectedType);
        final User user2 = newUser(_inc);
        user2.getSecurity().setAuthType("authtype");
        _userRepo.save(user2);

        final List<User> found = _userRepo.findAll(hasAuthType(expectedType));
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());
    }

    @Test
    public void testUserIsNameNotOneOf() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        final User user2 = newUser(_inc);
        _userRepo.save(user2);

        final Specification<User> spec = userIsNameNotOneOf(Arrays.asList(new String[]{user1.getUsername()}));
        final List<User> found = _userRepo.findAll(spec);
        assertEquals(1, found.size());
        assertEquals(user1.getId(), found.get(0).getId());

        assertEquals(0, _userRepo.findAll(userIsNameNotOneOf(Arrays.asList(new String[]{"1", "2"}))).size());
        assertEquals(2, _userRepo.findAll(userIsNameNotOneOf(Arrays.asList(new String[]{user1.getUsername(),
            user2.getUsername()}))).size());
        assertEquals(1, _userRepo.findAll(userIsNameNotOneOf(Arrays.asList(new String[]{user1.getUsername(),
            user1.getUsername()}))).size());

    }

    @Test
    public void testUserHasEnabled() throws Exception {
        User user1 = _userRepo.save(newUser(_inc));
        final User user2 = newUser(_inc);
        _userRepo.save(user2);
        final User user3 = newUser(_inc);
        user3.setEnabled(false);
        _userRepo.save(user3);

        final Specification<User> spec = hasEnabled(true);

        final List<User> enabled = _userRepo.findAll(hasEnabled(true));
        assertEquals(2, enabled.size());

        final List<User> notEnabled = _userRepo.findAll(hasEnabled(false));
        assertEquals(1, notEnabled.size());
        assertEquals(user3.getId(), notEnabled.get(0).getId());
    }
}
