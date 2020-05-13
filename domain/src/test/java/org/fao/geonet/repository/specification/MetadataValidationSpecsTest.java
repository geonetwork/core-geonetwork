package org.fao.geonet.repository.specification;


import static org.fao.geonet.repository.specification.MetadataValidationSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.MetadataValidationSpecs.isInvalidAndRequiredForMetadata;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.MetadataValidationRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test class for {@link org.fao.geonet.repository.specification.MetadataValidationSpecs}.
 *
 * @author Jose García
 */
public class MetadataValidationSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private MetadataRepository _metadataRepo;

    @Autowired
    private MetadataValidationRepository _metadataValidationRepo;

    @Test
    public void testHasMetadataId() throws Exception {
        MetadataValidation md1 =  _metadataValidationRepo.save(newMetadataValidation());
        md1.setValid(true);
        _metadataValidationRepo.save(md1);

        final List<MetadataValidation> found = _metadataValidationRepo.findAll(hasMetadataId(md1.getId().getMetadataId()));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
    }

    @Test
    public void testIsInvalidAndRequiredForMetadata() throws Exception {
        MetadataValidation md1 =  _metadataValidationRepo.save(newMetadataValidation());
        md1.setValid(true);
        _metadataValidationRepo.save(md1);

        MetadataValidation md2 =  _metadataValidationRepo.save(newMetadataValidation());
        md2.setValid(false);
        _metadataValidationRepo.save(md2);

        List<MetadataValidation> found = _metadataValidationRepo.findAll(isInvalidAndRequiredForMetadata(md1.getId().getMetadataId()));
        assertEquals(0, found.size());

        found = _metadataValidationRepo.findAll(isInvalidAndRequiredForMetadata(md2.getId().getMetadataId()));
        assertEquals(1, found.size());
    }

    private MetadataValidation newMetadataValidation() {
        return MetadataValidationRepositoryTest.newValidation(_inc, _metadataRepo);
    }
}
