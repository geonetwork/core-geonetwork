/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.api.anonymousAccessLink;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AnonymousAccessLinkServiceTest extends AbstractServiceIntegrationTest {

	@Autowired
	private AnonymousAccessLinkService toTest;

	@Autowired
	private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

	private ServiceContext context;

	@Before
	public void initContext() throws Exception {
		context = createServiceContext();
		loginAsAdmin(context);
	}

	@Test
	public void createAnonymousAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);

		AnonymousAccessLinkDto created = toTest.createAnonymousAccessLink(md.getUuid());

		AnonymousAccessLink stored = anonymousAccessLinkRepository.findOneByMetadataUuid(md.getUuid());
		assertEquals(stored.getMetadataUuid(), created.getMetadataUuid());
		assertEquals(stored.getMetadataId(), created.getMetadataId());
		assertEquals(stored.getHash() + stored.getMetadataUuid(), created.getHash());
	}

	@Test
	public void listAnonymousAccessLinkWithDocs() throws Exception {
		AbstractMetadata indexedMd1 = injectMetadataInDb(getSampleMetadataXml(), context, true, IndexingMode.full);
		toTest.createAnonymousAccessLink(indexedMd1.getUuid());
		AbstractMetadata notIndexedMd2 = injectMetadataInDb(getSampleMetadataXml(), context, true);
		toTest.createAnonymousAccessLink(notIndexedMd2.getUuid());
		AbstractMetadata indexedMd3 = injectMetadataInDb(getSampleMetadataXml(), context, true, IndexingMode.full);
		toTest.createAnonymousAccessLink(indexedMd3.getUuid());

		List<AnonymousAccessLinkDto> listed = toTest.getAllAnonymousAccessLinksWithMdInfos();
		AnonymousAccessLinkDto linkForMd1 = listed.stream() //
				.filter(dto -> indexedMd1.getUuid().equals(dto.getMetadataUuid())).findFirst().get();
		AnonymousAccessLinkDto linkForMd2 = listed.stream() //
				.filter(dto -> notIndexedMd2.getUuid().equals(dto.getMetadataUuid())).findFirst().get();
		AnonymousAccessLinkDto linkForMd3 = listed.stream() //
				.filter(dto -> indexedMd3.getUuid().equals(dto.getMetadataUuid())).findFirst().get();

		assertNull(linkForMd1.getHash());
		assertNotNull(linkForMd1.getGetResultSource());
		assertEquals("Title",
				((HashMap)((HashMap) linkForMd1.getGetResultSource()).get("resourceTitleObject")).get("default"));
		assertEquals("Abstract {uuid}",
				((HashMap)((HashMap)linkForMd1.getGetResultSource()).get("resourceAbstractObject")).get("default"));
		assertEquals("admin admin", ((HashMap)linkForMd1.getGetResultSource()).get("recordOwner"));
		assertTrue(((HashMap)linkForMd1.getGetResultSource()).get("dateStamp").toString().startsWith("2012-01-18"));
		assertNull(linkForMd2.getGetResultSource());
		assertNotNull(linkForMd3.getGetResultSource());
		assertEquals("Title",
				((HashMap)((HashMap) linkForMd3.getGetResultSource()).get("resourceTitleObject")).get("default"));
	}

	@Test
	public void deleteAnonymousAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		toTest.createAnonymousAccessLink(md.getUuid());

		toTest.deleteAnonymousAccessLink(md.getUuid());

		assertNull(anonymousAccessLinkRepository.findOneByMetadataUuid(md.getUuid()));
	}

	@Test
	public void getAnonymousAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		toTest.createAnonymousAccessLink(md.getUuid());

		assertNotNull(toTest.getAnonymousAccessLink(md.getUuid()));

		toTest.deleteAnonymousAccessLink(md.getUuid());

		assertNull(toTest.getAnonymousAccessLink(md.getUuid()));
	}

	@Test
	public void cannotBindTwoLinksToTheSameMd() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		toTest.createAnonymousAccessLink(md.getUuid());

		assertThrows(ResourceAlreadyExistException.class, () -> toTest.createAnonymousAccessLink(md.getUuid()));

		List<AnonymousAccessLinkDto> listed = toTest.getAllAnonymousAccessLinksWithMdInfos();
		List<AnonymousAccessLinkDto> linksForMd = listed.stream() //
				.filter(dto -> md.getUuid().equals(dto.getMetadataUuid())).collect(Collectors.toList());
		assertEquals(1, linksForMd.size());
	}

	@Test
	public void deleteAnonymousAccessLinkDoNotErrorWhenLinkDoesNotExist() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);

		toTest.deleteAnonymousAccessLink(md.getUuid());
	}

}
