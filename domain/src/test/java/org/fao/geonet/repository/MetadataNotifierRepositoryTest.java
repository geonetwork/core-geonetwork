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


import org.fao.geonet.domain.MetadataNotifier;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataNotifierRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataNotifierRepository _repo;

    public static MetadataNotifier newMetadataNotifier(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataNotifier metadataCategory = new MetadataNotifier();
        metadataCategory.setName("name" + val);
        metadataCategory.setPassword("password" + val);
        metadataCategory.setUrl("url" + val);
        metadataCategory.setUsername("username" + val);
        metadataCategory.setEnabled(val % 2 == 0);

        return metadataCategory;
    }

    @Test
    public void testFindOne() {
        MetadataNotifier notifier1 = newMetadataNotifier();
        notifier1 = _repo.save(notifier1);

        MetadataNotifier notifier2 = newMetadataNotifier();
        notifier2 = _repo.save(notifier2);

        assertEquals(notifier2, _repo.findOne(notifier2.getId()));
        assertEquals(notifier1, _repo.findOne(notifier1.getId()));
    }

    @Test
    public void testFindAllByEnabled() {
        MetadataNotifier notifier1 = newMetadataNotifier();
        notifier1 = _repo.save(notifier1);

        MetadataNotifier notifier2 = newMetadataNotifier();
        notifier2 = _repo.save(notifier2);

        List<MetadataNotifier> metadataCategory = _repo.findAllByEnabled(notifier1.isEnabled());
        assertEquals(notifier1.getName(), metadataCategory.get(0).getName());

        metadataCategory = _repo.findAllByEnabled(notifier2.isEnabled());
        assertEquals(notifier2.getName(), metadataCategory.get(0).getName());
    }

    private MetadataNotifier newMetadataNotifier() {
        AtomicInteger inc = _inc;
        return newMetadataNotifier(inc);
    }

}
