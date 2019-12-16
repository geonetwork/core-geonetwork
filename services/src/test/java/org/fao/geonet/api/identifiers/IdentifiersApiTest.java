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
package org.fao.geonet.api.identifiers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.MetadataIdentifierTemplate;
import org.fao.geonet.repository.MetadataIdentifierTemplateRepository;
import org.fao.geonet.repository.specification.MetadataIdentifierTemplateSpecs;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for IdentifiersApi.
 *
 * @author Jose García
 */
public class IdentifiersApiTest  extends AbstractServiceIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MetadataIdentifierTemplateRepository metadataIdentifierTemplateRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() {
        createTestData();
    }


    @Test
    public void getIdentifiers() throws Exception {
        Long identifierTemplatesCount = metadataIdentifierTemplateRepo.count();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/identifiers")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(identifierTemplatesCount.intValue())));
    }

    @Test
    public void getIdentifiersuUserDefinedOnly() throws Exception {
        Long identifierTemplatesCount = metadataIdentifierTemplateRepo.count(
            MetadataIdentifierTemplateSpecs.isSystemProvided(false));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockMvc.perform(get("/srv/api/identifiers?userDefinedOnly=true")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING))
            .andExpect(jsonPath("$", hasSize(identifierTemplatesCount.intValue())));
    }

    @Test
    public void addIdentifier() throws Exception {
        MetadataIdentifierTemplate metadataIdentifierTemplateToAdd =
            metadataIdentifierTemplateRepo.findOneByName("new-template");

        Assert.assertNull(metadataIdentifierTemplateToAdd);

        metadataIdentifierTemplateToAdd = new MetadataIdentifierTemplate();
        // TODO: Would be better that id is an Integer to use null for new records
        metadataIdentifierTemplateToAdd.setId(-99);
        metadataIdentifierTemplateToAdd.setName("new-template");
        metadataIdentifierTemplateToAdd.setTemplate("{aaaa}");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(metadataIdentifierTemplateToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/identifiers")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        MetadataIdentifierTemplate metadataIdentifierTemplateAdded =
            metadataIdentifierTemplateRepo.findOneByName("new-template");
        Assert.assertNotNull(metadataIdentifierTemplateAdded);
    }

    @Test
    public void addExistingIdentifier() throws Exception {
        MetadataIdentifierTemplate metadataIdentifierTemplateToAdd =
            metadataIdentifierTemplateRepo.findOneByName("id-template-1");
        Assert.assertNotNull(metadataIdentifierTemplateToAdd);

        metadataIdentifierTemplateToAdd.setTemplate("{AAAA}");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(metadataIdentifierTemplateToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/identifiers")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description", is("A metadata identifier template with id '" + metadataIdentifierTemplateToAdd.getId() + "' already exist.")));
    }


    @Test
    public void updateIdentifier() throws Exception {
        MetadataIdentifierTemplate metadataIdentifierTemplateToUpdate =
            metadataIdentifierTemplateRepo.findOneByName("id-template-1");
        Assert.assertNotNull(metadataIdentifierTemplateToUpdate);

        metadataIdentifierTemplateToUpdate.setTemplate("{AAAA}");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(metadataIdentifierTemplateToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/identifiers/" + metadataIdentifierTemplateToUpdate.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        MetadataIdentifierTemplate metadataIdentifierTemplateAdded =
            metadataIdentifierTemplateRepo.findOneByName("id-template-1");
        Assert.assertNotNull(metadataIdentifierTemplateAdded);

    }

    @Test
    public void updateNonExistingIdentifier() throws Exception {
        MetadataIdentifierTemplate metadataIdentifierTemplateToUpdate =
            metadataIdentifierTemplateRepo.findOne(222);
        Assert.assertNull(metadataIdentifierTemplateToUpdate);

        metadataIdentifierTemplateToUpdate = new MetadataIdentifierTemplate();
        metadataIdentifierTemplateToUpdate.setId(222);
        metadataIdentifierTemplateToUpdate.setName("non-exisiting-to-upfste");
        metadataIdentifierTemplateToUpdate.setTemplate("{AAAA}");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(metadataIdentifierTemplateToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/identifiers/" + metadataIdentifierTemplateToUpdate.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void deleteIdentifier() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MetadataIdentifierTemplate identifierTemplate = metadataIdentifierTemplateRepo.findOneByName("id-template-1");
        Assert.assertNotNull(identifierTemplate);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/identifiers/" + identifierTemplate.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        identifierTemplate = metadataIdentifierTemplateRepo.findOneByName("id-template-1");
        Assert.assertNull(identifierTemplate);
    }



    @Test
    public void deleteNonExistingIdentifier() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MetadataIdentifierTemplate identifierTemplateToDelete =
            metadataIdentifierTemplateRepo.findOne(222);
        Assert.assertNull(identifierTemplateToDelete);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/identifiers/222")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    private void createTestData() {
        MetadataIdentifierTemplate metadataIdentifierTemplateSystem = new MetadataIdentifierTemplate();
        metadataIdentifierTemplateSystem.setName("id-template-system");
        metadataIdentifierTemplateSystem.setSystemDefault(true);
        metadataIdentifierTemplateSystem.setTemplate("{DD} {AAAA}");

        metadataIdentifierTemplateRepo.save(metadataIdentifierTemplateSystem);

        MetadataIdentifierTemplate metadataIdentifierTemplate1 = new MetadataIdentifierTemplate();
        metadataIdentifierTemplate1.setName("id-template-1");
        metadataIdentifierTemplate1.setSystemDefault(false);
        metadataIdentifierTemplate1.setTemplate("{FFFF} {DD}");

        metadataIdentifierTemplateRepo.save(metadataIdentifierTemplate1);

        MetadataIdentifierTemplate metadataIdentifierTemplate2 = new MetadataIdentifierTemplate();
        metadataIdentifierTemplate2.setName("id-template-2");
        metadataIdentifierTemplate2.setSystemDefault(false);
        metadataIdentifierTemplate2.setTemplate("{AAAA} {DD}");

        metadataIdentifierTemplateRepo.save(metadataIdentifierTemplate2);
    }
}
