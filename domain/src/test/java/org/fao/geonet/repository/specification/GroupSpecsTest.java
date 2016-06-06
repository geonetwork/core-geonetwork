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

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.specification.GroupSpecs.isReserved;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.data.jpa.domain.Specifications.not;

/**
 * Test the Group specs.
 * <p/>
 * User: Jesse Date: 9/10/13 Time: 10:22 AM
 */
public class GroupSpecsTest extends AbstractSpringDataTest {

    @Autowired
    GroupRepository _repo;

    @Test
    public void testIsNotReserved() throws Exception {
        for (ReservedGroup reservedGroup : ReservedGroup.values()) {
            Group group = _repo.save(reservedGroup.getGroupEntityTemplate());

            final Field idField = ReflectionUtils.findField(ReservedGroup.class, "_id");
            idField.setAccessible(true);
            ReflectionUtils.setField(idField, reservedGroup, group.getId());
        }

        Group notReserved = _repo.save(GroupRepositoryTest.newGroup(_inc));

        List<Group> found = _repo.findAll(isReserved());

        assertEquals(ReservedGroup.values().length, found.size());
        for (Group group : found) {
            assertTrue(group.isReserved());
        }

        found = _repo.findAll(not(isReserved()));

        assertEquals(1, found.size());
        assertSameContents(notReserved, found.get(0));
    }
}
