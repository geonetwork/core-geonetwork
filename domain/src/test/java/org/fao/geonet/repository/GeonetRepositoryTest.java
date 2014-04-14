package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.repository.statistic.PathSpec;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Test class for GeonetRepository.
 * <p/>
 * User: jeichar
 * Date: 9/5/13
 * Time: 11:44 AM
 */
public class GeonetRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository _repo;

    @Test(expected = JpaObjectRetrievalFailureException.class)
    public void testUpdateMetadataBadId() throws Exception {
        _repo.update(123123325, new Updater<Metadata>() {
            @Nullable
            @Override
            public void apply(@Nullable Metadata input) {
                // do nothing
            }
        });
    }

    @Test
    public void testUpdateMetadataReturnSameMd() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));

        final String updatedUUID1 = "Updated uuid";
        md = _repo.update(md.getId(), new Updater<Metadata>() {
            @Nullable
            @Override
            public void apply(@Nullable Metadata input) {
                input.setUuid(updatedUUID1);
            }
        });

        assertEquals(updatedUUID1, md.getUuid());
        assertEquals(updatedUUID1, _repo.findOne(md.getId()).getUuid());
    }

    @Test
    public void testBatchUpdateAttributes() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));


        final Specifications<Metadata> spec = where(hasMetadataId(md2.getId())).or(hasMetadataId(md3.getId()));
        PathSpec<Metadata, String> dataPathSpec = new PathSpec<Metadata, String>() {
            @Override
            public Path<String> getPath(Root<Metadata> root) {
                return root.get(Metadata_.data);
            }
        };
        String newData = "Updated DataElem";

        PathSpec<Metadata, Integer> sourcePathSpec = new PathSpec<Metadata, Integer>() {
            @Override
            public Path<Integer> getPath(Root<Metadata> root) {
                return root.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);
            }
        };
        Integer newGroupId = 12345678;

        final BatchUpdateQuery<Metadata> updateQuery = _repo.createBatchUpdateQuery(dataPathSpec, newData);
        updateQuery.add(sourcePathSpec, newGroupId);

        updateQuery.setSpecification(spec);

        int updated = updateQuery.execute();

        assertEquals(2, updated);
        Metadata reloadedMd1 = _repo.findOne(md.getId());
        assertEquals(md.getData(), reloadedMd1.getData());
        assertFalse(newGroupId == reloadedMd1.getSourceInfo().getGroupOwner());

        Metadata reloadedMd2 = _repo.findOne(md2.getId());
        assertEquals(newData, reloadedMd2.getData());
        assertEquals(newGroupId, reloadedMd2.getSourceInfo().getGroupOwner());

        Metadata reloadedMd3 = _repo.findOne(md3.getId());
        assertEquals(newData, reloadedMd3.getData());
        assertEquals(newGroupId, reloadedMd3.getSourceInfo().getGroupOwner());
    }

    @Test
    public void testDeleteAllSpec() throws Exception {
        Metadata md = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md2 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));
        Metadata md3 = _repo.save(MetadataRepositoryTest.newMetadata(_inc));


        final Specifications<Metadata> spec = where(hasMetadataId(md2.getId())).or(hasMetadataId(md3.getId()));

        final int deleted = _repo.deleteAll(spec);

        assertEquals(2, deleted);
        assertEquals(1, _repo.count());

        assertNotNull(_repo.findOne(md.getId()));
        assertNull(_repo.findOne(md2.getId()));
        assertNull(_repo.findOne(md3.getId()));

        assertEquals(md.getId(), _repo.findAll().get(0).getId());
    }
}
