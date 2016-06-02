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
 * Test HarvestHistory specs User: Jesse Date: 9/20/13 Time: 4:01 PM
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
