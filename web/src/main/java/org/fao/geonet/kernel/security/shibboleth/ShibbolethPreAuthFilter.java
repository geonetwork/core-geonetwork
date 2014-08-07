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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.security.GeonetworkUser;
import org.fao.geonet.kernel.security.shibboleth.ShibbolethUserUtils.MinimalUser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import org.springframework.web.filter.GenericFilterBean;

/**
 * Preauthentication Filter for Shibboleth.
 *
 * See {@link org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter}
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class ShibbolethPreAuthFilter extends GenericFilterBean implements ApplicationContextAware {
    
	private ApplicationContext applicationContext;

    private ShibbolethUserConfiguration configuration;

    private RequestCache requestCache;

    public ShibbolethPreAuthFilter() {
        if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
            Log.debug(Geonet.LOG_AUTH, "Setting up Shibboleth pre-auth filter");
        }
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

        if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
            Log.debug(Geonet.LOG_AUTH, "Performing Shibboleth pre-auth check. Existing auth is " + SecurityContextHolder.getContext().getAuthentication());
        }

        HttpServletRequest hreq = (HttpServletRequest)request;

        String username = "UNIDENTIFIED";
        try {
            Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
            MinimalUser minimal =  MinimalUser.create(request, configuration);

            if(minimal != null) { // for logging only
                username = minimal.getUsername();

                if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
                    Log.debug(Geonet.LOG_AUTH, "Found Shibboleth credentials for user " + username);
                }
            } else {
                if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
                    Log.debug(Geonet.LOG_AUTH, "No Shibboleth credentials found");
                }
            }

            if(currentUser == null && // not in any user context
                    minimal != null) {   // valid headers found

                if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
                    Log.debug(Geonet.LOG_AUTH, "Trying to authenticate via Shibboleth...");
                }

//                if (! currentUser.getName().equals(minimal.getUsername()) ) {
//                    Log.info(Geonet.LOG_AUTH, "Pre-authenticated principal has changed from "
//                            + currentUser.getName()+" to "
//                            + minimal.getUsername() + " and will be reauthenticated");
//                }

                ResourceManager resourceManager = applicationContext.getBean(ResourceManager.class);
                ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
                SerialFactory serialFactory = applicationContext.getBean(SerialFactory.class);
                GeonetworkUser user = ShibbolethUserUtils.setupUser(request, resourceManager, profileManager, serialFactory, configuration);

                if(user != null) {

                    if (Log.isDebugEnabled(Geonet.LOG_AUTH)) {
                        Log.debug(Geonet.LOG_AUTH, "Shibboleth user found " + user.getUsername() + " with authorities: " + user.getAuthorities());
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities() ) ;
                    auth.setDetails(user);
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Log.info(Geonet.LOG_AUTH, "User '"+user.getUsername()+"' properly authenticated via Shibboleth");

                    HttpServletResponse hresp = (HttpServletResponse)response;

                    if(requestCache != null) {
                        String redirect = null;

                        SavedRequest savedReq = requestCache.getRequest(hreq, hresp);
                        if(savedReq != null) {
                            redirect = savedReq.getRedirectUrl();
                            Log.debug(Geonet.LOG_AUTH, "Found saved request location: " + redirect);
                        } else {
                            Log.debug(Geonet.LOG_AUTH, "No saved request found");
                        }

                        if(redirect != null) {
                            Log.info(Geonet.LOG_AUTH, "Redirecting to " + redirect);

                            // Removing original request, since we want to retain current headers.
                            // If request remains in cache, requestCacheFilter will reinstate the original headers and we don't want it.
                            requestCache.removeRequest(hreq, hresp);

                            hresp.sendRedirect(redirect);
                            return; // no further chain processing allowed
                        }
                    }


                } else {
                    Log.warning(Geonet.LOG_AUTH, "Error in GN shibboleth precedures handling user '" + username);
                }
            }
        } catch (Exception ex) {
            Log.warning(Geonet.LOG_AUTH, "Error during Shibboleth login for user " + username + ": " + ex.getMessage(), ex );
        }

		chain.doFilter(request, response);
	}


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setConfiguration(ShibbolethUserConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

}
