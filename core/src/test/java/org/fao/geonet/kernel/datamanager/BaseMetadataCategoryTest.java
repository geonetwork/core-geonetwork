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
package org.fao.geonet.kernel.datamanager;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Tests for {@link BaseMetadataCategory}.
 *
 * @author delawen María Arias de Reyna
 */
public class BaseMetadataCategoryTest extends AbstractCoreIntegrationTest {

    @Autowired
    private IMetadataCategory metadataCategory;

    @Autowired
    private MetadataCategoryRepository metadataCategoryRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    private Metadata md;
    private MetadataCategory mdc;

    @Before
    public void init() {
        md = new Metadata();
        populate(md);
        metadataRepository.save(md);
    }

    @Test
    public void test() throws Exception {

        assertTrue(metadataCategory.getCategories(String.valueOf(md.getId())).isEmpty());

        assertFalse(metadataCategory.isCategorySet(String.valueOf(md.getId()), mdc.getId()));

        ServiceContext context = createServiceContext();

        assertTrue(metadataCategory.setCategory(context, String.valueOf(md.getId()), String.valueOf(mdc.getId())));

        assertTrue(metadataCategory.isCategorySet(String.valueOf(md.getId()), mdc.getId()));
        assertFalse(metadataCategory.getCategories(String.valueOf(md.getId())).isEmpty());
        assertEquals(md.getCategories().size(), 1);

        metadataCategory.unsetCategory(context, String.valueOf(md.getId()), mdc.getId());

        assertFalse(metadataCategory.isCategorySet(String.valueOf(md.getId()), mdc.getId()));
    }

    @Test
    public void testCornerCases() throws Exception {

        ServiceContext context = createServiceContext();
        assertTrue(metadataCategory.setCategory(context, String.valueOf(md.getId()), String.valueOf(mdc.getId())));
        assertFalse(metadataCategory.setCategory(context, String.valueOf(md.getId()), String.valueOf(mdc.getId())));

        assertTrue(metadataCategory.unsetCategory(context, String.valueOf(md.getId()), mdc.getId()));
        assertFalse(metadataCategory.unsetCategory(context, String.valueOf(md.getId()), mdc.getId()));

        assertFalse(metadataCategory.setCategory(context, String.valueOf(Integer.MAX_VALUE), String.valueOf(mdc.getId())));
        assertFalse(metadataCategory.unsetCategory(context, String.valueOf(Integer.MAX_VALUE), mdc.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNoMetadata() throws Exception {
        metadataCategory.getCategories("-20");
    }

    @After
    public void cleanup() {
        metadataRepository.delete(md);
    }

    private void populate(AbstractMetadata md) {
        md.setUuid("test-metadata");
        md.setData("<xml></xml>");
        md.getSourceInfo().setGroupOwner(1);
        md.getSourceInfo().setOwner(1);
        md.getSourceInfo().setSourceId("test-faking");
        md.getDataInfo().setSchemaId("isoFake");

        mdc = metadataCategoryRepository.findAll().get(0);
    }

}
