package org.fao.geonet.repository;


import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    private MetadataStatus newMetadataStatus() {
        int val = _inc.incrementAndGet();

        MetadataStatus metadataStatus = new MetadataStatus();

        MetadataStatusId id = new MetadataStatusId();
        id.setMetadataId(_inc.incrementAndGet());
        id.setChangeDate(new ISODate());
        id.setStatusId(val);
        id.setUserId(_inc.incrementAndGet());
        metadataStatus.setId(id);
        metadataStatus.setChangeMessage("change message "+val);

        return metadataStatus;
    }

}
