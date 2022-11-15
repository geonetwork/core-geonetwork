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

import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reads from the Open ID server's JSON configuration and creates a spring-security ClientRegistration.
 * This defines how to communicate with the remote oauth2 open id connect server.
 * <p>
 * This is based on the Spring ClientRegistration and ClientRegistrations code.
 * However, most of that code is private, so its brought here for use.
 */
public class GeonetworkClientRegistrationProvider {

    public static String CLIENTREGISTRATION_NAME = "geonetwork-oicd";

    ClientRegistration clientRegistration;

    OIDCConfiguration oidcConfiguration;

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
    public GeonetworkClientRegistrationProvider(Resource metadataResource,
                                                String serverMetadataJsonText,
                                                String oidcMetadataConfigURL,
                                                OIDCConfiguration oidcConfiguration) throws IOException, ParseException {
        this.oidcConfiguration = oidcConfiguration;
        String clientId = oidcConfiguration.clientId;
        String clientSecret = oidcConfiguration.clientSecret;
        if ( (oidcMetadataConfigURL !=null) && (oidcMetadataConfigURL.toLowerCase().startsWith("http")) ) {
            //they provided a URL to the JSON - lets get it and use that
            Log.debug(Geonet.SECURITY,"Download OIDC Configuration from -"+oidcMetadataConfigURL);
            URL url = new URL(oidcMetadataConfigURL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            try {
                clientRegistration = createClientRegistration(http.getInputStream(), clientId, clientSecret);
            }
            finally {
                http.disconnect();
            }
        }
        //50 is just to check if there's some text in the string (not just the name of the environment var)
        else if (!StringUtils.isBlank(serverMetadataJsonText) && (serverMetadataJsonText.trim().length() > 50)) {
            Log.debug(Geonet.SECURITY, "OpenID Connect - using IDP server metadata config from text");
            clientRegistration = createClientRegistration(new ByteArrayInputStream(serverMetadataJsonText.getBytes()), clientId, clientSecret);
        }
        else {
            Log.debug(Geonet.SECURITY, "OpenID Connect - using IDP server metadata config from resource file");
            clientRegistration = createClientRegistration(metadataResource, clientId, clientSecret);
        }
    }

    //get the JSON from an inputstream (i.e. from a file, string, or resource)
    public GeonetworkClientRegistrationProvider(InputStream inputStream,
                                                OIDCConfiguration oidcConfiguration) throws IOException, ParseException {
        this.oidcConfiguration = oidcConfiguration;
        this.oidcConfiguration = oidcConfiguration;
        String clientId = oidcConfiguration.clientId;
        String clientSecret = oidcConfiguration.clientSecret;
        clientRegistration = createClientRegistration(inputStream, clientId, clientSecret);
    }

    /**
     * given a resource, read its content and return it as a string
     */
    public static String resourceToString(Resource resource) throws IOException {
        return inputStreamToString(resource.getInputStream());
    }

    /**
     * given an inputstream, read its content and return it as a string
     */
    static String inputStreamToString(InputStream inputStream) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw e;
        }
    }

    // Finds the "best" authentication method (usually BASIC)
    //taken from spring's ClientRegistrations#getClientAuthenticationMethod which is private
    private static ClientAuthenticationMethod getClientAuthenticationMethod(String issuer,
                                                                            List<com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod> metadataAuthMethods) {
        if (metadataAuthMethods == null || metadataAuthMethods.contains(com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)) {
            // If null, the default includes client_secret_basic
            return ClientAuthenticationMethod.BASIC;
        }
        if (metadataAuthMethods.contains(com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod.CLIENT_SECRET_POST)) {
            return ClientAuthenticationMethod.POST;
        }
        if (metadataAuthMethods.contains(com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod.NONE)) {
            return ClientAuthenticationMethod.NONE;
        }
        throw new IllegalArgumentException("Only ClientAuthenticationMethod.BASIC, ClientAuthenticationMethod.POST and "
            + "ClientAuthenticationMethod.NONE are supported. The issuer \"" + issuer + "\" returned a configuration of " + metadataAuthMethods);
    }

    // create the ClientRegistration from the input stream. Use "inputstream" as issuer (not used anywhere).
    // assumes oidcConfiguration is already set
    ClientRegistration createClientRegistration(InputStream inputStream,
                                                String clientId,
                                                String clientSecret) throws IOException, ParseException {
        return createClientRegistration(inputStreamToString(inputStream), "inputstream", clientId, clientSecret);
    }

    // create the ClientRegistration from the input stream. Uses the filename as issuer (not used anywhere).
    // assumes oidcConfiguration is already set
    ClientRegistration createClientRegistration(Resource metadataResource,
                                                String clientId,
                                                String clientSecret) throws IOException, ParseException {
        return createClientRegistration(resourceToString(metadataResource), metadataResource.getFilename(), clientId, clientSecret);
    }

    // returns all the scopes in the server's configuration.
    //taken from spring's ClientRegistrations#getScopes which is private
    //Added: limit scopes using an allow-list (null = allow all).
    private List<String> getScopes(AuthorizationServerMetadata metadata) {
        Scope scope = metadata.getScopes();
        List<String> scopes;
        if (scope == null) {
            // If null, default to "openid" which must be supported
            scopes = Collections.singletonList(OidcScopes.OPENID);
        } else {
            scopes = scope.toStringList();
        }

        // we set the scopes to what the user requested - independent of what the server says the allowed scopes are
        if (oidcConfiguration.getScopeSet() != null) {
            scopes = new ArrayList<>(oidcConfiguration.getScopeSet());
            if (!scopes.contains(OidcScopes.OPENID))
                scopes.add(OidcScopes.OPENID);
        }
        return scopes;
    }

    /**
     * @return the actual ClientRegistration
     */
    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }


    /**
     * creates a ClientRegistration by reading the standard configuration json (from the IDP server).
     * Also requires the client ID (from server) and client secret (from server).
     * <p>
     * this uses the oidcConfiguration to limit requested scopes.
     * <p>
     * Most of this code is from Spring.
     *
     * @param clientId
     * @param clientSecret
     * @return
     * @throws IOException
     * @throws ParseException
     */
    ClientRegistration createClientRegistration(String jsonServerConfig,
                                                String fname,
                                                String clientId,
                                                String clientSecret) throws IOException, ParseException {

        String json = jsonServerConfig;
        String issuer = "issuer: " + fname;


        //from spring ClientRegistrations#withProviderConfiguration
        OIDCProviderMetadata oidcMetadata = OIDCProviderMetadata.parse(json);
        ClientAuthenticationMethod method = getClientAuthenticationMethod(issuer, oidcMetadata.getTokenEndpointAuthMethods());
        List<GrantType> grantTypes = oidcMetadata.getGrantTypes();
        // If null, the default includes authorization_code
        if (grantTypes != null && !grantTypes.contains(GrantType.AUTHORIZATION_CODE)) {
            throw new IllegalArgumentException("Only AuthorizationGrantType.AUTHORIZATION_CODE is supported. The issuer \"" + issuer +
                "\" returned a configuration of " + grantTypes);
        }
        List<String> scopes = getScopes(oidcMetadata);


        Map<String, Object> configurationMetadata = new LinkedHashMap<>(oidcMetadata.toJSONObject());

        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(CLIENTREGISTRATION_NAME)
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .scope(scopes)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientAuthenticationMethod(method)
            .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .authorizationUri(oidcMetadata.getAuthorizationEndpointURI().toASCIIString())
            .providerConfigurationMetadata(configurationMetadata)
            .tokenUri(oidcMetadata.getTokenEndpointURI().toASCIIString())
            .clientName(issuer);

        builder.jwkSetUri(oidcMetadata.getJWKSetURI().toASCIIString());
        if (oidcMetadata.getUserInfoEndpointURI() != null) {
            builder.userInfoUri(oidcMetadata.getUserInfoEndpointURI().toASCIIString());
        }

        builder.clientId(clientId)
            .clientSecret(clientSecret)
            .clientName("geonetwork via spring security");

        ClientRegistration clientRegistration = builder.build();
        return clientRegistration;
    }

}
