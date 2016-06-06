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
import org.fao.geonet.domain.MetadataRelation;
import org.fao.geonet.domain.MetadataRelationId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.MetadataRepositoryTest.newMetadata;
import static org.junit.Assert.assertEquals;

public class MetadataRelationRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRelationRepository _repo;
    @Autowired
    MetadataRepository _metadataRepo;

    public static MetadataRelation newMetadataRelation(AtomicInteger inc, MetadataRepository metadataRepo) {
        Metadata metadata1 = metadataRepo.save(newMetadata(inc));
        Metadata metadata2 = metadataRepo.save(newMetadata(inc));

        MetadataRelation relation = new MetadataRelation();
        relation.setId(new MetadataRelationId(metadata1.getId(), metadata2.getId()));

        return relation;
    }

    @Test
    public void testFindOne() {
        MetadataRelation relation = newMetadataRelation();
        relation = _repo.save(relation);

        MetadataRelation relation1 = newMetadataRelation();
        relation1 = _repo.save(relation1);

        assertEquals(relation1, _repo.findOne(relation1.getId()));
        assertEquals(relation, _repo.findOne(relation.getId()));
    }

    @Test
    public void testFindAllById_MetadataId() {
        MetadataRelation relation = newMetadataRelation();
        relation = _repo.save(relation);

        MetadataRelation relation1 = newMetadataRelation();
        _repo.save(relation1);

        final List<MetadataRelation> found = _repo.findAllById_MetadataId(relation.getId().getMetadataId());

        assertEquals(1, found.size());
        assertEquals(relation, found.get(0));
    }

    private MetadataRelation newMetadataRelation() {
        return newMetadataRelation(_inc, _metadataRepo);
    }

}
