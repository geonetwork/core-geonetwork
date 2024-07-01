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
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AudienceAccessTokenValidatorTest {


    String clientId = "MYCLIENTID";


    public AudienceAccessTokenValidator getValidator() {
        AudienceAccessTokenValidator validator = new AudienceAccessTokenValidator();
        validator.oidcConfiguration = new OIDCConfiguration();
        validator.oidcConfiguration.setClientId(clientId);
        return validator;
    }

    @Test
    public void testContainsGood() {
        assertTrue(AudienceAccessTokenValidator.contains("dave", "dave"));
        assertTrue(AudienceAccessTokenValidator.contains(Arrays.asList("dave"), "dave"));
        assertTrue(AudienceAccessTokenValidator.contains(Arrays.asList("dave", "ted"), "dave"));
        assertTrue(AudienceAccessTokenValidator.contains(Arrays.asList("ted", "dave"), "dave"));
    }


    @Test
    public void testContainsBad() {
        assertFalse(AudienceAccessTokenValidator.contains("ted", "dave"));
        assertFalse(AudienceAccessTokenValidator.contains("", "dave"));
        assertFalse(AudienceAccessTokenValidator.contains(Arrays.asList(), "dave"));
        assertFalse(AudienceAccessTokenValidator.contains(Arrays.asList("ted"), "dave"));
        assertFalse(AudienceAccessTokenValidator.contains(Arrays.asList("pam", "ted"), "dave"));
    }


    @Test
    public void testAzureGood() throws Exception {
        Map claims = new HashMap();
        claims.put("appid", clientId);

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }

    @Test
    public void testKeyCloakGood() throws Exception {
        Map claims = new HashMap();
        claims.put("azp", clientId);

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }

    @Test
    public void testSelfCreatedGood() throws Exception {
        Map claims = new HashMap();
        claims.put("aud", clientId);

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }

    @Test(expected = Exception.class)
    public void testBad1() throws Exception {
        Map claims = new HashMap();
        claims.put("aud", "badaud");

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }

    @Test(expected = Exception.class)
    public void testBad2() throws Exception {
        Map claims = new HashMap();
        claims.put("azp", "badaud");

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }


    @Test(expected = Exception.class)
    public void testBad3() throws Exception {
        Map claims = new HashMap();
        claims.put("appid", "badaud");

        AudienceAccessTokenValidator validator = getValidator();
        validator.verifyToken(claims, null);
    }
}
