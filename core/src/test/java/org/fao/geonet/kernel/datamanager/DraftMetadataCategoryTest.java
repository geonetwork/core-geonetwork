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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import jeeves.server.context.ServiceContext;

/**
 * Tests for {@link DraftMetadataCategory}.
 * 
 * @author delawen María Arias de Reyna
 * 
 */
@ContextConfiguration(inheritLocations = true, locations = { "classpath:draft-test-context.xml" })
public class DraftMetadataCategoryTest extends AbstractCoreIntegrationTest {

	@Autowired
	private BaseMetadataCategory metadataCategory;

	@Autowired
	private MetadataCategoryRepository metadataCategoryRepository;

	@Autowired
	private MetadataDraftRepository metadataRepository;

	private MetadataDraft md;
	private MetadataCategory mdc;

	@Before
	public void init() {
		md = new MetadataDraft();
		populate(md);
		metadataRepository.save(md);
	}

	/**
	 *  On draft metadata, categories don't work.
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {

		assertTrue(metadataRepository.exists(md.getId()));

		assertTrue(metadataCategory.getCategories(String.valueOf(md.getId())).isEmpty());

		assertFalse(metadataCategory.isCategorySet(String.valueOf(md.getId()), mdc.getId()));

		ServiceContext context = createServiceContext();

		assertFalse(metadataCategory.setCategory(context, String.valueOf(md.getId()), String.valueOf(mdc.getId())));

		assertFalse(metadataCategory.isCategorySet(String.valueOf(md.getId()), mdc.getId()));
		assertTrue(metadataCategory.getCategories(String.valueOf(md.getId())).isEmpty());

		metadataCategory.unsetCategory(context, String.valueOf(md.getId()), mdc.getId());
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
