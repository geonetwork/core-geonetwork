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

package org.fao.geonet.api.records;

import com.google.common.base.Optional;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.GroupOperations;
import org.fao.geonet.api.records.model.GroupPrivilege;
import org.fao.geonet.api.records.model.SharingParameter;
import org.fao.geonet.api.records.model.SharingResponse;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordGroupOwnerChangeEvent;
import org.fao.geonet.events.history.RecordOwnerChangeEvent;
import org.fao.geonet.events.history.RecordPrivilegesChangeEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.*;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specification.where;

@RequestMapping(value = {
    "/{portal}/api/records"
})
@Tag(name = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@PreAuthorize("hasAuthority('Editor')")
@Controller("recordSharing")
@ReadWriteController
public class MetadataSharingApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    DataManager dataManager;

    @Autowired
    IMetadataIndexer metadataIndexer;

    @Autowired
    AccessManager accessManager;

    @Autowired
    SettingManager sm;

    @Autowired
    IMetadataUtils metadataUtils;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    IMetadataValidator validator;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    MetadataValidationRepository metadataValidationRepository;

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    OperationAllowedRepository operationAllowedRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserGroupRepository userGroupRepository;

    /**
     * What does publish mean?
     */
    @Autowired
    @Qualifier("publicationConfig")
    private Map publicationConfig;

    public static Vector<OperationAllowedId> retrievePrivileges(ServiceContext context, String id, Integer userId, Integer groupId) throws Exception {

        OperationAllowedRepository opAllowRepo = context.getBean(OperationAllowedRepository.class);

        int iMetadataId = Integer.parseInt(id);
        Specification<OperationAllowed> spec =
            where(hasMetadataId(iMetadataId));
        if (groupId != null) {
            spec = spec.and(hasGroupId(groupId));
        }

        List<OperationAllowed> operationsAllowed = opAllowRepo.findAllWithOwner(userId, Optional.of(spec));

        Vector<OperationAllowedId> result = new Vector<OperationAllowedId>();
        for (OperationAllowed operationAllowed : operationsAllowed) {
            result.add(operationAllowed.getId());
        }

        return result;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set privileges for ALL group to publish the metadata for all users.")
    @RequestMapping(
        value = "/{metadataUuid:.+}/publish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void publish(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        shareMetadataWithAllGroup(metadataUuid, true, session, request);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Unsets privileges for ALL group to publish the metadata for all users.")
    @RequestMapping(
        value = "/{metadataUuid:.+}/unpublish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Reviewer')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void unpublish(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        shareMetadataWithAllGroup(metadataUuid, false, session, request);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set record sharing",
        description = "Privileges are assigned by group. User needs to be able " +
            "to edit a record to set sharing settings. For reserved group " +
            "(ie. Internet, Intranet & Guest), user MUST be reviewer of one group. " +
            "For other group, if Only set privileges to user's groups is set " +
            "in catalog configuration user MUST be a member of the group.<br/>" +
            "Clear first allows to unset all operations first before setting the new ones." +
            "Clear option does not remove reserved groups operation if user is not an " +
            "administrator, a reviewer or the owner of the record.<br/>" +
            "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/publishing/managing-privileges.html'>More info</a>")
    @RequestMapping(
        value = "/{metadataUuid:.+}/sharing",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void share(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Privileges",
            required = true
        )
        @RequestBody(
            required = true
        )
            SharingParameter sharing,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean skipAllReservedGroup = false;

        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them
        UserSession us = ApiUtils.getUserSession(session);
        boolean isAdmin = Profile.Administrator == us.getProfile();
        if (!isAdmin && !accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
            skipAllReservedGroup = true;
        }

        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }

        List<GroupOperations> privileges = sharing.getPrivileges();
        setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
            ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, null, request);
        metadataIndexer.indexMetadataPrivileges(metadata.getUuid(), metadata.getId());
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Publish one or more records",
        description = "See record sharing for more details.")
    @RequestMapping(value = "/publish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about updated privileges."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    MetadataProcessingReport publish(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {

        SharingParameter sharing = buildSharingForPublicationConfig(true);
        return shareSelection(uuids, bucket, sharing, session, request);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Un-publish one or more records",
        description = "See record sharing for more details.")
    @RequestMapping(value = "/unpublish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about updated privileges."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    MetadataProcessingReport unpublish(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {

        SharingParameter sharing = buildSharingForPublicationConfig(false);
        return shareSelection(uuids, bucket, sharing, session, request);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set sharing settings for one or more records",
        description = "See record sharing for more details.")
    @RequestMapping(value = "/sharing",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Report about updated privileges."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    MetadataProcessingReport share(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @Parameter(
            description = "Privileges",
            required = true
        )
        @RequestBody(
            required = true
        )
            SharingParameter sharing,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {

        return shareSelection(uuids, bucket, sharing, session, request);
    }

    private void setOperations(
        SharingParameter sharing,
        DataManager dataMan,
        ServiceContext context,
        ApplicationContext appContext,
        AbstractMetadata metadata,
        Map<String, Integer> operationMap,
        List<GroupOperations> privileges,
        Integer userId,
        boolean skipAllReservedGroup,
        MetadataProcessingReport report,
        HttpServletRequest request) throws Exception {
        if (privileges != null) {

            boolean sharingChanges = false;

            boolean allowPublishInvalidMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD);
            boolean allowPublishNonApprovedMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_NON_APPROVED_MD);

            SharingResponse sharingBefore = getRecordSharingSettings(metadata.getUuid(), request.getSession(), request);

            if (sharing.isClear()) {
                dataManager.deleteMetadataOper(context, String.valueOf(metadata.getId()), skipAllReservedGroup);
            }

            for (GroupOperations p : privileges) {
                for (Map.Entry<String, Boolean> o : p.getOperations().entrySet()) {
                    Integer opId = operationMap.get(o.getKey());
                    // Never set editing for reserved group
                    if (opId == ReservedOperation.editing.getId() &&
                        ReservedGroup.isReserved(p.getGroup())) {
                        continue;
                    }

                    if (o.getValue()) {
                        // For privileges to ALL group, check if it's allowed or not to publish invalid metadata
                        if ((p.getGroup() == ReservedGroup.all.getId())) {
                            try {
                                checkCanPublishToAllGroup(context, dataMan, metadata,
                                    allowPublishInvalidMd, allowPublishNonApprovedMd);
                            } catch (Exception ex) {
                                // If building a report of the sharing, annotate the error and continue
                                // processing the other group privileges, otherwise throw the exception
                                if (report != null) {
                                    report.addMetadataError(metadata, ex.getMessage());
                                    break;
                                } else {
                                    throw ex;
                                }
                            }

                        }
                        dataMan.setOperation(
                            context, metadata.getId(), p.getGroup(), opId);
                        sharingChanges = true;
                    } else if (!sharing.isClear() && !o.getValue()) {
                        dataMan.unsetOperation(
                            context, metadata.getId(), p.getGroup(), opId);
                        sharingChanges = true;
                    }
                }
            }

            if (sharingChanges) {
                new RecordPrivilegesChangeEvent(metadata.getId(), userId,
                    ObjectJSONUtils.convertObjectInJsonObject(sharingBefore.getPrivileges(), RecordPrivilegesChangeEvent.FIELD),
                    ObjectJSONUtils.convertObjectInJsonObject(privileges, RecordPrivilegesChangeEvent.FIELD)).publish(appContext);
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record sharing settings",
        description = "Return current sharing options for a record.")
    @RequestMapping(
        value = "/{metadataUuid:.+}/sharing",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The record sharing settings."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SharingResponse getRecordSharingSettings(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        // TODO: Restrict to user group only in response depending on settings?
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(session);

        SharingResponse sharingResponse = new SharingResponse();
        sharingResponse.setOwner(userSession.getUserId());
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();
        if (groupOwner != null) {
            sharingResponse.setGroupOwner(String.valueOf(groupOwner));
        }

        //--- retrieve groups operations
        Set<Integer> userGroups = accessManager.getUserGroups(
            userSession,
            context.getIpAddress(), // TODO: Use the request
            false);

        List<Group> elGroup = groupRepository.findAll();
        List<Operation> allOperations = operationRepository.findAll();

        List<GroupPrivilege> groupPrivileges = new ArrayList<>(elGroup.size());
        if (elGroup != null) {
            for (Group g : elGroup) {
                GroupPrivilege groupPrivilege = new GroupPrivilege();
                groupPrivilege.setGroup(g.getId());
                groupPrivilege.setReserved(g.isReserved());
                // TODO: Restrict to user group only in response depending on settings?
                groupPrivilege.setUserGroup(userGroups.contains(g.getId()));

                // TODO: Collecting all those info is probably a bit slow when having lots of groups
                final Specification<UserGroup> hasGroupId = UserGroupSpecs.hasGroupId(g.getId());
                final Specification<UserGroup> hasUserId = UserGroupSpecs.hasUserId(userSession.getUserIdAsInt());
                final Specification<UserGroup> hasUserIdAndGroupId = where(hasGroupId).and(hasUserId);
                List<UserGroup> userGroupEntities = userGroupRepository.findAll(hasUserIdAndGroupId);
                List<Profile> userGroupProfile = new ArrayList<>();
                for (UserGroup ug : userGroupEntities) {
                    userGroupProfile.add(ug.getProfile());
                }
                groupPrivilege.setUserProfile(userGroupProfile);


                //--- get all operations that this group can do on given metadata
                Specification<OperationAllowed> hasGroupIdAndMetadataId =
                    where(hasGroupId(g.getId()))
                        .and(hasMetadataId(metadata.getId()));
                List<OperationAllowed> operationAllowedForGroup =
                    operationAllowedRepository.findAll(hasGroupIdAndMetadataId);

                Map<String, Boolean> operations = new HashMap<>(allOperations.size());
                for (Operation o : allOperations) {

                    boolean operationSetForGroup = false;
                    for (OperationAllowed operationAllowed : operationAllowedForGroup) {
                        if (o.getId() == operationAllowed.getId().getOperationId()) {
                            operationSetForGroup = true;
                            break;
                        }
                    }
                    operations.put(o.getName(), operationSetForGroup);
                }
                groupPrivilege.setOperations(operations);
                groupPrivileges.add(groupPrivilege);
            }
        }
        sharingResponse.setPrivileges(groupPrivileges);
        return sharingResponse;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set record group",
        description = "A record is related to one group.")
    @RequestMapping(
        value = "/{metadataUuid:.+}/group",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record group updated."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setRecordGroup(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Group identifier",
            required = true
        )
        @RequestBody(
            required = true
        )
            Integer groupIdentifier,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        Group group = groupRepository.findById(groupIdentifier).get();
        if (group == null) {
            throw new ResourceNotFoundException(String.format(
                "Group with identifier '%s' not found.", groupIdentifier
            ));
        }

        Integer previousGroup = metadata.getSourceInfo().getGroupOwner();
        Group oldGroup = null;
        if (previousGroup != null) {
            oldGroup = groupRepository.findById(previousGroup).get();
        }

        metadata.getSourceInfo().setGroupOwner(groupIdentifier);
        metadataManager.save(metadata);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);

        new RecordGroupOwnerChangeEvent(metadata.getId(), ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(oldGroup, RecordGroupOwnerChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(group, RecordGroupOwnerChangeEvent.FIELD)).publish(appContext);
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get record sharing settings",
        description = "")
    @RequestMapping(
        value = "/sharing",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description =
            "Return a default array of group and operations " +
                "that can be used to set record sharing properties."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseBody
    public SharingResponse getSharingSettings(
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(session);

        SharingResponse sharingResponse = new SharingResponse();
        sharingResponse.setOwner(userSession.getUserId());

        List<Operation> allOperations = operationRepository.findAll();

        //--- retrieve groups operations
        Set<Integer> userGroups = accessManager.getUserGroups(
            context.getUserSession(),
            context.getIpAddress(), false);

        List<Group> elGroup = groupRepository.findAll();
        List<GroupPrivilege> groupPrivileges = new ArrayList<>(elGroup.size());

        for (Group g : elGroup) {
            GroupPrivilege groupPrivilege = new GroupPrivilege();
            groupPrivilege.setGroup(g.getId());
            groupPrivilege.setReserved(g.isReserved());
            groupPrivilege.setUserGroup(userGroups.contains(g.getId()));

            Map<String, Boolean> operations = new HashMap<>(allOperations.size());
            for (Operation o : allOperations) {
                operations.put(o.getName(), false);
            }
            groupPrivilege.setOperations(operations);
            groupPrivileges.add(groupPrivilege);
        }
        sharingResponse.setPrivileges(groupPrivileges);
        return sharingResponse;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set group and owner for one or more records",
        description = "")
    @RequestMapping(value = "/ownership",
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Records group and owner updated"),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    public
    @ResponseBody
    MetadataProcessingReport setGroupAndOwner(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false)
            String[] uuids,
        @Parameter(
            description = "Group identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer groupIdentifier,
        @Parameter(
            description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
            String bucket,
        @Parameter(
            description = "User identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer userIdentifier,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(required = false, defaultValue = "false")
            Boolean approved,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                updateOwnership(groupIdentifier, userIdentifier,
                    report, dataManager, accessManager, metadataRepository,
                    serviceContext, listOfUpdatedRecords, uuid, session, approved);
            }
            dataManager.flush();
            dataManager.indexMetadata(listOfUpdatedRecords);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }
        return report;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set record group and owner",
        description = "")
    @RequestMapping(
        value = "/{metadataUuid:.+}/ownership",
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Record group and owner updated"),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasAuthority('Editor')")
    public
    @ResponseBody
    MetadataProcessingReport setRecordOwnership(
        @Parameter(
            description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Group identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer groupIdentifier,
        @Parameter(
            description = "User identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer userIdentifier,
        @Parameter(description = "Use approved version or not", example = "true")
        @RequestParam(required = false, defaultValue = "true")
            Boolean approved,
        @Parameter(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        try {
            report.setTotalRecords(1);

            final ApplicationContext context = ApplicationContextHolder.get();

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            List<String> listOfUpdatedRecords = new ArrayList<>();
            updateOwnership(groupIdentifier, userIdentifier,
                report, dataManager, accessManager, metadataRepository,
                serviceContext, listOfUpdatedRecords, metadataUuid, session, approved);
            dataManager.flush();
            dataManager.indexMetadata(String.valueOf(metadata.getId()), true);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }
        return report;
    }

    private void updateOwnership(Integer groupIdentifier,
                                 Integer userIdentifier,
                                 MetadataProcessingReport report,
                                 DataManager dataManager,
                                 AccessManager accessMan,
                                 MetadataRepository metadataRepository,
                                 ServiceContext serviceContext,
                                 List<String> listOfUpdatedRecords, String uuid,
                                 HttpSession session, Boolean approved) throws Exception {
        AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
        if (metadata == null) {
            report.incrementNullRecords();
        } else if (!accessMan.canEdit(
            serviceContext, String.valueOf(metadata.getId()))) {
            report.addNotEditableMetadataId(metadata.getId());
        } else {
            //-- Get existing owner and privileges for that owner - note that
            //-- owners don't actually have explicit permissions - only their
            //-- group does which is why we have an ownerGroup (parameter groupid)
            Integer sourceUsr = metadata.getSourceInfo().getOwner();
            Integer sourceGrp = metadata.getSourceInfo().getGroupOwner();
            Vector<OperationAllowedId> sourcePriv =
                retrievePrivileges(serviceContext, String.valueOf(metadata.getId()), sourceUsr, sourceGrp);

            // -- Set new privileges for new owner from privileges of the old
            // -- owner, if none then set defaults
            if (sourcePriv.size() == 0) {
                dataManager.copyDefaultPrivForGroup(
                    serviceContext,
                    String.valueOf(metadata.getId()),
                    String.valueOf(groupIdentifier),
                    false);
                report.addMetadataInfos(metadata, String.format(
                    "No privileges for user '%s' on metadata '%s', so setting default privileges",
                    sourceUsr, metadata.getUuid()
                ));
            } else {
                for (OperationAllowedId priv : sourcePriv) {
                    if (sourceGrp != null) {
                        dataManager.unsetOperation(serviceContext,
                            metadata.getId(),
                            sourceGrp,
                            priv.getOperationId());
                    }
                    dataManager.setOperation(serviceContext,
                        metadata.getId(),
                        groupIdentifier,
                        priv.getOperationId());
                }
            }

            Long metadataId = Long.parseLong(ApiUtils.getInternalId(uuid, approved));
            ApplicationContext context = ApplicationContextHolder.get();
            if (!Objects.equals(groupIdentifier, sourceGrp)) {
                Group newGroup = groupRepository.findById(groupIdentifier).get();
                Group oldGroup = sourceGrp == null ? null : groupRepository.findById(sourceGrp).get();
                new RecordGroupOwnerChangeEvent(metadataId,
                    ApiUtils.getUserSession(session).getUserIdAsInt(),
                    sourceGrp == null ? null : ObjectJSONUtils.convertObjectInJsonObject(oldGroup, RecordGroupOwnerChangeEvent.FIELD),
                    ObjectJSONUtils.convertObjectInJsonObject(newGroup, RecordGroupOwnerChangeEvent.FIELD)).publish(context);
            }
            if (!Objects.equals(userIdentifier, sourceUsr)) {
                User newOwner = userRepository.findById(userIdentifier).get();
                User oldOwner = userRepository.findById(sourceUsr).get();
                new RecordOwnerChangeEvent(metadataId, ApiUtils.getUserSession(session).getUserIdAsInt(), ObjectJSONUtils.convertObjectInJsonObject(oldOwner, RecordOwnerChangeEvent.FIELD), ObjectJSONUtils.convertObjectInJsonObject(newOwner, RecordOwnerChangeEvent.FIELD)).publish(context);
            }
            // -- set the new owner into the metadata record
            dataManager.updateMetadataOwner(metadata.getId(),
                String.valueOf(userIdentifier),
                String.valueOf(groupIdentifier));
            report.addMetadataId(metadata.getId());
            report.incrementProcessedRecords();
            listOfUpdatedRecords.add(metadata.getId() + "");
        }
    }

    /**
     * For privileges to {@link ReservedGroup#all} group, check if it's allowed or not to publish invalid metadata.
     *
     * @param context
     * @param dm
     * @param metadata
     * @return
     * @throws Exception
     */
    private void checkCanPublishToAllGroup(ServiceContext context, DataManager dm, AbstractMetadata metadata,
                                           boolean allowPublishInvalidMd, boolean allowPublishNonApprovedMd) throws Exception {
        MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);
        IMetadataValidator validator = context.getBean(IMetadataValidator.class);
        IMetadataStatus metadataStatusRepository = context.getBean(IMetadataStatus.class);

        if (!allowPublishInvalidMd) {
            boolean hasValidation =
                (metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(metadata.getId())) > 0);

            if (!hasValidation) {
                validator.doValidate(metadata, context.getLanguage());
                dm.indexMetadata(metadata.getId() + "", true);
            }

            boolean isInvalid =
                (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

            if (isInvalid) {
                throw new Exception("The metadata " + metadata.getUuid() + " it's not valid, can't be published.");
            }
        }

        if (!allowPublishNonApprovedMd) {
            MetadataStatus metadataStatus = metadataStatusRepository.getStatus(metadata.getId());
            if (metadataStatus != null) {
                String statusId = metadataStatus.getStatusValue().getId() + "";
                boolean isApproved = statusId.equals(StatusValue.Status.APPROVED);

                if (!isApproved) {
                    throw new Exception("The metadata " + metadata.getUuid() + " it's not approved, can't be published.");
                }
            }
        }

    }


    /**
     * Shares a metadata based on the publicationConfig to publish/unpublish it.
     *
     * @param metadataUuid Metadata uuid.
     * @param publish      Flag to publish/unpublish the metadata.
     * @param request
     * @param session
     * @throws Exception
     */
    private void shareMetadataWithAllGroup(String metadataUuid, boolean publish,
                                           HttpSession session, HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);


        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them
        UserSession us = ApiUtils.getUserSession(session);
        boolean isAdmin = Profile.Administrator == us.getProfile();
        boolean isMdGroupReviewer = accessManager.getReviewerGroups(us).contains(metadata.getSourceInfo().getGroupOwner());
        boolean isReviewOperationAllowedOnMdForUser = accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()));
        boolean isPublishForbiden = !isMdGroupReviewer && !isAdmin && !isReviewOperationAllowedOnMdForUser;
        if (isPublishForbiden) {

            throw new Exception(String.format("User not allowed to publish the metadata %s. You need to be administrator, or reviewer of the metadata group or reviewer with edit privilege on the metadata.",
                    metadataUuid));

        }

        DataManager dataManager = appContext.getBean(DataManager.class);

        OperationRepository operationRepository = appContext.getBean(OperationRepository.class);
        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }

        SharingParameter sharing = buildSharingForPublicationConfig(publish);

        List<GroupOperations> privileges = sharing.getPrivileges();
        setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
            ApiUtils.getUserSession(session).getUserIdAsInt(), true,null, request);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);
    }


    /**
     * Shares a metadata selection with a list of groups, returning a report with the results.
     *
     * @param uuids   Metadata list of uuids to share.
     * @param bucket
     * @param sharing Sharing privileges.
     * @param session
     * @param request
     * @return Report with the results.
     * @throws Exception
     */
    private MetadataProcessingReport shareSelection(String[] uuids, String bucket, SharingParameter sharing,
                                                    HttpSession session, HttpServletRequest request) throws Exception {

        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext appContext = ApplicationContextHolder.get();
            final DataManager dataMan = appContext.getBean(DataManager.class);
            final AccessManager accessMan = appContext.getBean(AccessManager.class);
            final IMetadataUtils metadataRepository = appContext.getBean(IMetadataUtils.class);

            UserSession us = ApiUtils.getUserSession(session);
            boolean isAdmin = Profile.Administrator == us.getProfile();

            ServiceContext context = ApiUtils.createServiceContext(request);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                AbstractMetadata metadata = metadataRepository.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    boolean skipAllReservedGroup = false;
                    if (!isAdmin && accessMan.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
                        skipAllReservedGroup = true;
                    }

                    OperationRepository operationRepository = appContext.getBean(OperationRepository.class);
                    List<Operation> operationList = operationRepository.findAll();
                    Map<String, Integer> operationMap = new HashMap<>(operationList.size());
                    for (Operation o : operationList) {
                        operationMap.put(o.getName(), o.getId());
                    }

                    List<GroupOperations> privileges = sharing.getPrivileges();
                    setOperations(sharing, dataMan, context, appContext, metadata, operationMap, privileges,
                        ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, report, request);
                    report.incrementProcessedRecords();
                    listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                }
            }
            dataMan.flush();
            dataMan.indexMetadata(listOfUpdatedRecords);

        } catch (Exception exception) {
            report.addError(exception);
        } finally {
            report.close();
        }

        return report;
    }


    /**
     * Creates a ref {@link SharingParameter} object with privileges to publih/un-publish
     * metadata in {@link ReservedGroup#all} group.
     *
     * @param publish Flag to add/remove sharing privileges.
     * @return
     */
    private SharingParameter buildSharingForPublicationConfig(boolean publish) {
        SharingParameter sharing = new SharingParameter();
        sharing.setClear(false);

        List<GroupOperations> privilegesList = new ArrayList<>();

        final Iterator iterator = publicationConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object[]> e = (Map.Entry<String, Object[]>) iterator.next();
            GroupOperations privAllGroup = new GroupOperations();
            privAllGroup.setGroup(Integer.parseInt(e.getKey()));

            Map<String, Boolean> operations = new HashMap<>();
            for (Object operation : e.getValue()) {
                operations.put((String) operation, publish);
            }

            privAllGroup.setOperations(operations);
            privilegesList.add(privAllGroup);
        }

        sharing.setPrivileges(privilegesList);
        return sharing;
    }

    public Map getPublicationConfig() {
        return publicationConfig;
    }

    public void setPublicationConfig(Map publicationConfig) {
        this.publicationConfig = publicationConfig;
    }
}
