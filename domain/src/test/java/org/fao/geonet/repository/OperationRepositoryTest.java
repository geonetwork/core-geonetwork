package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class OperationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    OperationRepository repo;

    @Autowired
    ApplicationContext context;

    @PersistenceContext
    EntityManager _entityManager;

    private AtomicInteger nextId = new AtomicInteger(20);

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, repo.count());
        Operation savedOp = repo.save(newOperation());

        repo.flush();
        _entityManager.clear();

        assertEquals(1, repo.count());
        assertSameContents(savedOp, repo.findOne(savedOp.getId()));

        repo.deleteAll();
        assertEquals(0, repo.count());
    }

    @Test
    public void testIsReserved() throws Exception {
        int normalId = ReservedOperation.view.getId();
        int id = repo.save(ReservedOperation.view.getOperationEntity()).getId();
        setId(ReservedOperation.view, id);
        try {
            repo.flush();
            _entityManager.clear();
            assertEquals(1, repo.count());
            List<Operation> all = repo.findAll();
            assertEquals(1, all.size());

            Operation loadedOp = repo.findOne(ReservedOperation.view.getId());
            assertTrue(loadedOp.isReserved());
            assertTrue(loadedOp.is(ReservedOperation.view));
        } finally {
            setId(ReservedOperation.view, normalId);
        }
    }

    private void setId(ReservedOperation view, int normalId) throws Exception {
        Field declaredField = view.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(view, normalId);
    }

    @Test
    public void testUpdate() throws Exception {
        assertEquals(0, repo.count());

        Operation operation = repo.save(newOperation());

        repo.flush();
        _entityManager.clear();

        assertEquals(1, repo.count());
        assertSameContents(operation, repo.findOne(operation.getId()));

        operation.setName("New Name");
        Operation updatedOperation = repo.save(operation);

        repo.flush();
        _entityManager.clear();

        assertEquals(1, repo.count());
        assertSameContents(operation, repo.findOne(operation.getId()));
        assertSameContents(operation, updatedOperation);
        assertSameContents(updatedOperation, repo.findOne(operation.getId()));
    }

    @Test
    public void testFindByName() throws Exception {
        Operation savedOp = repo.save(newOperation());

        repo.flush();
        _entityManager.clear();

        assertSameContents(savedOp, repo.findByName(savedOp.getName()));
        assertNull(repo.findByName("some wrong name"));
    }

    @Test
    public void testFindReservedOperation() throws Exception {
        int normalId = ReservedOperation.view.getId();
        int id = repo.save(ReservedOperation.view.getOperationEntity()).getId();
        setId(ReservedOperation.view, id);
        try {
            repo.save(ReservedOperation.view.getOperationEntity());

            repo.flush();
            _entityManager.clear();

            assertSameContents(ReservedOperation.view.getOperationEntity(), repo.findReservedOperation(ReservedOperation.view));
            assertNull(repo.findReservedOperation(ReservedOperation.editing));
        } finally {
            setId(ReservedOperation.view, normalId);
        }
    }

    private Operation newOperation() {
        int id = nextId.incrementAndGet();
        return newOperation(id, "name " + id);
    }

    private Operation newOperation(int id, String name) {
        return new Operation().setId(id).setName(name);
    }

}
