package org.fao.geonet.repository;


import org.fao.geonet.domain.CustomElementSet;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CustomElementSetRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    CustomElementSetRepository _repo;

    @Test
    public void testFindOne() {
        CustomElementSet info1 = newCustomElementSet();
        info1 = _repo.save(info1);

        CustomElementSet info2 = newCustomElementSet();
        info2 = _repo.save(info2);

        CustomElementSet info = _repo.findOne(info1.getXpathHashcode());

        assertEquals(info1.getXpath(), info.getXpath());

        info = _repo.findOne(info2.getXpathHashcode());

        assertEquals(info2.getXpath(), info.getXpath());
    }

    private CustomElementSet newCustomElementSet() {
        return newCustomElementSet(_inc);
    }
    public static CustomElementSet newCustomElementSet(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        CustomElementSet customElementSet = new CustomElementSet().setXpath("xpath" + val);
        return customElementSet;
    }

}
