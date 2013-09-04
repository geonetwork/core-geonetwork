package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Test
    public void testFindAllIdsAndChangeDates() throws Exception {

        Metadata metadata = repo.save(updateChangeDate(newMetadata(), "1990-12-13"));
        Metadata metadata2 = repo.save(updateChangeDate(newMetadata(), "1980-12-13"));
        Metadata metadata3 = repo.save(updateChangeDate(newMetadata(), "1995-12-13"));

        final Sort sortByIdAsc = new Sort(Sort.Direction.DESC, Metadata_.id.getName());
        PageRequest page1 = new PageRequest(0,2, sortByIdAsc);
        PageRequest page2 = new PageRequest(1,2, sortByIdAsc);
        List<Pair<Integer, ISODate>> firstPage = repo.findAllIdsAndChangeDates(page1);
        List<Pair<Integer, ISODate>> secondPage = repo.findAllIdsAndChangeDates(page2);

        assertEquals(2, firstPage.size());
        assertEquals(1, secondPage.size());

        assertEquals((Integer) metadata3.getId(), firstPage.get(0).one());
        assertEquals((Integer) metadata2.getId(), firstPage.get(1).one());
        assertEquals((Integer) metadata.getId(), secondPage.get(0).one());

        assertEquals(metadata3.getDataInfo().getChangeDate(), firstPage.get(0).two());
        assertEquals(metadata2.getDataInfo().getChangeDate(), firstPage.get(1).two());
        assertEquals(metadata.getDataInfo().getChangeDate(), secondPage.get(0).two());

        final Sort sortByChangeDate = new Sort(Metadata_.dataInfo.getName()+"."+ MetadataDataInfo_.changeDate.getName());
        page1 = new PageRequest(0,1, sortByChangeDate);
        page2 = new PageRequest(0,3, sortByChangeDate);
        firstPage = repo.findAllIdsAndChangeDates(page1);
        secondPage = repo.findAllIdsAndChangeDates(page2);

        assertEquals(1, firstPage.size());
        assertEquals(3, secondPage.size());

        assertEquals((Integer) metadata2.getId(), firstPage.get(0).one());
        assertEquals((Integer) metadata2.getId(), secondPage.get(0).one());
        assertEquals((Integer) metadata.getId(), secondPage.get(1).one());
        assertEquals((Integer) metadata3.getId(), secondPage.get(2).one());

        assertEquals(metadata2.getDataInfo().getChangeDate(), firstPage.get(0).two());
        assertEquals(metadata2.getDataInfo().getChangeDate(), secondPage.get(0).two());
        assertEquals(metadata.getDataInfo().getChangeDate(), secondPage.get(1).two());
        assertEquals(metadata3.getDataInfo().getChangeDate(), secondPage.get(2).two());
    }

    private Metadata updateChangeDate(Metadata metadata, String date) {
        metadata.getDataInfo().setChangeDate(new ISODate(date));
        return metadata;
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
