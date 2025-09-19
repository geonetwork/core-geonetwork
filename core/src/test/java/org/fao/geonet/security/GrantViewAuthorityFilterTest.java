package org.fao.geonet.security;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

public class GrantViewAuthorityFilterTest extends AbstractCoreIntegrationTest {

	@Test
	public void nominal() throws ServletException, IOException {
		String hash = "hash-hash";
		Integer mdId = 666;
		AnonymousAccessLinkRepository repositoryMock = Mockito.mock(AnonymousAccessLinkRepository.class);
		Mockito.when(repositoryMock.getAuthorities(argThat(hash::equals))).thenReturn(Optional.of(mdId));
		GrantViewMdAuthorityFilter toTest = new GrantViewMdAuthorityFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(loginAsAnonymous());
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		toTest.doFilter(request, response, filterChain);

		Optional<ViewMdGrantedAuthority> extraAuth = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance) //
				.map(ViewMdGrantedAuthority.class::cast) //
				.findFirst();
		assertTrue(extraAuth.isPresent());
		assertEquals(mdId.toString(), extraAuth.get().getAuthority());
	}
}
