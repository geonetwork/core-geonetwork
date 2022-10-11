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

import com.nimbusds.jose.util.Base64URL;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * factory for creating a JwtDecoder based on an RSA "e" and "n" value.
 * NOTE: this doesn't work for MS Azure AD JWT tokens, but does work for keycloak tokens.
 */
public class JwtDecoderFactory {

    //taken from nimbus-jose
    public JwtDecoder createJwtDecoder(String nStr, String eStr) throws Exception {
        // this is for people who don't want to use Bearer tokens - don't cause errors if we don't have to
        if (nStr == null) {
            Log.warning(Geonet.SECURITY, "OpenID Connect - Bearer Token - public key - null 'n' value.");
            // will throw, below
        }
        if (eStr == null) {
            Log.warning(Geonet.SECURITY, "OpenID Connect - Bearer Token - public key - null 'e' value.");
            // will throw, below
        }

        Base64URL n = new Base64URL(nStr);
        Base64URL e = new Base64URL(eStr);

        BigInteger modulus = n.decodeToBigInteger();
        BigInteger exponent = e.decodeToBigInteger();

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(spec);
        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        return decoder;
    }
}
