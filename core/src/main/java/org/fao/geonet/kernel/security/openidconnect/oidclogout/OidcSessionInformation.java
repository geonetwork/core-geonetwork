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

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/** *
 *     An OIDC session information class that extends Spring Security's SessionInformation.
      Referencing org.springframework.security.oauth2.client.oidc.session.OidcSessionInformation
 */
public class OidcSessionInformation extends SessionInformation {
    private static final long serialVersionUID = -1703808683027974918L;
    private final Map<String, String> authorities;

    public OidcSessionInformation(String sessionId, Map<String, String> authorities, OidcUser user) {
        super(user, sessionId, new Date());
        this.authorities = (Map)(authorities != null ? new LinkedHashMap(authorities) : Collections.emptyMap());
    }

    public Map<String, String> getAuthorities() {
        return this.authorities;
    }

    public OidcUser getPrincipal() {
        return (OidcUser)super.getPrincipal();
    }

    public OidcSessionInformation withSessionId(String sessionId) {
        return new OidcSessionInformation(sessionId, this.getAuthorities(), this.getPrincipal());
    }
}
