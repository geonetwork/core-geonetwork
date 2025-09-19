package org.fao.geonet.security;

import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class GrantViewMdAuthorityFilter extends GenericFilterBean {

    @Autowired
    AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
