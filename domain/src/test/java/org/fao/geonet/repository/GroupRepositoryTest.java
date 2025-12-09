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


import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.SpringDataTestSupport.setId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GroupRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    GroupRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    public static Group newGroup(AtomicInteger nextId) {
        int id = nextId.incrementAndGet();
        return new Group()
            .setDescription("Desc " + id)
            .setEmail(id + "@geonet.org")
            .setName("Name " + id);
    }

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, _repo.count());
        Group group = newGroup();
        Group savedGroup = _repo.save(group);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        group.setId(savedGroup.getId());
        assertEquals(1, _repo.count());
        assertSameContents(group, _repo.findById(group.getId()).get());

        _repo.deleteAll();

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(0, _repo.count());
    }

    @Test
    public void testUpdate() throws Exception {
        assertEquals(0, _repo.count());
        Group group = newGroup();

        Group savedGroup = _repo.save(group);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        group.setId(savedGroup.getId());

        assertEquals(1, _repo.count());
        assertSameContents(group, _repo.findById(group.getId()).get());

        group.setName("New Name");
        Group savedGroup2 = _repo.save(group);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, savedGroup2);

        assertEquals(1, _repo.count());
        assertSameContents(group, _repo.findById(group.getId()).get());
    }

    @Test
    public void testFindByName() throws Exception {
        Group savedGroup = _repo.save(newGroup());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, _repo.findByName(savedGroup.getName()));
        assertNull(_repo.findByName("some wrong name"));
    }

    @Test
    public void testFindByEmail() throws Exception {
        Group savedGroup = _repo.save(newGroup());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, _repo.findByEmail(savedGroup.getEmail()));
        assertNull(_repo.findByEmail("some wrong email"));
    }

    @Test
    public void testFindByMinimumProfileForPrivilegesNotNull() throws Exception {
        Group savedGroup = _repo.save(newGroup().setMinimumProfileForPrivileges(Profile.Reviewer));
        Group savedGroup2 = _repo.save(newGroup());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        List<Group> groups = _repo.findByMinimumProfileForPrivilegesNotNull();
        assertEquals(1, groups.size());
        assertSameContents(savedGroup, groups.get(0));
    }

    @Test
    public void testFindReservedGroup() throws Exception {
        Group savedGroup = _repo.save(ReservedGroup.all.getGroupEntityTemplate());
        int normalId = ReservedGroup.all.getId();
        try {
            setId(ReservedGroup.all, savedGroup.getId());
            assertSameContents(savedGroup, _repo.findReservedGroup(ReservedGroup.all));
        } finally {
            setId(ReservedGroup.all, normalId);
        }
    }

    @Test
    public void testFindReservedOperation() throws Exception {
        int normalId = ReservedGroup.all.getId();
        int id = _repo.save(ReservedGroup.all.getGroupEntityTemplate()).getId();
        setId(ReservedGroup.all, id);
        try {
            _repo.save(ReservedGroup.all.getGroupEntityTemplate());

            _repo.flush();
            _entityManager.flush();
            _entityManager.clear();

            assertSameContents(ReservedGroup.all.getGroupEntityTemplate(), _repo.findReservedGroup(ReservedGroup.all));
            assertNull(_repo.findReservedGroup(ReservedGroup.intranet));
        } finally {
            setId(ReservedGroup.all, normalId);
        }
    }

    @Test
    public void testFindAllIds() throws Exception {
        Group g1 = _repo.save(newGroup());
        Group g2 = _repo.save(newGroup());
        Group g3 = _repo.save(newGroup());

        List<Integer> ids = _repo.findIds();

        assertEquals(3, ids.size());
        assertEquals(g1.getId(), ids.get(0).intValue());
        assertEquals(g2.getId(), ids.get(1).intValue());
        assertEquals(g3.getId(), ids.get(2).intValue());
    }

    private Group newGroup() {
        return newGroup(_inc);
    }

}
