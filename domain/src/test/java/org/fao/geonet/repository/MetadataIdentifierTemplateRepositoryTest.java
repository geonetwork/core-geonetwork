package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for MetadataIdentifierTemplateRepository.
 *
 * @author Jose García
 */

public class MetadataIdentifierTemplateRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataIdentifierTemplateRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    @Test
    public void testFindOne() {
        MetadataIdentifierTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataIdentifierTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        assertEquals(mdUrnTemplate2, _repo.findOne(mdUrnTemplate2.getId()));
        assertEquals(mdUrnTemplate1, _repo.findOne(mdUrnTemplate1.getId()));
    }

    @Test
    public void testFindOneByName() {
        MetadataIdentifierTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataIdentifierTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        MetadataIdentifierTemplate metadataIdentifierTemplate = _repo.findOneByName(mdUrnTemplate1.getName());
        assertEquals(mdUrnTemplate1.getName(), metadataIdentifierTemplate.getName());

        metadataIdentifierTemplate = _repo.findOneByName(mdUrnTemplate2.getName());
        assertEquals(mdUrnTemplate2.getName(), metadataIdentifierTemplate.getName());
    }

    private MetadataIdentifierTemplate newMetadataUrnTemplate() {
        return newMetadataUrnTemplate(_inc);
    }

    public static MetadataIdentifierTemplate newMetadataUrnTemplate(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataIdentifierTemplate mdUrnTemplate = new MetadataIdentifierTemplate();
        mdUrnTemplate.setName("name" + val);
        mdUrnTemplate.setTemplate("xxxx-{AA}-" + val);

        return mdUrnTemplate;
    }

}
