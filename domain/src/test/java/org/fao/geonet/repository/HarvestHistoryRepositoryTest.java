package org.fao.geonet.repository;


import org.fao.geonet.domain.HarvestHistory;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@Transactional
public class HarvestHistoryRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    HarvestHistoryRepository _repo;

    AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testFindOne() {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2 = _repo.save(history2);


        assertEquals(history2, _repo.findOne(history2.getId()));
        assertEquals(history1, _repo.findOne(history1.getId()));
    }
    @Test
    public void testFindCustomFindAllAsXml() {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        Element xml = _repo.findAllAsXml();


    }

    @Test
    public void testFindByEmailCswServerCapabilitiesInfo() {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2 = _repo.save(history2);

        List<HarvestHistory> histories = _repo.findAllByHarvesterType(history1.getHarvesterType());

        assertEquals(history1.getHarvesterType(), histories.get(0).getHarvesterType());

        histories = _repo.findAllByHarvesterType(history2.getHarvesterType());

        assertEquals(history2.getHarvesterType(), histories.get(0).getHarvesterType());
    }

    @Test
    public void testMarkAllAsDeleted() {
        HarvestHistory history1 = newHarvestHistory();
        history1.setDeleted(false);
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2.setDeleted(false);
        history2 = _repo.save(history2);

        _repo.markAllAsDeleted(history1.getHarvesterUuid());

        List<HarvestHistory> found = _repo.findAll(Collections.singleton(history1.getId()));
        assertTrue(found.get(0).isDeleted());
        List<HarvestHistory> found2 = _repo.findAll(Collections.singleton(history2.getId()));
        assertFalse(found2.get(0).isDeleted());
        assertTrue(_repo.findOne(history1.getId()).isDeleted());
        assertFalse(_repo.findOne(history2.getId()).isDeleted());
    }

    private HarvestHistory newHarvestHistory() {
        int val = _inc.incrementAndGet();
        HarvestHistory customElementSet = new HarvestHistory()
                .setDeleted(val % 2 == 0)
                .setHarvesterName("name" + val)
                .setHarvesterType("type" + val)
                .setHarvesterUuid("uuid" + val);
        return customElementSet;
    }

}
