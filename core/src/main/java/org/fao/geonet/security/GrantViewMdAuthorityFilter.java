package org.fao.geonet.security;

import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GrantViewMdAuthorityFilter extends GenericFilterBean {

    @Autowired
    AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication instanceof AnonymousAuthenticationToken;
        if (!isAnonymous) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String hash = servletRequest.getParameter("hash");
        Optional<Integer> authority = anonymousAccessLinkRepository.getAuthorities(hash);
        if (authority.isEmpty()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        boolean alreadyGranted = authentication.getAuthorities().stream() //
                .filter(ViewMdGrantedAuthority.class::isInstance) //
                .map(ViewMdGrantedAuthority.class::cast) //
                .map(ViewMdGrantedAuthority::getAuthority) //
                .anyMatch(authority.get().toString()::equals);
        if (alreadyGranted){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        List<GrantedAuthority> authorities = new ArrayList<>(authentication.getAuthorities());
        authorities.add(new ViewMdGrantedAuthority().setMdId(authority.get().toString()));
        AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(authentication.getName(), authentication.getPrincipal(), authorities);
        SecurityContextHolder.getContext().setAuthentication(token);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
