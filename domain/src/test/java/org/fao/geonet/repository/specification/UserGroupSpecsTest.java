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

import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.specification.UserGroupSpecs.*;
import static org.junit.Assert.*;

/**
 * Test the UserGroupSpecs.
 * <p/>
 * User: Jesse Date: 9/10/13 Time: 2:37 PM
 */
public class UserGroupSpecsTest extends AbstractSpringDataTest {

    @Autowired
    UserGroupRepository _repo;
    @Autowired
    UserRepository _userRepo;
    @Autowired
    GroupRepository _groupRepo;

    @Test
    public void testHasGroupId() throws Exception {
        UserGroup ug1 = _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());

        UserGroup found = _repo.findOne(hasGroupId(ug1.getId().getGroupId()));
        assertEquals(ug1.getId(), found.getId());
    }

    @Test
    public void testHasUserId() throws Exception {
        UserGroup ug1 = _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());

        UserGroup found = _repo.findOne(hasUserId(ug1.getId().getUserId()));
        assertEquals(ug1.getId(), found.getId());
    }

    @Test
    public void testHasProfile() throws Exception {
        UserGroup ug1 = _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());
        _repo.save(newUserGroup());

        List<UserGroup> found = _repo.findAll(hasProfile(ug1.getProfile()));

        for (UserGroup userGroup : found) {
            assertEquals(ug1.getProfile(), userGroup.getProfile());
        }
    }

    @Test
    public void testIsReservedGroup() throws Exception {
        UserGroup ug1 = _repo.save(newUserGroup());
        UserGroup ug2 = _repo.save(newUserGroup());
        UserGroup ug3 = _repo.save(newUserGroup());
        UserGroup ug4 = _repo.save(newUserGroup());

        Map<ReservedGroup, Integer> normalIds = new HashMap<ReservedGroup, Integer>();
        // Set all reserveGroup ids so that they are not the the same as those in the other groups
        for (ReservedGroup reservedGroup : ReservedGroup.values()) {
            normalIds.put(reservedGroup, reservedGroup.getId());
            setReservedGroupId(ug1.getId().getGroupId() - 10 + reservedGroup.getId(), reservedGroup);
        }

        try {
            final int groupId = ug3.getId().getGroupId();
            final ReservedGroup group = ReservedGroup.intranet;
            setReservedGroupId(groupId, group);

            setReservedGroupId(groupId, group);
            setReservedGroupId(groupId, group);
            setReservedGroupId(groupId, group);

            List<Integer> found = _repo.findGroupIds(isReservedGroup(true));
            assertFalse(found.contains(ug1.getId().getGroupId()));
            assertFalse(found.contains(ug2.getId().getGroupId()));
            assertTrue(found.contains(groupId));
            assertFalse(found.contains(ug4.getId().getGroupId()));

            found = _repo.findGroupIds(isReservedGroup(false));
            assertTrue(found.contains(ug1.getId().getGroupId()));
            assertTrue(found.contains(ug2.getId().getGroupId()));
            assertFalse(found.contains(groupId));
            assertTrue(found.contains(ug4.getId().getGroupId()));
        } finally {
            for (ReservedGroup reservedGroup : ReservedGroup.values()) {
                setReservedGroupId(normalIds.get(reservedGroup), reservedGroup);
            }

        }
    }

    private void setReservedGroupId(int groupId, ReservedGroup group) {
        final Field idField = ReflectionUtils.findField(ReservedGroup.class, "_id");
        idField.setAccessible(true);
        ReflectionUtils.setField(idField, group, groupId);
    }

    private UserGroup newUserGroup() {
        return UserGroupRepositoryTest.getUserGroup(_inc, _userRepo, _groupRepo);
    }
}
