package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Test class for GeonetRepository.
 *
 * User: jeichar
 * Date: 9/5/13
 * Time: 11:44 AM
 */
@Transactional
public class GeonetRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

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

}
