package org.fao.geonet.repository;


import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class StatusValueRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    StatusValueRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testFindOne() {
        StatusValue status = newStatusValue();
        status = _repo.save(status);

        StatusValue status1 = newStatusValue();
        status1 = _repo.save(status1);

        assertEquals(status1, _repo.findOne(status1.getId()));
        assertEquals(status, _repo.findOne(status.getId()));
    }
    private StatusValue newStatusValue() {

        return newStatusValue(_inc);
    }

    public static StatusValue newStatusValue(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        StatusValue statusValue = new StatusValue();
        statusValue.setName("name"+val);
        statusValue.setDisplayOrder(val);
        statusValue.setReserved(val % 2 == 0);

        return statusValue;
    }

}
