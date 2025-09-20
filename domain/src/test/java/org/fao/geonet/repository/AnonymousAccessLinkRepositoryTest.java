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
