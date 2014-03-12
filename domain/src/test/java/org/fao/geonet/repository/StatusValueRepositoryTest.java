package org.fao.geonet.repository;


import org.fao.geonet.domain.StatusValue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class StatusValueRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    StatusValueRepository _repo;

    @Test
    public void testFindOne() {
        StatusValue status = newStatusValue();
        status = _repo.save(status);

        StatusValue status1 = newStatusValue();
        status1 = _repo.save(status1);

        assertEquals(status1, _repo.findOne(status1.getId()));
        assertEquals(status, _repo.findOne(status.getId()));
    }

    @Test
    public void testFindOneByName() {
        StatusValue status = newStatusValue();
        status = _repo.save(status);

        StatusValue status1 = newStatusValue();
        status1 = _repo.save(status1);

        assertEquals(status1, _repo.findOneByName(status1.getName()));
        assertEquals(status, _repo.findOneByName(status.getName()));
    }

    private StatusValue newStatusValue() {

        return newStatusValue(_inc);
    }

    public static StatusValue newStatusValue(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        StatusValue statusValue = new StatusValue();
        statusValue.setName("name" + val);
        statusValue.setDisplayOrder(val);
        statusValue.setReserved(val % 2 == 0);

        return statusValue;
    }

}
