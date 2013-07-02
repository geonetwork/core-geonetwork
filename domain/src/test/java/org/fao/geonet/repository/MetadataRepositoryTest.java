package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class MetadataRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository repo;

    @PersistenceContext
    EntityManager _entityManager;
    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testfindByUUID() throws Exception {
        Metadata metadata = repo.save(newMetadata());

        repo.flush();
        _entityManager.clear();

        assertEquals(1, repo.count());

        assertSameContents(metadata, repo.findByUuid(metadata.getUuid()));
        assertNull(repo.findByUuid("wrong uuid"));
    }

    @Test
    public void testFindByIdString() throws Exception {

        Metadata metadata = repo.save(newMetadata());

        repo.flush();
        _entityManager.clear();

        assertEquals(1, repo.count());

        assertSameContents(metadata, repo.findByIdString(String.valueOf(metadata.getId())));

        assertNull(repo.findByIdString("213213215"));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testFindByIdStringBadId() throws Exception {
        assertNull(repo.findByIdString("no a number"));
    }

    private Metadata newMetadata() {
        int val = inc.incrementAndGet();
        return new Metadata().setUuid("uuid" + val);
    }

}
