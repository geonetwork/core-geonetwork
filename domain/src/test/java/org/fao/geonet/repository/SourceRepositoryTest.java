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

import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for the SourceRepository repository.
 * <p/>
 * User: Jesse Date: 9/10/13 Time: 12:04 PM
 */
public class SourceRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SourceRepository _repo;

    public static Source newSource(AtomicInteger inc) {
        int val = inc.incrementAndGet();

        Source source = new Source();
        source.setName("name" + val);
        source.setType(SourceType.harvester);
        source.setUuid("uuid" + val);

        source.getLabelTranslations().put("eng", "enlabel" + val);
        source.getLabelTranslations().put("fre", "frlabel" + val);

        return source;
    }

    @Test
    public void testFindOneByName() throws Exception {
        Source source1 = _repo.save(newSource());
        final Source source2BeforeSave = newSource();
        Source source2 = _repo.save(source2BeforeSave);

        assertEquals(source1.getUuid(), _repo.findOneByName(source1.getName()).getUuid());
        assertEquals(source2BeforeSave.getUuid(), _repo.findOneByName(source2.getName()).getUuid());
        assertEquals(source2BeforeSave.getLabel("eng"), _repo.findOneByName(source2.getName()).getLabel("eng"));
        assertNotNull(_repo.findOneByName(source2.getName()).getLabel("eng"));
        assertEquals(source2BeforeSave.getLabel("fre"), _repo.findOneByName(source2.getName()).getLabel("fre"));
    }

    @Test
    public void testFindOneByUuid() throws Exception {
        Source source1 = _repo.save(newSource());
        Source source2 = _repo.save(newSource());

        assertEquals(source1.getUuid(), _repo.findOne(source1.getUuid()).getUuid());
        assertEquals(source2.getUuid(), _repo.findOne(source2.getUuid()).getUuid());
    }

    public Source newSource() {
        return newSource(_inc);
    }
}
