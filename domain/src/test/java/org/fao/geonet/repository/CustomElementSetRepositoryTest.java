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


import org.fao.geonet.domain.CustomElementSet;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CustomElementSetRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    CustomElementSetRepository _repo;

    public static CustomElementSet newCustomElementSet(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        CustomElementSet customElementSet = new CustomElementSet().setXpath("xpath" + val);
        return customElementSet;
    }

    @Test
    public void testFindOne() {
        CustomElementSet info1 = newCustomElementSet();
        info1 = _repo.save(info1);

        CustomElementSet info2 = newCustomElementSet();
        info2 = _repo.save(info2);

        CustomElementSet info = _repo.findOne(info1.getXpathHashcode());

        assertEquals(info1.getXpath(), info.getXpath());

        info = _repo.findOne(info2.getXpathHashcode());

        assertEquals(info2.getXpath(), info.getXpath());
    }

    private CustomElementSet newCustomElementSet() {
        return newCustomElementSet(_inc);
    }

}
