package org.fao.geonet.repository;

import static org.fao.geonet.repository.SpringDataTestSupport.assertSameContents;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

        assertSameContents(metadata, repo.findOneByUuid(metadata.getUuid()));
        assertNull(repo.findOneByUuid("wrong uuid"));
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
    public void testFindAllByHarvestInfo_Uuid() throws Exception {

        Metadata metadata = repo.save(newMetadata());
        Metadata metadata2 = repo.save(newMetadata());
        Metadata metadata3 = newMetadata();
        metadata3.getHarvestInfo().setUuid(metadata2.getHarvestInfo().getUuid());
        metadata3 = repo.save(metadata3);

        assertEquals(3, repo.count());

        List<Metadata> found = repo.findAllByHarvestInfo_Uuid(metadata.getHarvestInfo().getUuid());
        assertEquals(1, found.size());
        assertSameContents(metadata, found.get(0));

        found = repo.findAllByHarvestInfo_Uuid(metadata2.getHarvestInfo().getUuid());
        assertEquals(2, found.size());

        found = repo.findAllByHarvestInfo_Uuid("blarg");
        assertEquals(0, found.size());
    }

    @Test
    public void testFindAllIdsAndChangeDates() throws Exception {

        Metadata metadata = repo.save(updateChangeDate(newMetadata(), "1990-12-13"));
        Metadata metadata2 = repo.save(updateChangeDate(newMetadata(), "1980-12-13"));
        Metadata metadata3 = repo.save(updateChangeDate(newMetadata(), "1995-12-13"));

        final Sort sortByIdAsc = new Sort(Sort.Direction.DESC, Metadata_.id.getName());
        PageRequest page1 = new PageRequest(0,2, sortByIdAsc);
        PageRequest page2 = new PageRequest(1,2, sortByIdAsc);
        Page<Pair<Integer, ISODate>> firstPage = repo.findAllIdsAndChangeDates(page1);
        Page<Pair<Integer, ISODate>> secondPage = repo.findAllIdsAndChangeDates(page2);

        assertEquals(2, firstPage.getNumberOfElements());
        assertEquals(1, firstPage.getNumber());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(3, firstPage.getTotalElements());
        assertTrue(firstPage.isFirstPage());
        assertTrue(firstPage.hasContent());

        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(2, secondPage.getNumber());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(3, secondPage.getTotalElements());
        assertFalse(secondPage.isFirstPage());
        assertTrue(secondPage.isFirstPage());
        assertTrue(secondPage.hasContent());

        assertEquals((Integer) metadata3.getId(), firstPage.getContent().get(0).one());
        assertEquals((Integer) metadata2.getId(), firstPage.getContent().get(1).one());
        assertEquals((Integer) metadata.getId(), secondPage.getContent().get(0).one());

        assertEquals(metadata3.getDataInfo().getChangeDate(), firstPage.getContent().get(0).two());
        assertEquals(metadata2.getDataInfo().getChangeDate(), firstPage.getContent().get(1).two());
        assertEquals(metadata.getDataInfo().getChangeDate(), secondPage.getContent().get(0).two());

        final Sort sortByChangeDate = new Sort(Metadata_.dataInfo.getName()+"."+ MetadataDataInfo_.changeDate.getName());
        page1 = new PageRequest(0,1, sortByChangeDate);
        page2 = new PageRequest(0,3, sortByChangeDate);
        firstPage = repo.findAllIdsAndChangeDates(page1);
        secondPage = repo.findAllIdsAndChangeDates(page2);

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(3, secondPage.getNumberOfElements());

        assertEquals((Integer) metadata2.getId(), firstPage.getContent().get(0).one());
        assertEquals((Integer) metadata2.getId(), secondPage.getContent().get(0).one());
        assertEquals((Integer) metadata.getId(), secondPage.getContent().get(1).one());
        assertEquals((Integer) metadata3.getId(), secondPage.getContent().get(2).one());

        assertEquals(metadata2.getDataInfo().getChangeDate(), firstPage.getContent().get(0).two());
        assertEquals(metadata2.getDataInfo().getChangeDate(), secondPage.getContent().get(0).two());
        assertEquals(metadata.getDataInfo().getChangeDate(), secondPage.getContent().get(1).two());
        assertEquals(metadata3.getDataInfo().getChangeDate(), secondPage.getContent().get(2).two());
    }

    private Metadata updateChangeDate(Metadata metadata, String date) {
        metadata.getDataInfo().setChangeDate(new ISODate(date));
        return metadata;
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testFindByIdStringBadId() throws Exception {
        assertNull(repo.findOne("not a number"));
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
        metadata.getHarvestInfo().setUuid("huuid" + val);
        metadata.getHarvestInfo().setHarvested(val % 2 == 0);
        return metadata;
    }

}
