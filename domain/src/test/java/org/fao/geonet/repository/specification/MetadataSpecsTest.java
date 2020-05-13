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

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataCategoryRepositoryTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasCategory;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasExtra;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasHarvesterUuid;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataIdIn;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasSchemaId;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasSource;
import static org.fao.geonet.repository.specification.MetadataSpecs.hasType;
import static org.fao.geonet.repository.specification.MetadataSpecs.isHarvested;
import static org.fao.geonet.repository.specification.MetadataSpecs.isOwnedByOneOfFollowingGroups;
import static org.fao.geonet.repository.specification.MetadataSpecs.isType;

/**
 * Test for MetadataSpecs.
 * <p/>
 * User: Jesse Date: 9/4/13 Time: 10:08 PM
 */
public class MetadataSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRepository _repository;
    @Autowired
    MetadataCategoryRepository _categoryRepo;

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        assertFindsCorrectMd(md1, (Specification<Metadata>)hasMetadataId(md1.getId()), true);
    }

    @Test
    public void testHasMetadataType() throws Exception {
        final Metadata metadata = newMetadata(_inc);
        metadata.getDataInfo().setType(MetadataType.METADATA);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        Metadata md2 = _repository.save(metadata2);

        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getDataInfo().setType(MetadataType.TEMPLATE);
        Metadata md3 = _repository.save(metadata3);

        assertEquals(1, _repository.findAll((Specification<Metadata>)hasType(MetadataType.METADATA)).size());
        assertEquals(1, _repository.findAll((Specification<Metadata>)hasType(MetadataType.SUB_TEMPLATE)).size());
        assertEquals(1, _repository.findAll((Specification<Metadata>)hasType(MetadataType.TEMPLATE)).size());

        assertEquals(md1.getId(), _repository.findOne((Specification<Metadata>)hasType(MetadataType.METADATA)).getId());
        assertEquals(md2.getId(), _repository.findOne((Specification<Metadata>)hasType(MetadataType.SUB_TEMPLATE)).getId());
        assertEquals(md3.getId(), _repository.findOne((Specification<Metadata>)hasType(MetadataType.TEMPLATE)).getId());
    }

    @Test
    public void testHasSchemaId() throws Exception {
        String schemaId1 = "schemaId1";
        String schemaId2 = "schemaId2";
        final Metadata metadata = newMetadata(_inc);
        metadata.getDataInfo().setSchemaId(schemaId1);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getDataInfo().setSchemaId(schemaId2);
        Metadata md2 = _repository.save(metadata2);

        assertEquals(1, _repository.findAll((Specification<Metadata>)hasSchemaId(schemaId1)).size());
        assertEquals(1, _repository.findAll((Specification<Metadata>)hasSchemaId(schemaId2)).size());
        assertEquals(0, _repository.findAll((Specification<Metadata>)hasSchemaId("other")).size());

        assertEquals(md1.getId(), _repository.findOne((Specification<Metadata>)hasSchemaId(schemaId1)).getId());
        assertEquals(md2.getId(), _repository.findOne((Specification<Metadata>)hasSchemaId(schemaId2)).getId());
    }

    @Test
    public void testHasCategory() throws Exception {
        final MetadataCategory cat1 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat2 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat3 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));
        final MetadataCategory cat4 = _categoryRepo.save(MetadataCategoryRepositoryTest.newMetadataCategory(_inc));

        final Metadata metadata = newMetadata(_inc);
        metadata.getCategories().add(cat1);
        metadata.getCategories().add(cat2);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getCategories().add(cat1);
        metadata2.getCategories().add(cat3);
        Metadata md2 = _repository.save(metadata2);

        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getCategories().add(cat2);
        Metadata md3 = _repository.save(metadata3);

        List<Metadata> found = _repository.findAll((Specification<Metadata>)hasCategory(cat1), SortUtils.createSort(Metadata_.id));

        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md2.getId(), found.get(1).getId());

        found = _repository.findAll((Specification<Metadata>)hasCategory(cat2), SortUtils.createSort(Metadata_.id));
        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md3.getId(), found.get(1).getId());

        found = _repository.findAll((Specification<Metadata>)hasCategory(cat3), SortUtils.createSort(Metadata_.id));
        assertEquals(1, found.size());
        assertEquals(md2.getId(), found.get(0).getId());

        found = _repository.findAll((Specification<Metadata>)hasCategory(cat4), SortUtils.createSort(Metadata_.id));
        assertEquals(0, found.size());

    }

    @Test
    public void testIsOwnedByOneOfFollowingGroups() throws Exception {


        final Metadata metadata = newMetadata(_inc);
        metadata.getSourceInfo().setGroupOwner(1);
        Metadata md1 = _repository.save(metadata);

        final Metadata metadata2 = newMetadata(_inc);
        metadata2.getSourceInfo().setGroupOwner(2);
        _repository.save(metadata2);


        final Metadata metadata3 = newMetadata(_inc);
        metadata3.getSourceInfo().setGroupOwner(3);
        Metadata md3 = _repository.save(metadata3);

        List<Metadata> found = _repository.findAll((Specification<Metadata>)isOwnedByOneOfFollowingGroups(Arrays.asList(1)));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());

        found = _repository.findAll((Specification<Metadata>)isOwnedByOneOfFollowingGroups(Arrays.asList(1, 3)), SortUtils.createSort(Metadata_.id));
        assertEquals(2, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
        assertEquals(md3.getId(), found.get(1).getId());
    }

    @Test
    public void testHasMetadataUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = (Specification<Metadata>)hasMetadataUuid(md1.getUuid());

        assertFindsCorrectMd(md1, spec, true);
    }


    @Test
    public void testHasHarvesterUuid() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = (Specification<Metadata>)hasHarvesterUuid(md1.getHarvestInfo().getUuid());
        assertFindsCorrectMd(md1, spec, true);
    }

    @Test
    public void testIsHarvested() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getHarvestInfo().setHarvested(false);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getHarvestInfo().setHarvested(true);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, (Specification<Metadata>)isHarvested(false), false);
        assertFindsCorrectMd(md2, (Specification<Metadata>)isHarvested(true), false);
    }

    @Test
    public void testHasMetadataIdIn() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2 = _repository.save(md2);

        List<Metadata> all = _repository.findAll((Specification<Metadata>)hasMetadataIdIn(Arrays.asList(md1.getId())));
        assertEquals(1, all.size());
        assertEquals(md1.getId(), all.get(0).getId());

        all = _repository.findAll((Specification<Metadata>)hasMetadataIdIn(Arrays.asList(md1.getId(), md2.getId())));
        assertEquals(2, all.size());

        all = _repository.findAll((Specification<Metadata>)hasMetadataIdIn(Collections.<Integer>emptyList()));
        assertTrue(all.isEmpty());


    }

    @Test
    public void testIsTemplate() throws Exception {
        Metadata md1 = newMetadata(_inc);
        md1.getDataInfo().setType(MetadataType.METADATA);
        md1 = _repository.save(md1);
        Metadata md2 = newMetadata(_inc);
        md2.getDataInfo().setType(MetadataType.SUB_TEMPLATE);
        md2 = _repository.save(md2);

        assertFindsCorrectMd(md1, (Specification<Metadata>)isType(MetadataType.METADATA), false);
        assertFindsCorrectMd(md2, (Specification<Metadata>)isType(MetadataType.SUB_TEMPLATE), false);
    }

    @Test
    public void testHasSource() throws Exception {
        Metadata md1 = _repository.save(newMetadata(_inc));
        Specification<Metadata> spec = (Specification<Metadata>)hasSource(md1.getSourceInfo().getSourceId());
        assertFindsCorrectMd(md1, spec, true);
    }

    @Test
    public void testHasExtra() throws Exception {
        final Metadata entity = newMetadata(_inc);
        final String extra = "extra data";
        entity.getDataInfo().setExtra(extra);
        Metadata md1 = _repository.save(entity);

        assertFindsCorrectMd(md1, (Specification<Metadata>)hasExtra(extra), true);

        assertEquals(0, _repository.count((Specification<Metadata>)hasExtra("wrong extra")));
    }

    private void assertFindsCorrectMd(Metadata md1, Specification<Metadata> spec, boolean addNewMetadata) {
        if (addNewMetadata) {
            _repository.save(newMetadata(_inc));
        }
        List<Integer> found = _repository.findAllIdsBy(spec);
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).intValue());
    }
}
