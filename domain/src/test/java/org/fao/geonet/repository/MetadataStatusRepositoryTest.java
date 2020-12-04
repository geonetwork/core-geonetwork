/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.repository;


import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.StatusValue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

public class MetadataStatusRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    StatusValueRepository _statusRepo;
    @Autowired
    MetadataStatusRepository _repo;

    public static MetadataStatus newMetadataStatus(AtomicInteger inc, StatusValueRepository statusRepo) {
        int val = inc.incrementAndGet();

        MetadataStatus metadataStatus = new MetadataStatus();

        metadataStatus.setMetadataId(inc.incrementAndGet());
        metadataStatus.setChangeDate(new ISODate());
        metadataStatus.setUserId(inc.incrementAndGet());
        metadataStatus.setChangeMessage("change message " + val);
        metadataStatus.setOwner(1);
        metadataStatus.setUuid(UUID.randomUUID().toString());
        metadataStatus.setTitles(new LinkedHashMap<String, String>(){{
            put("eng", "SampleTitle");
        }});
        final StatusValue statusValue = statusRepo.save(StatusValueRepositoryTest.newStatusValue(inc));
        metadataStatus.setStatusValue(statusValue);

        return metadataStatus;
    }

    @Test
    public void testFindOne() {
        MetadataStatus status = newMetadataStatus();
        status = _repo.save(status);

        MetadataStatus status1 = newMetadataStatus();
        status1 = _repo.save(status1);

        assertEquals(status1, _repo.findById(status1.getId()).get());
        assertEquals(status, _repo.findById(status.getId()).get());
    }

    @Test
    public void testDeleteAllByMetadataId() throws Exception {
        MetadataStatus status1 = _repo.save(newMetadataStatus());
        MetadataStatus status2 = newMetadataStatus();
        status2.setMetadataId(status1.getMetadataId());
        status2 = _repo.save(status2);
        MetadataStatus status3 = _repo.save(newMetadataStatus());

        assertEquals(3, _repo.count());
        _repo.deleteAllById_MetadataId(status1.getMetadataId());
        assertEquals(1, _repo.count());
        assertFalse(_repo.findById(status1.getId()).isPresent());
        assertFalse(_repo.findById(status2.getId()).isPresent());

        final List<MetadataStatus> all = _repo.findAll();
        assertEquals(1, all.size());
        assertEquals(status3.getId(), all.get(0).getId());
        assertNotNull(_repo.findById(status3.getId()).get());
    }

    @Test
    public void testFindAllByMetadataId() {
        MetadataStatus status = newMetadataStatus();
        status = _repo.save(status);
        MetadataStatus status2 = newMetadataStatus();
        status2.setMetadataId(status.getMetadataId());
        status2 = _repo.save(status2);

        MetadataStatus status1 = newMetadataStatus();
        status1 = _repo.save(status1);


        final Sort sort = SortUtils.createSort(MetadataStatus_.metadataId);
        assertEquals(2, _repo.findAllByMetadataId(status.getMetadataId(), sort).size());
        assertEquals(1, _repo.findAllByMetadataId(status1.getMetadataId(), sort).size());
    }

    private MetadataStatus newMetadataStatus() {

        return newMetadataStatus(_inc, _statusRepo);
    }

}
