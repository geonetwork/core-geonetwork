package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataCategory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class MetadataCategoryRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataCategoryRepository _repo;

    AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testFindOne() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        category2 = _repo.save(category2);

        assertEquals(category2, _repo.findOne(category2.getId()));
        assertEquals(category1, _repo.findOne(category1.getId()));
    }

    @Test
    public void testFindOneByNameIgnoreCase() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        _repo.save(category2);

        assertEquals(category1, _repo.findOneByNameIgnoreCase(category1.getName().toLowerCase()));
        assertEquals(category1, _repo.findOneByNameIgnoreCase(category1.getName().toUpperCase()));
    }

    @Test
    public void testFindOneByName() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        category2 = _repo.save(category2);

        MetadataCategory metadataCategory = _repo.findOneByName(category1.getName());
        assertEquals(category1.getName(), metadataCategory.getName());

        metadataCategory = _repo.findOneByName(category2.getName());
        assertEquals(category2.getName(), metadataCategory.getName());
    }

    private MetadataCategory newMetadataCategory() {
        return newMetadataCategory(_inc);
    }
    public static MetadataCategory newMetadataCategory(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataCategory metadataCategory = new MetadataCategory();
        metadataCategory.setName("name"+val);
        metadataCategory.getLabelTranslations().put("eng", "engLab"+val);
        metadataCategory.getLabelTranslations().put("fra", "fraLab"+val);
        metadataCategory.getLabelTranslations().put("ger", "gerLab"+val);

        return metadataCategory;
    }

}
