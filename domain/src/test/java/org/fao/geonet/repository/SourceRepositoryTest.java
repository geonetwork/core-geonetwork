package org.fao.geonet.repository;

import org.fao.geonet.domain.Source;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the SourceRepository repository.
 * <p/>
 * User: Jesse
 * Date: 9/10/13
 * Time: 12:04 PM
 */
public class SourceRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SourceRepository _repo;

    @Test
    public void testFindOneByName() throws Exception {
        Source source1 = _repo.save(newSource());
        Source source2 = _repo.save(newSource());

        assertEquals(source1.getUuid(), _repo.findOneByName(source1.getName()).getUuid());
        assertEquals(source2.getUuid(), _repo.findOneByName(source2.getName()).getUuid());
    }

    @Test
    public void testFindOneByUuid() throws Exception {
        Source source1 = _repo.save(newSource());
        Source source2 = _repo.save(newSource());

        assertEquals(source1.getUuid(), _repo.findOne(source1.getUuid()).getUuid());
        assertEquals(source2.getUuid(), _repo.findOne(source2.getUuid()).getUuid());
    }

    public Source newSource() {
        return newSource(_inc);
    }

    public static Source newSource(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        Source source = new Source();
        source.setName("name" + val);
        source.setLocal(val % 2 == 0);
        source.setUuid("uuid" + val);

        return source;
    }
}
