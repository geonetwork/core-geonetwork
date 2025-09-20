/*
 * Copyright (C) 2025 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.oidclogout;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a session authentication strategy that links an OIDC user session
 * to the current HTTP session. It saves the session information in an OIDC session registry.
 *
 * Referencing org.springframework.security.config.annotation.web.configurers.oauth2.client.OidcSessionRegistryAuthenticationStrategy
 */
public final class OidcSessionRegistryAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Autowired
    private OidcSessionRegistry oidcSessionRegistry;

    private OidcSessionRegistryAuthenticationStrategy() {
    }

    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object auth = authentication.getPrincipal();
            if (auth instanceof OidcUser) {
                OidcUser user = (OidcUser)auth;
                String sessionId = session.getId();
                CsrfToken csrfToken = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
               Map<String, String> headers = csrfToken != null ? new HashMap<String, String>() {{put(csrfToken.getHeaderName(), csrfToken.getToken());
    }} : Collections.emptyMap();
                OidcSessionInformation registration = new OidcSessionInformation(sessionId, headers, user);

                Log.debug(Geonet.SECURITY, String.format("Linking a provider [%s] session to this client's session", user.getIssuer()));

                this.oidcSessionRegistry.saveSessionInformation(registration);
            }
        }
    }


}
