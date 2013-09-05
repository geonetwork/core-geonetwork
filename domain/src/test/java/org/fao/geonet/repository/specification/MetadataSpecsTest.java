package org.fao.geonet.repository.specification;

import static junit.framework.Assert.*;
import static org.fao.geonet.repository.specification.MetadataSpecs.*;
import static org.fao.geonet.repository.MetadataRepositoryTest.*;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for MetadataSpecs.
 *
 * User: Jesse
 * Date: 9/4/13
 * Time: 10:08 PM
 */
@Transactional
public class MetadataSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private MetadataRepository _repository;

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        assertFindsCorrectMd(md1, hasMetadataId(md1.getId()));
    }

    @Test
    public void testHasMetadataUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasMetadataUuid(md1.getUuid());

        assertFindsCorrectMd(md1, spec);
    }


    @Test
    public void testHasHarvesterUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasHarvesterUuid(md1.getHarvestInfo().getUuid());
        assertFindsCorrectMd(md1,spec);
    }

    private void assertFindsCorrectMd(Metadata md1, Specification<Metadata> spec) {
        Metadata md2 = _repository.save(newMetadata(_inc));

        List<Integer> found = _repository.findAllIdsBy(spec);
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).intValue());
    }
}
