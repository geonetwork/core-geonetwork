package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataNotifier;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataNotifierRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataNotifierRepository _repo;

    @Test
    public void testFindOne() {
        MetadataNotifier notifier1 = newMetadataNotifier();
        notifier1 = _repo.save(notifier1);

        MetadataNotifier notifier2 = newMetadataNotifier();
        notifier2 = _repo.save(notifier2);

        assertEquals(notifier2, _repo.findOne(notifier2.getId()));
        assertEquals(notifier1, _repo.findOne(notifier1.getId()));
    }

    @Test
    public void testFindAllByEnabled() {
        MetadataNotifier notifier1 = newMetadataNotifier();
        notifier1 = _repo.save(notifier1);

        MetadataNotifier notifier2 = newMetadataNotifier();
        notifier2 = _repo.save(notifier2);

        List<MetadataNotifier> metadataCategory = _repo.findAllByEnabled(notifier1.isEnabled());
        assertEquals(notifier1.getName(), metadataCategory.get(0).getName());

        metadataCategory = _repo.findAllByEnabled(notifier2.isEnabled());
        assertEquals(notifier2.getName(), metadataCategory.get(0).getName());
    }

    private MetadataNotifier newMetadataNotifier() {
        AtomicInteger inc = _inc;
        return newMetadataNotifier(inc);
    }

    public static MetadataNotifier newMetadataNotifier(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataNotifier metadataCategory = new MetadataNotifier();
        metadataCategory.setName("name" + val);
        metadataCategory.setPassword("password" + val);
        metadataCategory.setUrl("url" + val);
        metadataCategory.setUsername("username" + val);
        metadataCategory.setEnabled(val % 2 == 0);

        return metadataCategory;
    }

}
