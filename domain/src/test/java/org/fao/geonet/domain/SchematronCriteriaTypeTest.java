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

package org.fao.geonet.domain;

import org.fao.geonet.repository.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test schematron criteria type accept methods Created by Jesse on 2/27/14.
 */
public class SchematronCriteriaTypeTest extends AbstractSpringDataTest {

    @Autowired
    ApplicationContext _appContext;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MetadataRepository repository;


    @Test
    public void testAcceptsSingleGroup() throws Exception {
        final Group group = groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata.getSourceInfo().setGroupOwner(group.getId());
        metadata = repository.saveAndFlush(metadata);

        int metadataId = metadata.getId();
        assertTrue(SchematronCriteriaType.GROUP.accepts(_appContext, "" + group.getId(), metadataId, metadata.getXmlData(false), null));
        assertFalse(SchematronCriteriaType.GROUP.accepts(_appContext, "" + (group.getId() + 1), metadataId, metadata.getXmlData(false
        ), null));

    }

    @Test
    public void testAcceptsSingleOrGroup() throws Exception {
        final Group group = groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        final Group group2 = groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        final Group group3 = groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata.getSourceInfo().setGroupOwner(group.getId());
        metadata = repository.saveAndFlush(metadata);

        Metadata metadata2 = MetadataRepositoryTest.newMetadata(_inc);
        metadata2.getSourceInfo().setGroupOwner(group2.getId());
        metadata2 = repository.saveAndFlush(metadata2);

        Metadata metadata3 = MetadataRepositoryTest.newMetadata(_inc);
        metadata3.getSourceInfo().setGroupOwner(group3.getId());
        metadata3 = repository.saveAndFlush(metadata3);

        final String groups = group.getId() + "," + group3.getId();
        assertTrue(SchematronCriteriaType.GROUP.accepts(_appContext, groups, metadata.getId(), metadata.getXmlData(false), null));
        assertFalse(SchematronCriteriaType.GROUP.accepts(_appContext, groups, metadata2.getId(), metadata2.getXmlData(false), null));
        assertTrue(SchematronCriteriaType.GROUP.accepts(_appContext, groups, metadata3.getId(), metadata3.getXmlData(false), null));

    }
}
