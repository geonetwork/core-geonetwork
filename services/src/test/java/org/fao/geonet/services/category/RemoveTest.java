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

package org.fao.geonet.services.category;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for Category Remove service User: Jesse Date: 11/1/13 Time: 7:08 PM
 */
public class RemoveTest extends AbstractServiceIntegrationTest {
    @Autowired
    MetadataRepository _MetadataRepository;
    @Autowired
    MetadataCategoryRepository _categoryRepository;
    @Autowired
    Remove remove;

    AtomicInteger inc = new AtomicInteger();

    @Test
    public void testExec() throws Exception {
        long beforeCount = _categoryRepository.count();
        final MetadataCategory category = _categoryRepository.findAll().get(0);
        assertEquals(beforeCount, _categoryRepository.count());

        Metadata entity = MetadataRepositoryTest.newMetadata(inc);
        entity.getMetadataCategories().add(category);
        entity = _MetadataRepository.save(entity);

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        remove.exec(category.getId());

        assertEquals(beforeCount - 1, _categoryRepository.count());
        assertEquals(1, _MetadataRepository.count());
        entity = _MetadataRepository.findOne(entity.getId());
        assertTrue(entity.getMetadataCategories().isEmpty());
    }
}
