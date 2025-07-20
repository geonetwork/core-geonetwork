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
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.Selection;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataIndexer;
import org.fao.geonet.kernel.mef.MEFLibIntegrationTest;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SelectionRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for UserSelectionsApi.
 */
public class UserSelectionsApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Autowired
    private SelectionRepository selectionRepository;

    @Autowired
    private MetadataRepository _metadataRepo;

    @Autowired
    private DataManager _dataManager;

    @Autowired
    private UserSelectionsApi userSelectionsApi;

    @Autowired
    private BaseMetadataIndexer metadataIndexerSpy;

    ServiceContext context;

    @Before
    public void setUp() throws Exception {
        metadataIndexerSpy = Mockito.spy(metadataIndexerSpy);
        ReflectionTestUtils.setField(userSelectionsApi, "metadataIndexer", metadataIndexerSpy);
        this.mockHttpSession = loginAsAdmin();
        context = createServiceContext();
    }

    @Test
    public void getSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/userselections")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(2)));
    }


    @Test
    public void deleteNonExistingSelection() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();
        this.mockMvc.perform(delete("/srv/api/userselections/99999")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());
    }

    @Test
    public void addUserSelection() throws Exception {
        String name = "notification";

        Selection newSelection = selectionRepository.findOneByName(name);
        junit.framework.Assert.assertNull(newSelection);

        newSelection = new Selection();
        newSelection.setId(-99);
        newSelection.setName(name);
        newSelection.setWatchable(true);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            // GsonBuilder set watchable property to y instead of true : TODO
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "watchable"))
            .create();
        String json = gson.toJson(newSelection);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/userselections")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));
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
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "watchable"))
            .create();
        String json = gson.toJson(newSelection);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Create
        MvcResult r = this.mockMvc.perform(put("/srv/api/userselections")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andReturn();

        // Check in DB
        Selection createdSelection = selectionRepository.findOneByName(name);
        assertNotNull(createdSelection);
        assertEquals(name, createdSelection.getName());
        assertEquals(false, createdSelection.isWatchable());

        // Check in API
        // Unknown selection set return 404
        this.mockMvc.perform(put("/srv/api/userselections/11111/11111")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());

        // Unknown user return 404
        this.mockMvc.perform(put("/srv/api/userselections/" + createdSelection.getId() + "/11111")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());

        // Unkown metadata return 404
        this.mockMvc.perform(put("/srv/api/userselections/" + createdSelection.getId() + "/1?uuid=ABCD")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());


        String metadataId = importMetadata(context);
        String metadataUuid = _dataManager.getMetadataUuid(metadataId);
        this.mockMvc.perform(put("/srv/api/userselections/" + createdSelection.getId() + "/1?uuid=" + metadataUuid)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isCreated());

        verify(this.metadataIndexerSpy, times(1)).indexMetadata(eq(metadataId), any(Boolean.class), eq(IndexingMode.full));

        this.mockMvc.perform(get("/srv/api/userselections/" + createdSelection.getId() + "/1")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[*]", hasItem(metadataUuid)));

        this.mockMvc.perform(delete("/srv/api/userselections/" + createdSelection.getId() + "/1?uuid=" + metadataUuid)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());

        verify(this.metadataIndexerSpy, times(2)).indexMetadata(eq(metadataId), any(Boolean.class), eq(IndexingMode.full));

        // Delete
        this.mockMvc.perform(delete("/srv/api/userselections/" + createdSelection.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        // Check in DB
        assertFalse(selectionRepository.existsById(createdSelection.getId()));
    }

    private String importMetadata(ServiceContext context) throws Exception {
        final MEFLibIntegrationTest.ImportMetadata importMetadata =
            new MEFLibIntegrationTest.ImportMetadata(this, context).invoke();

        assertEquals(1, _metadataRepo.count());
        return importMetadata.getMetadataIds().get(0);
    }
}
