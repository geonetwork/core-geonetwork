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
package org.fao.geonet.kernel.security.openidconnect;

import org.junit.Test;

import static org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProviderTest.keycloakConfig;
import static org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProviderTest.string2InputStream;
import static org.junit.Assert.assertEquals;

/**
 * tests the GeonetworkClientRegistrationRepository
 */
public class GeonetworkClientRegistrationRepositoryTest {

    //tests that you can get the configured RegistrationProvider and it has the correct name
    @Test
    public void testRepo() throws Exception {
        OIDCConfiguration configuration = new OIDCConfiguration();
        configuration.setClientId("clientid");
        configuration.setClientSecret("clientsecret");
        configuration.setScopes("");//use all scopes
        GeonetworkClientRegistrationProvider clientRegistrationProvider = new GeonetworkClientRegistrationProvider(
            string2InputStream(keycloakConfig),
            configuration
        );

        GeonetworkClientRegistrationRepository out = new GeonetworkClientRegistrationRepository(clientRegistrationProvider);

        assertEquals(clientRegistrationProvider.getClientRegistration(),
            out.findByRegistrationId(GeonetworkClientRegistrationProvider.CLIENTREGISTRATION_NAME));
    }
}
