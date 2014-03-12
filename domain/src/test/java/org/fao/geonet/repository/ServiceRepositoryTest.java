package org.fao.geonet.repository;


import org.fao.geonet.domain.Service;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ServiceRepositoryTest extends AbstractSpringDataTest {

    @PersistenceContext
    EntityManager _EntityManager;

    @Autowired
    ServiceRepository _repo;

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
    public static Service newService(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        Service service = new Service();

        service.setId(val);
        service.setName("name" + val);
        service.setClassName("classname" + val);
        service.setDescription("description" + val);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name_1_" + val, "value_1_" + val);
        params.put("name_2_" + val, "value_2_" + val);
        service.setParameters(params);

        return service;
    }

}
