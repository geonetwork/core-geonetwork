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

package org.fao.geonet.api.usersearches;

import com.google.gson.Gson;
import org.fao.geonet.api.usersearches.model.UserSearchDto;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSearchRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link org.fao.geonet.api.usersearches.UserSearchesApi}.
 *
 */
public class UserSearchesApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getUserSearchesAllAdministrator() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/usersearches")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getFeaturedUserSearches() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockMvc.perform(get("/srv/api/usersearches/featured")
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void deleteUserSearch() throws Exception {
        UserSearch userSearchToDelete = userSearchRepository.findAll().get(0);
        Assert.assertNotNull(userSearchToDelete);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/usersearches/" + userSearchToDelete.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        userSearchToDelete = userSearchRepository.findOne(userSearchToDelete.getId());
        Assert.assertNull(userSearchToDelete);
    }


    @Test
    public void createUserSearch() throws Exception {
        UserSearchDto userSearch = new UserSearchDto();
        userSearch.setFeaturedType(UserSearchFeaturedType.HOME.asString());
        userSearch.setUrl("http://customsearch");
        userSearch.setLogo("logo.png");
        userSearch.setCreationDate(UserSearchDto.ISO_DATE_FORMAT.format(new Date()));
        userSearch.addName("eng", "myusersearch_eng");
        userSearch.addName("fre", "myusersearch_fre");

        Gson gson = new Gson();
        String json = gson.toJson(userSearch);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        MvcResult result = this.mockMvc.perform(put("/srv/api/usersearches")
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201))
            .andReturn();

        String content = result.getResponse().getContentAsString();
        UserSearch userSearchCreated = userSearchRepository.findOne(Integer.parseInt(content));
        Assert.assertNotNull(userSearchCreated);
        Assert.assertEquals(true, userSearchCreated.isFeatured());
    }


    @Test
    public void updateUserSearch() throws Exception {
        UserSearch userSearchToUpdate = userSearchRepository.findAll().get(0);
        Assert.assertNotNull(userSearchToUpdate);

        int userSearchId = userSearchToUpdate.getId();

        UserSearchDto userSearch = UserSearchDto.from(userSearchToUpdate);
        userSearch.setUrl("http://urlupdated");

        Gson gson = new Gson();
        String json = gson.toJson(userSearch);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/usersearches/" + userSearchId)
            .content(json)
            .contentType(API_JSON_EXPECTED_ENCODING)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        UserSearch userSearchUpdated = userSearchRepository.findOne(userSearchId);
        Assert.assertNotNull(userSearchUpdated);

        Assert.assertEquals("http://urlupdated", userSearchUpdated.getUrl());
    }
    /**
     * Create sample data for the tests.
     */
    private void createTestData() {
        // Editor
        User testUserEditor = new User();
        testUserEditor.setUsername("testuser-editor");
        testUserEditor.setProfile(Profile.Editor);
        testUserEditor.setEnabled(true);
        testUserEditor.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserEditor);

        UserSearch userSearch1 = new UserSearch();
        userSearch1.setCreator(testUserEditor);
        userSearch1.setCreationDate(new Date());
        userSearch1.setUrl("http://customsearch1");
        userSearchRepository.save(userSearch1);

        // Administrator
        User admin = userRepository.findOneByUsername("admin");

        UserSearch userSearch2 = new UserSearch();
        userSearch2.setCreator(admin);
        userSearch2.setCreationDate(new Date());
        userSearch2.setFeaturedType(UserSearchFeaturedType.HOME);
        userSearch2.setUrl("http://customsearch2");
        userSearchRepository.save(userSearch2);


        UserSearch userSearch3 = new UserSearch();
        userSearch3.setCreator(admin);
        userSearch3.setCreationDate(new Date());
        userSearch3.setUrl("http://customsearch3");
        userSearchRepository.save(userSearch3);

    }
}
