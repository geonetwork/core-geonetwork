package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MetadataRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository _repo;

    @Autowired
    MetadataCategoryRepository _categoryRepo;

    @PersistenceContext
    EntityManager _entityManager;

    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testFindByUUID() throws Exception {
        Metadata metadata = _repo.save(newMetadata());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());

        assertSameContents(metadata, _repo.findOneByUuid(metadata.getUuid()));
        assertNull(_repo.findOneByUuid("wrong uuid"));
    }

    @Test
    public void testFindAllIdsBy() throws Exception {
        Metadata metadata = _repo.save(newMetadata());
        _repo.save(newMetadata());
        _repo.save(newMetadata());

        assertEquals(3, _repo.count());

        List<Integer> ids = _repo.findAllIdsBy(MetadataSpecs.hasMetadataUuid(metadata.getUuid()));

        assertArrayEquals(new Integer[]{metadata.getId()}, ids.toArray(new Integer[1]));
    }

    @Test
    public void testFindByIdString() throws Exception {

        Metadata metadata = _repo.save(newMetadata());

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(1, _repo.count());

        assertSameContents(metadata, _repo.findOne(String.valueOf(metadata.getId())));

        assertNull(_repo.findOne("213213215"));
    }

    @Test
    public void testFindAllByHarvestInfo_Uuid() throws Exception {

        Metadata metadata = _repo.save(newMetadata());
        Metadata metadata2 = _repo.save(newMetadata());
        Metadata metadata3 = newMetadata();
        metadata3.getHarvestInfo().setUuid(metadata2.getHarvestInfo().getUuid());
        _repo.save(metadata3);

        assertEquals(3, _repo.count());

        List<Metadata> found = _repo.findAllByHarvestInfo_Uuid(metadata.getHarvestInfo().getUuid());
        assertEquals(1, found.size());
        assertSameContents(metadata, found.get(0));

        found = _repo.findAllByHarvestInfo_Uuid(metadata2.getHarvestInfo().getUuid());
        assertEquals(2, found.size());

        found = _repo.findAllByHarvestInfo_Uuid("blarg");
        assertEquals(0, found.size());
    }

    @Test
    public void testFindOneOldestByChangeDate() throws Exception {

        Metadata metadata1 = newMetadata();
        metadata1.getDataInfo().setChangeDate(new ISODate("1960-01-01"));
        _repo.save(metadata1);

        Metadata metadata2 = newMetadata();
        metadata2.getDataInfo().setChangeDate(new ISODate("1920-01-01"));
        _repo.save(metadata2);

        assertEquals(2, _repo.count());

        Metadata found = _repo.findOneOldestByChangeDate();
        assertNotNull(found);
        assertSameContents(metadata1, found);
    }

    @Test
    public void testFindAllIdsAndChangeDates() throws Exception {

        Metadata metadata = _repo.save(updateChangeDate(newMetadata(), "1990-12-13"));
        Metadata metadata2 = _repo.save(updateChangeDate(newMetadata(), "1980-12-13"));
        Metadata metadata3 = _repo.save(updateChangeDate(newMetadata(), "1995-12-13"));

        final Sort sortByIdAsc = new Sort(Sort.Direction.DESC, Metadata_.id.getName());
        PageRequest page1 = new PageRequest(0, 2, sortByIdAsc);
        PageRequest page2 = new PageRequest(1, 2, sortByIdAsc);
        Page<Pair<Integer, ISODate>> firstPage = _repo.findAllIdsAndChangeDates(page1);
        Page<Pair<Integer, ISODate>> secondPage = _repo.findAllIdsAndChangeDates(page2);

        assertEquals(2, firstPage.getNumberOfElements());
        assertEquals(0, firstPage.getNumber());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals(3, firstPage.getTotalElements());
        assertTrue(firstPage.isFirstPage());
        assertFalse(firstPage.isLastPage());
        assertTrue(firstPage.hasContent());

        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(1, secondPage.getNumber());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(3, secondPage.getTotalElements());
        assertFalse(secondPage.isFirstPage());
        assertTrue(secondPage.isLastPage());
        assertTrue(secondPage.hasContent());

        assertEquals((Integer) metadata3.getId(), firstPage.getContent().get(0).one());
        assertEquals((Integer) metadata2.getId(), firstPage.getContent().get(1).one());
        assertEquals((Integer) metadata.getId(), secondPage.getContent().get(0).one());

        assertEquals(metadata3.getDataInfo().getChangeDate(), firstPage.getContent().get(0).two());
        assertEquals(metadata2.getDataInfo().getChangeDate(), firstPage.getContent().get(1).two());
        assertEquals(metadata.getDataInfo().getChangeDate(), secondPage.getContent().get(0).two());

        final Sort sortByChangeDate = SortUtils.createSort(Metadata_.dataInfo, MetadataDataInfo_.changeDate);
        page1 = new PageRequest(0, 1, sortByChangeDate);
        page2 = new PageRequest(0, 3, sortByChangeDate);
        firstPage = _repo.findAllIdsAndChangeDates(page1);
        secondPage = _repo.findAllIdsAndChangeDates(page2);

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
        assertNull(_repo.findOne("not a number"));
    }

    private Metadata newMetadata() {
        return newMetadata(_inc);
    }

    /**
     * Create a new metadata entity with some default values and ready to save.
     *
     * @param inc an atomic integer for making each creation different from others.
     */
    public static Metadata newMetadata(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Metadata metadata = new Metadata().setUuid("uuid" + val).setData("<md>metadata" + val + "</md>");
        metadata.getDataInfo().setSchemaId("customSchema" + val);
        metadata.getSourceInfo().setSourceId("source" + val);
        metadata.getHarvestInfo().setUuid("huuid" + val);
        metadata.getHarvestInfo().setHarvested(val % 2 == 0);
        return metadata;
    }

}
