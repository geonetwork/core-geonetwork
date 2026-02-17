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

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Date;
import java.util.Optional;

/**
 * simple token parser that doesn't validate signature (or anything else)
 */
public class SimpleTokenParser implements AccessTokenParser {

    @Override
    public Jwt parseToken(String token) throws Exception {
        JWT jwt = JWTParser.parse(token);
        var claimsSet = jwt.getJWTClaimsSet();

        return Jwt.withTokenValue(token)
            .headers(h -> h.putAll(jwt.getHeader().toJSONObject()))
            .claims(c -> c.putAll(claimsSet.getClaims()))
            .issuedAt(Optional.ofNullable(claimsSet.getIssueTime()).map(Date::toInstant).orElse(null))
            .expiresAt(Optional.ofNullable(claimsSet.getExpirationTime()).map(Date::toInstant).orElse(null))
            .build();
    }
}
