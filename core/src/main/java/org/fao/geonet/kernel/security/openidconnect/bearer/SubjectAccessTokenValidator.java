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

import java.util.Map;

/**
 * This verifies that the token is about our user (i.e. the access token and userinfo endpoint agree on who).
 *
 * for keycloak, the "sub" of the JWT and userInfo are the same.
 * for Azure AD, the "sub" of the userInfo is in the JWT "xms_st" claim.
 *  "xms_st": {
 *     "sub": "982kuI1hxIANLB__lrKejDgDnyjPnhbKLdPUF0JmOD1"
 *   },
 *
 *   The spec suggests verifying the user vs token subjects match, so this does that check.
 */
public class SubjectAccessTokenValidator implements AccessTokenValidator {

    private final String SUBJECT_CLAIM_NAME = "sub";
    private final String AZURE_SUBJECT_CONTAINER_NAME = "xms_st";


    @Override
    public void verifyToken(Map claims, Map userInfoClaims) throws Exception {
        //normal case - subjects are the same
        if  ( (claims.get(SUBJECT_CLAIM_NAME) != null) &&   (userInfoClaims.get(SUBJECT_CLAIM_NAME) != null) ) {
            if (claims.get(SUBJECT_CLAIM_NAME).equals(userInfoClaims.get(SUBJECT_CLAIM_NAME)))
                return;
        }

        //Azure AD case - use accesstoken.xms_st.sub vs userinfo.sub
        if ((claims.get(AZURE_SUBJECT_CONTAINER_NAME) != null) && (claims.get(AZURE_SUBJECT_CONTAINER_NAME) instanceof Map)) {
            Map xmls_st = (Map) claims.get(AZURE_SUBJECT_CONTAINER_NAME);
            if (xmls_st.get(SUBJECT_CLAIM_NAME) != null) {
                if (xmls_st.get(SUBJECT_CLAIM_NAME).equals(userInfoClaims.get(SUBJECT_CLAIM_NAME)))
                    return;
            }
        }
        throw new Exception("JWT Bearer token VS UserInfo - subjects don't match");
    }
}
