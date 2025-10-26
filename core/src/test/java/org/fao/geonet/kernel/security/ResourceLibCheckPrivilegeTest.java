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

package org.fao.geonet.kernel.security;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.AbstractIntegrationTestWithMockedSingletons;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataOperations;
import org.fao.geonet.lib.ResourceLib;
import org.fao.geonet.utils.Xml;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ResourceLibCheckPrivilegeTest extends AbstractIntegrationTestWithMockedSingletons {

	@Autowired
	ServiceManager serviceManager;

	@Autowired
	BaseMetadataOperations baseMetadataOperations;

	private ServiceContext adminServiceContext;
	private SecurityContext adminSecurityContext;
	private ServiceContext anonymousServiceContext;
	private SecurityContext anonymousSecurityContext;
	private ServiceContext anonymousWithAuthorityServiceContext;
	private SecurityContext anonymousWithAuthoritySecurityContext;
	private ViewMdGrantedAuthority viewMdGrantedAuthority;

	@Before
	public void initUserContexts() throws Exception {
		adminServiceContext = createServiceContext();
		loginAsAdmin(adminServiceContext);
		adminSecurityContext = SecurityContextHolder.getContext();
		SecurityContextHolder.clearContext();

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dummy");
		request.setSession(loginAsAnonymous());
		anonymousServiceContext = serviceManager.createServiceContext("Api", "fre", request);
		anonymousSecurityContext = SecurityContextHolder.getContext();
		SecurityContextHolder.clearContext();

		MockHttpServletRequest requestWithAuthority = new MockHttpServletRequest("GET", "/dummy");
		request.setSession(loginAsAnonymous());
		anonymousWithAuthorityServiceContext = serviceManager.createServiceContext("Api", "fre", requestWithAuthority);
		anonymousWithAuthoritySecurityContext = SecurityContextHolder.getContext();
		viewMdGrantedAuthority = new ViewMdGrantedAuthority();
		List<GrantedAuthority> authorities =
				new ArrayList<>(anonymousWithAuthoritySecurityContext.getAuthentication().getAuthorities());
		authorities.add(viewMdGrantedAuthority);
		AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(anonymousWithAuthoritySecurityContext.getAuthentication().getName(), anonymousWithAuthoritySecurityContext.getAuthentication().getPrincipal(), authorities);
		anonymousWithAuthoritySecurityContext.setAuthentication(token);
		SecurityContextHolder.clearContext();
	}

	@Test
	public void anonymousCanViewMdIfAllowedOperationInDb() throws Exception {
		SecurityContextHolder.setContext(adminSecurityContext);
		AbstractMetadata md = insertMdInDb();

		SecurityContextHolder.setContext(anonymousSecurityContext);
		Exception exception = assertThrows(OperationNotAllowedEx.class, () -> new ResourceLib().checkPrivilege(anonymousServiceContext, String.valueOf(md.getId()), ReservedOperation.view));
		assertEquals("Operation not allowed", exception.getMessage());

		SecurityContextHolder.setContext(adminSecurityContext);
		baseMetadataOperations.setOperation(adminServiceContext, md.getId(), ReservedGroup.all.getId(), ReservedOperation.view.getId());

		SecurityContextHolder.setContext(anonymousSecurityContext);
		new ResourceLib().checkPrivilege(anonymousServiceContext, String.valueOf(md.getId()), ReservedOperation.view);
		assertThrows(OperationNotAllowedEx.class, () -> new ResourceLib().checkPrivilege(anonymousServiceContext, String.valueOf(md.getId()), ReservedOperation.editing));
	}

	@Test
	public void anonymousCanViewMdIfAllowedOperationInSession() throws Exception {
		SecurityContextHolder.setContext(adminSecurityContext);
		AbstractMetadata md1 = insertMdInDb();
		AbstractMetadata md2 = insertMdInDb();

		SecurityContextHolder.setContext(anonymousWithAuthoritySecurityContext);
		viewMdGrantedAuthority.setAnonymousAccessLink(new AnonymousAccessLink().setMetadataId(md1.getId()));

		new ResourceLib().checkPrivilege(anonymousWithAuthorityServiceContext, String.valueOf(md1.getId()), ReservedOperation.view);
		assertThrows(OperationNotAllowedEx.class, () -> new ResourceLib().checkPrivilege(anonymousWithAuthorityServiceContext, String.valueOf(md1.getId()), ReservedOperation.editing));
		Exception exception = assertThrows(OperationNotAllowedEx.class, () -> new ResourceLib().checkPrivilege(anonymousServiceContext, String.valueOf(md2.getId()), ReservedOperation.view));
		assertEquals("Operation not allowed", exception.getMessage());
	}

	private AbstractMetadata insertMdInDb() throws Exception {
		Element mdElem = getSampleISO19115MetadataXml();
		Element uuidElement = Xml.selectElement(mdElem, "*//mcc:MD_Identifier/mcc:code/gco:CharacterString");
		uuidElement.setText( UUID.randomUUID().toString());
		return insertTemplateResourceInDb(adminServiceContext, mdElem);
	}
}
