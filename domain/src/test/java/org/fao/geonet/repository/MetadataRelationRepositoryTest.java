package org.fao.geonet.repository;


import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.junit.Assert.assertEquals;

public class MetadataRelationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRelationRepository _repo;
    @Autowired
    MetadataRepository _metadataRepo;

    @Test
    public void testFindOne() {
        MetadataRelation relation = newMetadataRelation();
        relation = _repo.save(relation);

        MetadataRelation relation1 = newMetadataRelation();
        relation1 = _repo.save(relation1);

        assertEquals(relation1, _repo.findOne(relation1.getId()));
        assertEquals(relation, _repo.findOne(relation.getId()));
    }

    @Test
    public void testFindAllById_MetadataId() {
        MetadataRelation relation = newMetadataRelation();
        relation = _repo.save(relation);

        MetadataRelation relation1 = newMetadataRelation();
        _repo.save(relation1);

        final List<MetadataRelation> found = _repo.findAllById_MetadataId(relation.getId().getMetadataId());

        assertEquals(1, found.size());
        assertEquals(relation, found.get(0));
    }

    private MetadataRelation newMetadataRelation() {
        return newMetadataRelation(_inc, _metadataRepo);
    }
    public static MetadataRelation newMetadataRelation(AtomicInteger inc, MetadataRepository metadataRepo) {
        Metadata metadata1 = metadataRepo.save(newMetadata(inc));
        Metadata metadata2 = metadataRepo.save(newMetadata(inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));

        return relation;
    }

}
