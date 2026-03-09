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

package org.fao.geonet.api.records;

import com.google.gson.Gson;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.records.model.GroupOperations;
import org.fao.geonet.api.records.model.SharingParameter;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for class {@link MetadataSharingApi}.
 *
 **/
public class MetadataSharingApiTest extends AbstractServiceIntegrationTest {
    private static final int SAMPLE_GROUP_ID = 2;
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    private User editorUser;
    private User reviewerUser;
    private int metadataId;
    private String metadataUuid;
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
    }

    @Test
    public void shareMetadataWithEditorUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(editorUser);

        checkMetadataHasNoPrivileges();

        SharingParameter privilegesRequest = createPrivilegesRequest(false);

        Gson gson = new Gson();
        String json = gson.toJson(privilegesRequest);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());


        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);

        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertFalse(hasReservedGroupPrivileges);

        long countPrivilegesToSampleGroup = metadataOperations.stream().filter(op -> op.getId().getGroupId() == SAMPLE_GROUP_ID).count();
        assertEquals(3, countPrivilegesToSampleGroup);
    }

    @Test
    public void shareMetadataForPublicationWithEditorUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(editorUser);

        SharingParameter privilegesRequest = createPrivilegesRequest(true);

        Gson gson = new Gson();
        String json = gson.toJson(privilegesRequest);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shareMetadataForPublicationWithReviewerUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);

        checkMetadataHasNoPrivileges();

        SharingParameter privilegesRequest = createPrivilegesRequest(true);

        Gson gson = new Gson();
        String json = gson.toJson(privilegesRequest);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue(hasReservedGroupPrivileges);

        long countPrivilegesToSampleGroup = metadataOperations.stream().filter(op -> op.getId().getGroupId() == SAMPLE_GROUP_ID).count();
        assertEquals(3, countPrivilegesToSampleGroup);
    }

    @Test
    public void shareMetadataForPublicationWithAdminUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        checkMetadataHasNoPrivileges();

        SharingParameter privilegesRequest = createPrivilegesRequest(true);

        Gson gson = new Gson();
        String json = gson.toJson(privilegesRequest);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(json)
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue(hasReservedGroupPrivileges);

        long countPrivilegesToSampleGroup = metadataOperations.stream().filter(op -> op.getId().getGroupId() == SAMPLE_GROUP_ID).count();
        assertEquals(3, countPrivilegesToSampleGroup);
    }

    @Test
    public void publishMetadataWithReviewerUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);

        checkMetadataHasNoPrivileges();

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue(hasReservedGroupPrivileges);
    }

    @Test
    public void publishMetadataWithAdminUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        checkMetadataHasNoPrivileges();

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue(hasReservedGroupPrivileges);
    }

    @Test
    public void unpublishMetadataWithReviewerUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);

        publishMetadata();

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/unpublish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertFalse(hasReservedGroupPrivileges);
    }

    @Test
    public void unpublishMetadataWithAdminUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAsAdmin();

        publishMetadata();

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/unpublish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertFalse(hasReservedGroupPrivileges);
    }

    private SharingParameter createPrivilegesRequest(boolean addPublicationPrivileges) {
        SharingParameter privileges = new SharingParameter();
        List<GroupOperations> groupOperations = new ArrayList<>();
        GroupOperations groupOperation = new GroupOperations();
        groupOperation.setGroup(2);

        Map<String, Boolean> operations = new HashMap<>();
        operations.put(ReservedOperation.view.toString(), true);
        operations.put(ReservedOperation.download.toString(), true);
        operations.put(ReservedOperation.dynamic.toString(), true);

        groupOperation.setOperations(operations);

        groupOperations.add(groupOperation);


        if (addPublicationPrivileges) {
            Map<String, Boolean> operationsForPublication = new HashMap<>();

            operationsForPublication.put(ReservedOperation.view.toString(), true);
            operationsForPublication.put(ReservedOperation.download.toString(), true);
            operationsForPublication.put(ReservedOperation.dynamic.toString(), true);

            GroupOperations groupOperationPublic = new GroupOperations();
            groupOperationPublic.setGroup(ReservedGroup.all.getId());
            groupOperationPublic.setOperations(operationsForPublication);

            groupOperations.add(groupOperationPublic);
        }

        privileges.setPrivileges(groupOperations);

        return privileges;
    }

    private void checkMetadataHasNoPrivileges() {
        List<OperationAllowed> metadataOperations = operationAllowedRepository.findAllById_MetadataId(metadataId);
        assertEquals(0, metadataOperations.size());
    }

    private void publishMetadata() {
        OperationAllowed op = new OperationAllowed();
        op.getId().setMetadataId(metadataId).setGroupId(ReservedGroup.all.getId()).setOperationId(ReservedOperation.view.getId());
        operationAllowedRepository.save(op);

        op = new OperationAllowed();
        op.getId().setMetadataId(metadataId).setGroupId(ReservedGroup.all.getId()).setOperationId(ReservedOperation.download.getId());
        operationAllowedRepository.save(op);

        op = new OperationAllowed();
        op.getId().setMetadataId(metadataId).setGroupId(ReservedGroup.all.getId()).setOperationId(ReservedOperation.dynamic.getId());
        operationAllowedRepository.save(op);
    }

    private void createTestData() throws Exception {
        loginAsAdmin(context);

        Group sampleGroup = _groupRepo.findById(SAMPLE_GROUP_ID).get();

        editorUser = UserRepositoryTest.newUser(_inc);
        editorUser.setUsername("metadataeditor");
        editorUser.setProfile(Profile.Editor);
        _userRepo.save(editorUser);
        _userGroupRepo.save(new UserGroup().setGroup(sampleGroup).setProfile(Profile.Editor).setUser(editorUser));

        reviewerUser = UserRepositoryTest.newUser(_inc);
        reviewerUser.setUsername("metadatareviewer");
        reviewerUser.setProfile(Profile.Reviewer);
        _userRepo.save(reviewerUser);
        _userGroupRepo.save(new UserGroup().setGroup(sampleGroup).setProfile(Profile.Reviewer).setUser(reviewerUser));

        Metadata metadata = (Metadata) injectMetadataInDb(getSampleMetadataXml(), context, true);
        metadata.getSourceInfo().setOwner(editorUser.getId());
        metadata.getSourceInfo().setGroupOwner(SAMPLE_GROUP_ID);
        metadataRepository.save(metadata);
        metadataId = metadata.getId();
        metadataUuid = metadata.getUuid();
    }
}
