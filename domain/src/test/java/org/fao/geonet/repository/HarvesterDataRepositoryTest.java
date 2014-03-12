package org.fao.geonet.repository;

import static org.junit.Assert.*;
import org.fao.geonet.domain.HarvesterData;
import org.fao.geonet.domain.HarvesterDataId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test {@link HarvesterDataRepository}.
 *
 * Created by Jesse on 1/23/14.
 */
public class HarvesterDataRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    private HarvesterDataRepository _dataRepository;

    @Test
    public void testFindAllById_HarvesterUuid() throws Exception {
        final HarvesterData data1 = newHarvesterData(_inc);
        _dataRepository.save(data1);
        final HarvesterData data2 = newHarvesterData(_inc);
        _dataRepository.save(data2);
        final HarvesterData data3 = newHarvesterData(_inc);
        data3.getId().setHarvesterUuid(data1.getId().getHarvesterUuid());
        _dataRepository.save(data3);

        List<HarvesterData> found = _dataRepository.findAllById_HarvesterUuid(data1.getId().getHarvesterUuid());

        assertEquals(2, found.size());

        for (HarvesterData harvesterData : found) {
            assertEquals(data1.getId().getHarvesterUuid(), harvesterData.getId().getHarvesterUuid());
            if (harvesterData.getId().getKey().equals(data2.getId().getKey())) {
                fail("should not have found data2: "+data2);
            } else {
                final boolean equalsData1 = data1.equals(harvesterData);
                final boolean equalsData3 = data3.equals(harvesterData);
                assertTrue("Expected "+harvesterData+" to equals: "+data1+" or "+data3, equalsData1 || equalsData3);
            }
        }
    }

    public static HarvesterData newHarvesterData(AtomicInteger inc) {
        int id = inc.incrementAndGet();
        final HarvesterData data = new HarvesterData();
        data.setValue("value_"+id);
        data.setId(new HarvesterDataId("uuid_"+id, "key_"+id));

        return data;
    }
}
