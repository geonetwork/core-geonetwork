package org.fao.geonet.repository;

import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.SpringDataTestSupport.setId;
import static org.junit.Assert.*;

public class OperationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    OperationRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, _repo.count());
        Operation savedOp = _repo.save(newOperation());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());
        assertSameContents(savedOp, _repo.findOne(savedOp.getId()));

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

            Operation loadedOp = _repo.findOne(ReservedOperation.view.getId());
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
        assertSameContents(operation, _repo.findOne(operation.getId()));

        operation.setName("New Name");
        Operation updatedOperation = _repo.save(operation);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());
        assertSameContents(operation, _repo.findOne(operation.getId()));
        assertSameContents(operation, updatedOperation);
        assertSameContents(updatedOperation, _repo.findOne(operation.getId()));
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
    public static Operation newOperation(AtomicInteger inc) {
        int id = inc.incrementAndGet();
        return newOperation(id, "name " + id);
    }

    private static Operation newOperation(int id, String name) {
        return new Operation().setName(name);
    }

}
