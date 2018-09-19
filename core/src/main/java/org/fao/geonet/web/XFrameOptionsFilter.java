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

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_CORS_ALLOWEDHOSTS;

/**
 * Filter to avoid clickjaking attacks.
 *
 * See https://www.owasp.org/index.php/Clickjacking_Defense_Cheat_Sheet.
 *
 * Modes supported:
 *  - DENY: prevents any domain from framing the content.
 *  - SAMEORIGIN, which only allows the current site to frame the content.
 *  - ALLOW-FROM uri, which permits the specified 'uri' to frame this page.
 *      Not all browsers support this mode.
 *      Use ',' to separate multiple uri
 *      Use 'db' to use the host list defined for the CORS headers
 *
 * Any other value will default to DENY.
 *
 * Sets X-Frame-Options and Content-Security-Policy (for frame-ancestors) headers.
 *
 * @author Jose García
 */
public class XFrameOptionsFilter implements Filter {
    private static String MODE_DENY = "DENY";
    private static String MODE_SAMEORIGIN = "SAMEORIGIN";
    private static String MODE_ALLOWFROM = "ALLOW-FROM";
    private static String MODE_ALLOWALL = "ALLOWALL";

    private String mode;
    private String urls;
    private List<String> domains;
    private boolean isUsingDb = false;

    public void init(FilterConfig filterConfig) throws ServletException {
        mode = filterConfig.getInitParameter("mode");
        urls = filterConfig.getInitParameter("url");

        // Mode: DENY, SAMEORIGIN, ALLOW-FROM. Any other value will default to SAMEORIGIN
        if (!mode.equals(MODE_DENY) && !mode.equals(MODE_SAMEORIGIN) && !mode.equals(MODE_ALLOWFROM)) {
            mode = MODE_DENY;
        }

        // If ALLOW-FROM, make sure a valid url is given, otherwise fallback to deny
        if (mode.equals(MODE_ALLOWFROM)) {
            if (StringUtils.isEmpty(urls)) {
                Log.info(Geonet.GEONETWORK,
                    "XFrameOptions filter url parameter is missing for mode ALLOW-FROM. Setting mode to DENY.");
                mode = MODE_DENY;
            } else {
                if(urls.equals("db")) {
                    isUsingDb = true;
                }
                else {
                    domains = this.getDomains(urls);
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.GEONETWORK)) {
            Log.debug(Geonet.GEONETWORK, String.format(
                "XFrameOptions filter initialized. Using mode %s.", mode));
        }

    }


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (isUsingDb) {
            SettingManager settingManager =
                    ApplicationContextHolder.get().getBean(SettingManager.class);
            String allowedHosts = settingManager.getValue(SYSTEM_CORS_ALLOWEDHOSTS);
            if (allowedHosts != null) {
                if (allowedHosts.equals("*")) {
                    mode = MODE_ALLOWALL;
                } else if (!allowedHosts.equals("")) {
                    mode = MODE_ALLOWFROM;
                    domains = Arrays.asList(allowedHosts.split(","));
                }
            }
        }

        if (mode.equals(MODE_ALLOWFROM)) {
            domains.forEach(domain -> {
                response.addHeader("X-Frame-Options", mode + " " + domain);
            });
        } else {
            response.addHeader("X-Frame-Options", mode);
        }

        response.addHeader("Content-Security-Policy", getContentSecurityPolicyFramAncestorsValue());

        filterChain.doFilter(servletRequest, response);
    }


    public void destroy() {
    }

    /**
     * Calculates the Content-Security-Policy header frame-ancestors value.
     *
     * @return Content-Security-Policy header frame-ancestors value.
     */
    private String getContentSecurityPolicyFramAncestorsValue() {
        if (mode.equals(MODE_SAMEORIGIN)) {
            return "frame-ancestors 'self'";
        } else if (mode.equals(MODE_ALLOWFROM)) {
            return "frame-ancestors " + String.join(" ", domains);
        } else {
            return "frame-ancestors 'none'";
        }
    }

    private List<String> getDomains(String urls) {
        return Arrays.stream(urls.split(",")).map(url -> getDomainFromUrl(url)).collect(Collectors.toList());
    }

    private String getDomainFromUrl(String url) {
        try {
            URL urlValue = new URL(url);
            return urlValue.getHost() + ((urlValue.getPort() == -1) ? "" : ":" + urlValue.getPort());
        }
        catch (MalformedURLException ex) {
            Log.info(Geonet.GEONETWORK, String.format(
                    "XFrameOptions filter url parameter (%s) is not valid for mode ALLOW-FROM. Setting mode to DENY.", url));
            mode = MODE_DENY;
        }
        return null;
    }

}
