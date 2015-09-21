package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepository;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.Assert.assertEquals;

/**
 * Tests for MetadataIdentifierTemplateSpecs.
 *
 * @author Jose García
 */
public class MetadataIdentifierTemplateSpecsTest  extends AbstractSpringDataTest {


    @Autowired
    private MetadataIdentifierTemplateRepository mdIdentifierTemplateRepository;

    @Test
    public void testIsSystemProvided() throws Exception {
        MetadataIdentifierTemplate template1 = MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc);
        template1.setSystemDefault(true);
        template1 = mdIdentifierTemplateRepository.save(template1);

        MetadataIdentifierTemplate template2 = mdIdentifierTemplateRepository.save(MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc));
        MetadataIdentifierTemplate template3 = mdIdentifierTemplateRepository.save(MetadataIdentifierTemplateRepositoryTest.newMetadataUrnTemplate(_inc));

        final Specification<MetadataIdentifierTemplate> specificationDefault = MetadataIdentifierTemplateSpecs.isSystemProvided(true);

        long numDefaultTemplates = mdIdentifierTemplateRepository.count(specificationDefault);
        assertEquals(1, numDefaultTemplates);

        final Specification<MetadataIdentifierTemplate> specificationNoDefault = MetadataIdentifierTemplateSpecs.isSystemProvided(false);

        long numNoDefaultTemplates = mdIdentifierTemplateRepository.count(specificationNoDefault);
        assertEquals(2, numNoDefaultTemplates);

    }
}