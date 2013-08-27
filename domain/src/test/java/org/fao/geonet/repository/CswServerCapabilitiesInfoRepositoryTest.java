package org.fao.geonet.repository;


import org.fao.geonet.domain.CswServerCapabilitiesInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class CswServerCapabilitiesInfoRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    CswServerCapabilitiesInfoRepository _repo;

    AtomicInteger _inc = new AtomicInteger();
    @Test
    public void testFindOne() {
        CswServerCapabilitiesInfo info1 = newCswServerCapabilitiesInfo();
        info1 = _repo.save(info1);

        CswServerCapabilitiesInfo info2 = newCswServerCapabilitiesInfo();
        info2 = _repo.save(info2);

        assertEquals(info2, _repo.findOne(info2.getId()));
        assertEquals(info1, _repo.findOne(info1.getId()));
    }

    @Test
    public void testFindAllByField() {
        CswServerCapabilitiesInfo info1 = newCswServerCapabilitiesInfo();
        info1 = _repo.save(info1);

        CswServerCapabilitiesInfo info2 = newCswServerCapabilitiesInfo();
        info2 = _repo.save(info2);

        List<CswServerCapabilitiesInfo> infos = _repo.findAllByField(info1.getField());

        assertEquals(1, infos.size());
        assertEquals(info1.getField(), infos.get(0).getField());

        infos = _repo.findAllByField(info2.getField());

        assertEquals(1, infos.size());
        assertEquals(info2.getField(), infos.get(0).getField());
    }

    private CswServerCapabilitiesInfo newCswServerCapabilitiesInfo() {
        int val = _inc.incrementAndGet();
        CswServerCapabilitiesInfo CswServerCapabilitiesInfo = new CswServerCapabilitiesInfo().setField("field" + val).setLabel("lang" + val).setLangId("l_" + val);
        return CswServerCapabilitiesInfo;
    }

}
