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


import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;


/**
 * trivial implementation of ClientRegistrationRepository.
 * We have EXACTLY one - so we can just hardcode it (always return it).
 */
public class GeonetworkClientRegistrationRepository implements ClientRegistrationRepository {

    ClientRegistration clientRegistration;


    public GeonetworkClientRegistrationRepository(GeonetworkClientRegistrationProvider clientRegistrationProvider) throws Exception {
        if (clientRegistrationProvider == null)
            throw new Exception("clientRegistration must not be null!");
        clientRegistration = clientRegistrationProvider.getClientRegistration();
    }


    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return clientRegistration;
    }
}
