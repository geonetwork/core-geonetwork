/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
package org.fao.geonet.api.groups;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Optional;
import org.fao.geonet.api.FieldNameExclusionStrategy;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for GroupsApi.
 *
 * @author Jose García
 */
public class GroupsApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getGroups() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/groups")
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getGroupsWithReservedGroups() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        MvcResult result = this.mockMvc.perform(get("/srv/api/groups?withReservedGroup=true")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].name", hasItem("GUEST")))
            .andExpect(jsonPath("$[*].name", hasItem("all")))
            .andExpect(jsonPath("$[*].name", hasItem("intranet")))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING)).andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    public void getGroup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/groups/2")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("sample")))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }


    @Test
    public void getGroupUsers() throws Exception {
        Group sampleGroup = _groupRepo.findByName("sample");
        Assert.assertNotNull(sampleGroup);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/groups/" + sampleGroup.getId() + "/users")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getGroupUsersNonExistingGroup() throws Exception {
        Optional<Group> nonExistingGroup = _groupRepo.findById(222);
        Assert.assertFalse(nonExistingGroup.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/groups/222/users")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }


    @Test
    public void deleteGroup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Group sampleGroup = _groupRepo.findByName("sample");
        Assert.assertNotNull(sampleGroup);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/groups/" + sampleGroup.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(403));

        this.mockMvc.perform(delete("/srv/api/groups/" + sampleGroup.getId() + "?force=true")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        this.mockMvc.perform(get("/srv/api/groups/" + sampleGroup.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));

        sampleGroup = _groupRepo.findByName("sample");
        Assert.assertNull(sampleGroup);
    }

    @Test
    public void deleteNonExistingGroup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Optional<Group> groupToDelete = _groupRepo.findById(222);
        Assert.assertFalse(groupToDelete.isPresent());

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/groups/222")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void updateGroup() throws Exception {
        Group groupToUpdate = _groupRepo.findByName("sample");
        Assert.assertNotNull(groupToUpdate);

        groupToUpdate.setEmail("group@mail.com");
        groupToUpdate.setDescription("A test group");
        groupToUpdate.setWebsite("http://link");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();
        String json = gson.toJson(groupToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/groups/" + groupToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateInvalidNameGroup() throws Exception {
        List<String> namesToTest = Lists.newArrayList("--invalidName", "_invalidName", "invalidName_",
            ".invalidName", "invalidName.", "@invalidName", "invalidName@", "invalid@Name", "?nvalidName", "invälidName");

        Group groupToUpdate = _groupRepo.findByName("sample");
        Assert.assertNotNull(groupToUpdate);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        groupToUpdate.setEmail("group@mail.com");
        groupToUpdate.setDescription("A test group");
        groupToUpdate.setWebsite("http://link");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();

        for (String invalidName : namesToTest) {
            groupToUpdate.setName(invalidName);


            String json = gson.toJson(groupToUpdate);


            this.mockMvc.perform(put("/srv/api/groups/" + groupToUpdate.getId())
                    .content(json)
                    .contentType(API_JSON_EXPECTED_ENCODING)
                    .session(this.mockHttpSession)
                    .accept(MediaType.parseMediaType("application/json")))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != 400) {
                        System.err.println(invalidName + " has been accepted as group name and it shouldn't");
                    }
                })
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Group name may only contain alphanumeric "
                    + "characters or single hyphens. Cannot begin or end with a hyphen.")));
        }
    }

    @Test
    public void updateNonExistingGroup() throws Exception {
        Optional<Group> groupToUpdateOptional = _groupRepo.findById(222);
        Assert.assertFalse(groupToUpdateOptional.isPresent());

        Group groupToUpdate = new Group();
        groupToUpdate.setId(222);
        groupToUpdate.setEmail("group@mail.com");
        groupToUpdate.setDescription("A test group");
        groupToUpdate.setWebsite("http://link");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();
        String json = gson.toJson(groupToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/groups/" + groupToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404));
    }

    @Test
    public void addGroup() throws Exception {
        Group groupToAdd = _groupRepo.findByName("test-group");
        Assert.assertNull(groupToAdd);

        groupToAdd = new Group();
        // TODO: Would be better that id is an Integer to use null for new records
        groupToAdd.setId(-99);
        groupToAdd.setName("test-group");
        groupToAdd.setEmail("group@mail.com");
        groupToAdd.setDescription("A test group");
        groupToAdd.setWebsite("http://link");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();
        String json = gson.toJson(groupToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/groups")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201));

        Group groupAdded = _groupRepo.findByName("test-group");
        Assert.assertNotNull(groupAdded);
    }

    @Test
    public void addGroupInvalidName() throws Exception {
        List<String> namesToTest = Lists.newArrayList("--invalidName", "_invalidName", "invalidName_",
            ".invalidName", "invalidName.", "@invalidName", "invalidName@", "invalid@Name", "ínvalidName", "invälidName");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        for (String groupName : namesToTest) {
            Group groupToAdd = _groupRepo.findByName(groupName);

            Assert.assertNull(groupToAdd);

            groupToAdd = new Group();
            // TODO: Would be better that id is an Integer to use null for new records
            groupToAdd.setId(-99);
            groupToAdd.setName(groupName);
            groupToAdd.setEmail("group@mail.com");
            groupToAdd.setDescription("A test group");
            groupToAdd.setWebsite("http://link");

            Gson gson = new GsonBuilder()
                .setFieldNamingStrategy(new JsonFieldNamingStrategy())
                .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
                .create();
            String json = gson.toJson(groupToAdd);


            this.mockMvc.perform(put("/srv/api/groups")
                    .content(json)
                    .contentType(API_JSON_EXPECTED_ENCODING)
                    .session(this.mockHttpSession)
                    .accept(MediaType.parseMediaType("application/json")))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != 400) {
                        System.err.println(groupName + " has been accepted as group name and it shouldn't");
                    }
                })
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is("Group name may only contain alphanumeric "
                    + "characters or single hyphens. Cannot begin or end with a hyphen.")));
        }
    }

    @Test
    public void addExistingGroup() throws Exception {
        Group groupToAdd = _groupRepo.findByName("sample");
        Assert.assertNotNull(groupToAdd);

        groupToAdd = new Group();
        // TODO: Would be better that id is an Integer to use null for new records
        groupToAdd.setId(-99);
        groupToAdd.setName("sample");
        groupToAdd.setEmail("group@mail.com");
        groupToAdd.setDescription("A test group");
        groupToAdd.setWebsite("http://link");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .setExclusionStrategies(new FieldNameExclusionStrategy("_labelTranslations"))
            .create();
        String json = gson.toJson(groupToAdd);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/groups")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.message", is("A group with name 'sample' already exist.")));
    }

    /**
     * Create sample data for the tests.
     */
    private void createTestData() {
        Group sampleGroup = _groupRepo.findByName("sample");

        // Editor - Group sample
        User testUserEditor = new User();
        testUserEditor.setUsername("testuser-editor");
        testUserEditor.setProfile(Profile.Editor);
        testUserEditor.setEnabled(true);
        testUserEditor.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserEditor);

        UserGroup userGroupEditor = new UserGroup().setGroup(sampleGroup)
            .setProfile(Profile.Editor).setUser(testUserEditor);
        _userGroupRepo.save(userGroupEditor);

        // UserAdmin - Group sample
        User testUserUserAdmin = new User();
        testUserUserAdmin.setUsername("testuser-useradmin");
        testUserUserAdmin.setProfile(Profile.UserAdmin);
        testUserUserAdmin.setEnabled(true);
        testUserUserAdmin.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserUserAdmin);

        UserGroup userGroupUserAdmin = new UserGroup().setGroup(sampleGroup)
            .setProfile(Profile.Editor).setUser(testUserUserAdmin);
        _userGroupRepo.save(userGroupUserAdmin);
    }
}
