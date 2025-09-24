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

package org.fao.geonet.kernel.security.openidconnect.bearer;

import com.nimbusds.oauth2.sdk.ParseException;
import org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProvider;
import org.fao.geonet.kernel.security.openidconnect.OIDCConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.IOException;
import java.io.InputStream;


/**
 * Reads from the Open ID server's JSON configuration and creates a spring-security ClientRegistration.
 * This defines how to communicate with the remote oauth2 open id connect server.
 * <p>
 * This extended from GeonetworkClientRegistrationProvider but only changes the registered name and grant type to CLIENT_CREDENTIALS which is used for service account login.
 */

public class GeonetworkClientServiceAccountRegistrationProvider extends GeonetworkClientRegistrationProvider {

    public static String CLIENT_SERVICE_ACCOUNT_REGISTRATION_NAME = "geonetwork-oidc-service-account";

    private static final AuthorizationGrantType GRANT_TYPE = AuthorizationGrantType.CLIENT_CREDENTIALS;

    /**
     * Create a spring ClientRegistration from either a Resource (i.e. file containing the JSON) or from
     * a string containing the JSON text.
     * <p>
     * It also requires the server's clientid and clientsecret.
     * <p>
     * <p>
     * NOTE: either set metadataResource OR serverMetadataJsonText.  If both are set then serverMetadataJsonText is used.
     * Allowing both makes the spring.xml configuration simpler.
     *
     * @param metadataResource       - reference to a file (spring will convert a fname to Resource for us)
     * @param serverMetadataJsonText - text of JSON file
     * @param oidcMetadataConfigURL  - URL to the OIDC configuration JSON document (/.well-known/openid-configuration)
     * @param oidcConfiguration      - GN's oidc configuration
     * @throws IOException
     * @throws ParseException
     */
    public GeonetworkClientServiceAccountRegistrationProvider(Resource metadataResource,
                                                              String serverMetadataJsonText,
                                                              String oidcMetadataConfigURL,
                                                              OIDCConfiguration oidcConfiguration) throws IOException, ParseException {
        super(metadataResource, serverMetadataJsonText, oidcMetadataConfigURL, oidcConfiguration);
    }

    //get the JSON from an inputstream (i.e. from a file, string, or resource)
    public GeonetworkClientServiceAccountRegistrationProvider(InputStream inputStream,
                                                              OIDCConfiguration oidcConfiguration) throws IOException, ParseException {
        super(inputStream, oidcConfiguration);
    }

    @Override
    protected OIDCConfiguration.ClientConfig getClientConfig(OIDCConfiguration oidcConfiguration) {
        return oidcConfiguration.getServiceAccountConfig();
    };

    @Override
    protected String getClientRegistrationName() {
        return CLIENT_SERVICE_ACCOUNT_REGISTRATION_NAME;
    };

    @Override
    protected AuthorizationGrantType getAuthorizationGrantType() {
        return GRANT_TYPE;
    };
}
