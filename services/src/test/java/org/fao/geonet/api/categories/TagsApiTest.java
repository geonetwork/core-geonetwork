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
package org.fao.geonet.api.categories;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.repository.MetadataCategoryRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for TagsApi.
 *
 * @author Jose García
 */
public class TagsApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MetadataCategoryRepository _categoriesRepo;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Test
    public void getTags() throws Exception {
        List<MetadataCategory> categories = _categoriesRepo.findAll();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/tags")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(categories.size())));
    }

    @Test
    public void getTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/tags/" + category.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(category.getName())));

    }

    @Test
    public void getNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        MvcResult result = this.mockMvc.perform(get("/srv/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andReturn();
    }

    @Test
    public void deleteTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/tags/" + category.getId())
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void deleteNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void putTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOneByName("newcategory");
        Assert.assertNull(category);

        MetadataCategory newCategory = new MetadataCategory();
        newCategory.setId(-99);
        newCategory.setName("newcategory");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "_records"))
            .create();
        String json = gson.toJson(newCategory);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/tags")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        MetadataCategory categoryCreated = _categoriesRepo.findOneByName(newCategory.getName());
        Assert.assertNotNull(categoryCreated);
    }

    @Test
    public void updateTag() throws Exception {
        // TODO test with update and creation with an anonymous user

        MetadataCategory category = _categoriesRepo.findOne(1);
        Assert.assertNotNull(category);

        category.setName(category.getName() + "-2");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations", "_records"))
            .create();
        String json = gson.toJson(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(put("/srv/api/tags/" + category.getId())
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }


    @Test
    public void updateNonExistingTag() throws Exception {
        MetadataCategory category = _categoriesRepo.findOne(222);
        Assert.assertNull(category);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(delete("/srv/api/tags/222")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }
}
