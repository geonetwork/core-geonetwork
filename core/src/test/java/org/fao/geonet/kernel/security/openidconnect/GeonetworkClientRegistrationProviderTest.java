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
package org.fao.geonet.kernel.security.openidconnect;

import org.junit.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.fao.geonet.kernel.security.openidconnect.GeonetworkClientRegistrationProvider.CLIENT_REGISTRATION_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Simple test cases that verify that we can parse some different oauth2 openid connect server metadata configuration documents.
 * We test with a keycloak-based JSON, and an Azure AD base JSON.
 * We verify that it is parseable, and it is correct.
 */
public class GeonetworkClientRegistrationProviderTest {

    /**
     * this is an example keycloak JSON configuration - a localhost:8080 with realm=demo
     */
    public static String keycloakConfig = "{\n" +
        "  \"issuer\":\"http://localhost:8080/realms/demo\",\n" +
        "  \"authorization_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/auth\",\n" +
        "  \"token_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/token\",\n" +
        "  \"introspection_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/token/introspect\",\n" +
        "  \"userinfo_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/userinfo\",\n" +
        "  \"end_session_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/logout\",\n" +
        "  \"frontchannel_logout_session_supported\":true,\n" +
        "  \"frontchannel_logout_supported\":true,\n" +
        "  \"jwks_uri\":\"http://localhost:8080/realms/demo/protocol/openid-connect/certs\",\n" +
        "  \"check_session_iframe\":\"http://localhost:8080/realms/demo/protocol/openid-connect/login-status-iframe.html\",\n" +
        "  \"grant_types_supported\":[\"authorization_code\",\"implicit\",\"refresh_token\",\"password\",\"client_credentials\",\"urn:ietf:params:oauth:grant-type:device_code\",\"urn:openid:params:grant-type:ciba\"],\n" +
        "  \"response_types_supported\":[\"code\",\"none\",\"id_token\",\"token\",\"id_token token\",\"code id_token\",\"code token\",\"code id_token token\"],\n" +
        "  \"subject_types_supported\":[\"public\",\"pairwise\"],\n" +
        "  \"id_token_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"id_token_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"],\n" +
        "  \"id_token_encryption_enc_values_supported\":[\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\"],\n" +
        "  \"userinfo_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\",\"none\"],\n" +
        "  \"request_object_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\",\"none\"],\n" +
        "  \"request_object_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"],\n" +
        "  \"request_object_encryption_enc_values_supported\":[\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\"],\n" +
        "  \"response_modes_supported\":[\"query\",\"fragment\",\"form_post\",\"query.jwt\",\"fragment.jwt\",\"form_post.jwt\",\"jwt\"],\n" +
        "  \"registration_endpoint\":\"http://localhost:8080/realms/demo/clients-registrations/openid-connect\",\n" +
        "  \"token_endpoint_auth_methods_supported\":[\"private_key_jwt\",\"client_secret_basic\",\"client_secret_post\",\"tls_client_auth\",\"client_secret_jwt\"],\n" +
        "  \"token_endpoint_auth_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"introspection_endpoint_auth_methods_supported\":[\"private_key_jwt\",\"client_secret_basic\",\"client_secret_post\",\"tls_client_auth\",\"client_secret_jwt\"],\n" +
        "  \"introspection_endpoint_auth_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"authorization_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"authorization_encryption_alg_values_supported\":[\"RSA-OAEP\",\"RSA-OAEP-256\",\"RSA1_5\"],\n" +
        "  \"authorization_encryption_enc_values_supported\":[\"A256GCM\",\"A192GCM\",\"A128GCM\",\"A128CBC-HS256\",\"A192CBC-HS384\",\"A256CBC-HS512\"],\n" +
        "  \"claims_supported\":[\"aud\",\"sub\",\"iss\",\"auth_time\",\"name\",\"given_name\",\"family_name\",\"preferred_username\",\"email\",\"acr\"],\n" +
        "  \"claim_types_supported\":[\"normal\"],\n" +
        "  \"claims_parameter_supported\":true,\n" +
        "  \"scopes_supported\":[\"openid\",\"phone\",\"roles\",\"microprofile-jwt\",\"offline_access\",\"email\",\"web-origins\",\"profile\",\"address\"],\n" +
        "  \"request_parameter_supported\":true,\n" +
        "  \"request_uri_parameter_supported\":true,\n" +
        "  \"require_request_uri_registration\":true,\n" +
        "  \"code_challenge_methods_supported\":[\"plain\",\"S256\"],\n" +
        "  \"tls_client_certificate_bound_access_tokens\":true,\n" +
        "  \"revocation_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/revoke\",\n" +
        "  \"revocation_endpoint_auth_methods_supported\":[\"private_key_jwt\",\"client_secret_basic\",\"client_secret_post\",\"tls_client_auth\",\"client_secret_jwt\"],\n" +
        "  \"revocation_endpoint_auth_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"HS256\",\"HS512\",\"ES256\",\"RS256\",\"HS384\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"backchannel_logout_supported\":true,\n" +
        "  \"backchannel_logout_session_supported\":true,\n" +
        "  \"device_authorization_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/auth/device\",\n" +
        "  \"backchannel_token_delivery_modes_supported\":[\"poll\",\"ping\"],\n" +
        "  \"backchannel_authentication_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/ext/ciba/auth\",\n" +
        "  \"backchannel_authentication_request_signing_alg_values_supported\":[\"PS384\",\"ES384\",\"RS384\",\"ES256\",\"RS256\",\"ES512\",\"PS256\",\"PS512\",\"RS512\"],\n" +
        "  \"require_pushed_authorization_requests\":false,\n" +
        "  \"pushed_authorization_request_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/ext/par/request\",\n" +
        "  \"mtls_endpoint_aliases\":\n" +
        "    {\n" +
        "      \"token_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/token\",\n" +
        "      \"revocation_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/revoke\",\n" +
        "      \"introspection_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/token/introspect\",\n" +
        "      \"device_authorization_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/auth/device\",\n" +
        "      \"registration_endpoint\":\"http://localhost:8080/realms/demo/clients-registrations/openid-connect\",\n" +
        "      \"userinfo_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/userinfo\",\n" +
        "      \"pushed_authorization_request_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/ext/par/request\",\n" +
        "      \"backchannel_authentication_endpoint\":\"http://localhost:8080/realms/demo/protocol/openid-connect/ext/ciba/auth\"\n" +
        "    }\n" +
        "}\n";

    /**
     * a Azure AD JSON configuration (with tenant ID = 00000000-0000-0000-0000-000000000000)
     */
    public static String azureADConfig = "{\"token_endpoint\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/token\",\"token_endpoint_auth_methods_supported\":[\"client_secret_post\",\"private_key_jwt\",\"client_secret_basic\"],\"jwks_uri\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/discovery/v2.0/keys\",\"response_modes_supported\":[\"query\",\"fragment\",\"form_post\"],\"subject_types_supported\":[\"pairwise\"],\"id_token_signing_alg_values_supported\":[\"RS256\"],\"response_types_supported\":[\"code\",\"id_token\",\"code id_token\",\"id_token token\"],\"scopes_supported\":[\"openid\",\"profile\",\"email\",\"offline_access\"],\"issuer\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/v2.0\",\"request_uri_parameter_supported\":false,\"userinfo_endpoint\":\"https://graph.microsoft.com/oidc/userinfo\",\"authorization_endpoint\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/authorize\",\"device_authorization_endpoint\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/devicecode\",\"http_logout_supported\":true,\"frontchannel_logout_supported\":true,\"end_session_endpoint\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/logout\",\"claims_supported\":[\"sub\",\"iss\",\"cloud_instance_name\",\"cloud_instance_host_name\",\"cloud_graph_host_name\",\"msgraph_host\",\"aud\",\"exp\",\"iat\",\"auth_time\",\"acr\",\"nonce\",\"preferred_username\",\"name\",\"tid\",\"ver\",\"at_hash\",\"c_hash\",\"email\"],\"kerberos_endpoint\":\"https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/kerberos\",\"tenant_region_scope\":\"EU\",\"cloud_instance_name\":\"microsoftonline.com\",\"cloud_graph_host_name\":\"graph.windows.net\",\"msgraph_host\":\"graph.microsoft.com\",\"rbac_url\":\"https://pas.windows.net\"}";

    //converts a string into an inputstream
    public static InputStream string2InputStream(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

    //Tests that all scopes are being used
    //  configuration.setScopes("") -> use all from server
    @Test
    public void testScopes_all() throws Exception {
        OIDCConfiguration configuration = new OIDCConfiguration();
        configuration.getClientConfig().setClientId("clientid");
        configuration.getClientConfig().setClientSecret("clientsecret");
        configuration.getClientConfig().setScopes("");//use all scopes from server
        GeonetworkClientRegistrationProvider out = new GeonetworkClientRegistrationProvider(
            string2InputStream(keycloakConfig),
            configuration
        );

        assertNotNull(out.getClientRegistration());
        assertEquals(9, out.getClientRegistration().getScopes().size());
    }

    // test to limit scope usage to openid and email
    @Test
    public void testScopes_limited() throws Exception {
        OIDCConfiguration configuration = new OIDCConfiguration();
        configuration.getClientConfig().setClientId("clientid");
        configuration.getClientConfig().setClientSecret("clientsecret");
        configuration.getClientConfig().setScopes("openid email");//use 2 scopes
        GeonetworkClientRegistrationProvider out = new GeonetworkClientRegistrationProvider(
            string2InputStream(keycloakConfig),
            configuration
        );

        assertNotNull(out.getClientRegistration());
        assertEquals(2, out.getClientRegistration().getScopes().size());
        assertTrue(out.getClientRegistration().getScopes().contains("openid"));
        assertTrue(out.getClientRegistration().getScopes().contains("email"));
    }

    //tests parsing of a keycloak JSON configuration
    @Test
    public void testParsingKeycloakConfigurationMetadataJson() throws Exception {
        OIDCConfiguration configuration = new OIDCConfiguration();
        configuration.getClientConfig().setClientId("clientid");
        configuration.getClientConfig().setClientSecret("clientsecret");
        configuration.getClientConfig().setScopes("");//use all scopes
        GeonetworkClientRegistrationProvider out = new GeonetworkClientRegistrationProvider(
            string2InputStream(keycloakConfig),
            configuration
        );

        assertNotNull(out.getClientRegistration());

        assertEquals("clientid", out.getClientRegistration().getClientId());
        assertEquals("clientsecret", out.getClientRegistration().getClientSecret());
        assertEquals(CLIENT_REGISTRATION_NAME, out.getClientRegistration().getRegistrationId());
        assertEquals(9, out.getClientRegistration().getScopes().size());
        assertTrue(out.getClientRegistration().getScopes().contains("openid"));

        assertEquals(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, out.getClientRegistration().getClientAuthenticationMethod());
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE, out.getClientRegistration().getAuthorizationGrantType());

        assertEquals("{baseUrl}/{action}/oauth2/code/{registrationId}", out.getClientRegistration().getRedirectUri());
        assertEquals("http://localhost:8080/realms/demo/protocol/openid-connect/token", out.getClientRegistration().getProviderDetails().getTokenUri());
        assertEquals("http://localhost:8080/realms/demo/protocol/openid-connect/auth", out.getClientRegistration().getProviderDetails().getAuthorizationUri());

    }

    //tests parsing of a azure AD JSON configuration
    @Test
    public void testParsingAzureADConfigurationMetadataJson() throws Exception {
        OIDCConfiguration configuration = new OIDCConfiguration();
        configuration.getClientConfig().setClientId("clientid");
        configuration.getClientConfig().setClientSecret("clientsecret");
        configuration.getClientConfig().setScopes("");//use all scopes
        GeonetworkClientRegistrationProvider out = new GeonetworkClientRegistrationProvider(
            string2InputStream(azureADConfig),
            configuration
        );

        assertNotNull(out.getClientRegistration());

        assertEquals("clientid", out.getClientRegistration().getClientId());
        assertEquals("clientsecret", out.getClientRegistration().getClientSecret());
        assertEquals(CLIENT_REGISTRATION_NAME, out.getClientRegistration().getRegistrationId());
        assertEquals(4, out.getClientRegistration().getScopes().size());
        assertTrue(out.getClientRegistration().getScopes().contains("openid"));

        assertEquals(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, out.getClientRegistration().getClientAuthenticationMethod());
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE, out.getClientRegistration().getAuthorizationGrantType());

        assertEquals("{baseUrl}/{action}/oauth2/code/{registrationId}", out.getClientRegistration().getRedirectUri());
        assertEquals("https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/token", out.getClientRegistration().getProviderDetails().getTokenUri());
        assertEquals("https://login.microsoftonline.com/00000000-0000-0000-0000-000000000000/oauth2/v2.0/authorize", out.getClientRegistration().getProviderDetails().getAuthorizationUri());

    }
}
