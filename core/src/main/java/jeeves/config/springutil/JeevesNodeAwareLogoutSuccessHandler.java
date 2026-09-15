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

package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.security.RedirectUtil;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;
import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getServletContext;

/**
 * When user sign out, user is redirected to main /logout page
 * which then needs to redirect the user to the last portal accessed.
 *
 * Created by Jesse on 2/17/14.
 */
public class JeevesNodeAwareLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {
    @Autowired
    ServletContext context;

    @Autowired
    SettingManager settingManager;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        super.handle(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(getServletContext(context));

        NodeInfo nodeInfo = applicationContext.getBean(NodeInfo.class);

        String urlPatternValue = super.determineTargetUrl(request, response);

        // Only honour the requested target when it is relative to the current server
        // or an absolute URL pointing back to this same site. Otherwise fall back to
        // the configured default target URL. This also rejects protocol-relative URLs
        // such as "//evil.example.com", which would otherwise resolve to an external host.
        String siteHost = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
        String siteProtocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        Integer sitePort = settingManager.getServerPort();

        if (!RedirectUtil.isSafeRedirect(urlPatternValue, siteHost, siteProtocol, sitePort)) {
            if (StringUtils.isNotEmpty(urlPatternValue)) {
                Log.warning(Geonet.SECURITY,
                    "Refused unsafe logout redirect to '" + urlPatternValue + "'. Redirected to the default target instead.");
            }
            urlPatternValue = getDefaultTargetUrl();
        }

        return urlPatternValue.replace("@@nodeId@@", nodeInfo.getId());
    }

}
