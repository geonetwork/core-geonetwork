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
package org.fao.geonet.api.users;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.api.users.model.PasswordResetDto;
import org.fao.geonet.api.users.model.UserDto;
import org.fao.geonet.constants.Params;
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

import static org.fao.geonet.repository.specification.UserGroupSpecs.hasUserId;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test class is designed to validate the functionality and behavior of the Users API.
 * It contains setup and test methods for various use cases related to user management,
 * including creation, deletion, updating, and retrieval of user accounts and their associated data.
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

        this.mockMvc.perform(get("/srv/api/users")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(7)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getNonExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/users/222")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/users/1")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }


    @Test
    public void getUserGroups() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User editorUser = _userRepo.findOneByUsername("testuser-editor");
        final Group sampleGroup = _groupRepo.findByName("sample");

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/users/" + editorUser.getId() + "/groups")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id.groupId", is(sampleGroup.getId())))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void deleteExistingUser() throws Exception {
        User userToDelete = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToDelete);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/users/" + userToDelete.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));

        userToDelete = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNull(userToDelete);
    }

    @Test
    public void deleteNonExistingUser() throws Exception {
        Optional<User> userToDelete = _userRepo.findById(222);
        Assert.assertFalse(userToDelete.isPresent());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 404 is returned
        this.mockMvc.perform(delete("/srv/api/users/222")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(404))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void deleteUserLogged() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);
        Assert.assertTrue(user.getProfile().equals(Profile.Editor));
        this.mockHttpSession = loginAs(user);

        // Check 400 is returned and a message indicating that a user can't delete himself
        this.mockMvc.perform(delete("/srv/api/users/" + user.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.message", is("You cannot delete yourself from the user database")))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
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
        this.mockMvc.perform(delete("/srv/api/users/" + userToDelete.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.message", is("You don't have rights to delete this user because the user is not part of your group")))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }


    @Test
    public void createUser() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("newuser");
        user.setName("new");
        user.setProfile(Profile.Editor.name());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(Collections.singletonList("mail@test.com"));
        user.setPassword("Password7$");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/users")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
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
        user.setPassword("Password1$");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is required
        this.mockMvc.perform(put("/srv/api/users")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("username is a required parameter for newuser operation")))
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
        user.setPassword("Password1$");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/srv/api/users")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("Users with username " + user.getUsername()
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
        user.setPassword("Password1$");
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        // Check 400 is returned and a message indicating that username is duplicated
        this.mockMvc.perform(put("/srv/api/users")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("Users with username " + user.getUsername()
                + " ignore case already exists")))
            .andExpect(status().is(400));

    }

    @Test
    public void resetPassword() throws Exception {
        User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword1$");

        String json = gson.toJson(passwordReset);

        this.mockMvc.perform(post("/srv/api/users/" + user.getId() + "/actions/forget-password")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.message", is("The old password is not valid.")));


        passwordReset.setPasswordOld("testuser-editor-password");
        json = gson.toJson(passwordReset);

        this.mockMvc.perform(post("/srv/api/users/" + user.getId() + "/actions/forget-password")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
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

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPasswordOld("testuser-editor-password");
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword1$");

        String json = gson.toJson(passwordReset);

        // Try to update the password of admin user from a user with Editor profile
        this.mockMvc.perform(post("/srv/api/users/" + admin.getId() + "/actions/forget-password")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("You don't have rights to do this")))
            .andExpect(status().is(400));
    }

    @Test
    public void resetPasswordNotEqual() throws Exception {
        User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPasswordOld("testuser-editor-password");
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword2%");

        String json = gson.toJson(passwordReset);

        // Check 400 is returned and a message indicating that passwords should be equal
        this.mockMvc.perform(post("/srv/api/users/" + user.getId() + "/actions/forget-password")
                .contentType(API_JSON_EXPECTED_ENCODING)
                .content(json)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("Passwords should be equal")))
            .andExpect(status().is(400));
    }

    @Test
    public void resetPasswordWrongOldPassword() throws Exception {
        User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPasswordOld("testuser-editor-password-wrong");
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword1$");

        String json = gson.toJson(passwordReset);

        // Check 400 is returned and a message indicating that passwords should be equal
        this.mockMvc.perform(post("/srv/api/users/" + user.getId() + "/actions/forget-password")
                .contentType(API_JSON_EXPECTED_ENCODING)
                .content(json)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("The old password is not valid.")))
            .andExpect(status().is(400));
    }

    @Test
    public void resetPasswordNotExistingUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        String userId = "2222";

        this.mockHttpSession = loginAsAdmin();

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPasswordOld("oldpassword");
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword1$");

        String json = gson.toJson(passwordReset);

        // Check 404 is returned
        this.mockMvc.perform(post("/srv/api/users/" + userId + "/actions/forget-password")
                .contentType(API_JSON_EXPECTED_ENCODING)
                .content(json)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("User not found")))
            .andExpect(status().is(404));
    }

    @Test
    public void resetPasswordSameUser() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        final User user = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(user);
        Assert.assertTrue(user.getProfile().equals(Profile.Editor));
        this.mockHttpSession = loginAs(user);

        Gson gson = new Gson();
        PasswordResetDto passwordReset = new PasswordResetDto();
        passwordReset.setPasswordOld("testuser-editor-password");
        passwordReset.setPassword("NewPassword1$");
        passwordReset.setPassword2("NewPassword1$");

        String json = gson.toJson(passwordReset);

        this.mockMvc.perform(post("/srv/api/users/" + user.getId() + "/actions/forget-password")
                .contentType(API_JSON_EXPECTED_ENCODING)
                .content(json)
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

        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(204));
    }

    @Test
    public void updateUserByUserAdminNotAllowed() throws Exception {
        // User admin in Group test
        User loginUser = _userRepo.findOneByUsername("testuser-useradmin-testgroup");
        Assert.assertNotNull(loginUser);
        // User to update in Group sample
        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull(userToUpdate);

        UserDto user = new UserDto();
        user.setId(String.valueOf(userToUpdate.getId()));
        user.setUsername(userToUpdate.getUsername());
        user.setName(userToUpdate.getName() + "-updated");
        user.setProfile(userToUpdate.getProfile().toString());
        user.setGroupsEditor(Collections.singletonList("2"));
        user.setEmail(new ArrayList(userToUpdate.getEmailAddresses()));
        user.setEnabled(true);

        Gson gson = new Gson();
        String json = gson.toJson(user);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAs(loginUser);

        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("You don't have rights to do this")))
            .andExpect(status().is(400));
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
        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("Another user with username "
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
        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(jsonPath("$.message", is("Another user with username 'testuser-editor' ignore case already exists")))
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
        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
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
        this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.message", is("Another user with username 'testuser-editor' ignore case already exists")));
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
        testUserEditor.getSecurity().setPassword("testuser-editor-password");
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
        userGroupReviewer = new UserGroup().setGroup(testGroup)
            .setProfile(Profile.Reviewer).setUser(testUserReviewer);
        _userGroupRepo.save(userGroupReviewer);

        // UserAdmin - Group sample
        User testUserUserAdmin = new User();
        testUserUserAdmin.setUsername("testuser-useradmin");
        testUserUserAdmin.setProfile(Profile.UserAdmin);
        testUserUserAdmin.setEnabled(true);
        testUserUserAdmin.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserUserAdmin);

        UserGroup userGroupUserAdmin = new UserGroup().setGroup(sampleGroup)
            .setProfile(Profile.UserAdmin).setUser(testUserUserAdmin);
        _userGroupRepo.save(userGroupUserAdmin);

        // UserAdmin - Test group
        User testUserUserAdminForTestGroup = new User();
        testUserUserAdminForTestGroup.setUsername("testuser-useradmin-testgroup");
        testUserUserAdminForTestGroup.setProfile(Profile.UserAdmin);
        testUserUserAdminForTestGroup.setEnabled(true);
        testUserUserAdminForTestGroup.getEmailAddresses().add("test@mail.com");
        _userRepo.save(testUserUserAdminForTestGroup);

        UserGroup userGroupUserAdminForTestGroup = new UserGroup().setGroup(testGroup)
            .setProfile(Profile.UserAdmin).setUser(testUserUserAdminForTestGroup);
        _userGroupRepo.save(userGroupUserAdminForTestGroup);

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

    @Test
    public void addUserInvalid() throws Exception {
        List<String> invalidNames = Lists.newArrayList(
            // Starting with special characters
            "-invalidName",
            "_invalidName",
            ".invalidName",
            ":invalidName",
            "@invalidName",

            // Ending with special characters
            "invalidName-",
            "invalidName_",
            "invalidName.",
            "invalidName:",
            "invalidName@",

            // Consecutive special characters
            "invalid--name",
            "invalid__name",
            "invalid..name",
            "invalid::name",
            "invalid@@name",
            "invalid-_name",
            "invalid._name",
            "invalid@:name",

            // Special characters only
            "-",
            "_",
            ".",
            ":",
            "@",
            "--",

            // Empty or whitespace
            "invalid name",  // space in the middle
            " invalidName",  // starting with space
            "invalidName ",  // ending with space

            // Non-ASCII characters
            "ínvalidName",
            "invälidName",
            "invalid名前",
            "usuário",
            "用户",
            "użytkownik",

            // Special symbols not allowed
            "invalid!name",
            "invalid#name",
            "invalid$name",
            "invalid%name",
            "invalid&name",
            "invalid*name",
            "invalid(name",
            "invalid)name",
            "invalid+name",
            "invalid=name",
            "invalid[name",
            "invalid]name",
            "invalid{name",
            "invalid}name",
            "invalid|name",
            "invalid\\name",
            "invalid/name",
            "invalid?name",
            "invalid<name",
            "invalid>name",
            "invalid,name",
            "invalid;name",
            "invalid'name",
            "invalid\"name",
            "invalid~name",
            "invalid`name"
        );

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        for (String username : invalidNames) {
            User userToAdd = _userRepo.findOneByUsername(username);
            Assert.assertNull(userToAdd);

            UserDto user = new UserDto();
            user.setUsername(username);
            user.setName("new");
            user.setProfile(Profile.Editor.name());
            user.setGroupsEditor(Collections.singletonList("2"));
            user.setEmail(Collections.singletonList("mail@test.com"));
            user.setPassword("Password7$");
            user.setEnabled(true);

            Gson gson = new Gson();
            String json = gson.toJson(user);


            this.mockMvc.perform(put("/srv/api/users")
                    .content(json)
                    .contentType(API_JSON_EXPECTED_ENCODING)
                    .session(this.mockHttpSession)
                    .accept(MediaType.parseMediaType("application/json")))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != 400) {
                        System.err.println(username + " has been accepted as group name and it shouldn't");
                    }
                })
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is(Params.USERNAME
                    + " may only contain alphanumeric characters or single hyphens, single colons, "
                    + "single at signs or single dots. "
                    + "Cannot begin or end with a hyphen, colon, at sign or dot.")));
        }
    }


    @Test
    public void updateInvalidUsername() throws Exception {
        List<String> invalidNames = Lists.newArrayList(
            // Starting with special characters
            "-invalidName",
            "_invalidName",
            ".invalidName",
            ":invalidName",
            "@invalidName",

            // Ending with special characters
            "invalidName-",
            "invalidName_",
            "invalidName.",
            "invalidName:",
            "invalidName@",

            // Consecutive special characters
            "invalid--name",
            "invalid__name",
            "invalid..name",
            "invalid::name",
            "invalid@@name",
            "invalid-_name",
            "invalid._name",
            "invalid@:name",

            // Special characters only
            "-",
            "_",
            ".",
            ":",
            "@",
            "--",

            // Empty or whitespace
            "invalid name",  // space in the middle
            " invalidName",  // starting with space
            "invalidName ",  // ending with space

            // Non-ASCII characters
            "ínvalidName",
            "invälidName",
            "invalid名前",
            "usuário",
            "用户",
            "użytkownik",

            // Special symbols not allowed
            "invalid!name",
            "invalid#name",
            "invalid$name",
            "invalid%name",
            "invalid&name",
            "invalid*name",
            "invalid(name",
            "invalid)name",
            "invalid+name",
            "invalid=name",
            "invalid[name",
            "invalid]name",
            "invalid{name",
            "invalid}name",
            "invalid|name",
            "invalid\\name",
            "invalid/name",
            "invalid?name",
            "invalid<name",
            "invalid>name",
            "invalid,name",
            "invalid;name",
            "invalid'name",
            "invalid\"name",
            "invalid~name",
            "invalid`name"
        );

        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull("The user must exists before running this test", userToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        for (String username : invalidNames) {

            UserDto user = new UserDto();
            user.setUsername(username);
            user.setName("new");
            user.setProfile(Profile.Editor.name());
            user.setGroupsEditor(Collections.singletonList("2"));
            user.setEmail(Collections.singletonList("mail@test.com"));
            user.setPassword("Password7$");
            user.setEnabled(true);

            Gson gson = new Gson();
            String json = gson.toJson(user);


            this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                    .content(json)
                    .contentType(API_JSON_EXPECTED_ENCODING)
                    .session(this.mockHttpSession)
                    .accept(MediaType.parseMediaType("application/json")))
                .andDo(result -> {
                    if (result.getResponse().getStatus() != 400) {
                        System.err.println(username + " has been accepted as username and it shouldn't");
                    }
                })
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message", is(Params.USERNAME
                    + " may only contain alphanumeric characters or single hyphens, single colons, "
                    + "single at signs or single dots. "
                    + "Cannot begin or end with a hyphen, colon, at sign or dot.")));
        }
    }

    /**
     * Tests the update of a user's username with a variety of valid formats.
     * <p>
     * This method retrieves an existing user, modifies their username, and verifies
     * that the username can be successfully updated for each valid format. The test
     * ensures that the application accepts allowed usernames and no errors occur
     * during the update process.
     *
     * @throws Exception if any errors occur during the test execution, including failed assertions,
     *                   HTTP status mismatches, or unexpected exceptions.
     */
    @Test
    public void updateSomeValidUsernames() throws Exception {
        List<String> validNames = Lists.newArrayList(
            "validname",
            "valid-name",
            "valid.name",
            "valid:name",
            "valid@name",
            "valid-name123",
            "valid.name456",
            "valid:name789",
            "valid@name000",
            "123validname",
            "456valid-name",
            "789valid.name",
            "000valid:name",
            "abc123def",
            "abc-123-def",
            "abc.123.def",
            "abc:123:def",
            "abc@123@def",
            "abcdefghijklmnop",
            "a1b2c3d4e5f6g7h8"
        );

        User userToUpdate = _userRepo.findOneByUsername("testuser-editor");
        Assert.assertNotNull("The user must exists before running this test", userToUpdate);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        this.mockHttpSession = loginAsAdmin();

        for (String username : validNames) {

            UserDto user = new UserDto();
            user.setUsername(username);
            user.setName("new");
            user.setProfile(Profile.Editor.name());
            user.setGroupsEditor(Collections.singletonList("2"));
            user.setEmail(Collections.singletonList("mail@test.com"));
            user.setPassword("Password7$");
            user.setEnabled(true);

            Gson gson = new Gson();
            String json = gson.toJson(user);


            this.mockMvc.perform(put("/srv/api/users/" + userToUpdate.getId())
                    .content(json)
                    .contentType(API_JSON_EXPECTED_ENCODING)
                    .session(this.mockHttpSession)
                    .accept(MediaType.parseMediaType("application/json")))
                .andDo(result -> {
                    if (result.getResponse().getStatus() == 400) {
                        System.err.println(username + " hasn't been accepted as group name and it should");
                    }
                })
                .andExpect(status().is(204))
                .andExpect(content().string(""));
        }

    }
}
