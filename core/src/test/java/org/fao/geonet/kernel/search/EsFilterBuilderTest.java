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

package org.fao.geonet.kernel.search;

import jeeves.constants.Jeeves;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.owasp.esapi.crypto.CryptoToken.ANONYMOUS_USER;

public class EsFilterBuilderTest {

	public static final SimpleGrantedAuthority ROLE_ANONYMOUS = new SimpleGrantedAuthority("ROLE_ANONYMOUS");

	@BeforeClass
	public static void initEsFilterBuilder() throws Exception {
		AccessManager accessManagerMock = mock(AccessManager.class);
		when(accessManagerMock.getUserGroups(any(UserSession.class), anyString(), eq(false))).thenReturn(Set.of(ReservedGroup.all.getId()));
		new EsFilterBuilder(accessManagerMock, null);
	}

	@BeforeClass
	public static void mockApplicationContext() {
		ApplicationContextHolder.set(mock(ConfigurableApplicationContext.class));
	}

	@Test
	public void anonymousWithOrWithoutViewMdGrantedAuthority() throws Exception {
		ServiceContext anonymousContext = loginAsAnonymousWithAuthority(ROLE_ANONYMOUS);
		String filter = EsFilterBuilder.buildPermissionsFilter(anonymousContext);
		assertEquals("(op0:())", filter);

		ViewMdGrantedAuthority viewMdGrantedAuthority = new ViewMdGrantedAuthority().setAnonymousAccessLink( //
				new AnonymousAccessLink().setMetadataUuid("uuid"));
		anonymousContext = loginAsAnonymousWithAuthority(ROLE_ANONYMOUS, viewMdGrantedAuthority);
		filter = EsFilterBuilder.buildPermissionsFilter(anonymousContext);
		assertEquals("(op0:() _id:uuid)", filter);

		ViewMdGrantedAuthority viewMdGrantedAuthority2 = new ViewMdGrantedAuthority().setAnonymousAccessLink( //
				new AnonymousAccessLink().setMetadataUuid("uuid2"));
		anonymousContext = loginAsAnonymousWithAuthority(ROLE_ANONYMOUS, viewMdGrantedAuthority, viewMdGrantedAuthority2);
		filter = EsFilterBuilder.buildPermissionsFilter(anonymousContext);
		assertEquals("(op0:() _id:uuid _id:uuid2)", filter);
	}

	private ServiceContext loginAsAnonymousWithAuthority(GrantedAuthority ... grantedAuthorities) {
		MockHttpSession session = new MockHttpSession();
		AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken( //
				ANONYMOUS_USER, ANONYMOUS_USER, //
				List.of(grantedAuthorities));
		SecurityContextHolder.getContext().setAuthentication(auth);
		UserSession userSession = new UserSession();
		session.setAttribute(Jeeves.Elem.SESSION, userSession);
		userSession.setsHttpSession(session);
		ServiceContext serviceContext = mock(ServiceContext.class);
		when(serviceContext.getUserSession()).thenReturn(userSession);
		return serviceContext;
	}
}
