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


import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataCategoryRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataCategoryRepository _repo;

    @Autowired
    MetadataRepository _metadataRepo;

    @PersistenceContext
    EntityManager _entityManager;

    public static MetadataCategory newMetadataCategory(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataCategory metadataCategory = new MetadataCategory();
        metadataCategory.setName("name" + val);
        metadataCategory.getLabelTranslations().put("eng", "engLab" + val);
        metadataCategory.getLabelTranslations().put("fre", "fraLab" + val);
        metadataCategory.getLabelTranslations().put("ger", "gerLab" + val);

        return metadataCategory;
    }

    @Test
    public void testFindOne() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        category2 = _repo.save(category2);

        assertEquals(category2, _repo.findById(category2.getId()).get());
        assertEquals(category1, _repo.findById(category1.getId()).get());
    }

    @Test
    public void testFindOneByNameIgnoreCase() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        _repo.save(category2);

        assertEquals(category1, _repo.findOneByNameIgnoreCase(category1.getName().toLowerCase()));
        assertEquals(category1, _repo.findOneByNameIgnoreCase(category1.getName().toUpperCase()));
    }

    @Test
    public void testFindOneByName() {
        MetadataCategory category1 = newMetadataCategory();
        category1 = _repo.save(category1);

        MetadataCategory category2 = newMetadataCategory();
        category2 = _repo.save(category2);

        MetadataCategory metadataCategory = _repo.findOneByName(category1.getName());
        assertEquals(category1.getName(), metadataCategory.getName());

        metadataCategory = _repo.findOneByName(category2.getName());
        assertEquals(category2.getName(), metadataCategory.getName());
    }

    @Test
    public void testDeleteDeletesFromMetadata() throws Exception {

        MetadataCategory cat1 = _repo.save(newMetadataCategory(_inc));
        MetadataCategory cat2 = _repo.save(newMetadataCategory(_inc));

        Metadata metadata1 = MetadataRepositoryTest.newMetadata(_inc);
        metadata1.getCategories().add(cat1);
        metadata1.getCategories().add(cat2);
        metadata1 = _metadataRepo.save(metadata1);

        Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2.getCategories().add(cat1);
        metadata2 = _metadataRepo.save(metadata2);

        _repo.deleteCategoryAndMetadataReferences(cat1.getId());

        _entityManager.flush();
        _entityManager.clear();

        // org.fao.geonet.services.category.Remove assumes that this test passes.  If this test can't pass
        // then there needs to be a way to fix Remove as well.
        final Set<MetadataCategory> foundCategories = _metadataRepo.findById(metadata1.getId()).get().getCategories();
        assertEquals(1, foundCategories.size());
        assertEquals(cat2.getId(), foundCategories.iterator().next().getId());

        assertEquals(0, _metadataRepo.findById(metadata2.getId()).get().getCategories().size());
    }

    private MetadataCategory newMetadataCategory() {
        return newMetadataCategory(_inc);
    }

}
