/*
 * Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.keycloak;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.annotation.PostConstruct;

public class KeycloakUtil {
    public static String signinPath = null;
    private static LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    @Autowired
    private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint0;

    @PostConstruct
    private void init () {
        loginUrlAuthenticationEntryPoint = this.loginUrlAuthenticationEntryPoint0;
    }

    public static String getSigninPath() {
        if (signinPath == null) {
            try {
                signinPath = loginUrlAuthenticationEntryPoint.getLoginFormUrl().split("\\?")[0];
            } catch(BeansException e) {
                // If we cannot find the bean then we will just use a default.
                Log.debug(Geonet.SECURITY, "Could not find the bean, using the default instead");
            }
            // If signinPath is null then something may have gone wrong.
            // This should generally not happen - if it does then lets set to what it currently expected and then log a warning.
            if (StringUtils.isEmpty(signinPath)) {
                signinPath = "/signin";
                Log.warning(Geonet.SECURITY,
                    "Could not detect signin path from configuration. Using /signin");
            }
        }
        return signinPath;
    }
}
