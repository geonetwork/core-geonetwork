package org.fao.geonet.repository.specification;


import static org.fao.geonet.repository.specification.MetadataStatusSpecs.*;
import static org.junit.Assert.assertEquals;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases for MetadataStatusSpec
 * User: Jesse
 * Date: 9/6/13
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
@Transactional
public class MetadataStatusSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private StatusValueRepository _statusRepo;

    @Autowired
    private MetadataStatusRepository _repo;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testHasMetadataId() throws Exception {
        MetadataStatus md1 = _repo.save(newMetadataStatus());
        _repo.save(newMetadataStatus());

        final List<MetadataStatus> found = _repo.findAll(hasMetadataId(md1.getId().getMetadataId()));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
    }

    @Test
    public void testHasUserId() throws Exception {
        MetadataStatus md1 = _repo.save(newMetadataStatus());
        _repo.save(newMetadataStatus());

        final List<MetadataStatus> found = _repo.findAll(hasUserId(md1.getId().getUserId()));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
    }

    private MetadataStatus newMetadataStatus() {
        return MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusRepo);
    }
}
