package org.fao.geonet.api.anonymousAccessLink;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class AnonymousAccessLinkServiceTest extends AbstractServiceIntegrationTest {

	@Autowired
	private AnonymousAccessLinkService toTest;

	@Autowired
	private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

	@Test
	public void createAnonymousAccessLink() throws Exception {
		ServiceContext context = createServiceContext();
		loginAsAdmin(context);
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);

		AnonymousAccessLinkDto created = //
				toTest.createAnonymousAccessLink(new AnonymousAccessLinkDto().setMetadataUuid(md.getUuid()));

		AnonymousAccessLink stored = anonymousAccessLinkRepository.findOneByMetadataUuid(md.getUuid());
		assertEquals(stored.getMetadataUuid(), created.getMetadataUuid());
		assertEquals(stored.getHash(), created.getHash());
	}
}
