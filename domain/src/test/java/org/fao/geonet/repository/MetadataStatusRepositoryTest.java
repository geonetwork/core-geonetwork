package org.fao.geonet.repository;


import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.junit.Assert.assertEquals;

@Transactional
public class MetadataStatusRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataStatusRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

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
    public void testFindAllById_MetadataId() {
        MetadataStatus status = newMetadataStatus();
        status = _repo.save(status);
        MetadataStatus status2 = newMetadataStatus();
        status2.getId().setMetadataId(status.getId().getMetadataId());
        status2 = _repo.save(status2);

        MetadataStatus status1 = newMetadataStatus();
        status1 = _repo.save(status1);


        final Sort sort = new Sort(MetadataStatus_.id.getName() + "." + MetadataStatusId_
                .metadataId.getName());
        assertEquals(2, _repo.findAllById_MetadataId(status.getId().getMetadataId(), sort).size());
        assertEquals(1, _repo.findAllById_MetadataId(status1.getId().getMetadataId(), sort).size());
    }

    private MetadataStatus newMetadataStatus() {
        int val = _inc.incrementAndGet();

        MetadataStatus metadataStatus = new MetadataStatus();

        MetadataStatusId id = new MetadataStatusId();
        id.setMetadataId(_inc.incrementAndGet());
        id.setChangeDate(new ISODate());
        id.setStatusId(val);
        id.setUserId(_inc.incrementAndGet());
        metadataStatus.setId(id);
        metadataStatus.setChangeMessage("change message " + val);

        return metadataStatus;
    }

}
