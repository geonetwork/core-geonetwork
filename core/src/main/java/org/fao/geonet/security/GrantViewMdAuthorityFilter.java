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

package org.fao.geonet.security;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.fao.geonet.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrantViewMdAuthorityFilter extends GenericFilterBean {

    @Autowired
    AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Autowired
    @Qualifier(PasswordUtil.ENCODER_ID)
    PasswordEncoder encoder;

    private HttpSessionSecurityContextRepository repo;

    public GrantViewMdAuthorityFilter(HttpSessionSecurityContextRepository httpSessionSecurityContextRepository) {
        httpSessionSecurityContextRepository.setTrustResolver(new AuthenticationTrustResolver() {
            AuthenticationTrustResolver delegate = new AuthenticationTrustResolverImpl();

            @Override
            public boolean isAnonymous(Authentication authentication) {
                if (authentication.getAuthorities().stream().anyMatch(ViewMdGrantedAuthority.class::isInstance)) {
                    return false;
                }
                return delegate.isAnonymous(authentication);
            }

            @Override
            public boolean isRememberMe(Authentication authentication) {
                return delegate.isRememberMe(authentication);
            }
        });
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication instanceof AnonymousAuthenticationToken;
        if (!isAnonymous) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String hashParam = servletRequest.getParameter("hash");
        boolean paramCannotBeSplitIntoHashAndUuid = hashParam == null || hashParam.length() < AnonymousAccessLink.getRandomHashLength() + 1;
        if (paramCannotBeSplitIntoHashAndUuid) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String hash = hashParam.substring(0, AnonymousAccessLink.getRandomHashLength());
        String uuid = hashParam.substring(AnonymousAccessLink.getRandomHashLength());
        AnonymousAccessLink authority = anonymousAccessLinkRepository.findOneByMetadataUuid(uuid);
        if (authority == null || !encoder.matches(hash, authority.getHash())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        boolean alreadyGranted = authentication.getAuthorities().stream() //
                .filter(ViewMdGrantedAuthority.class::isInstance) //
                .map(ViewMdGrantedAuthority.class::cast) //
                .map(ViewMdGrantedAuthority::getAnonymousAccessLink) //
                .anyMatch(authority::equals);
        if (alreadyGranted){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        authorities.add(new ViewMdGrantedAuthority().setAnonymousAccessLink(authority));
        AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(authentication.getName(), authentication.getPrincipal(), authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
