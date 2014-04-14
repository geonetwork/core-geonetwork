package org.fao.geonet.repository;

import static org.junit.Assert.*;
import org.fao.geonet.domain.ThesaurusActivation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test the spring data repository.
 * Created by Jesse on 12/11/13.
 */
public class ThesaurusActivationRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private ThesaurusActivationRepository _repo;

    private AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testFindOne() throws Exception {
        final ThesaurusActivation activation = _repo.save(newThesaurusActivation(_inc));
        assertEquals(activation, _repo.findOne(activation.getId()));
    }

    public static ThesaurusActivation newThesaurusActivation(AtomicInteger inc) {
        ThesaurusActivation act = new ThesaurusActivation();
        final int i = inc.incrementAndGet();
        act.setActivated(i % 2 == 0);
        act.setId("ThesaurusId_"+i);
        return act;
    }
}
