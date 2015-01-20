package org.fao.geonet.repository;


import org.fao.geonet.domain.CswCapabilitiesInfoField;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CswCapabilitiesInfoFieldRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    CswCapabilitiesInfoFieldRepository _repo;

    @Before
    public void setUp() throws Exception {
        _inc.set(0);

    }

    @Test
    public void testFindOne() {
        CswCapabilitiesInfoField info1 = newCswServerCapabilitiesInfo();
        info1 = _repo.save(info1);

        CswCapabilitiesInfoField info2 = newCswServerCapabilitiesInfo();
        info2 = _repo.save(info2);

        assertEquals(info2, _repo.findOne(info2.getId()));
        assertEquals(info1, _repo.findOne(info1.getId()));
    }

    @Test
    public void testFindAllByField() {
        CswCapabilitiesInfoField info1 = newCswServerCapabilitiesInfo();
        info1 = _repo.save(info1);

        CswCapabilitiesInfoField info2 = newCswServerCapabilitiesInfo();
        info2 = _repo.save(info2);

        List<CswCapabilitiesInfoField> infos = _repo.findAllByFieldName(info1.getFieldName());

        assertEquals(1, infos.size());
        assertEquals(info1.getFieldName(), infos.get(0).getFieldName());

        infos = _repo.findAllByFieldName(info2.getFieldName());

        assertEquals(1, infos.size());
        assertEquals(info2.getFieldName(), infos.get(0).getFieldName());
    }

    @Test
    public void testFindAllByLangId() {
        CswCapabilitiesInfoField info1 = newCswServerCapabilitiesInfo();
        info1 = _repo.save(info1);

        CswCapabilitiesInfoField info2 = newCswServerCapabilitiesInfo();
        info2 = _repo.save(info2);

        List<CswCapabilitiesInfoField> infos = _repo.findAllByLangId(info1.getLangId());

        assertEquals(1, infos.size());
        assertEquals(info1.getLangId(), infos.get(0).getLangId());

        infos = _repo.findAllByLangId(info2.getLangId());

        assertEquals(1, infos.size());
        assertEquals(info2.getLangId(), infos.get(0).getLangId());
    }

    @Test
    public void testFindAndSaveCswServerCapabilitiesInfo() {
        CswCapabilitiesInfo info = _repo.findCswCapabilitiesInfo("eng");

        assertNotNull(info);

        assertNull(info.getTitle());
        assertNull(info.getAbstract());
        assertNull(info.getAccessConstraints());
        assertNull(info.getFees());
        assertEquals("eng", info.getLangId());

        info.setAbstract("abstract");
        info.setAccessConstraints("accessConstraints");
        info.setFees("fees");
        info.setTitle("title");

        _repo.save(info);

        CswCapabilitiesInfo info2 = _repo.findCswCapabilitiesInfo("fre");
        info2.setAbstract("abstract2");
        info2.setAccessConstraints("accessConstraints2");
        info2.setFees("fees2");
        info2.setTitle("title2");

        _repo.save(info2);

        final CswCapabilitiesInfo engFound = _repo.findCswCapabilitiesInfo("eng");

        assertEquals(info.getLangId(), engFound.getLangId());
        assertEquals(info.getAbstract(), engFound.getAbstract());

        final CswCapabilitiesInfo freFound = _repo.findCswCapabilitiesInfo("fre");

        assertEquals(info2.getLangId(), freFound.getLangId());
        assertEquals(info2.getAbstract(), freFound.getAbstract());

    }

    private CswCapabilitiesInfoField newCswServerCapabilitiesInfo() {
        return newCswServerCapabilitiesInfo(_inc);
    }
    public static CswCapabilitiesInfoField newCswServerCapabilitiesInfo(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        CswCapabilitiesInfoField CswCapabilitiesInfoField = new CswCapabilitiesInfoField().setFieldName("field" + val).setValue("lang"
                                                                                                                                + val)
                .setLangId("l_" + val);
        return CswCapabilitiesInfoField;
    }

}
