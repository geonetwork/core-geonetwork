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
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

        if (urlPatternValue != null) {

            if (!urlPatternValue.startsWith("/")) {
                // Check the url to redirect it's from the same site, otherwise use the default target url
                URL urlPattern = null;
                try {
                    urlPattern = new URL(urlPatternValue);

                    String hostName = urlPattern.getHost();
                    String protocol = urlPattern.getProtocol();
                    int port = urlPattern.getPort() == -1 ? urlPattern.getDefaultPort() : urlPattern.getPort();

                    String siteHost = settingManager.getValue(Settings.SYSTEM_SERVER_HOST);
                    String siteProtocol = settingManager.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
                    int sitePort = settingManager.getValueAsInt(Settings.SYSTEM_SERVER_PORT);


                    if (!hostName.equalsIgnoreCase(siteHost) ||
                        !protocol.equalsIgnoreCase(siteProtocol) ||
                        port != sitePort) {
                        urlPatternValue = getDefaultTargetUrl();
                    }
                } catch (MalformedURLException e) {
                    urlPatternValue = getDefaultTargetUrl();
                }
            }
        } else {
            urlPatternValue = getDefaultTargetUrl();
        }

        return urlPatternValue.replace("@@nodeId@@", nodeInfo.getId());
    }

}
