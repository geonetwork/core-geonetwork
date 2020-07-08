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


import org.fao.geonet.domain.Translations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TranslationsRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    TranslationsRepository _repo;

    public static Translations newTranslations(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Translations Translations = new Translations().setFieldName("field" + val).setValue("lang"
            + val)
            .setLangId("l_" + val);
        return Translations;
    }

    @Before
    public void setUp() throws Exception {
        _inc.set(0);

    }

    @Test
    public void testFindOne() {
        Translations info1 = newTranslations();
        info1 = _repo.save(info1);

        Translations info2 = newTranslations();
        info2 = _repo.save(info2);

        assertEquals(info2, _repo.findById(info2.getId()).get());
        assertEquals(info1, _repo.findById(info1.getId()).get());
    }

    @Test
    public void testFindAllByField() {
        Translations info1 = newTranslations();
        info1 = _repo.save(info1);

        Translations info2 = newTranslations();
        info2 = _repo.save(info2);

        List<Translations> infos = _repo.findAllByFieldName(info1.getFieldName());

        assertEquals(1, infos.size());
        assertEquals(info1.getFieldName(), infos.get(0).getFieldName());

        infos = _repo.findAllByFieldName(info2.getFieldName());

        assertEquals(1, infos.size());
        assertEquals(info2.getFieldName(), infos.get(0).getFieldName());
    }

    @Test
    public void testFindAllByLangId() {
        Translations info1 = newTranslations();
        info1 = _repo.save(info1);

        Translations info2 = newTranslations();
        info2 = _repo.save(info2);

        List<Translations> infos = _repo.findAllByLangId(info1.getLangId());

        assertEquals(1, infos.size());
        assertEquals(info1.getLangId(), infos.get(0).getLangId());

        infos = _repo.findAllByLangId(info2.getLangId());

        assertEquals(1, infos.size());
        assertEquals(info2.getLangId(), infos.get(0).getLangId());
    }

    private Translations newTranslations() {
        return newTranslations(_inc);
    }

}
