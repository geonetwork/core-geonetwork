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
package org.fao.geonet.api.users;


import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.users.model.UserDto;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for UsersApi.
 *
 * @author Jose García
 */
public class UsersApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getUsers() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/api/users")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void getNonExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/api/users/222")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void getExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/api/users/1")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }


    @Test
    public void getUserGroups() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User editorUser = _userRepo.findOneByUsername("testuser-editor");
        final Group sampleGroup = _groupRepo.findByName("sample");

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/api/users/" + editorUser.getId() + "/groups")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id.groupId", is(sampleGroup.getId())))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void deleteExistingUser() throws Exception {
        User userToDelete = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToDelete);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/api/users/" + userToDelete.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        userToDelete = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNull(userToDelete);
    }

    @Test
    public void deleteNonExistingUser() throws Exception {
        User userToDelete = _userRepo.findOne(222);
        Assert.assertNull(userToDelete);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 404 is returned
        this.mockMvc.perform(delete("/api/users/222")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void deleteUserLogged() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);
        Assert.assertTrue(user.getProfile().equals(Profile.Editor));
        this.mockHttpSession = loginAs(user);

        // Check 400 is returned and a message indicating that a user can't delete himself
        this.mockMvc.perform(delete("/api/users/" + user.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description", is("You cannot delete yourself from the user database")))
            .andExpect(content().contentType("application/json"));
    }

    @Test
    public void deleteUserNotAllowedToUserAdmin() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User userToDelete = _userRepo.findOneByUsername("testuser-reviewer");
        Assert.assertNotNull(userToDelete);
        Assert.assertTrue(userToDelete.getProfile().equals(Profile.Reviewer));

        List<Integer> userToDeleteGroupIds = _userGroupRepo.findGroupIds(hasUserId(userToDelete.getId()));

        final User userAdmin = _userRepo.findOneByUsername("testuser-useradmin");
        Assert.assertNotNull(userAdmin);
        Assert.assertTrue(userAdmin.getProfile().equals(Profile.UserAdmin));

        List<Integer> userAdminGroupIds = _userGroupRepo.findGroupIds(hasUserId(userAdmin.getId()));

        // Check no common groups
        Assert.assertTrue(CollectionUtils.intersection(userToDeleteGroupIds, userAdminGroupIds).isEmpty());

        this.mockHttpSession = loginAs(userAdmin);

        // Check 400 is returned and a message indicating that a user can't delete himself
        this.mockMvc.perform(delete("/api/users/" + userToDelete.getId())
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description", is("You don't have rights to delete this user because the user is not part of your group")))
            .andExpect(content().contentType("application/json"));
    }


    @Test
    public void createUser() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("newuser");
        user.setName("new");
        user.setProfile(Profile.Editor.name());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(Collections.singletonList("mail@test.com"));
        user.setPassword("password");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/api/users")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        User userCreated = _userRepo.findOneByUsername(user.getUsername());
        Assert.assertNotNull(userCreated);
    }

    @Test
    public void createUserMissingUsername() throws Exception {
        UserDto user = new UserDto();
        user.setName("new");
        user.setProfile(Profile.Editor.name());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(Collections.singletonList("mail@test.com"));
        user.setPassword("password");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is required
        this.mockMvc.perform(put("/api/users")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("username is a required parameter for newuser operation")))
            .andExpect(status().is(400));
    }

    @Test
    public void createDuplicatedUsername() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("testuser-editor");
        user.setName("test");
        user.setProfile(Profile.Editor.name());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(Collections.singletonList("mail@test.com"));
        user.setPassword("password");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("Users with username " + user.getUsername()
                + " ignore case already exists")))
            .andExpect(status().is(400));

    }

    @Test
    public void createDuplicatedUsernameIgnoreCase() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("tEsTuSeR-eDiToR");
        user.setName("test");
        user.setProfile(Profile.Editor.name());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(Collections.singletonList("mail@test.com"));
        user.setPassword("password");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("Users with username " + user.getUsername()
                + " ignore case already exists")))
            .andExpect(status().is(400));

    }

    @Test
    public void resetPassword() throws Exception {
        User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(post("/api/users/" + user.getId() + "/actions/forget-password")
            .param("password", "newpassword")
            .param("password2", "newpassword")
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void resetPasswordToNotAllowedUser() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User admin = _userRepo.findAllByProfile(Profile.Administrator).get(0);

        final User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);
        Assert.assertTrue(user.getProfile().equals(Profile.Editor));
        this.mockHttpSession = loginAs(user);

        // Try to update the password of admin user from a user with Editor profile
        this.mockMvc.perform(post("/api/users/" + admin.getId() + "/actions/forget-password")
            .param("password", "newpassword")
            .param("password2", "newpassword")
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("You don't have rights to do this")))
            .andExpect(status().is(400));
    }

    @Test
    public void resetPasswordNotEqual() throws Exception {
        User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that passwords should be equal
        this.mockMvc.perform(post("/api/users/" + user.getId() + "/actions/forget-password")
            .contentType(MediaType.APPLICATION_JSON)
            .param("password", "newpassword")
            .param("password2", "newpassword2")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("Passwords should be equal")))
            .andExpect(status().is(400));
    }


    @Test
    public void resetPasswordNotExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        String userId = "222";

        this.mockHttpSession = loginAsAdmin();

        // Check 404 is returned
        this.mockMvc.perform(post("/api/users/" + userId + "/actions/forget-password")
            .contentType(MediaType.APPLICATION_JSON)
            .param("password", "newpassword")
            .param("password2", "newpassword")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("User not found")))
            .andExpect(status().is(404));
    }

    @Test
    public void resetPasswordSameUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);
        Assert.assertTrue(user.getProfile().equals(Profile.Editor));
        this.mockHttpSession = loginAs(user);

        this.mockMvc.perform(post("/api/users/" + user.getId() + "/actions/forget-password")
            .contentType(MediaType.APPLICATION_JSON)
            .param("password", "newpassword")
            .param("password2", "newpassword")
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateUser() throws Exception {
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        UserDto user = new UserDto();
        user.setUsername(userToUpdate.getUsername());
        user.setName(userToUpdate.getName() + "-updated");
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/api/users/" + userToUpdate.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }


    @Test
    public void updateUserDuplicatedUsername() throws Exception {
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        User userToReuseUsername = _userRepo.findOneByUsername("testuser-reviewer");
        Assert.assertNotNull(userToReuseUsername);

        UserDto user = new UserDto();
        // Try to set the username of other existing user
        user.setUsername(userToReuseUsername.getUsername());
        user.setName(userToUpdate.getName());
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users/" + userToUpdate.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("Another user with username "
                + "'testuser-editor' ignore case already exists")))
            .andExpect(status().is(400));
    }

    @Test
    public void updateUserDuplicatedUsernameIgnoreCase() throws Exception {
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        User userToReuseUsername = _userRepo.findOneByUsername("testuser-reviewer");
        Assert.assertNotNull(userToReuseUsername);

        UserDto user = new UserDto();
        // Try to set the username of other existing user with other letter case
        user.setUsername(StringUtils.swapCase(userToReuseUsername.getUsername()));
        user.setName(userToUpdate.getName());
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users/" + userToUpdate.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.description", is("Another user with username 'testuser-editor' ignore case already exists")))
            .andExpect(status().is(400));
    }

    @Test
    public void updateUserDuplicatedUsernameChangeNameCase() throws Exception {
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        User userToReuseUsername = _userRepo.findOneByUsername("testuser-EDITOR");
        Assert.assertNotNull(userToReuseUsername);

        UserDto user = new UserDto();
        // Try to set the username of other existing user with other letter case
        user.setUsername("TESTUSER-editor");
        user.setName(userToUpdate.getName());
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users/" + userToUpdate.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateUserAlreadyExistingUsernameCase() throws Exception {
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        User userToReuseUsername = _userRepo.findOneByUsername("testuser-EDITOR");
        Assert.assertNotNull(userToReuseUsername);

        UserDto user = new UserDto();
        // Try to set the username of other existing user with other letter case
        user.setUsername(userToReuseUsername.getUsername());
        user.setName(userToUpdate.getName());
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/api/users/" + userToUpdate.getId())
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .session(this.mockHttpSession)
            .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.description", is("Another user with username 'testuser-editor' ignore case already exists")));
    }

    /**
     * Create sample data for the tests.
     */
    private void createTestData() {
        Group sampleGroup = _groupRepo.findByName("sample");

        Group testGroup = new Group().setName("test");
        _groupRepo.save(testGroup);

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

        // Reviewer - Group test

        User testUserReviewer = new User();
        testUserReviewer.setUsername("testuser-reviewer");
        testUserReviewer.setProfile(Profile.Reviewer);
        testUserReviewer.setEnabled(true);
        testUserReviewer.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserReviewer);

        UserGroup userGroupReviewer = new UserGroup().setGroup(testGroup)
            .setProfile(Profile.Editor).setUser(testUserReviewer);
        _userGroupRepo.save(userGroupReviewer);

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

        // User with same name different letter case
        User testUserEditor2 = new User();
        testUserEditor2.setUsername("testuser-EDITOR");
        testUserEditor2.setProfile(Profile.Editor);
        testUserEditor2.setEnabled(true);
        testUserEditor2.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserEditor2);

        UserGroup userGroupEditor2 = new UserGroup().setGroup(sampleGroup)
            .setProfile(Profile.Editor).setUser(testUserEditor);
        _userGroupRepo.save(userGroupEditor2);
    }
}
