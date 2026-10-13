/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.api.users;

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Tests for the forgot-password endpoint of the password recovery API.
 *
 * The endpoint returns the same generic response for every request, whatever the state of the
 * account. The email is sent in the background, so no mail server is needed here (the send just fails and is logged).
 */
public class PasswordApiEmailTest extends AbstractServiceIntegrationTest {
    private static final String USER_WITH_EMAIL = "testuser-email";
    private static final String USER_WITHOUT_EMAIL = "testuser-noemail";
    private static final String USER_LDAP = "testuser-email-ldap";
    private static final String UNKNOWN_USER = "testuser-email-unknown";

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        saveUser(USER_WITH_EMAIL, "with@mail.com", false);
        saveUser(USER_WITHOUT_EMAIL, null, false);
        saveUser(USER_LDAP, "ldap@mail.com", true);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    private void saveUser(String username, String email, boolean ldap) {
        User user = new User();
        user.setUsername(username);
        user.getSecurity().setPassword("password");
        user.setProfile(Profile.Editor);
        user.setEnabled(true);
        if (email != null) {
            user.getEmailAddresses().add(email);
        }
        if (ldap) {
            user.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);
        }
        _userRepo.save(user);
    }

    /**
     * All cases (existing user with email, user without email, LDAP user, unknown user) must
     * return the same status and body.
     */
    @Test
    public void testSameResponseWhicheverTheUser() throws Exception {
        String expectedMessage = ResourceBundle.getBundle(
            "org.fao.geonet.api.Messages", Locale.ENGLISH).getString("user_password_sent");

        for (String username : new String[]{
            USER_WITH_EMAIL, USER_WITHOUT_EMAIL, USER_LDAP, UNKNOWN_USER}) {
            MockHttpServletResponse response = forgotPassword(username);

            Assert.assertEquals(username,
                HttpStatus.CREATED.value(), response.getStatus());
            Assert.assertEquals(username, expectedMessage, response.getContentAsString());
            // The requested user name must not be echoed back to the client.
            Assert.assertFalse(username, response.getContentAsString().contains(username));
        }
    }

    private MockHttpServletResponse forgotPassword(String username) throws Exception {
        return this.mockMvc.perform(put("/srv/api/user/actions/forgot-password")
                .param("username", username)
                .accept(MediaType.TEXT_PLAIN))
            .andReturn().getResponse();
    }
}
