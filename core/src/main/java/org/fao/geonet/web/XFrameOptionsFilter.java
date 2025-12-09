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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

    private String mode;
    private String url;
    private String domain;

    public void init(FilterConfig filterConfig) throws ServletException {
        mode = filterConfig.getInitParameter("mode");
        url = filterConfig.getInitParameter("url");

        // Mode: DENY, SAMEORIGIN, ALLOW-FROM. Any other value will default to SAMEORIGIN
        if (!mode.equals(MODE_DENY) && !mode.equals(MODE_SAMEORIGIN) && !mode.equals(MODE_ALLOWFROM)) {
            mode = MODE_DENY;
        }

        // If ALLOW-FROM, make sure a valid url is given, otherwise fallback to deny
        if (mode.equals(MODE_ALLOWFROM)) {
            if (StringUtils.isEmpty(url)) {
                Log.info(Geonet.GEONETWORK,
                    "XFrameOptions filter url parameter is missing for mode ALLOW-FROM. Setting mode to DENY.");
                mode = MODE_DENY;
            } else {
                try {
                    URL urlValue = new URL(url);
                    domain = urlValue.getHost() +
                            ((urlValue.getPort() == -1) ? "" : ":" + urlValue.getPort());

                } catch (MalformedURLException ex) {
                    Log.info(Geonet.GEONETWORK, String.format(
                            "XFrameOptions filter url parameter (%s) is not valid for mode ALLOW-FROM. Setting mode to DENY.", url));
                    mode = MODE_DENY;
                }
            }
        }

        if (Log.isDebugEnabled(Geonet.GEONETWORK)) {
            Log.debug(Geonet.GEONETWORK, String.format(
                "XFrameOptions filter initialized. Using mode %s.", getXFrameOptionsValue()));
        }

    }


    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("X-Frame-Options", getXFrameOptionsValue());
        response.addHeader("Content-Security-Policy", getContentSecurityPolicyFramAncestorsValue());

        filterChain.doFilter(servletRequest, response);
    }


    public void destroy() {
    }


    /**
     * Calculates the X-Frame-Options header value.
     *
     * @return X-Frame-Options header value.
     */
    private String getXFrameOptionsValue() {
        if (mode.equals(MODE_ALLOWFROM)) {
            return mode + " " + url;
        } else {
            return mode;
        }
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
            return "frame-ancestors " + domain;
        } else {
            return "frame-ancestors 'none'";
        }
    }

}
