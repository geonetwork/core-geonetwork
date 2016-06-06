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


import org.fao.geonet.domain.Address;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class AddressRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    AddressRepository _repo;

    public static Address newAddress(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Address Address = new Address().setAddress("address" + val).setCity("city" + val).setCountry("country" + val).setState("state"
            + val)
            .setZip("zip" + val);
        return Address;
    }

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

}
