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


import org.fao.geonet.domain.StatusValue;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class StatusValueRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    StatusValueRepository _repo;

    public static StatusValue newStatusValue(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        StatusValue statusValue = new StatusValue();
        statusValue.setName("name" + val);
        statusValue.setDisplayOrder(val);
        statusValue.setReserved(val % 2 == 0);

        return statusValue;
    }

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

}
