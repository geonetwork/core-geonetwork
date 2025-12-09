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

import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.SpringDataTestSupport.setId;
import static org.junit.Assert.*;

public class OperationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    OperationRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    public static Operation newOperation(AtomicInteger inc) {
        int id = inc.incrementAndGet();
        return newOperation(id, "name " + id);
    }

    private static Operation newOperation(int id, String name) {
        return new Operation().setName(name);
    }

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, _repo.count());
        Operation savedOp = _repo.save(newOperation());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());
        assertSameContents(savedOp, _repo.findById(savedOp.getId()).get());

        _repo.deleteAll();
        assertEquals(0, _repo.count());
    }

    @Test
    public void testIsReserved() throws Exception {
        int normalId = ReservedOperation.view.getId();
        int id = _repo.save(ReservedOperation.view.getOperationEntity()).getId();
        setId(ReservedOperation.view, id);
        try {
            _repo.flush();
            _entityManager.flush();
            _entityManager.clear();
            assertEquals(1, _repo.count());
            List<Operation> all = _repo.findAll();
            assertEquals(1, all.size());

            Operation loadedOp = _repo.findById(ReservedOperation.view.getId()).get();
            assertTrue(loadedOp.isReserved());
            assertTrue(loadedOp.is(ReservedOperation.view));
        } finally {
            setId(ReservedOperation.view, normalId);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        assertEquals(0, _repo.count());

        Operation operation = _repo.save(newOperation());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());
        assertSameContents(operation, _repo.findById(operation.getId()).get());

        operation.setName("New Name");
        Operation updatedOperation = _repo.save(operation);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());
        assertSameContents(operation, _repo.findById(operation.getId()).get());
        assertSameContents(operation, updatedOperation);
        assertSameContents(updatedOperation, _repo.findById(operation.getId()).get());
    }

    @Test
    public void testFindByName() throws Exception {
        Operation savedOp = _repo.save(newOperation());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertSameContents(savedOp, _repo.findByName(savedOp.getName()));
        assertNull(_repo.findByName("some wrong name"));
    }

    @Test
    public void testFindReservedOperation() throws Exception {
        int normalId = ReservedOperation.view.getId();
        int id = _repo.save(ReservedOperation.view.getOperationEntity()).getId();
        setId(ReservedOperation.view, id);
        try {
            _repo.save(ReservedOperation.view.getOperationEntity());

            _repo.flush();
            _entityManager.flush();
            _entityManager.clear();

            assertSameContents(ReservedOperation.view.getOperationEntity(), _repo.findReservedOperation(ReservedOperation.view));
            assertNull(_repo.findReservedOperation(ReservedOperation.editing));
        } finally {
            setId(ReservedOperation.view, normalId);
        }
    }

    private Operation newOperation() {
        return newOperation(_inc);
    }

}
