/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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
package org.fao.geonet.api.uisetting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.UiSetting;
import org.fao.geonet.domain.UiSetting;
import org.fao.geonet.repository.UiSettingsRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for UiSettingApi.
 *
 * @author Jose García
 */
public class UiApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UiSettingsRepository uiSettingsRepository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void putUiConfiguration() throws Exception {
        Optional<UiSetting> uiConfig = uiSettingsRepository.findById("default");
        Assert.assertFalse(uiConfig.isPresent());

        UiSetting newUiConfig = new UiSetting();
        newUiConfig.setId("default");
        newUiConfig.setConfiguration("{\"mods\": true}");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(newUiConfig);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/ui")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().is(201));

        UiSetting one = uiSettingsRepository.findById(newUiConfig.getId()).get();
        Assert.assertNotNull(one);


        // Get
        UiSetting uiConfiguration = uiSettingsRepository.findById("default").get();
        Assert.assertNotNull(uiConfiguration);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/ui/" + uiConfiguration.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(uiConfiguration.getId())));


        // update
        uiConfiguration = uiSettingsRepository.findById("default").get();
        Assert.assertNotNull(uiConfiguration);

        uiConfiguration.setConfiguration("{}");

        json = gson.toJson(uiConfiguration);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(put("/srv/api/ui/" + uiConfiguration.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        // Delete
        Assert.assertTrue( uiSettingsRepository.existsById("default"));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/ui/" + uiConfiguration.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        Assert.assertFalse( uiSettingsRepository.existsById("default"));
    }

    @Test
    public void getUiConfigurations() throws Exception {
        List<UiSetting> uiConfigurations = uiSettingsRepository.findAll();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/ui")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(uiConfigurations.size())));
    }


    @Test
    public void getNonExistingUiConfiguration() throws Exception {
        Optional<UiSetting> one = uiSettingsRepository.findById("222");
        Assert.assertFalse(one.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MvcResult result = this.mockMvc.perform(get("/srv/api/ui/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andReturn();
    }


    @Test
    public void updateNonExistingUiConfiguration() throws Exception {
        Optional<UiSetting> one = uiSettingsRepository.findById("222");
        Assert.assertFalse(one.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/ui/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }


    @Test
    public void deleteNonExistingUiConfiguration() throws Exception {
        Optional<UiSetting> one = uiSettingsRepository.findById("222");
        Assert.assertFalse(one.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/ui/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }
}
