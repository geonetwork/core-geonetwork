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
package org.fao.geonet.api.selections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.Selection;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.repository.SelectionRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for UserSelectionsApi.
 *
 */
public class UserSelectionsApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Autowired
    private SelectionRepository selectionRepository;

    @Before
    public void setUp() throws Exception {
        this.mockHttpSession = loginAsAdmin();
        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);
        ServiceContext context = createServiceContext();
    }

    @Test
    public void getSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        UserSession session = ApiUtils.getUserSession( this.mockHttpSession);

        this.mockMvc.perform(get("/api/userselections")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    public void deleteNonExistingSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();
        this.mockMvc.perform(delete("/api/userselections/99999")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());
    }

    @Test
    public void addPersistentSelection() throws Exception {
        String name = "notification";

        Selection groupToAdd = selectionRepository.findOneByName(name);
        junit.framework.Assert.assertNull(groupToAdd);

        groupToAdd = new Selection();
        groupToAdd.setId(-99);
        groupToAdd.setName(name);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();
        String json = gson.toJson(groupToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/api/userselections")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        Selection groupAdded = selectionRepository.findOneByName(name);
        junit.framework.Assert.assertNotNull(groupAdded);
    }

    @Test
    public void addDeleteSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        String name = "notification";

        // Check not in db
        Selection selection = selectionRepository.findOneByName(name);
        assertNull(selection);

        Selection newSelection = new Selection();
        newSelection.setId(10);
        newSelection.setName(name);
        newSelection.setWatchable(true);
        Map<String, String> t = new HashMap<>();
        t.put("fre", "french");
        newSelection.setLabelTranslations(t);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "_records"))
            .create();
        String json = gson.toJson(newSelection);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Create
        this.mockMvc.perform(put("/api/userselections")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201))
            .andExpect(content().contentType("application/json"));


        // Check in DB
        Selection createdSelection = selectionRepository.findOneByName("test");
        assertNotNull(createdSelection);
        assertEquals(name, createdSelection.getName());
        assertEquals(true, createdSelection.isWatchable());

        // Check in API
//        this.mockMvc.perform(get("/api/selections/persistent")
//            .session(this.mockHttpSession)
//            .accept(MediaType.parseMediaType("application/json")))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType("application/json"))
//            .andExpect(jsonPath("$", hasSize(1)));

        // Delete
        this.mockMvc.perform(delete("/api/userselections/" + createdSelection.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));


        // Check in DB

        // Check in API
    }
}
