package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.HarvestHistoryRepository;
import org.fao.geonet.repository.HarvestHistoryRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.specification.HarvestHistorySpecs.hasHarvesterUuid;
import static org.junit.Assert.assertEquals;

/**
 * Test HarvestHistory specs
 * User: Jesse
 * Date: 9/20/13
 * Time: 4:01 PM
 */
public class HarvestHistorySpecsTest extends AbstractSpringDataTest {

    @Autowired
    HarvestHistoryRepository _repo;

    @Test
    public void testHasHarvesterUuid() throws Exception {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        _repo.save(history2);


        final List<HarvestHistory> found = _repo.findAll(hasHarvesterUuid(history1.getHarvesterUuid()));
        assertEquals(1, found.size());
        assertEquals(history1.getId(), found.get(0).getId());
    }

    private HarvestHistory newHarvestHistory() {
        return HarvestHistoryRepositoryTest.createHarvestHistory(_inc);
    }
}
