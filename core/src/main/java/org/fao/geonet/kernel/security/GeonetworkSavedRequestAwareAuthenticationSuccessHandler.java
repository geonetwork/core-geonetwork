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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link SavedRequestAwareAuthenticationSuccessHandler} that restricts the
 * post-login redirect supplied through the {@code targetUrlParameter}
 * (e.g. {@code redirectUrl}) to safe destinations.
 *
 * <p>A destination is honoured when it is relative to the current server or an
 * absolute URL pointing back to this same site. Any other target (a cross-site
 * absolute URL or a protocol-relative URL such as {@code //evil.example.com})
 * falls back to the configured default target URL.</p>
 */
public class GeonetworkSavedRequestAwareAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private SettingManager settingManager;

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = super.determineTargetUrl(request, response);

        String siteHost = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
        String siteProtocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        Integer sitePort = settingManager.getServerPort();

        if (RedirectUtil.isSafeRedirect(targetUrl, siteHost, siteProtocol, sitePort)) {
            return targetUrl;
        }

        Log.warning(Geonet.SECURITY,
            "Refused unsafe login redirect to '" + targetUrl + "'. Redirected to the default target instead.");
        return getDefaultTargetUrl();
    }
}
