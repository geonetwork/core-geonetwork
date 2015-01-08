package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test the MetadataValidationRepository class
 * User: Jesse
 * Date: 9/4/13
 * Time: 4:01 PM
 */
public class MetadataValidationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    private MetadataValidationRepository _metadataValidationRepository;
    @Autowired
    private MetadataRepository _metadataRepository;

    @Test
    public void testFindById_MetadataId() throws Exception {
        MetadataValidation val1 = _metadataValidationRepository.save(newValidation());
        MetadataValidation val2 = _metadataValidationRepository.save(newValidation());
        MetadataValidation val3 = _metadataValidationRepository.save(newValidation());

        List<MetadataValidation> found = _metadataValidationRepository.findAllById_MetadataId(val1.getId().getMetadataId());
        assertEquals(1, found.size());
        assertEquals(val1.getId(), found.get(0).getId());

        found = _metadataValidationRepository.findAllById_MetadataId(val2.getId().getMetadataId());
        assertEquals(1, found.size());
        assertEquals(val2.getId(), found.get(0).getId());

        found = _metadataValidationRepository.findAllById_MetadataId(val3.getId().getMetadataId());
        assertEquals(1, found.size());
        assertEquals(val3.getId(), found.get(0).getId());
    }

    @Test
    public void testDeleteAllById_MetadataId() throws Exception {
        MetadataValidation val1 = _metadataValidationRepository.save(newValidation());
        MetadataValidation val2 = newValidation();
        val2.getId().setMetadataId(val1.getId().getMetadataId());
        val2 = _metadataValidationRepository.save(val2);
        MetadataValidation val3 = _metadataValidationRepository.save(newValidation());

        assertEquals(3, _metadataValidationRepository.count());
        _metadataValidationRepository.deleteAllById_MetadataId(val1.getId().getMetadataId());
        assertEquals(1, _metadataValidationRepository.count());
        final List<MetadataValidation> all = _metadataValidationRepository.findAll();
        assertEquals(1, all.size());
        assertEquals(val3.getId(), all.get(0).getId());
        assertNull(_metadataValidationRepository.findOne(val1.getId()));
    }

    private MetadataValidation newValidation() {
        return newValidation(_inc, _metadataRepository);
    }

    public static MetadataValidation newValidation(AtomicInteger inc, MetadataRepository metadataRepository) {
        int val = inc.incrementAndGet();

        Metadata metadata = metadataRepository.save(MetadataRepositoryTest.newMetadata(inc));

        MetadataValidation validation = new MetadataValidation();
        MetadataValidationId id = new MetadataValidationId();
        id.setMetadataId(metadata.getId());
        id.setValidationType("valType" + val);
        validation.setId(id);
        validation.setStatus(val % 2 == 0 ? MetadataValidationStatus.INVALID : MetadataValidationStatus.VALID);
        validation.setValidationDate(new ISODate());
        validation.setValid(val % 2 == 1);

        return validation;
    }
}
