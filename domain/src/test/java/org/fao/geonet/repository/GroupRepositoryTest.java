package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ReservedGroup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class GroupRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    GroupRepository repo;

    @PersistenceContext
    EntityManager _entityManager;

    private AtomicInteger nextId = new AtomicInteger();

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, repo.count());
        Group group = newGroup();
        Group savedGroup = repo.save(group);

        repo.flush();
        _entityManager.clear();

        group.setId(savedGroup.getId());
        assertEquals(1, repo.count());
        assertSameContents(group, repo.findOne(group.getId()));
        
        repo.deleteAll();

        repo.flush();
        _entityManager.clear();

        assertEquals(0, repo.count());
    }
    
    @Test
    public void testUpdate() throws Exception {
        assertEquals(0, repo.count());
        Group group = newGroup();

        Group savedGroup = repo.save(group);

        repo.flush();
        _entityManager.clear();

        group.setId(savedGroup.getId());

        assertEquals(1, repo.count());
        assertSameContents(group, repo.findOne(group.getId()));

        group.setName("New Name");
        Group savedGroup2 = repo.save(group);

        repo.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, savedGroup2);
        
        assertEquals(1, repo.count());
        assertSameContents(group, repo.findOne(group.getId()));
    }

    @Test
    public void testFindByName() throws Exception {
        Group savedGroup = repo.save(newGroup());

        repo.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, repo.findByName(savedGroup.getName()));
        assertNull(repo.findByName("some wrong name"));
    }
    
    @Test
    public void testFindByEmail() throws Exception {
        Group savedGroup = repo.save(newGroup());

        repo.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, repo.findByEmail(savedGroup.getEmail()));
        assertNull(repo.findByEmail("some wrong email"));
    }
    
    public void testFindReservedGroup() throws Exception {
        Group savedGroup = repo.save(ReservedGroup.all.getGroupEntityTemplate());

        repo.flush();
        _entityManager.clear();

        assertSameContents(savedGroup, repo.findReservedGroup(ReservedGroup.all));
    }

    @Test
    public void testFindReservedOperation() throws Exception {
        int normalId = ReservedGroup.all.getId();
        int id = repo.save(ReservedGroup.all.getGroupEntityTemplate()).getId();
        setId(ReservedGroup.all, id);
        try {
            repo.save(ReservedGroup.all.getGroupEntityTemplate());

            repo.flush();
            _entityManager.clear();

            assertSameContents(ReservedGroup.all.getGroupEntityTemplate(), repo.findReservedGroup(ReservedGroup.all));
            assertNull(repo.findReservedGroup(ReservedGroup.intranet));
        } finally {
            setId(ReservedGroup.all, normalId);
        }
    }

    private void setId(ReservedGroup group, int normalId) throws Exception {
        Field declaredField = group.getClass().getDeclaredField("_id");
        declaredField.setAccessible(true);
        declaredField.set(group, normalId);
    }

    private Group newGroup() {
        int id = nextId.incrementAndGet();
        return new Group()
                .setDescription("Desc "+id)
                .setEmail(id+"@geonet.org")
                .setName("Name "+id);
    }

}
