/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.fao.geonet.domain.User;
import org.geoserver.security.jwtheaders.JwtConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Basic integration tests for the filter.
 * <p>
 * We are mocking all the other interactions and directly calling JwtHeadersAuthFilter#doFilter
 * and validating the results.
 */
public class JwtHeadersIntegrationTest {


    //JWT example
    public static String JWT = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICItWEdld190TnFwaWRrYTl2QXNJel82WEQtdnJmZDVyMlNWTWkwcWMyR1lNIn0.eyJleHAiOjE3MDcxNTMxNDYsImlhdCI6MTcwNzE1Mjg0NiwiYXV0aF90aW1lIjoxNzA3MTUyNjQ1LCJqdGkiOiJlMzhjY2ZmYy0zMWNjLTQ0NmEtYmU1Yy04MjliNDE0NTkyZmQiLCJpc3MiOiJodHRwczovL2xvZ2luLWxpdmUtZGV2Lmdlb2NhdC5saXZlL3JlYWxtcy9kYXZlLXRlc3QyIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImVhMzNlM2NjLWYwZTEtNDIxOC04OWNiLThkNDhjMjdlZWUzZCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImxpdmUta2V5MiIsIm5vbmNlIjoiQldzc2M3cTBKZ0tHZC1OdFc1QlFhVlROMkhSa25LQmVIY0ZMTHZ5OXpYSSIsInNlc3Npb25fc3RhdGUiOiIxY2FiZmU1NC1lOWU0LTRjMmMtODQwNy03NTZiMjczZmFmZmIiLCJhY3IiOiIwIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtZGF2ZS10ZXN0MiIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJsaXZlLWtleTIiOnsicm9sZXMiOlsiR2Vvc2VydmVyQWRtaW5pc3RyYXRvciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcGhvbmUgb2ZmbGluZV9hY2Nlc3MgbWljcm9wcm9maWxlLWp3dCBwcm9maWxlIGFkZHJlc3MgZW1haWwiLCJzaWQiOiIxY2FiZmU1NC1lOWU0LTRjMmMtODQwNy03NTZiMjczZmFmZmIiLCJ1cG4iOiJkYXZpZC5ibGFzYnlAZ2VvY2F0Lm5ldCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiYWRkcmVzcyI6e30sIm5hbWUiOiJkYXZpZCBibGFzYnkiLCJncm91cHMiOlsiZGVmYXVsdC1yb2xlcy1kYXZlLXRlc3QyIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJkYXZpZC5ibGFzYnlAZ2VvY2F0Lm5ldCIsImdpdmVuX25hbWUiOiJkYXZpZCIsImZhbWlseV9uYW1lIjoiYmxhc2J5IiwiZW1haWwiOiJkYXZpZC5ibGFzYnlAZ2VvY2F0Lm5ldCJ9.fHzXd7oISnqWb09ah9wikfP2UOBeiOA3vd_aDg3Bw-xcfv9aD3CWhAK5FUDPYSPyj4whAcknZbUgUzcm0qkaI8V_aS65F3Fug4jt4nC9YPL4zMSJ5an4Dp6jlQ3OQhrKFn4FwaoW61ndMmScsZZWEQyj6gzHnn5cknqySB26tVydT6q57iTO7KQFcXRdbXd6GWIoFGS-ud9XzxQMUdNfYmsDD7e6hoWhe9PJD9Zq4KT6JN13hUU4Dos-Z5SBHjRa6ieHoOe9gqkjKyA1jT1NU42Nqr-mTV-ql22nAoXuplpvOYc5-09-KDDzSDuVKFwLCNMN3ZyRF1wWuydJeU-gOQ";
    JwtHeadersConfiguration config;
    FilterChain filterChain;
    ServletResponse response;
    JwtHeadersUserUtil jwtHeadersUserUtil;
    User user;
    User user2;

    /**
     * standard configuration for testing JSON
     */
    public static JwtHeadersConfiguration getBasicConfig() {
        JwtHeadersConfiguration config = new JwtHeadersConfiguration(new JwtHeadersSecurityConfig());
        var jwtheadersConfiguration = config.getJwtConfiguration();
        jwtheadersConfiguration.setUserNameHeaderAttributeName("OIDC_id_token_payload");

        jwtheadersConfiguration.setUserNameFormatChoice(JwtConfiguration.UserNameHeaderFormat.JSON);
        jwtheadersConfiguration.setUserNameJsonPath("preferred_username");


        jwtheadersConfiguration.setRolesJsonPath("resource_access.live-key2.roles");
        jwtheadersConfiguration.setRolesHeaderName("OIDC_id_token_payload");
        jwtheadersConfiguration.setJwtHeaderRoleSource("JSON");

        jwtheadersConfiguration.setRoleConverterString("GeonetworkAdministrator=ADMINISTRATOR");
        jwtheadersConfiguration.setOnlyExternalListedRoles(false);

        jwtheadersConfiguration.setValidateToken(false);

        jwtheadersConfiguration.setValidateTokenAgainstURL(true);
        jwtheadersConfiguration.setValidateTokenAgainstURLEndpoint("");
        jwtheadersConfiguration.setValidateSubjectWithEndpoint(true);

        jwtheadersConfiguration.setValidateTokenAudience(true);
        jwtheadersConfiguration.setValidateTokenAudienceClaimName("");
        jwtheadersConfiguration.setValidateTokenAudienceClaimValue("");

        jwtheadersConfiguration.setValidateTokenSignature(true);
        jwtheadersConfiguration.setValidateTokenSignatureURL("");

        return config;
    }

    /**
     * standard configuration for testing JWT
     */
    public static JwtHeadersConfiguration getBasicConfigJWT() {
        JwtHeadersConfiguration config = new JwtHeadersConfiguration(new JwtHeadersSecurityConfig());
        var jwtheadersConfiguration = config.getJwtConfiguration();
        jwtheadersConfiguration.setUserNameHeaderAttributeName("TOKEN");

        jwtheadersConfiguration.setUserNameFormatChoice(JwtConfiguration.UserNameHeaderFormat.JWT);
        jwtheadersConfiguration.setUserNameJsonPath("preferred_username");


        jwtheadersConfiguration.setRolesJsonPath("resource_access.live-key2.roles");
        jwtheadersConfiguration.setRolesHeaderName("TOKEN");
        jwtheadersConfiguration.setJwtHeaderRoleSource("JWT");

        jwtheadersConfiguration.setRoleConverterString("GeoserverAdministrator=ADMINISTRATOR");
        jwtheadersConfiguration.setOnlyExternalListedRoles(false);

        jwtheadersConfiguration.setValidateToken(false);

        jwtheadersConfiguration.setValidateTokenAgainstURL(true);
        jwtheadersConfiguration.setValidateTokenAgainstURLEndpoint("");
        jwtheadersConfiguration.setValidateSubjectWithEndpoint(true);

        jwtheadersConfiguration.setValidateTokenAudience(true);
        jwtheadersConfiguration.setValidateTokenAudienceClaimName("");
        jwtheadersConfiguration.setValidateTokenAudienceClaimValue("");

        jwtheadersConfiguration.setValidateTokenSignature(true);
        jwtheadersConfiguration.setValidateTokenSignatureURL("");

        return config;
    }

    @Before
    public void setUp() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(null);


        config = getBasicConfig();

        filterChain = Mockito.mock(FilterChain.class);
        response = Mockito.mock(ServletResponse.class);

        jwtHeadersUserUtil = Mockito.mock(JwtHeadersUserUtil.class);

        user = new User();
        user.setUsername("testcase-user@geocat.net");

        user2 = new User();
        user2.setUsername("testcase-user2222@geocat.net");
    }

    /**
     * trivial integration test - user arrives at site with header (gets access).
     */
    @Test
    public void testTrivialLogin() throws ServletException, IOException {
        doReturn(user)
            .when(jwtHeadersUserUtil).getUser(any(), any());

        var request = new MockHttpServletRequest();

        request.addHeader("oidc_id_token_payload", "{\"preferred_username\":\"david.blasby2@geocat.net\",\"resource_access\":{\"live-key2\":{\"roles\":[\"GeonetworkAdministrator\",\"group1:Reviewer\"]}}}");

        JwtHeadersAuthFilter filter = new JwtHeadersAuthFilter(config);
        filter.jwtHeadersUserUtil = jwtHeadersUserUtil;
        filter = spy(filter);

        //this should login the user
        filter.doFilter(request, response, filterChain);

        //this validate login
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(auth);
        Assert.assertTrue(auth instanceof JwtHeadersUsernamePasswordAuthenticationToken);
        var principle = (User) auth.getPrincipal();
        Assert.assertEquals(user.getUsername(), principle.getUsername());

        //logout() should not have been called
        verify(filter, never()).logout(any());
    }

    /**
     * integration test -
     * 1. user arrives at site with header (gets access).
     * 2. user then makes request (without headers) - should get logged out (i.e. not auth + logout() called)
     */
    @Test
    public void testLoginLogout() throws ServletException, IOException {
        doReturn(user)
            .when(jwtHeadersUserUtil).getUser(any(), any());

        var request = new MockHttpServletRequest();


        JwtHeadersAuthFilter filter = new JwtHeadersAuthFilter(config);
        filter = spy(filter);
        filter.jwtHeadersUserUtil = jwtHeadersUserUtil;

        //logged in
        request.addHeader("oidc_id_token_payload", "{\"preferred_username\":\"david.blasby2@geocat.net\",\"resource_access\":{\"live-key2\":{\"roles\":[\"GeonetworkAdministrator\",\"group1:Reviewer\"]}}}");

        //user should be logged in
        filter.doFilter(request, response, filterChain);

        //validate login
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(auth);
        Assert.assertTrue(auth instanceof JwtHeadersUsernamePasswordAuthenticationToken);
        var principle = (User) auth.getPrincipal();
        Assert.assertEquals(user.getUsername(), principle.getUsername());
        verify(filter, never()).logout(any()); //logout() should not have been called

        //logout
        request = new MockHttpServletRequest();
        filter.doFilter(request, response, filterChain);

        //no longer an auth
        auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNull(auth);
        verify(filter).logout(any());  //logout was called
    }

    /**
     * integration test -
     * 1. user1 arrives at site with header (gets access).
     * 2. switch to user2 then makes request (with headers)
     * - user1 should get logged out (i.e. not auth + logout() called)
     * - user2 gets logged in
     * <p>
     * In general, this shouldn't happen, but could happen:
     * 1. logon as low-rights user
     * 2. -- do stuff ---
     * 3. need high privileges, so change to higher-rights user
     * 4.  -- do stuff ---
     */
    @Test
    public void testLoginDifferentLogin() throws ServletException, IOException {
        doReturn(user)
            .when(jwtHeadersUserUtil).getUser(any(), any());

        var request = new MockHttpServletRequest();


        JwtHeadersAuthFilter filter = new JwtHeadersAuthFilter(config);
        filter = spy(filter);
        filter.jwtHeadersUserUtil = jwtHeadersUserUtil;


        //logged in
        request.addHeader("oidc_id_token_payload", "{\"preferred_username\":\"david.blasby2@geocat.net\",\"resource_access\":{\"live-key2\":{\"roles\":[\"GeonetworkAdministrator\",\"group1:Reviewer\"]}}}");

        filter.doFilter(request, response, filterChain);

        //validate user logged in
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(auth);
        Assert.assertTrue(auth instanceof JwtHeadersUsernamePasswordAuthenticationToken);
        var principle = (User) auth.getPrincipal();
        Assert.assertEquals(user.getUsername(), principle.getUsername());
        verify(filter, never()).logout(any()); //logout() should not have been called

        //login new user (user2)
        request = new MockHttpServletRequest();
        request.addHeader("oidc_id_token_payload", "{\"preferred_username\":\"david.blasby2@geocat.net\",\"resource_access\":{\"live-key2\":{\"roles\":[\"GeonetworkAdministrator\",\"group1:Reviewer\"]}}}");
        doReturn(user2)
            .when(jwtHeadersUserUtil).getUser(any(), any());

        filter.doFilter(request, response, filterChain);

        //validate that the correct user is logged in
        auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertNotNull(auth);
        Assert.assertTrue(auth instanceof JwtHeadersUsernamePasswordAuthenticationToken);
        principle = (User) auth.getPrincipal();
        Assert.assertEquals(user2.getUsername(), principle.getUsername());
        verify(filter).logout(any());  //logout must be called
    }

}
