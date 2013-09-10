package org.fao.geonet.repository.specification;

import static org.fao.geonet.repository.SpringDataTestSupport.*;

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

import static org.junit.Assert.assertEquals;

/**
 * Test the Group specs.
 *
 * User: Jesse
 * Date: 9/10/13
 * Time: 10:22 AM
 */
public class GroupSpecsTest extends AbstractSpringDataTest {

    @Autowired
    GroupRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testIsNotReserved() throws Exception {
        for (ReservedGroup reservedGroup : ReservedGroup.values()) {
            Group group = _repo.save(reservedGroup.getGroupEntityTemplate());

            final Field idField = ReflectionUtils.findField(ReservedGroup.class, "_id");
            idField.setAccessible(true);
            ReflectionUtils.setField(idField, reservedGroup, group.getId());
        }

        Group notReserved = _repo.save(GroupRepositoryTest.newGroup(_inc));

        List<Group> found = _repo.findAll(GroupSpecs.isNotReserved());

        assertEquals(1, found.size());
        assertSameContents(notReserved, found.get(0));
    }
}
