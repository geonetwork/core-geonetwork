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

import static org.junit.Assert.*;

import org.fao.geonet.domain.ThesaurusActivation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test the spring data repository. Created by Jesse on 12/11/13.
 */
public class ThesaurusActivationRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private ThesaurusActivationRepository _repo;

    private AtomicInteger _inc = new AtomicInteger();

    public static ThesaurusActivation newThesaurusActivation(AtomicInteger inc) {
        ThesaurusActivation act = new ThesaurusActivation();
        final int i = inc.incrementAndGet();
        act.setActivated(i % 2 == 0);
        act.setId("ThesaurusId_" + i);
        return act;
    }

    @Test
    public void testFindOne() throws Exception {
        final ThesaurusActivation activation = _repo.save(newThesaurusActivation(_inc));
        assertEquals(activation, _repo.findOne(activation.getId()));
    }
}
