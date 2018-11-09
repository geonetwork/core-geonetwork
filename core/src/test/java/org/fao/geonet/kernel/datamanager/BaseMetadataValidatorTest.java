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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.XmlSerializerIntegrationTest;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataValidator;
import org.fao.geonet.repository.GroupRepository;
import org.jdom.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link BaseMetadataValidator}.
 * 
 * @author delawen María Arias de Reyna
 * 
 */
public class BaseMetadataValidatorTest extends AbstractCoreIntegrationTest {

	@Autowired
	private IMetadataManager metadataManager;

	@Autowired
	private IMetadataValidator metadataValidator;
	@Autowired
	private GroupRepository groupRepository;

	private Group group;
	private AbstractMetadata md;

	@Before
	public void init() {

		for (Group g : groupRepository.findAll()) {
			if (!g.isReserved()) {
				group = g;
				break;
			}
		}

		try {
			md = metadataManager.save(createMetadata());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Should behave like the same function on {@link BaseMetadataManagerTest}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreate() throws Exception {
		metadataValidator.doValidate(md.getDataInfo().getSchemaId(), String.valueOf(md.getId()),
				new Document(md.getXmlData(false)), "eng");
	}

	private AbstractMetadata createMetadata() throws IOException {
		AbstractMetadata md = new Metadata();
		md.setUuid("test-metadata");
		try (InputStream is = XmlSerializerIntegrationTest.class.getResourceAsStream("valid-metadata.iso19139.xml")) {
			md.setData(IOUtils.toString(is));
		}
		md.getSourceInfo().setGroupOwner(group.getId());
		md.getSourceInfo().setOwner(1);
		md.getSourceInfo().setSourceId("test-faking");
		md.getDataInfo().setSchemaId("iso19139");
		md.getDataInfo().setType(MetadataType.TEMPLATE);
		return md;
	}

	@After
	public void cleanup() {

		metadataManager.delete(md.getId());

	}

}
