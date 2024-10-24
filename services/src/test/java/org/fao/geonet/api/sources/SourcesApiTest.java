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
package org.fao.geonet.api.sources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.SourceType;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for SourcesApi.
 *
 * @author Jose García
 */
public class SourcesApiTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SourceRepository sourceRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() throws Exception {
        createTestData();
    }

    @Test
    public void getSource() throws Exception {
        Source source = sourceRepo.findOneByName("source-test");
        Assert.assertNotNull(source);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/sources/" + source.getUuid())
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getNonExistingSource() throws Exception {
        Source sourceToUpdate = sourceRepo.findOneByName("source-test-2");
        Assert.assertNull(sourceToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/sources/source-test-2")
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(status().is(404));
    }

    @Test
    public void getSources() throws Exception {
        Long sourcesCount = sourceRepo.count();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/sources")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(sourcesCount.intValue())));
    }

    @Test
    public void updateSource() throws Exception {
        Source source = sourceRepo.findOneByName("source-test");
        Assert.assertNotNull(source);

        source.setName("source-test-updated");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "creationDate"))
            .create();
        String json = gson.toJson(source);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/sources/" + source.getUuid())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateNonExistingSource() throws Exception {
        Source sourceToUpdate = sourceRepo.findOneByName("source-test-2");
        Assert.assertNull(sourceToUpdate);

        sourceToUpdate = new Source();
        sourceToUpdate.setName("source-test-2");
        sourceToUpdate.setUuid(UUID.randomUUID().toString());
        sourceToUpdate.setType(SourceType.harvester);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "creationDate"))
            .create();
        String json = gson.toJson(sourceToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/sources/" + sourceToUpdate.getUuid())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("text/plain")))
            .andExpect(status().is(404));
    }

    private void createTestData() {
        Source source = new Source();
        source.setName("source-test");
        source.setUuid("source-uuid");
        source.setCreationDate(new ISODate());
        source.setType(SourceType.harvester);
        sourceRepo.save(source);
    }
}
