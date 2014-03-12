package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.fao.geonet.repository.specification.MetadataRelationSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.MetadataRelationSpecs.hasRelatedId;
import static org.junit.Assert.assertEquals;

/**
 * Tests for the MetadataRelationSpecs
 * <p/>
 * User: Jesse
 * Date: 9/30/13
 * Time: 7:58 PM
 */
public class MetadataRelationSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRelationRepository _repo;
    @Autowired
    MetadataRepository _metadataRepo;

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata metadata1 = _metadataRepo.save(newMetadata(_inc));
        Metadata metadata2 = _metadataRepo.save(newMetadata(_inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));
        _repo.save(relation);

        assertEquals(1, _repo.count(hasMetadataId(metadata1.getId())));
        assertEquals(0, _repo.count(hasMetadataId(metadata2.getId())));
    }

    @Test
    public void testHasRelatedId() throws Exception {
        Metadata metadata1 = _metadataRepo.save(newMetadata(_inc));
        Metadata metadata2 = _metadataRepo.save(newMetadata(_inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));
        _repo.save(relation);

        assertEquals(0, _repo.count(hasRelatedId(metadata1.getId())));
        assertEquals(1, _repo.count(hasRelatedId(metadata2.getId())));
    }
}
