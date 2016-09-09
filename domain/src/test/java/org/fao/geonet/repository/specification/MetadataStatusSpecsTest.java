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


import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataStatusRepositoryTest;
import org.fao.geonet.repository.StatusValueRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.fao.geonet.repository.specification.MetadataStatusSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.MetadataStatusSpecs.hasUserId;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for MetadataStatusSpec User: Jesse Date: 9/6/13 Time: 10:31 AM To change this template
 * use File | Settings | File Templates.
 */
public class MetadataStatusSpecsTest extends AbstractSpringDataTest {
    @Autowired
    private StatusValueRepository _statusRepo;

    @Autowired
    private MetadataStatusRepository _repo;

    @Test
    public void testHasMetadataId() throws Exception {
        MetadataStatus md1 = _repo.save(newMetadataStatus());
        _repo.save(newMetadataStatus());

        final List<MetadataStatus> found = _repo.findAll(hasMetadataId(md1.getId().getMetadataId()));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
    }

    @Test
    public void testHasUserId() throws Exception {
        MetadataStatus md1 = _repo.save(newMetadataStatus());
        _repo.save(newMetadataStatus());

        final List<MetadataStatus> found = _repo.findAll(hasUserId(md1.getId().getUserId()));
        assertEquals(1, found.size());
        assertEquals(md1.getId(), found.get(0).getId());
    }

    private MetadataStatus newMetadataStatus() {
        return MetadataStatusRepositoryTest.newMetadataStatus(_inc, _statusRepo);
    }
}
