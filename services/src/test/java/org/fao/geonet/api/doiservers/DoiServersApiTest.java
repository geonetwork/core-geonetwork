/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.doiservers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.fao.geonet.api.JsonFieldNamingStrategy;
import org.fao.geonet.api.doiservers.model.DoiServerDto;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.DoiServerRepository;
import org.fao.geonet.repository.DoiServerRepositoryTest;
import org.fao.geonet.repository.GroupRepositoryTest;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry;
import org.junit.Before;
import org.junit.BeforeClass;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DoiServersApiTest extends AbstractServiceIntegrationTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DoiServerRepository doiServerRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    private MockMvc mockMvc;

    private MockHttpSession mockHttpSession;


    @BeforeClass
    public static void init() {
        StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
        strongEncryptor.setPassword("testpassword");

        HibernatePBEEncryptorRegistry registry =
            HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("STRING_ENCRYPTOR", strongEncryptor);
    }

    @Before
    public void setUp() {
        createTestData();
    }

    @Test
    public void getDoiServers() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/doiservers")
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getDoiServer() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        List<DoiServer> doiServers = doiServerRepository.findAll();
        assertEquals(2, doiServers.size());
        DoiServer doiServerToRetrieve = doiServers.get(0);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/doiservers/" + doiServerToRetrieve.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(doiServerToRetrieve.getName())))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getDoiServerForMetadataAdministrator() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Metadata metadata = metadataRepository.findAll().get(0);
        Optional<Group> ownerGroup = _groupRepo.findById(metadata.getSourceInfo().getGroupOwner());
        assertTrue(ownerGroup.isPresent());

        List<DoiServer> doiServers = doiServerRepository.findAll();
        Optional<DoiServer> doiServerToRetrieve = doiServers.stream().filter(doiServer ->
                doiServer.getPublicationGroups().contains(ownerGroup.get()))
            .findFirst();
        assertTrue(doiServerToRetrieve.isPresent());

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/doiservers/metadata/" + metadata.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is(doiServerToRetrieve.get().getName())))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getDoiServerForMetadataOwner() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Optional<User> metadataOwnerUser = _userRepo.findAll().stream().filter(u -> u.getProfile() == Profile.Editor
            && u.getUsername().equals("metadataowner")).findFirst();
        assertTrue(metadataOwnerUser.isPresent());

        Metadata metadata = metadataRepository.findAll().get(0);
        assertEquals((int) metadata.getSourceInfo().getOwner(), metadataOwnerUser.get().getId());

        Optional<Group> metadataOwnerGroup = _groupRepo.findById(metadata.getSourceInfo().getGroupOwner());
        assertTrue(metadataOwnerGroup.isPresent());

        List<DoiServer> doiServers = doiServerRepository.findAll();
        Optional<DoiServer> doiServerToRetrieve = doiServers.stream().filter(doiServer ->
                doiServer.getPublicationGroups().contains(metadataOwnerGroup.get()))
            .findFirst();
        assertTrue(doiServerToRetrieve.isPresent());

        this.mockHttpSession = loginAs(metadataOwnerUser.get());

        this.mockMvc.perform(get("/srv/api/doiservers/metadata/" + metadata.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is(doiServerToRetrieve.get().getName())))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getDoiServerForMetadataReviewer() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Optional<User> metadataReviewerUser = _userRepo.findAll().stream().filter(u -> u.getProfile() == Profile.Reviewer).findFirst();
        assertTrue(metadataReviewerUser.isPresent());

        Metadata metadata = metadataRepository.findAll().get(0);

        Optional<Group> metadataOwnerGroup = _groupRepo.findById(metadata.getSourceInfo().getGroupOwner());
        assertTrue(metadataOwnerGroup.isPresent());

        Optional<UserGroup> metadataReviewerUserGroupInfo = _userGroupRepo.findAll().stream().filter(ug ->
            ug.getUser().getId() == metadataReviewerUser.get().getId() &&
                ug.getProfile() == metadataReviewerUser.get().getProfile() &&
                ug.getGroup().equals(metadataOwnerGroup.get())).findFirst();
        assertTrue(metadataReviewerUserGroupInfo.isPresent());

        List<DoiServer> doiServers = doiServerRepository.findAll();
        Optional<DoiServer> doiServerToRetrieve = doiServers.stream().filter(doiServer ->
                doiServer.getPublicationGroups().contains(metadataOwnerGroup.get()))
            .findFirst();
        assertTrue(doiServerToRetrieve.isPresent());

        this.mockHttpSession = loginAs(metadataReviewerUser.get());

        this.mockMvc.perform(get("/srv/api/doiservers/metadata/" + metadata.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is(doiServerToRetrieve.get().getName())))
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void getDoiServerForMetadataNotOwnerOrReviewer() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Optional<User> metadataEditorUser = _userRepo.findAll().stream().filter(u -> u.getProfile() == Profile.Editor
            && u.getUsername().equals("metadataeditor")).findFirst();
        assertTrue(metadataEditorUser.isPresent());

        Metadata metadata = metadataRepository.findAll().get(0);
        assertNotEquals((int) metadata.getSourceInfo().getOwner(), metadataEditorUser.get().getId());

        Optional<Group> metadataGroupOwner = _groupRepo.findById(metadata.getSourceInfo().getGroupOwner());
        assertTrue(metadataGroupOwner.isPresent());

        List<DoiServer> doiServers = doiServerRepository.findAll();
        Optional<DoiServer> doiServerToRetrieve = doiServers.stream().filter(doiServer -> doiServer.getPublicationGroups().contains(metadataGroupOwner.get()))
            .findFirst();
        assertTrue(doiServerToRetrieve.isPresent());

        this.mockHttpSession = loginAs(metadataEditorUser.get());

        this.mockMvc.perform(get("/srv/api/doiservers/metadata/" + metadata.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getDoiServerForMetadataNotFound() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        int nonExistingMetadataId = 99999;
        assertTrue(metadataRepository.findById(nonExistingMetadataId).isEmpty());

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(get("/srv/api/doiservers/metadata/" + nonExistingMetadataId)
                .session(this.mockHttpSession)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteDoiServer() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        List<DoiServer> doiServers = doiServerRepository.findAll();
        assertEquals(2, doiServers.size());
        DoiServer doiServerToDelete = doiServers.get(0);

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/doiservers/" + doiServerToDelete.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/srv/api/doiservers/" + doiServerToDelete.getId())
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());

        Optional<DoiServer> doiServerOpt = doiServerRepository.findOneById(doiServerToDelete.getId());
        assertTrue(doiServerOpt.isEmpty());
    }

    @Test
    public void deleteNonExistingDoiServer() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        Optional<DoiServer> doiServerToDelete = doiServerRepository.findOneById(222);
        assertFalse(doiServerToDelete.isPresent());

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(delete("/srv/api/doiservers/222")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(API_JSON_EXPECTED_ENCODING));
    }

    @Test
    public void updateDoiServer() throws Exception {
        List<DoiServer> doiServers = doiServerRepository.findAll();
        assertEquals(2, doiServers.size());
        DoiServer doiServerToUpdate = doiServers.get(0);

        DoiServerDto doiServerDto = DoiServerDto.from(doiServerToUpdate);
        doiServerDto.setName("New name");
        doiServerDto.setDescription("New description");
        doiServerDto.setUrl("http://newurl");

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(doiServerDto);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/doiservers/" + doiServerToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());

        Optional<DoiServer> doiServerUpdatedOpt = doiServerRepository.findOneById(doiServerToUpdate.getId());
        assertTrue(doiServerUpdatedOpt.isPresent());
        assertEquals(doiServerDto.getName(), doiServerUpdatedOpt.get().getName());
        assertEquals(doiServerDto.getDescription(), doiServerUpdatedOpt.get().getDescription());
        assertEquals(doiServerDto.getUrl(), doiServerUpdatedOpt.get().getUrl());
    }

    @Test
    public void updateNonExistingDoiServer() throws Exception {
        Optional<DoiServer> doiServerToUpdateOptional = doiServerRepository.findOneById(222);
        assertFalse(doiServerToUpdateOptional.isPresent());

        DoiServer doiServerToUpdate = DoiServerRepositoryTest.newDoiServer(_inc);
        doiServerToUpdate.setId(222);
        DoiServerDto doiServerToUpdateDto = DoiServerDto.from(doiServerToUpdate);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(doiServerToUpdateDto);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(put("/srv/api/doiservers/" + doiServerToUpdate.getId())
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateDoiServerAuth() throws Exception {
        List<DoiServer> doiServers = doiServerRepository.findAll();
        assertEquals(2, doiServers.size());
        DoiServer doiServerToUpdate = doiServers.get(0);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        this.mockMvc.perform(post("/srv/api/doiservers/" + doiServerToUpdate.getId() + "/auth")
                .param("username", "newusername")
                .param("password", "newpassword")
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().isNoContent());
    }

    @Test
    public void addDoiServer() throws Exception {
        DoiServer doiServerToAdd = DoiServerRepositoryTest.newDoiServer(_inc);
        DoiServerDto doiServerToAddDto = DoiServerDto.from(doiServerToAdd);

        Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(new JsonFieldNamingStrategy())
            .create();
        String json = gson.toJson(doiServerToAddDto);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        this.mockHttpSession = loginAsAdmin();

        MvcResult result = this.mockMvc.perform(put("/srv/api/doiservers")
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .session(this.mockHttpSession)
                .accept(MediaType.parseMediaType("application/json")))
            .andExpect(status().is(201))
            .andReturn();

        int createdDoiServerId = Integer.parseInt(result.getResponse().getContentAsString());
        Optional<DoiServer> doiServerAdded = doiServerRepository.findOneById(createdDoiServerId);
        assertTrue(doiServerAdded.isPresent());
    }

    private void createTestData() {
        Group groupForDOIServer = GroupRepositoryTest.newGroup(_inc);
        _groupRepo.save(groupForDOIServer);


        DoiServer doiServer1 = DoiServerRepositoryTest.newDoiServer(_inc);
        doiServer1.getPublicationGroups().add(groupForDOIServer);
        doiServerRepository.save(doiServer1);

        DoiServer doiServer2 = DoiServerRepositoryTest.newDoiServer(_inc);
        doiServerRepository.save(doiServer2);

        User metadataOwner = UserRepositoryTest.newUser(_inc);
        metadataOwner.setUsername("metadataowner");
        metadataOwner.setProfile(Profile.Editor);
        _userRepo.save(metadataOwner);

        _userGroupRepo.save(new UserGroup().setGroup(groupForDOIServer).setProfile(Profile.Editor).setUser(metadataOwner));

        User metadataReviewer = UserRepositoryTest.newUser(_inc);
        metadataReviewer.setUsername("metadatareviewer");
        metadataReviewer.setProfile(Profile.Reviewer);
        _userRepo.save(metadataReviewer);

        _userGroupRepo.save(new UserGroup().setGroup(groupForDOIServer).setProfile(Profile.Reviewer).setUser(metadataReviewer));

        Metadata metadata = MetadataRepositoryTest.newMetadata(_inc);
        metadata.getSourceInfo().setOwner(metadataOwner.getId());
        metadata.getSourceInfo().setGroupOwner(groupForDOIServer.getId());
        metadataRepository.save(metadata);

        // Editor user, not owner
        User otherEditorUser = UserRepositoryTest.newUser(_inc);
        otherEditorUser.setUsername("metadataeditor");
        otherEditorUser.setProfile(Profile.Editor);
        _userRepo.save(otherEditorUser);

        _userGroupRepo.save(new UserGroup().setGroup(groupForDOIServer).setProfile(Profile.Editor).setUser(otherEditorUser));

    }
}
