package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataUrnTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataUrnTemplateRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataUrnTemplateRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    @Test
    public void testFindOne() {
        MetadataUrnTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataUrnTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        assertEquals(mdUrnTemplate2, _repo.findOne(mdUrnTemplate2.getId()));
        assertEquals(mdUrnTemplate1, _repo.findOne(mdUrnTemplate1.getId()));
    }

    @Test
    public void testFindOneByName() {
        MetadataUrnTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataUrnTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        MetadataUrnTemplate metadataUrnTemplate = _repo.findOneByName(mdUrnTemplate1.getName());
        assertEquals(mdUrnTemplate1.getName(), metadataUrnTemplate.getName());

        metadataUrnTemplate = _repo.findOneByName(mdUrnTemplate2.getName());
        assertEquals(mdUrnTemplate2.getName(), metadataUrnTemplate.getName());
    }

    private MetadataUrnTemplate newMetadataUrnTemplate() {
        return newMetadataUrnTemplate(_inc);
    }

    public static MetadataUrnTemplate newMetadataUrnTemplate(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataUrnTemplate mdUrnTemplate = new MetadataUrnTemplate();
        mdUrnTemplate.setName("name" + val);
        mdUrnTemplate.setTemplate("xxxx-{AA}-" + val);

        return mdUrnTemplate;
    }

}
