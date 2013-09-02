package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.Metadata;
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
    public void testFindByUUID() throws Exception {
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

        assertSameContents(metadata, repo.findOne(String.valueOf(metadata.getId())));

        assertNull(repo.findOne("213213215"));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testFindByIdStringBadId() throws Exception {
        assertNull(repo.findOne("no a number"));
    }

    private Metadata newMetadata() {
        return newMetadata(inc);
    }

    /**
     * Create a new metadata entity with some default values and ready to save.
     *
     * @param inc an atomic integer for making each creation different from others.
     */
    static Metadata newMetadata(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Metadata metadata = new Metadata().setUuid("uuid" + val).setData("metadata" + val);
        metadata.getDataInfo().setSchemaId("customSchema" + val);
        metadata.getSourceInfo().setSource("source" + val);
        return metadata;
    }

}
