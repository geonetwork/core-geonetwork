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
import org.fao.geonet.api.records.model.GroupPrivilege;
import org.fao.geonet.api.records.model.SharingParameter;
import org.fao.geonet.api.records.model.SharingResponse;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.GroupType;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.UserRepositoryTest;
import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for class {@link MetadataSharingApi}.
 *
 **/
public class MetadataSharingApiTest extends AbstractServiceIntegrationTest {
    private static final int SAMPLE_GROUP_ID = 2;

    /**
     * Selects the metadata-level publication date (mdb:dateInfo) added by the
     * ISO19115-3.2018 {@code publicationdate-add} process. This does not match the
     * resource-level publication date carried in the sample's identification info.
     */
    private static final String PUBLICATION_DATE_INFO_XPATH =
        "mdb:dateInfo[cit:CI_Date/cit:dateType/cit:CI_DateTypeCode/@codeListValue = 'publication']";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;

    @Autowired
    private MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private StatusValueRepository statusValueRepository;

    @Autowired
    private SettingManager settingManager;

    @PersistenceContext
    private EntityManager entityManager;

    private User editorUser;
    private User reviewerUser;
    private int metadataId;
    private String metadataUuid;
    private ServiceContext context;

    @Before
    public void setUp() throws Exception {
        this.context = createServiceContext();
        createTestData();
        settingManager.setValue(Settings.METADATA_HISTORY_ENABLED, true);
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

    /**
     * Test for issue #9196: Editor user can not change privileges when the metadata is published.
     * <p>
     * Scenario: A Reviewer publishes a record, then an Editor modifies non-publication
     * privileges on that already-published record. The Editor should be able to set
     * group privileges without affecting the existing publication privileges.
     */
    @Test
    public void sharePublishedMetadataWithEditorUser() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        publishMetadata();

        MockHttpSession mockHttpSession = loginAs(editorUser);

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

        long countPrivilegesToSampleGroup2 = metadataOperations.stream().filter(op -> op.getId().getGroupId() == SAMPLE_GROUP_ID).count();
        assertEquals(3, countPrivilegesToSampleGroup2);

        boolean hasReservedGroupPrivileges2 = metadataOperations.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue("Publication privileges set by the Reviewer should be preserved", hasReservedGroupPrivileges2);
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

    @Test
    public void sharingResponseIncludesRecordPrivilegeFlag() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(editorUser);

        Group recordPrivilegeGroup = _groupRepo.save(new Group()
            .setName("record-privilege-test-group")
            .setType(GroupType.RecordPrivilege));
        int recordPrivilegeGroupId = recordPrivilegeGroup.getId();

        Group systemPrivilegeGroup = _groupRepo.save(new Group()
            .setName("system-privilege-test-group")
            .setType(GroupType.SystemPrivilege));
        int systemPrivilegeGroupId = systemPrivilegeGroup.getId();

        String responseBody = mockMvc.perform(get("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Gson gson = new Gson();
        SharingResponse sharingResponse = gson.fromJson(responseBody, SharingResponse.class);

        java.util.Optional<GroupPrivilege> recordPrivGroup = sharingResponse.getPrivileges().stream()
            .filter(p -> p.getGroup() == recordPrivilegeGroupId)
            .findFirst();
        assertTrue("RecordPrivilege group should appear in sharing response", recordPrivGroup.isPresent());
        assertTrue("RecordPrivilege group should have recordPrivilege=true", recordPrivGroup.get().isRecordPrivilege());

        java.util.Optional<GroupPrivilege> workspaceGroup = sharingResponse.getPrivileges().stream()
            .filter(p -> p.getGroup() == SAMPLE_GROUP_ID)
            .findFirst();
        assertTrue("Workspace group should appear in sharing response", workspaceGroup.isPresent());
        assertFalse("Workspace group should have recordPrivilege=false", workspaceGroup.get().isRecordPrivilege());

        boolean systemGroupPresent = sharingResponse.getPrivileges().stream()
            .anyMatch(p -> p.getGroup() == systemPrivilegeGroupId);
        assertFalse("SystemPrivilege group should be excluded from sharing response", systemGroupPresent);
    }

    @Test
    public void settingPrivilegesToNoneCreatesPrivilegesHistoryStatusEntry() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(editorUser);

        SharingParameter addPrivilegesRequest = createPrivilegesRequest(false);
        Gson gson = new Gson();

        // Precondition: record has at least one privilege before setting privileges to none.
        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(gson.toJson(addPrivilegesRequest))
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        long historyEventsBefore = metadataStatusRepository.findAllByMetadataId(metadataId, Sort.unsorted()).stream()
            .map(MetadataStatus::getStatusValue)
            .map(StatusValue::getId)
            .filter(id -> id.equals(StatusValue.Events.RECORDPRIVILEGESCHANGE.getId()))
            .count();

        SharingParameter clearPrivilegesRequest = new SharingParameter();
        clearPrivilegesRequest.setClear(true);
        clearPrivilegesRequest.setPrivileges(new ArrayList<>());

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/sharing")
                .session(mockHttpSession)
                .content(gson.toJson(clearPrivilegesRequest))
                .contentType(API_JSON_EXPECTED_ENCODING)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        long historyEventsAfter = metadataStatusRepository.findAllByMetadataId(metadataId, Sort.unsorted()).stream()
            .map(MetadataStatus::getStatusValue)
            .map(StatusValue::getId)
            .filter(id -> id.equals(StatusValue.Events.RECORDPRIVILEGESCHANGE.getId()))
            .count();

        assertEquals(historyEventsBefore + 1, historyEventsAfter);
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

        Metadata metadata = (Metadata) injectMetadataInDb(getSampleMetadataXml(), context);
        metadata.getSourceInfo().setOwner(editorUser.getId());
        metadata.getSourceInfo().setGroupOwner(SAMPLE_GROUP_ID);
        metadataRepository.save(metadata);
        metadataId = metadata.getId();
        metadataUuid = metadata.getUuid();
    }

    /**
     * Regression test for issue documented in docs/issue-useradmin-publication.md (Symptom 1).
     *
     * <p>A user who is {@code UserAdmin} in one group AND has a
     * {@code UserGroup(profile=Reviewer)} entry for the metadata's group owner MUST be
     * allowed to change reserved-group (publication) privileges.
     */
    @Test
    public void shareMetadataForPublicationAsUserAdminWithReviewerInGroup() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // Create a user who is UserAdmin in a dedicated group (which sets their top-level profile
        // to UserAdmin) and also holds Reviewer membership in the record's group owner.
        User userAdminWithReviewer = UserRepositoryTest.newUser(_inc);
        userAdminWithReviewer.setUsername("useradmin_reviewer");
        userAdminWithReviewer.setProfile(Profile.UserAdmin);
        _userRepo.save(userAdminWithReviewer);
        grantUserAdminInNewGroup(userAdminWithReviewer, "useradmin-reviewer-admin-group");
        Group sampleGroup = _groupRepo.findById(SAMPLE_GROUP_ID).get();
        _userGroupRepo.save(new UserGroup()
            .setGroup(sampleGroup)
            .setProfile(Profile.Reviewer)
            .setUser(userAdminWithReviewer));

        MockHttpSession mockHttpSession = loginAs(userAdminWithReviewer);

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

        List<OperationAllowed> metadataOperations =
            operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream()
            .anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue(
            "UserAdmin who is also per-group Reviewer should be able to publish",
            hasReservedGroupPrivileges);
    }

    /**
     * Back-end side of issue documented in docs/issue-useradmin-publication.md (Symptom 2).
     *
     * <p>A user who is {@code UserAdmin} in one group but has NO
     * {@code UserGroup(profile=Reviewer)} entry for the metadata's group owner MUST be
     * blocked from changing reserved-group (publication) privileges with 403 Forbidden.
     */
    @Test
    public void shareMetadataForPublicationAsUserAdminWithoutReviewerInGroup() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // Create a user who is UserAdmin in a dedicated group (which sets their top-level profile
        // to UserAdmin) but only holds Editor membership in the record's group owner.
        User userAdminEditorOnly = UserRepositoryTest.newUser(_inc);
        userAdminEditorOnly.setUsername("useradmin_editor");
        userAdminEditorOnly.setProfile(Profile.UserAdmin);
        _userRepo.save(userAdminEditorOnly);
        grantUserAdminInNewGroup(userAdminEditorOnly, "useradmin-editor-admin-group");
        Group sampleGroup = _groupRepo.findById(SAMPLE_GROUP_ID).get();
        _userGroupRepo.save(new UserGroup()
            .setGroup(sampleGroup)
            .setProfile(Profile.Editor)
            .setUser(userAdminEditorOnly));

        MockHttpSession mockHttpSession = loginAs(userAdminEditorOnly);

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
    public void publishMetadataAsUserAdminWithUserAdminInGroup() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        User userAdminInGroup = createUserAdminWithGroupProfile("useradmin_group_admin", Profile.UserAdmin);
        MockHttpSession mockHttpSession = loginAs(userAdminInGroup);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isForbidden());
    }

    @Test
    public void unpublishMetadataAsUserAdminWithUserAdminInGroup() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        publishMetadata();

        User userAdminInGroup = createUserAdminWithGroupProfile("useradmin_group_admin_unpublish", Profile.UserAdmin);
        MockHttpSession mockHttpSession = loginAs(userAdminInGroup);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/unpublish")
                .session(mockHttpSession))
            .andExpect(status().isForbidden());

        List<OperationAllowed> metadataOperations =
            operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean hasReservedGroupPrivileges = metadataOperations.stream()
            .anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue("Reserved-group publication privileges should remain set when unpublish is denied", hasReservedGroupPrivileges);
    }

    @Test
    public void unpublishMetadataAsUserAdminWithoutRequiredGroupProfile() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        publishMetadata();

        User userAdminEditorOnly = createUserAdminWithGroupProfile("useradmin_editor_unpublish", Profile.Editor);
        MockHttpSession mockHttpSession = loginAs(userAdminEditorOnly);

        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/unpublish")
                .session(mockHttpSession))
            .andExpect(status().isForbidden());
    }

    /**
     * Creates a user whose top-level profile is {@code UserAdmin} because they are
     * {@code UserAdmin} in a dedicated group, and who additionally holds {@code groupProfile}
     * membership in the sample group (group owner of the test record).
     *
     * <p>In GeoNetwork, {@code UserAdmin} is always a per-group role – not a standalone
     * "global" profile. A user's top-level {@code profile} field simply reflects the highest
     * role they hold in any of their groups. By giving the user an explicit
     * {@code UserGroup(profile=UserAdmin)} in a separate group we honour that constraint.
     */
    private User createUserAdminWithGroupProfile(String username, Profile groupProfile) {
        User user = UserRepositoryTest.newUser(_inc);
        user.setUsername(username);
        user.setProfile(Profile.UserAdmin);
        _userRepo.save(user);

        // Give the user UserAdmin membership in a dedicated group so the top-level
        // UserAdmin profile is backed by a real per-group assignment.
        grantUserAdminInNewGroup(user, username + "-admin-group");

        Group sampleGroup = _groupRepo.findById(SAMPLE_GROUP_ID).get();
        _userGroupRepo.save(new UserGroup()
            .setGroup(sampleGroup)
            .setProfile(groupProfile)
            .setUser(user));

        return user;
    }

    /**
     * When {@code system/metadataprivs/publication/managepublicationdate} is enabled, publishing an
     * ISO19115-3.2018 record must set the metadata-level publication date in the stored XML using the
     * schema's {@code publicationdate-add} process.
     */
    @Test
    public void publishIso191153UpdatesPublicationDateWhenManageEnabled() throws Exception {
        settingManager.setValue(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE, true);
        // Do not let validation block publication of the sample record; this test targets the publication date only.
        settingManager.setValue(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD, true);

        int isoId = injectIso191153Record();
        String isoUuid = metadataRepository.findById(isoId).get().getUuid();

        List<Namespace> ns = new ArrayList<>(ISO19115_3_2018SchemaPlugin.allNamespaces);

        // The sample has no metadata-level publication date before publishing.
        Element beforeXml = metadataRepository.findById(isoId).get().getXmlData(false);
        assertEquals(0, Xml.selectNodes(beforeXml, PUBLICATION_DATE_INFO_XPATH, ns).size());

        String expectedDate = new ISODate().getDateAsString();

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);
        mockMvc.perform(put("/srv/api/records/" + isoUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        // Re-read the stored record from the database.
        entityManager.flush();
        entityManager.clear();
        Element afterXml = metadataRepository.findById(isoId).get().getXmlData(false);

        assertEquals("A single metadata-level publication date should be present after publishing",
            1, Xml.selectNodes(afterXml, PUBLICATION_DATE_INFO_XPATH, ns).size());
        Element publicationDate = (Element) Xml.selectSingle(afterXml,
            PUBLICATION_DATE_INFO_XPATH + "/cit:CI_Date/cit:date/gco:Date", ns);
        assertNotNull("Publication date value should be set", publicationDate);
        assertEquals("Publication date should be the date of publication (today)",
            expectedDate, publicationDate.getText());
    }

    /**
     * When {@code system/metadataprivs/publication/managepublicationdate} is disabled, publishing an
     * ISO19115-3.2018 record must leave the stored XML untouched (no publication date added).
     */
    @Test
    public void publishIso191153DoesNotUpdatePublicationDateWhenManageDisabled() throws Exception {
        settingManager.setValue(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE, false);
        settingManager.setValue(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD, true);

        int isoId = injectIso191153Record();
        String isoUuid = metadataRepository.findById(isoId).get().getUuid();

        List<Namespace> ns = new ArrayList<>(ISO19115_3_2018SchemaPlugin.allNamespaces);

        Element beforeXml = metadataRepository.findById(isoId).get().getXmlData(false);
        int dateInfoCountBefore = Xml.selectNodes(beforeXml, "mdb:dateInfo", ns).size();
        assertEquals(0, Xml.selectNodes(beforeXml, PUBLICATION_DATE_INFO_XPATH, ns).size());

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);
        mockMvc.perform(put("/srv/api/records/" + isoUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        // The record is published ...
        List<OperationAllowed> ops = operationAllowedRepository.findAllById_MetadataId(isoId);
        assertTrue("Record should be published",
            ops.stream().anyMatch(op -> op.getId().getGroupId() == ReservedGroup.all.getId()));

        // ... but its XML must not gain a publication date, and its dateInfo blocks are unchanged.
        entityManager.flush();
        entityManager.clear();
        Element afterXml = metadataRepository.findById(isoId).get().getXmlData(false);

        assertEquals("No publication date should be added when the setting is disabled",
            0, Xml.selectNodes(afterXml, PUBLICATION_DATE_INFO_XPATH, ns).size());
        assertEquals("The metadata dateInfo blocks should be unchanged when the setting is disabled",
            dateInfoCountBefore, Xml.selectNodes(afterXml, "mdb:dateInfo", ns).size());
    }

    /**
     * Injects an ISO19115-3.2018 sample record owned by {@code editorUser} in the sample group so that
     * {@code reviewerUser} (a Reviewer in that group) can publish it.
     */
    private int injectIso191153Record() throws Exception {
        Metadata md = (Metadata) injectMetadataInDb(getSampleISO19115MetadataXml(), context);
        md.getSourceInfo().setOwner(editorUser.getId());
        md.getSourceInfo().setGroupOwner(SAMPLE_GROUP_ID);
        metadataRepository.save(md);
        return md.getId();
    }

    /**
     * Creates a new workspace group with the given name, saves it, and adds a
     * {@code UserGroup(profile=UserAdmin)} entry for {@code user} in that group.
     * This reflects the real-world constraint that {@code UserAdmin} is a per-group role.
     */
    private void grantUserAdminInNewGroup(User user, String groupName) {
        Group adminGroup = _groupRepo.save(new Group().setName(groupName));
        _userGroupRepo.save(new UserGroup()
            .setGroup(adminGroup)
            .setProfile(Profile.UserAdmin)
            .setUser(user));
    }

    /**
     * Verifies that publishing a TEMPLATE succeeds even when invalid and {@code allowPublishInvalidMd} is {@code false}.
     * Templates are exempt from validation checks during publish operations.
     */
    @Test
    public void publishInvalidTemplateSucceedsWhenPublishInvalidMdDisabled() throws Exception {
        // Disable publishing of invalid metadata.
        settingManager.setValue(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD, false);

        // Change the metadata type to TEMPLATE.
        Metadata template = metadataRepository.findById(metadataId).get();
        template.getDataInfo().setType(MetadataType.TEMPLATE);
        metadataRepository.save(template);

        // Persist a required, INVALID validation record so the check would fail for normal metadata.
        MetadataValidation invalidValidation = new MetadataValidation()
            .setId(new MetadataValidationId(metadataId, "xsd"))
            .setStatus(MetadataValidationStatus.INVALID)
            .setRequired(true)
            .setNumTests(1)
            .setNumFailures(1);
        metadataValidationRepository.save(invalidValidation);

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockHttpSession mockHttpSession = loginAs(reviewerUser);

        // Publishing the template must succeed (HTTP 204) despite the invalid validation record.
        mockMvc.perform(put("/srv/api/records/" + metadataUuid + "/publish")
                .session(mockHttpSession))
            .andExpect(status().isNoContent());

        // Confirm publication privileges were actually granted.
        List<OperationAllowed> ops = operationAllowedRepository.findAllById_MetadataId(metadataId);
        boolean published = ops.stream().anyMatch(op -> ReservedGroup.isReserved(op.getId().getGroupId()));
        assertTrue("Template should be published even when it has an invalid validation record", published);
    }
}
