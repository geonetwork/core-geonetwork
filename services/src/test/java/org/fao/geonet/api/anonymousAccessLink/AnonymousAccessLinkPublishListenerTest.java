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

	@Test
	public void createAnonymousAccessLinkDoesNotOperateForPublishedMd() throws Exception {
		AbstractMetadata md = injectMetadataInDb(getSampleMetadataXml(), context, true);
		baseMetadataOperations.setOperation(context, md.getId(), ReservedGroup.all.getId(), ReservedOperation.view.getId());

		AnonymousAccessLinkDto created = anonymousAccessLinkService.createAnonymousAccessLink(md.getUuid());

		assertNull(created);
		assertNull(anonymousAccessLinkService.getAnonymousAccessLink(md.getUuid()));
	}

}
