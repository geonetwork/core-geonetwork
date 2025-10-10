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

package org.fao.geonet.repository;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AnonymousAccessLinkRepositoryTest extends AbstractSpringDataTest {
	@Autowired
	AnonymousAccessLinkRepository anonymousAccessLinkRepository;

	@After
	public void cleanRepo() {
		anonymousAccessLinkRepository.deleteAll();
	}

	@Test
	public void nominal() {
		AnonymousAccessLink anonymousAccessLink = new AnonymousAccessLink() //
				.setMetadataId(666) //
				.setMetadataUuid("uuid") //
				.setHash("hash");
		anonymousAccessLinkRepository.save(anonymousAccessLink);

		List<AnonymousAccessLink> accesses = anonymousAccessLinkRepository.findAll();
		assertEquals(1, accesses.size());
		assertEquals(666, accesses.get(0).getMetadataId());
		assertEquals("uuid", accesses.get(0).getMetadataUuid());
		assertEquals("hash", accesses.get(0).getHash());
		assertTrue(accesses.get(0).getId() > 99);

		anonymousAccessLinkRepository.deleteAll();

		accesses = anonymousAccessLinkRepository.findAll();
		assertEquals(0, accesses.size());
	}

	@Test
	public void findByHash() {
		AnonymousAccessLink anonymousAccessLink = new AnonymousAccessLink() //
				.setMetadataId(666) //
				.setMetadataUuid("uuid") //
				.setHash("hash");
		anonymousAccessLinkRepository.save(anonymousAccessLink);

		AnonymousAccessLink auth = anonymousAccessLinkRepository.findOneByHash("hash");

		assertNotNull(auth);
		assertEquals(666, auth.getMetadataId());

		auth = anonymousAccessLinkRepository.findOneByHash("hush");

		assertNull(auth);
	}

}
