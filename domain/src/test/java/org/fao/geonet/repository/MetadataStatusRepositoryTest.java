package org.fao.geonet.repository;


import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class MetadataStatusRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    StatusValueRepository _statusRepo;
    @Autowired
    MetadataStatusRepository _repo;

    @Test
    public void testFindOne() {
        MetadataStatus status = newMetadataStatus();
        status = _repo.save(status);

        MetadataStatus status1 = newMetadataStatus();
        status1 = _repo.save(status1);

        assertEquals(status1, _repo.findOne(status1.getId()));
        assertEquals(status, _repo.findOne(status.getId()));
    }

    @Test
    public void testDeleteAllById_MetadataId() throws Exception {
        MetadataStatus status1 = _repo.save(newMetadataStatus());
        MetadataStatus status2 = newMetadataStatus();
        status2.getId().setMetadataId(status1.getId().getMetadataId());
        status2 = _repo.save(status2);
        MetadataStatus status3 = _repo.save(newMetadataStatus());

        assertEquals(3, _repo.count());
        _repo.deleteAllById_MetadataId(status1.getId().getMetadataId());
        assertEquals(1, _repo.count());
        assertNull(_repo.findOne(status1.getId()));
        assertNull(_repo.findOne(status2.getId()));

        final List<MetadataStatus> all = _repo.findAll();
        assertEquals(1, all.size());
        assertEquals(status3.getId(), all.get(0).getId());
        assertNotNull(_repo.findOne(status3.getId()));
    }

    @Test
    public void testFindAllById_MetadataId() {
        MetadataStatus status = newMetadataStatus();
        status = _repo.save(status);
        MetadataStatus status2 = newMetadataStatus();
        status2.getId().setMetadataId(status.getId().getMetadataId());
        status2 = _repo.save(status2);

        MetadataStatus status1 = newMetadataStatus();
        status1 = _repo.save(status1);


        final Sort sort = SortUtils.createSort(MetadataStatus_.id, MetadataStatusId_.metadataId);
        assertEquals(2, _repo.findAllById_MetadataId(status.getId().getMetadataId(), sort).size());
        assertEquals(1, _repo.findAllById_MetadataId(status1.getId().getMetadataId(), sort).size());
    }

    private MetadataStatus newMetadataStatus() {

        return newMetadataStatus(_inc, _statusRepo);
    }

    public static MetadataStatus newMetadataStatus(AtomicInteger inc, StatusValueRepository statusRepo) {
        int val = inc.incrementAndGet();

        MetadataStatus metadataStatus = new MetadataStatus();

        MetadataStatusId id = new MetadataStatusId();
        id.setMetadataId(inc.incrementAndGet());
        id.setChangeDate(new ISODate());
        id.setUserId(inc.incrementAndGet());
        metadataStatus.setId(id);
        metadataStatus.setChangeMessage("change message " + val);
        final StatusValue statusValue = statusRepo.save(StatusValueRepositoryTest.newStatusValue(inc));
        metadataStatus.setStatusValue(statusValue);

        return metadataStatus;
    }

}
