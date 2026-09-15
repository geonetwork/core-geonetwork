/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security;

import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Helper to safely redirect users to a location provided as a request parameter
 * (e.g. {@code redirectUrl} after login/logout).
 *
 * <p>A redirect target is only honoured when it points to a location that is
 * relative to the current server. Otherwise the request is redirected to the
 * application context home.</p>
 */
public final class RedirectUtil {

    private RedirectUtil() {
    }

    /**
     * Redirect the response to {@code redirectUrl} when it is a safe, server-local
     * relative location; otherwise redirect to the application context home.
     *
     * @param request     the current request, used to derive the context home.
     * @param response     the response to redirect.
     * @param redirectUrl the candidate redirect location (may be {@code null}).
     */
    public static void sendSafeRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl)
        throws IOException {
        if (isRelativeRedirect(redirectUrl)) {
            response.sendRedirect(redirectUrl);
        } else {
            if (StringUtils.isNotEmpty(redirectUrl)) {
                Log.warning(Geonet.SECURITY,
                    "Refused unsafe login redirect to '" + redirectUrl + "'. Redirected to context home instead.");
            }
            response.sendRedirect(request.getContextPath());
        }
    }

    /**
     * A redirect target is considered safe only when it is relative to the
     * current server. It must:
     * <ul>
     *     <li>not be empty,</li>
     *     <li>be a path starting with a single {@code /} (so it is anchored to the
     *     current host) but not {@code //} nor {@code /\\}, which browsers resolve
     *     as protocol-relative URLs pointing to an external host,</li>
     *     <li>not be an absolute URI (i.e. carry a scheme), and</li>
     *     <li>not declare any authority/host component.</li>
     * </ul>
     *
     * @param redirectUrl the candidate redirect location (may be {@code null}).
     * @return {@code true} if the location is a safe, server-local relative path.
     */
    public static boolean isRelativeRedirect(String redirectUrl) {
        if (StringUtils.isEmpty(redirectUrl)) {
            return false;
        }

        // Browsers may normalise backslashes to forward slashes, so account for both
        // when checking for protocol-relative URLs such as "//host" or "/\host".
        String normalized = redirectUrl.replace('\\', '/');

        // Must be anchored to the current host with a single leading slash.
        if (!normalized.startsWith("/") || normalized.startsWith("//")) {
            return false;
        }

        try {
            URI redirectUri = new URI(redirectUrl);
            // Reject absolute URLs (with a scheme) and any URL declaring a host/authority.
            if (redirectUri.isAbsolute()
                || redirectUri.getHost() != null
                || redirectUri.getAuthority() != null) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }

    /**
     * Like {@link #isRelativeRedirect(String)} but, in addition to server-local
     * relative locations, also accepts absolute URLs that point back to the
     * current site (matching host, protocol and port).
     *
     * <p>This is used where redirecting to an absolute same-site URL is a
     * legitimate feature, e.g. returning the user to the page they were on
     * before signing in.</p>
     *
     * @param redirectUrl the candidate redirect location (may be {@code null}).
     * @param siteHost     the configured site host, e.g. {@code www.example.org}.
     * @param siteProtocol the configured site protocol, e.g. {@code https}.
     * @param sitePort     the configured site port (may be {@code null}).
     * @return {@code true} if the location is safe to redirect to.
     */
    public static boolean isSafeRedirect(String redirectUrl, String siteHost, String siteProtocol, Integer sitePort) {
        if (isRelativeRedirect(redirectUrl)) {
            return true;
        }

        if (StringUtils.isEmpty(redirectUrl) || StringUtils.isEmpty(siteHost)
            || StringUtils.isEmpty(siteProtocol) || sitePort == null) {
            return false;
        }

        try {
            URL url = new URL(redirectUrl);
            int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
            return siteHost.equalsIgnoreCase(url.getHost())
                && siteProtocol.equalsIgnoreCase(url.getProtocol())
                && sitePort == port;
        } catch (MalformedURLException e) {
            // Not a valid absolute URL (this also rejects protocol-relative "//host").
            return false;
        }
    }
}
