/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * This handles the JWT-Headers authentication filter.  It's based on the Shibboleth filter.
 */
public class JwtHeadersAuthFilter extends GenericFilterBean {

    @Autowired
    public JwtHeadersUserUtil jwtHeadersUserUtil;

    JwtHeadersConfiguration jwtHeadersConfiguration;

    //uniquely identify this authfilter
    //this is need if there are >1 Jwt-Header filters active at the same time
    String filterId = java.util.UUID.randomUUID().toString();


    public JwtHeadersAuthFilter(JwtHeadersConfiguration jwtHeadersConfiguration) {
        this.jwtHeadersConfiguration = jwtHeadersConfiguration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        var existingAuth = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = (HttpServletRequest) servletRequest;


        var config = jwtHeadersConfiguration.getJwtConfiguration();

        var user = JwtHeadersTrivialUser.create(config, request);

        //if request is already logged in by us (same filterId), but there aren't any Jwt-Headers attached
        //then log them out.
        if (user == null && existingAuth != null) {
            if (existingAuth instanceof JwtHeadersUsernamePasswordAuthenticationToken
                && ((JwtHeadersUsernamePasswordAuthenticationToken) existingAuth).authFilterId.equals(filterId)) {
                //at this point, there isn't a JWT header, but there's an existing auth that was made by us (JWT header)
                // in this case, we need to log-off.  They have a JSESSION auth that is no longer valid.
                logout(request);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }


        if (user == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return; // no valid user in header
        }

        //we have a valid user in the headers

        //existing user is the same user as the request
        if (existingAuth != null && existingAuth.getName().equals(user.getUsername())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return; // abort early - no need to do an expensive login.  Use the existing one.
        }

        //existing user isnt the same user as the request
        if (existingAuth != null && !existingAuth.getName().equals(user.getUsername())) {
            //in this case there are two auth's - the existing one (likely from JSESSION)
            //and one coming in from the JWT headers.  In this case, we kill the other login
            //and make a new one.
            logout(request);
        }

        var userDetails = jwtHeadersUserUtil.getUser(user, jwtHeadersConfiguration);
        if (userDetails != null) {
            UsernamePasswordAuthenticationToken auth = new JwtHeadersUsernamePasswordAuthenticationToken(
                filterId, userDetails, null, userDetails.getAuthorities());
            auth.setDetails(userDetails);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * handle a logout - clear out the security context, and invalidate the session
     *
     * @param request
     * @throws ServletException
     */
    public void logout(HttpServletRequest request) throws ServletException {
        request.logout();//dont think this does anything in GN
        SecurityContextHolder.getContext().setAuthentication(null);
        request.logout();
    }

}


