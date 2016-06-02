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


import org.fao.geonet.domain.MapServer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class MapServerTest extends AbstractSpringDataTest {

    @Autowired
    MapServerRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    private AtomicInteger _nextId = new AtomicInteger();

    public static MapServer newMapServer(AtomicInteger nextId) {
        int id = nextId.incrementAndGet();
        return new MapServer()
            .setDescription("Desc " + id)
            .setConfigurl("http://mygeoserver.org/" + id + "/rest")
            .setName("Name " + id)
            .setUsername("admin")
            .setPassword("123456")
            .setNamespace("http://geonet.org")
            .setNamespacePrefix("gn");
    }

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, _repo.count());
        MapServer MapServer = newMapServer();
        MapServer savedMapServer = _repo.save(MapServer);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        MapServer.setId(savedMapServer.getId());
        assertEquals(1, _repo.count());
        assertSameContents(MapServer, _repo.findOneById(MapServer.getId()));

        _repo.deleteAll();

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertEquals(0, _repo.count());
    }

    @Test
    public void testUpdate() throws Exception {
        assertEquals(0, _repo.count());
        MapServer MapServer = newMapServer();

        MapServer savedMapServer = _repo.save(MapServer);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        MapServer.setId(savedMapServer.getId());

        assertEquals(1, _repo.count());
        assertSameContents(MapServer, _repo.findOneById(MapServer.getId()));

        MapServer.setName("New Name");
        MapServer savedMapServer2 = _repo.save(MapServer);

        _repo.flush();
        _entityManager.flush();
        _entityManager.clear();

        assertSameContents(savedMapServer, savedMapServer2);

        assertEquals(1, _repo.count());
        assertSameContents(MapServer, _repo.findOneById(MapServer.getId()));
    }

    private MapServer newMapServer() {
        return newMapServer(_nextId);
    }

}
