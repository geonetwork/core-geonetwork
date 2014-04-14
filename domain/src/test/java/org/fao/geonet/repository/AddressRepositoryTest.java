package org.fao.geonet.repository;


import org.fao.geonet.domain.Address;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class AddressRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    AddressRepository _repo;

    @Test
    public void testFindOne() {
        Address address1 = newAddress();
        address1 = _repo.save(address1);

        Address address2 = newAddress();
        address2 = _repo.save(address2);

        assertEquals(address2, _repo.findOne(address2.getId()));
        assertEquals(address1, _repo.findOne(address1.getId()));
    }

    @Test
    public void testFindAllByZip() {
        Address address1 = newAddress();
        address1 = _repo.save(address1);

        Address address2 = newAddress();
        address2 = _repo.save(address2);

        List<Address> addresses = _repo.findAllByZip(address1.getZip());

        assertEquals(1, addresses.size());
        assertEquals(address1.getZip(), addresses.get(0).getZip());

        addresses = _repo.findAllByZip(address2.getZip());

        assertEquals(1, addresses.size());
        assertEquals(address2.getZip(), addresses.get(0).getZip());
    }

    private Address newAddress() {
        return newAddress(_inc);
    }
    public static Address newAddress(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Address Address = new Address().setAddress("address" + val).setCity("city" + val).setCountry("country" + val).setState("state"
                                                                                                                               + val)
                .setZip("zip" + val);
        return Address;
    }

}
