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


import org.fao.geonet.domain.Service;
import org.fao.geonet.domain.ServiceParam;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;

public class ServiceRepositoryTest extends AbstractSpringDataTest {

    @PersistenceContext
    EntityManager _EntityManager;

    @Autowired
    ServiceRepository _repo;

    public static Service newService(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        Service service = new Service();

        service.setId(val);
        service.setName("name" + val);
        service.setClassName("classname" + val);
        service.setDescription("description" + val);
        service.setExplicitQuery("+explicitQuery:" + val);

        service.addParameter(new ServiceParam("name_1_" + val, "value_1_" + val)).
            addParameter(new ServiceParam("name_2_" + val, "value_2_" + val));

        return service;
    }

    @Test
    public void testFindOne() throws Exception {
        Service service = newService();
        service = _repo.save(service);

        Service service1 = newService();
        service1 = _repo.save(service1);

        _EntityManager.flush();
        _EntityManager.clear();

        final Service found1 = _repo.findOne(service1.getId());
        assertEquals(2, found1.getParameters().size());
        assertSameContents(service1, found1);
        final Service found = _repo.findOne(service.getId());
        assertEquals(2, found.getParameters().size());
        assertSameContents(service, found);
    }

    @Test
    public void testFindOneByName() {
        Service service = newService();
        service = _repo.save(service);

        Service service1 = newService();
        service1 = _repo.save(service1);

        assertEquals(service1, _repo.findOneByName(service1.getName()));
        assertEquals(service, _repo.findOneByName(service.getName()));
    }

    private Service newService() {
        return newService(_inc);
    }

}
