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


import org.fao.geonet.domain.Language;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class LanguageRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    LanguageRepository _repo;

    public static Language newLanguage(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        Language lang = new Language();
        lang.setId("l-" + val);
        lang.setInspire(val % 2 == 1);
        lang.setName("name" + val);
        return lang;
    }

    @Test
    public void testFindOne() {
        Language language = newLanguage();
        language = _repo.save(language);

        Language language1 = newLanguage();
        language1 = _repo.save(language1);


        assertEquals(language1, _repo.findById(language1.getId()).get());
        assertEquals(language, _repo.findById(language.getId()).get());
    }

    @Test
    public void testFindByInspireFlag() {
        Language language1 = newLanguage();
        language1 = _repo.save(language1);

        Language language2 = newLanguage();
        language2 = _repo.save(language2);

        List<Language> histories = _repo.findAllByInspireFlag(language1.isInspire());
        assertEquals(language1.isInspire(), histories.get(0).isInspire());

        histories = _repo.findAllByInspireFlag(language2.isInspire());
        assertEquals(language2.isInspire(), histories.get(0).isInspire());
    }

    private Language newLanguage() {
        return newLanguage(_inc);
    }

}
