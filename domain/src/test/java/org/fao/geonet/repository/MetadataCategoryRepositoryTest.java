package org.fao.geonet.repository;


import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataCategoryRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataCategoryRepository _repo;

    @Autowired
    MetadataRepository _metadataRepo;

    @PersistenceContext
    EntityManager _entityManager;

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


    @Test
    public void testDeleteDeletesFromMetadata() throws Exception {

        MetadataCategory cat1 = _repo.save(newMetadataCategory(_inc));
        MetadataCategory cat2 = _repo.save(newMetadataCategory(_inc));

        Metadata metadata1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1.getCategories().add(cat1);
        metadata1.getCategories().add(cat2);
        metadata1 = _metadataRepo.save(metadata1);

        Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2.getCategories().add(cat1);
        metadata2 = _metadataRepo.save(metadata2);

        _repo.deleteCategoryAndMetadataReferences(cat1.getId());

        _entityManager.flush();
        _entityManager.clear();

        // org.fao.geonet.services.category.Remove assumes that this test passes.  If this test can't pass
        // then there needs to be a way to fix Remove as well.
        final Set<MetadataCategory> foundCategories = _metadataRepo.findOne(metadata1.getId()).getCategories();
        assertEquals(1, foundCategories.size());
        assertEquals(cat2.getId(), foundCategories.iterator().next().getId());

        assertEquals(0, _metadataRepo.findOne(metadata2.getId()).getCategories().size());
    }

    private MetadataCategory newMetadataCategory() {
        return newMetadataCategory(_inc);
    }

    public static MetadataCategory newMetadataCategory(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataCategory metadataCategory = new MetadataCategory();
        metadataCategory.setName("name" + val);
        metadataCategory.getLabelTranslations().put("eng", "engLab" + val);
        metadataCategory.getLabelTranslations().put("fre", "fraLab" + val);
        metadataCategory.getLabelTranslations().put("ger", "gerLab" + val);

        return metadataCategory;
    }

}
