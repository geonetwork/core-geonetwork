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

    @Override
    public void verifyToken(Map claims, Map userInfoClaims) throws Exception {
        //normal case - subjects are the same
        if  ( (claims.get("sub") != null) &&   (userInfoClaims.get("sub") != null) ) {
            if (claims.get("sub").equals(userInfoClaims.get("sub")))
                return;
        }

        //Azure AD case - use accesstoken.xms_st.sub vs userinfo.sub
        if ((claims.get("xms_st") != null) && (claims.get("xms_st") instanceof Map)) {
            Map xmls_st = (Map) claims.get("xms_st");
            if (xmls_st.get("sub") != null) {
                if (xmls_st.get("sub").equals(userInfoClaims.get("sub")))
                    return;
            }
        }
        throw new Exception("JWT Bearer token VS UserInfo - subjects dont match");
    }
}
