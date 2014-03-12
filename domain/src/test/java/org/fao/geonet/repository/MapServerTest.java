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

}
