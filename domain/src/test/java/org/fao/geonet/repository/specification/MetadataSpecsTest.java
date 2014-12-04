package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.fao.geonet.repository.specification.MetadataSpecs.*;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasExtra;

/**
 * Test for MetadataSpecs.
 * <p/>
 * User: Jesse
 * Date: 9/4/13
 * Time: 10:08 PM
 */
public class MetadataSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRepository _repository;
    @Autowired
    MetadataCategoryRepository _categoryRepo;

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        assertFindsCorrectMd(md1, hasMetadataId(md1.getId()), true);
    }

    @Test
    public void testHasMetadataType() throws Exception {
        final Metadata metadata = newMetadata(_inc);
        metadata.getDataInfo().setType(MetadataType.METADATA);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        Metadata md2 = _repository.save(metadata2);

        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getDataInfo().setType(MetadataType.TEMPLATE);
        Metadata md3 = _repository.save(metadata3);

        assertEquals(1, _repository.findAll(hasType(MetadataType.METADATA)).size());
        assertEquals(1, _repository.findAll(hasType(MetadataType.SUB_TEMPLATE)).size());
        assertEquals(1, _repository.findAll(hasType(MetadataType.TEMPLATE)).size());

        assertEquals(md1.getId(), _repository.findOne(hasType(MetadataType.METADATA)).getId());
        assertEquals(md2.getId(), _repository.findOne(hasType(MetadataType.SUB_TEMPLATE)).getId());
        assertEquals(md3.getId(), _repository.findOne(hasType(MetadataType.TEMPLATE)).getId());
    }

    @Test
    public void testHasCategory() throws Exception {
        final MetadataCategory cat1 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat2 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat3 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat4 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        final Metadata metadata = newMetadata(_inc);
        metadata.getCategories().add(cat1);
        metadata.getCategories().add(cat2);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getCategories().add(cat1);
        metadata2.getCategories().add(cat3);
        Metadata md2 = _repository.save(metadata2);

        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getCategories().add(cat2);
        Metadata md3 = _repository.save(metadata3);

        List<Metadata> found = _repository.findAll(hasCategory(cat1), SortUtils.createSort(Metadata_.id));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md2.getId(), found.get(1).getId());

        found = _repository.findAll(hasCategory(cat2), SortUtils.createSort(Metadata_.id));
        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md3.getId(), found.get(1).getId());

        found = _repository.findAll(hasCategory(cat3), SortUtils.createSort(Metadata_.id));
        assertEquals(1, found.size());
        assertEquals(md2.getId(), found.get(0).getId());

        found = _repository.findAll(hasCategory(cat4), SortUtils.createSort(Metadata_.id));
        assertEquals(0, found.size());

    }

    @Test
    public void testIsOwnedByOneOfFollowingGroups() throws Exception {


        final Metadata metadata = newMetadata(_inc);
        metadata.getSourceInfo().setGroupOwner(1);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getSourceInfo().setGroupOwner(2);
        _repository.save(metadata2);


        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getSourceInfo().setGroupOwner(3);
        Metadata md3 = _repository.save(metadata3);

        List<Metadata> found = _repository.findAll(isOwnedByOneOfFollowingGroups(Arrays.asList(1)));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());

        found = _repository.findAll(isOwnedByOneOfFollowingGroups(Arrays.asList(1, 3)), SortUtils.createSort(Metadata_.id));
        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md3.getId(), found.get(1).getId());
    }

    @Test
    public void testHasMetadataUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasMetadataUuid(md1.getUuid());

        assertFindsCorrectMd(md1, spec, true);
    }


    @Test
    public void testHasHarvesterUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasHarvesterUuid(md1.getHarvestInfo().getUuid());
        assertFindsCorrectMd(md1, spec, true);
    }

    @Test
    public void testIsHarvested() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getHarvestInfo().setHarvested(false);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getHarvestInfo().setHarvested(true);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, isHarvested(false), false);
        assertFindsCorrectMd(md2, isHarvested(true), false);
    }

    @Test
    public void testHasMetadataIdIn() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2 = _repository.save(md2);

        List<Metadata> all = _repository.findAll(hasMetadataIdIn(Arrays.asList(md1.getId())));
        assertEquals(1, all.size());
        assertEquals(md1.getId(), all.get(0).getId());

        all = _repository.findAll(hasMetadataIdIn(Arrays.asList(md1.getId(), md2.getId())));
        assertEquals(2, all.size());

        all = _repository.findAll(hasMetadataIdIn(Collections.<Integer>emptyList()));
        assertTrue(all.isEmpty());


    }

    @Test
    public void testIsTemplate() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getDataInfo().setType(MetadataType.METADATA);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, isType(MetadataType.METADATA), false);
        assertFindsCorrectMd(md2, isType(MetadataType.SUB_TEMPLATE), false);
    }

    @Test
    public void testHasSource() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = hasSource(md1.getSourceInfo().getSourceId());
        assertFindsCorrectMd(md1, spec, true);
    }

    @Test
    public void testHasExtra() throws Exception {
        final Metadata entity = newMetadata(_inc);
        final String extra = "extra data";
        entity.getDataInfo().setExtra(extra);
        Metadata md1 = _repository.save(entity);

        assertFindsCorrectMd(md1, hasExtra(extra), true);

        assertEquals(0, _repository.count(hasExtra("wrong extra")));
    }

    private void assertFindsCorrectMd(Metadata md1, Specification<Metadata> spec, boolean addNewMetadata) {
        if (addNewMetadata) {
            _repository.save(newMetadata(_inc));
        }
        List<Integer> found = _repository.findAllIdsBy(spec);
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).intValue());
    }
}
