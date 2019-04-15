/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
package org.fao.geonet.web;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_CORS_ALLOWEDHOSTS;

/**
 * Add CORS header on a list of allowed hosts.
 *
 * List of comma separated values of hosts for which
 * CORS headers are added.
 * Use '*' for all.
 * Use '' for none.
 * Use 'db' for using database settings. This mode
 * allows to change at run time the list of hosts. It
 * may be a bit slower as it requires to check for every
 * request the list of hosts (but JPA cache db queries).
 */
public class CORSResponseFilter
    implements Filter {

    static final String ALLOWED_HOSTS = "allowedHosts";
    private List<String> allowedRemoteHosts = new ArrayList<>();
    private boolean isUsingDb = false;
    private boolean addHeaderForAllHosts = false;

    public void init(FilterConfig config) {
        String allowedHosts = config.getInitParameter(ALLOWED_HOSTS);
        if (allowedHosts != null) {
            if (allowedHosts.equals("db")) {
                isUsingDb = true;
            } else if (allowedHosts.equals("*")) {
                addHeaderForAllHosts = true;
            } else if (!allowedHosts.equals("")) {
                allowedRemoteHosts = Arrays.asList(allowedHosts.split(","));
            }
        }
        if (Log.isDebugEnabled(Geonet.CORS)) {
            Log.debug(Geonet.CORS, String.format(
                "CORS filter initialized. Using db setting '%s': %s. Add header for all: %s. Allowed hosts: %s.",
                SYSTEM_CORS_ALLOWEDHOSTS, isUsingDb,
                addHeaderForAllHosts,
                allowedRemoteHosts
            ));
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        SettingManager settingManager =
            ApplicationContextHolder.get().getBean(SettingManager.class);

        if (isUsingDb) {
            String allowedHosts = settingManager.getValue(SYSTEM_CORS_ALLOWEDHOSTS);
            if (allowedHosts != null) {
                if (allowedHosts.equals("*")) {
                    addHeaderForAllHosts = true;
                } else if (!allowedHosts.equals("")) {
                    addHeaderForAllHosts = false;
                    allowedRemoteHosts = Arrays.asList(allowedHosts.split(","));
                }
            }
        }

        if (addHeaderForAllHosts || allowedRemoteHosts.size() > 0) {
            String clientOriginUrl = httpRequest.getHeader("origin");
            if (clientOriginUrl != null) {
                try {
                    String clientOriginHost = new java.net.URI(clientOriginUrl).getHost();
                    String myHost = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
                    if (addHeaderForAllHosts ||
                        allowedRemoteHosts.indexOf(clientOriginHost) != -1 ||
                        clientOriginHost.equals(myHost)) {
                        //httpResponse.setHeader("Access-Control-Allow-Origin", clientOriginUrl);
                        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
                        httpResponse.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
                        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                        httpResponse.setHeader("Vary", "Origin");
                    }
                } catch (Exception e) {
                    Log.warning(Geonet.CORS, String.format(
                        "An error occurs (%s) while getting the host of the origin header (ie. '%s') of the incoming request. CORS header not added.",
                        e.getMessage(),
                        clientOriginUrl));
                }
            }
        }

        chain.doFilter(request, httpResponse);
    }

    public synchronized void destroy() {
    }
}
