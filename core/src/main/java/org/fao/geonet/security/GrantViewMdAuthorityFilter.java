package org.fao.geonet.security;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private HttpSessionSecurityContextRepository repo;

    public GrantViewMdAuthorityFilter(HttpSessionSecurityContextRepository httpSessionSecurityContextRepository) {
        this.repo = httpSessionSecurityContextRepository;
        this.repo.setTrustResolver(new AuthenticationTrustResolver() {
            @Override
            public boolean isAnonymous(Authentication authentication) {
                return false;
            }

            @Override
            public boolean isRememberMe(Authentication authentication) {
                return false;
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
        String hash = servletRequest.getParameter("hash");
        AnonymousAccessLink authority = anonymousAccessLinkRepository.findOneByHash(hash);
        if (authority == null) {
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
