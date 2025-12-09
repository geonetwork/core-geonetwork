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

package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataCategoryRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataCategorySpecsTest extends AbstractSpringDataTest {


    @Autowired
    private MetadataCategoryRepository categoryRepository;

    @Test
    public void testHasCategoryNameIn() throws Exception {
        final MetadataCategory category = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory category2 = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory category3 = categoryRepository.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        final Specification<MetadataCategory> specification = MetadataCategorySpecs.hasCategoryNameIn(Arrays.asList
            (category.getName(), category3.getName()));

        final List<String> all = Lists.transform(categoryRepository.findAll(specification), new Function<MetadataCategory, String>() {
            @Nullable
            @Override
            public String apply(@Nullable MetadataCategory input) {
                return input.getName();
            }
        });

        assertEquals(2, all.size());
        assertTrue(all.contains(category.getName()));
        assertFalse(all.contains(category2.getName()));
        assertTrue(all.contains(category3.getName()));
    }
}
