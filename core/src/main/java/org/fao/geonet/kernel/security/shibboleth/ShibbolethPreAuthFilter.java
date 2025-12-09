/*
 *  Copyright (C) 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.kernel.security.shibboleth;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.security.shibboleth.ShibbolethUserUtils.MinimalUser;
import org.fao.geonet.utils.Log;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Preauthentication Filter for Shibboleth.
 *
 * See {@link org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter}
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class ShibbolethPreAuthFilter extends GenericFilterBean {

    private static final String SHIB_KEY = "SHIB_USER_AUTHEN";
    private RequestCache requestCache;

    public ShibbolethPreAuthFilter() {
        if (Log.isDebugEnabled(Log.JEEVES)) {
            Log.debug(Log.JEEVES, "Setting up Shibboleth pre-auth filter");
        }
    }

    /**
     * Check if there is a header with the username and then use it for authentication. If there is
     * no header, but the user was authenticated by shibboleth, then just de-authenticate him
     *
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
     * jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (Log.isDebugEnabled(Log.JEEVES)) {
            try {
                Log.debug(Log.JEEVES,
                    "Performing Shibboleth pre-auth check. Existing auth is "
                        + SecurityContextHolder.getContext()
                        .getAuthentication());
            } catch (Throwable t) {
            }
        }

        ShibbolethUserConfiguration configuration = ApplicationContextHolder.get().getBean(ShibbolethUserConfiguration.class);
        ShibbolethUserUtils utils = ApplicationContextHolder.get().getBean(ShibbolethUserUtils.class);


        HttpServletRequest hreq = (HttpServletRequest) request;

        String username = "UNIDENTIFIED";
        try {
            Authentication currentUser = SecurityContextHolder.getContext()
                .getAuthentication();
            MinimalUser minimal = MinimalUser.create(request, configuration);

            if (minimal != null) { // for logging only
                username = minimal.getUsername();

                if (Log.isDebugEnabled(Log.JEEVES)) {
                    Log.debug(Log.JEEVES,
                        "Found Shibboleth credentials for user " + username);
                }
            } else {
                if (Log.isDebugEnabled(Log.JEEVES)) {
                    Log.debug(Log.JEEVES, "No Shibboleth credentials found");
                }
            }
            String _uid = ShibbolethUserUtils.getHeader(hreq,
                configuration.getUsernameKey(), "");

            HttpServletRequest req = (HttpServletRequest) request;
            if ((currentUser == null // not in any user context
                || !currentUser.getName().equalsIgnoreCase(_uid))
                // Shibboleth logged another user
                && minimal != null) { // valid headers found

                if (Log.isDebugEnabled(Log.JEEVES)) {
                    Log.debug(Log.JEEVES,
                        "Trying to authenticate via Shibboleth...");
                }

                UserDetails user = utils.setupUser(request, configuration);

                if (user != null) {
                    //mark user as logged in with shibboleth key
                    req.getSession().setAttribute(SHIB_KEY, true);
                    if (Log.isDebugEnabled(Log.JEEVES)) {
                        Log.debug(
                            Log.JEEVES,
                            "Shibboleth user found " + user.getUsername()
                                + " with authorities: "
                                + user.getAuthorities());
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                    auth.setDetails(user);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Log.info(Log.JEEVES, "User '" + user.getUsername()
                        + "' properly authenticated via Shibboleth");

                    HttpServletResponse hresp = (HttpServletResponse) response;

                    if (requestCache != null) {
                        String redirect = null;

                        SavedRequest savedReq = requestCache.getRequest(hreq,
                            hresp);
                        if (savedReq != null) {
                            redirect = savedReq.getRedirectUrl();
                            Log.debug(Log.JEEVES,
                                "Found saved request location: " + redirect);
                        } else {
                            Log.debug(Log.JEEVES, "No saved request found");
                        }

                        if (redirect != null) {
                            Log.info(Log.JEEVES, "Redirecting to " + redirect);

                            // Removing original request, since we want to
                            // retain current headers.
                            // If request remains in cache, requestCacheFilter
                            // will reinstate the original headers and we don't
                            // want it.
                            requestCache.removeRequest(hreq, hresp);

                            hresp.sendRedirect(redirect);
                            return; // no further chain processing allowed
                        }
                    }

                } else {
                    Log.warning(Log.JEEVES,
                        "Error in GN shibboleth precedures handling user '"
                            + username);
                }
            } else if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication()
                .isAuthenticated() && minimal == null) {
                // Are we logged out?
                if (req.getSession().getAttribute(SHIB_KEY) != null) {
                    req.getSession().removeAttribute(SHIB_KEY);
                    SecurityContextHolder.getContext().setAuthentication(null);
                }
            }

        } catch (Exception ex) {
            Log.warning(Log.JEEVES, "Error during Shibboleth login for user "
                + username + ": " + ex.getMessage(), ex);
        }

        chain.doFilter(request, response);
    }

    public RequestCache getRequestCache() {
        return requestCache;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

}
