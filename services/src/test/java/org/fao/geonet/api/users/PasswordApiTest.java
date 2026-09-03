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

import com.google.gson.Gson;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Tests for the password recovery API.
 *
 * Uses the same password encoder as a real catalogue, instead of the no operation encoder used
 * by default in the tests, as the way the encoder handles an invalid change key is part of what
 * is tested here.
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:encoder-bean.xml")
public class PasswordApiTest extends AbstractServiceIntegrationTest {
    private static final String EXISTING_USERNAME = "testuser-password";
    private static final String UNKNOWN_USERNAME = "testuser-unknown";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PasswordEncoder encoder;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        User user = new User();
        user.setUsername(EXISTING_USERNAME);
        user.getSecurity().setPassword(encoder.encode("testuser-password-password"));
        user.setProfile(Profile.Editor);
        user.setEnabled(true);
        user.getEmailAddresses().add("test@mail.com");
        _userRepo.save(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    /**
     * A change key which is not the expected one must return the same response for an existing
     * and for an unknown user.
     */
    @Test
    public void testUpdatePasswordWithInvalidChangeKey() throws Exception {
        assertSameResponseForExistingAndUnknownUser(encoder.encode("not-the-change-key"));
    }

    /**
     * A change key the password encoder can't even read must be handled like any other invalid
     * change key. The encoder throws in that case (eg. StandardPasswordEncoder requires a hex
     * encoded value) and the resulting error response would only be returned for a user which
     * exists.
     */
    @Test
    public void testUpdatePasswordWithMalformedChangeKey() throws Exception {
        // Not an even number of hex characters, a non hex character, a hex value too short
        // to be a hash and no change key at all.
        for (String changeKey : new String[]{"x", "zz", "abcd", null}) {
            assertSameResponseForExistingAndUnknownUser(changeKey);
        }
    }

    private void assertSameResponseForExistingAndUnknownUser(String changeKey) throws Exception {
        MockHttpServletResponse existingUserResponse = updatePassword(EXISTING_USERNAME, changeKey);
        MockHttpServletResponse unknownUserResponse = updatePassword(UNKNOWN_USERNAME, changeKey);

        String expectedMessage = ResourceBundle.getBundle(
            "org.fao.geonet.api.Messages", Locale.ENGLISH).getString("user_password_notchanged");

        Assert.assertEquals(changeKey,
            HttpStatus.PRECONDITION_FAILED.value(), existingUserResponse.getStatus());
        Assert.assertEquals(changeKey,
            HttpStatus.PRECONDITION_FAILED.value(), unknownUserResponse.getStatus());
        Assert.assertEquals(changeKey, expectedMessage, existingUserResponse.getContentAsString());
        Assert.assertEquals(changeKey, expectedMessage, unknownUserResponse.getContentAsString());

        // The requested user name must not be echoed back to the client.
        Assert.assertFalse(unknownUserResponse.getContentAsString().contains(UNKNOWN_USERNAME));
        Assert.assertFalse(existingUserResponse.getContentAsString().contains(EXISTING_USERNAME));
    }

    private MockHttpServletResponse updatePassword(String username, String changeKey)
        throws Exception {
        PasswordUpdateParameter passwordAndChangeKey = new PasswordUpdateParameter();
        passwordAndChangeKey.setChangeKey(changeKey);
        passwordAndChangeKey.setPassword("NewPassword1$");

        return this.mockMvc.perform(patch("/srv/api/user/" + username)
                .content(new Gson().toJson(passwordAndChangeKey))
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.TEXT_PLAIN))
            .andReturn().getResponse();
    }
}
