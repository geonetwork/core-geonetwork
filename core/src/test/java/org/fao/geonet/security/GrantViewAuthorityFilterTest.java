package org.fao.geonet.security;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;

public class GrantViewAuthorityFilterTest extends AbstractCoreIntegrationTest {

	private int mdId;
	private String hash;
	private MockHttpServletRequest requestMock;
	private MockFilterChain filterChainMock;
	private MockHttpServletResponse responseMock;
	private AnonymousAccessLinkRepository repositoryMock;

	@Test
	public void nominal() throws ServletException, IOException {
		GrantViewMdAuthorityFilter toTest = prepareToTest();

		toTest.doFilter(requestMock, responseMock, filterChainMock);

		assertEquals(requestMock, filterChainMock.getRequest());
		assertEquals(responseMock, filterChainMock.getResponse());
		Optional<ViewMdGrantedAuthority> extraAuth = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance) //
				.map(ViewMdGrantedAuthority.class::cast).findFirst();
		assertTrue(extraAuth.isPresent());
		assertEquals(mdId, extraAuth.get().getAnonymousAccessLink().getMetadataId());
	}

	@Test
	public void hashIgnoredWhenNotAnonymous() throws ServletException, IOException {
		GrantViewMdAuthorityFilter toTest = prepareToTest();
		requestMock.setSession(loginAsAdmin());

		toTest.doFilter(requestMock, responseMock, filterChainMock);

		assertEquals(requestMock, filterChainMock.getRequest());
		assertEquals(responseMock, filterChainMock.getResponse());
		long extraAuthCount = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance).count();
		assertEquals(0, extraAuthCount);
	}

	@Test
	public void viewMdAuthorityCanBeAddedOnce() throws ServletException, IOException {
		GrantViewMdAuthorityFilter toTest = prepareToTest();

		toTest.doFilter(requestMock, responseMock, filterChainMock);
		filterChainMock.reset();
		toTest.doFilter(requestMock, responseMock, filterChainMock);

		assertEquals(requestMock, filterChainMock.getRequest());
		assertEquals(responseMock, filterChainMock.getResponse());
		long extraAuthCount = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance).count();
		assertEquals(1, extraAuthCount);
	}

	@Test
	public void canViewManyDifferentMds() throws ServletException, IOException {
		GrantViewMdAuthorityFilter toTest = prepareToTest();

		toTest.doFilter(requestMock, responseMock, filterChainMock);
		filterChainMock.reset();
		Mockito.when(repositoryMock.findOneByHash(argThat("hush-hush"::equals))).thenReturn(
				new AnonymousAccessLink().setMetadataId(123));
		requestMock.setParameter("hash", "hush-hush");
		toTest.doFilter(requestMock, responseMock, filterChainMock);

		assertEquals(requestMock, filterChainMock.getRequest());
		assertEquals(responseMock, filterChainMock.getResponse());
		List<Integer> extraAuth = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance) //
				.map(ViewMdGrantedAuthority.class::cast) //
				.map(ViewMdGrantedAuthority::getAnonymousAccessLink)
				.map(AnonymousAccessLink::getMetadataId)//
				.collect(Collectors.toList());
		assertEquals(2, extraAuth.size());
		assertTrue(extraAuth.contains(mdId));
		assertTrue(extraAuth.contains(123));
	}

	@Test
	public void unknownHashIgnored() throws ServletException, IOException {
		GrantViewMdAuthorityFilter toTest = prepareToTest();

		requestMock.setParameter("hash", "hush-hush");
		toTest.doFilter(requestMock, responseMock, filterChainMock);

		assertEquals(requestMock, filterChainMock.getRequest());
		assertEquals(responseMock, filterChainMock.getResponse());
		long extraAuthCount = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream() //
				.filter(ViewMdGrantedAuthority.class::isInstance).count();
		assertEquals(0, extraAuthCount);
	}

	private GrantViewMdAuthorityFilter prepareToTest() {
		hash = "hash-hash";
		mdId = 666;
		repositoryMock = mock(AnonymousAccessLinkRepository.class);
		Mockito.when(repositoryMock.findOneByHash(argThat(hash::equals))).thenReturn(new AnonymousAccessLink().setMetadataId(mdId));
		GrantViewMdAuthorityFilter toTest = new GrantViewMdAuthorityFilter(mock(HttpSessionSecurityContextRepository.class));
		toTest.anonymousAccessLinkRepository = repositoryMock;
		requestMock = new MockHttpServletRequest();
		requestMock.setParameter("hash", hash);
		responseMock = new MockHttpServletResponse();
		filterChainMock = new MockFilterChain();
		requestMock.setSession(loginAsAnonymous());
		return toTest;
	}
}
