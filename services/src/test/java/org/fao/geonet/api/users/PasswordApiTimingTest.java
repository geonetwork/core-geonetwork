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
import org.fao.geonet.kernel.security.ldap.LDAPConstants;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Checks that updating a password verifies the change key with the same encoder work whether or
 * not the account exists, so the response time doesn't reveal which usernames are registered.
 *
 * Uses a counting encoder as the {@code geonetworkEncoder} bean to observe the check on every
 * branch.
 */
@ContextConfiguration(inheritLocations = true, locations = "classpath:counting-encoder-bean.xml")
public class PasswordApiTimingTest extends AbstractServiceIntegrationTest {
    private static final String EXISTING_USERNAME = "testuser-timing";
    private static final String LDAP_USERNAME = "testuser-timing-ldap";
    private static final String UNKNOWN_USERNAME = "testuser-timing-unknown";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PasswordEncoder encoder;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        User user = new User();
        user.setUsername(EXISTING_USERNAME);
        user.getSecurity().setPassword(encoder.encode("testuser-timing-password"));
        user.setProfile(Profile.Editor);
        user.setEnabled(true);
        user.getEmailAddresses().add("timing@mail.com");
        _userRepo.save(user);

        User ldapUser = new User();
        ldapUser.setUsername(LDAP_USERNAME);
        ldapUser.getSecurity().setPassword(encoder.encode("testuser-timing-ldap-password"));
        ldapUser.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);
        ldapUser.setProfile(Profile.Editor);
        ldapUser.setEnabled(true);
        ldapUser.getEmailAddresses().add("timing-ldap@mail.com");
        _userRepo.save(ldapUser);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    /**
     * The change key must be verified against the encoder on every branch, so an unknown user and
     * an LDAP user cost the same as an existing one and the response time gives nothing away.
     */
    @Test
    public void testChangeKeyIsVerifiedForEveryUser() throws Exception {
        String changeKey = encoder.encode("not-the-change-key");

        assertEncoderCalledOnce(EXISTING_USERNAME, changeKey);
        assertEncoderCalledOnce(LDAP_USERNAME, changeKey);
        assertEncoderCalledOnce(UNKNOWN_USERNAME, changeKey);
    }

    private void assertEncoderCalledOnce(String username, String changeKey) throws Exception {
        CountingPasswordEncoder.resetMatchesCount();
        updatePassword(username, changeKey);
        Assert.assertEquals(
            "The change key should be verified for user " + username,
            1, CountingPasswordEncoder.getMatchesCount());
    }

    private void updatePassword(String username, String changeKey) throws Exception {
        PasswordUpdateParameter passwordAndChangeKey = new PasswordUpdateParameter();
        passwordAndChangeKey.setChangeKey(changeKey);
        passwordAndChangeKey.setPassword("NewPassword1$");

        this.mockMvc.perform(patch("/srv/api/user/" + username)
                .content(new Gson().toJson(passwordAndChangeKey))
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.TEXT_PLAIN))
            .andReturn().getResponse();
    }
}
