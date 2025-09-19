package org.fao.geonet.kernel.security;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.domain.AbstractMetadata;
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
	public void initAdminContext() throws Exception {
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
	public void anonymousWithAuthorityCanViewPrivateMd() throws Exception {
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
	public void anonymousCanViewPrivateMdIfAllowedOperationInSession() throws Exception {
		SecurityContextHolder.setContext(adminSecurityContext);
		AbstractMetadata md1 = insertMdInDb();
		AbstractMetadata md2 = insertMdInDb();

		SecurityContextHolder.setContext(anonymousWithAuthoritySecurityContext);
		viewMdGrantedAuthority.setMdId(Integer.toString(md1.getId()));

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
