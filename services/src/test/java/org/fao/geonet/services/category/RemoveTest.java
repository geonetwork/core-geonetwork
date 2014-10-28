package org.fao.geonet.services.category;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for Category Remove service
 * User: Jesse
 * Date: 11/1/13
 * Time: 7:08 PM
 */
public class RemoveTest extends AbstractServiceIntegrationTest {
    @Autowired
    MetadataRepository _MetadataRepository;
    @Autowired
    MetadataCategoryRepository _categoryRepository;
    @Autowired
    Remove remove;

    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testExec() throws Exception {
        long beforeCount = _categoryRepository.count();
        final MetadataCategory category = _categoryRepository.findAll().get(0);
        assertEquals(beforeCount, _categoryRepository.count());

        Metadata entity = MetadataRepositoryTest.newMetadata(inc);
        entity.getCategories().add(category);
        entity = _MetadataRepository.save(entity);

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        remove.exec(category.getId());

        assertEquals(beforeCount - 1, _categoryRepository.count());
        assertEquals(1, _MetadataRepository.count());
        entity = _MetadataRepository.findOne(entity.getId());
        assertTrue(entity.getCategories().isEmpty());
    }
}
