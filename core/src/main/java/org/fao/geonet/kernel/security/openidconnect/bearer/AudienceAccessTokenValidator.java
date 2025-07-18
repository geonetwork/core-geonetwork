/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.fao.geonet.kernel.security.openidconnect.OIDCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * This checks that the token is connected to this application.  This will prevent a token for another application
 * being used by us.
 * <p>
 * NOTE: Azure AD's JWT AccessToken has a fixed "aud", no "azp", but an "appid" (should be our client id).
 * example;
 * "aud": "00000003-0000-0000-c000-000000000000",
 * "appid": "b9e8d05a-08b6-48a5-81c8-9590a0f550f3",
 * <p>
 * NOTE: Keycloak has the audience as "account", no "appid", but "azp"  should be our client id.
 * example;
 * "aud": "account",
 * "azp": "live-key",
 */
public class AudienceAccessTokenValidator implements AccessTokenValidator {


    private final String AUDIENCE_CLAIM_NAME = "aud";
    private final String APPID_CLAIM_NAME = "appid";
    private final String KEYCLOAK_AUDIENCE_CLAIM_NAME = "azp";


    @Autowired
    OIDCConfiguration oidcConfiguration;

   /**
     * "aud" must be our client id (or, if its a list, contain our client id)
     * OR "azp" must be our client id
     * OR "appid" must be our client id.
     * <p>
     * Otherwise, its a token not for us...
     *
     *  This checks that the audience of the JWT access token is us.
     *  The main attack this tries to prevent is someone getting an access token (i.e. from keycloak or azure) that
     *  was meant for another application (say a silly calendar app), and then using that token here.  The IDP provider
     *  (keycloak/azure) will validate the token as "good", but it wasn't generated for us.  This does a check of the
     *  token that OUR client ID is mentioned (not another app).
     */
    @Override
    public void verifyToken(Map claimsJWT, Map userInfoClaims) throws Exception {
        //azp from keycloak
        if ((claimsJWT.get(KEYCLOAK_AUDIENCE_CLAIM_NAME) != null)
            && claimsJWT.get(KEYCLOAK_AUDIENCE_CLAIM_NAME).equals(oidcConfiguration.getClientId())) {
            return;
        }

        if ((claimsJWT.get(APPID_CLAIM_NAME) != null)
            && claimsJWT.get(APPID_CLAIM_NAME).equals(oidcConfiguration.getClientId())) {
            return; //azure specific
        }

        //aud
        Object aud = claimsJWT.get(AUDIENCE_CLAIM_NAME);
        if (aud != null) {
            if (aud instanceof String) {
                if (((String) aud).equals(oidcConfiguration.getClientId()))
                    return;
            } else if (aud instanceof List) {
                List auds = (List) aud;
                for (Object o : auds) {
                    if ((o instanceof String) && (o.equals(oidcConfiguration.getClientId()))) {
                        return;
                    }
                }
            }
        }
        throw new Exception("JWT Bearer token - probably not meant for this application");
    }
}
