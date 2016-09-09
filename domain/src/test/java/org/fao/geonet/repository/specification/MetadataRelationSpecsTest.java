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
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataRelationRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.fao.geonet.repository.specification.MetadataRelationSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.MetadataRelationSpecs.hasRelatedId;
import static org.junit.Assert.assertEquals;

/**
 * Tests for the MetadataRelationSpecs
 * <p/>
 * User: Jesse Date: 9/30/13 Time: 7:58 PM
 */
public class MetadataRelationSpecsTest extends AbstractSpringDataTest {
    @Autowired
    MetadataRelationRepository _repo;
    @Autowired
    MetadataRepository _metadataRepo;

    @Test
    public void testHasMetadataId() throws Exception {
        Metadata metadata1 = _metadataRepo.save(newMetadata(_inc));
        Metadata metadata2 = _metadataRepo.save(newMetadata(_inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));
        _repo.save(relation);

        assertEquals(1, _repo.count(hasMetadataId(metadata1.getId())));
        assertEquals(0, _repo.count(hasMetadataId(metadata2.getId())));
    }

    @Test
    public void testHasRelatedId() throws Exception {
        Metadata metadata1 = _metadataRepo.save(newMetadata(_inc));
        Metadata metadata2 = _metadataRepo.save(newMetadata(_inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));
        _repo.save(relation);

        assertEquals(0, _repo.count(hasRelatedId(metadata1.getId())));
        assertEquals(1, _repo.count(hasRelatedId(metadata2.getId())));
    }
}
