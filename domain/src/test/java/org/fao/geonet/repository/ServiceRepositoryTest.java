package org.fao.geonet.repository;


import org.fao.geonet.domain.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class ServiceRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    ServiceRepository _repo;

    AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testFindOne() {
        Service service = newService();
        service = _repo.save(service);

        Service service1 = newService();
        service1 = _repo.save(service1);

        assertEquals(service1, _repo.findOne(service1.getId()));
        assertEquals(service, _repo.findOne(service.getId()));
    }

    private Service newService() {
        int val = _inc.incrementAndGet();

        Service service = new Service();

        service.setId(val);
        service.setName("name" + val);
        service.setClassName("classname" + val);
        service.setDescription("description" + val);

        ArrayList<ServiceParameter> params = new ArrayList<ServiceParameter>();
        params.add(new ServiceParameter().setId(val).setName("name_1_" + val).setValue("value_1_" + val));
        params.add(new ServiceParameter().setId(val).setName("name_2_" + val).setValue("value_2_" + val));
        service.setParameters(params);

        return service;
    }

}
