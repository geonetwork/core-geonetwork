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

import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for MetadataIdentifierTemplateRepository.
 *
 * @author Jose García
 */

public class MetadataIdentifierTemplateRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataIdentifierTemplateRepository _repo;

    @PersistenceContext
    EntityManager _entityManager;

    public static MetadataIdentifierTemplate newMetadataUrnTemplate(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataIdentifierTemplate mdUrnTemplate = new MetadataIdentifierTemplate();
        mdUrnTemplate.setName("name" + val);
        mdUrnTemplate.setTemplate("xxxx-{AA}-" + val);

        return mdUrnTemplate;
    }

    @Test
    public void testFindOne() {
        MetadataIdentifierTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataIdentifierTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        assertEquals(mdUrnTemplate2, _repo.findOne(mdUrnTemplate2.getId()));
        assertEquals(mdUrnTemplate1, _repo.findOne(mdUrnTemplate1.getId()));
    }

    @Test
    public void testFindOneByName() {
        MetadataIdentifierTemplate mdUrnTemplate1 = newMetadataUrnTemplate();
        mdUrnTemplate1 = _repo.save(mdUrnTemplate1);

        MetadataIdentifierTemplate mdUrnTemplate2 = newMetadataUrnTemplate();
        mdUrnTemplate2 = _repo.save(mdUrnTemplate2);

        MetadataIdentifierTemplate metadataIdentifierTemplate = _repo.findOneByName(mdUrnTemplate1.getName());
        assertEquals(mdUrnTemplate1.getName(), metadataIdentifierTemplate.getName());

        metadataIdentifierTemplate = _repo.findOneByName(mdUrnTemplate2.getName());
        assertEquals(mdUrnTemplate2.getName(), metadataIdentifierTemplate.getName());
    }

    private MetadataIdentifierTemplate newMetadataUrnTemplate() {
        return newMetadataUrnTemplate(_inc);
    }

}
