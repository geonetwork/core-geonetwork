/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import org.fao.geonet.api.users.model.UserRegisterDto;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RegisterApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    StandardPBEStringEncryptor encryptor;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;


    @Test
    public void testFeatureDisabled() throws Exception {
        encryptor.initialize();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, false);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAnonymous();

        UserRegisterDto userRegister = new UserRegisterDto();
        userRegister.setName("John");
        userRegister.setSurname("Doe");
        userRegister.setUsername("test@mail.com");
        userRegister.setEmail("test@mail.com");
        userRegister.setProfile("Editor");
        userRegister.setGroup("2");

        Gson gson = new Gson();
        String json = gson.toJson(userRegister);

        this.mockMvc.perform(put("/srv/api/user/actions/register")
                .session(this.mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().isPreconditionFailed());
    }

    @Test
    public void testCreateUser() throws Exception {
        encryptor.initialize();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, true);
        settingManager.setValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST, "localhost");
        settingManager.setValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT, "25");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAnonymous();

        UserRegisterDto userRegister = new UserRegisterDto();
        userRegister.setName("John");
        userRegister.setSurname("Doe");
        userRegister.setUsername("test@mail.com");
        userRegister.setEmail("test@mail.com");
        userRegister.setProfile("Editor");
        userRegister.setGroup("2");

        Gson gson = new Gson();
        String json = gson.toJson(userRegister);

        this.mockMvc.perform(put("/srv/api/user/actions/register")
                .session(this.mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().isCreated())
            .andExpect(content().string(String.format("User '%s' registered.", userRegister.getUsername())))
            .andReturn();
    }


    @Test
    public void testCreateExistingUser() throws Exception {
        encryptor.initialize();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, true);
        settingManager.setValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST, "localhost");
        settingManager.setValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT, "25");

        User testUserEditor2 = new User();
        testUserEditor2.setUsername("test@mail.com");
        testUserEditor2.setProfile(Profile.Editor);
        testUserEditor2.setEnabled(true);
        _userRepo.save(testUserEditor2);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAnonymous();

        UserRegisterDto userRegister = new UserRegisterDto();
        userRegister.setName("John");
        userRegister.setSurname("Doe");
        userRegister.setUsername("test@mail.com");
        userRegister.setEmail("test@mail.com");
        userRegister.setProfile("Editor");
        userRegister.setGroup("2");

        Gson gson = new Gson();
        String json = gson.toJson(userRegister);

        this.mockMvc.perform(put("/srv/api/user/actions/register")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().isPreconditionFailed());
    }
}
