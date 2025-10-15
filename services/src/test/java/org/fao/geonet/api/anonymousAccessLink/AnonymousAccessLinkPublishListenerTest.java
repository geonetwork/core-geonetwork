package org.fao.geonet.api.anonymousAccessLink;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataOperations;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNull;

public class AnonymousAccessLinkPublishListenerTest extends AbstractServiceIntegrationTest {

	@Autowired
	private AnonymousAccessLinkService anonymousAccessLinkService;

	@Autowired
	private BaseMetadataOperations baseMetadataOperations;

	private ServiceContext context;

	@Before
	public void initContext() throws Exception {
		context = createServiceContext();
		loginAsAdmin(context);
	}

	@Test
	public void createAnonymousAccessLink() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		anonymousAccessLinkService.createAnonymousAccessLink(md.getUuid());

		baseMetadataOperations.setOperation(context, md.getId(), ReservedGroup.all.getId(), ReservedOperation.view.getId());

		assertNull(anonymousAccessLinkService.getAnonymousAccessLink(md.getUuid()));
	}

}
