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
import io.swagger.annotations.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
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
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.jdom.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specifications.where;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@PreAuthorize("hasRole('Editor')")
@Controller("recordSharing")
@ReadWriteController
public class MetadataSharingApi {

    @Autowired
    LanguageUtils languageUtils;


    @ApiOperation(
        value = "Set record sharing",
        notes = "Privileges are assigned by group. User needs to be able " +
            "to edit a record to set sharing settings. For reserved group " +
            "(ie. Internet, Intranet & Guest), user MUST be reviewer of one group. " +
            "For other group, if Only set privileges to user's groups is set " +
            "in catalog configuration user MUST be a member of the group.<br/>" +
            "Clear first allows to unset all operations first before setting the new ones." +
            "Clear option does not remove reserved groups operation if user is not an " +
            "administrator, a reviewer or the owner of the record.<br/>" +
            "<a href='http://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/publishing/managing-privileges.html'>More info</a>",
        nickname = "share")
    @RequestMapping(
        value = "/{metadataUuid}/sharing",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Settings updated."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void share(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Privileges",
            required = true
        )
        @RequestBody(
            required = true
        )
            SharingParameter sharing,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        boolean skip = false;

        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them
        UserSession us = ApiUtils.getUserSession(session);
        boolean isAdmin = Profile.Administrator == us.getProfile();
        boolean isReviewer = Profile.Reviewer == us.getProfile();
        if (us.getUserIdAsInt() == metadata.getSourceInfo().getOwner() &&
            !isAdmin &&
            !isReviewer) {
            skip = true;
        }

        DataManager dataManager = appContext.getBean(DataManager.class);
        if (sharing.isClear()) {
            dataManager.deleteMetadataOper(context, String.valueOf(metadata.getId()), skip);
        }

        OperationRepository operationRepository = appContext.getBean(OperationRepository.class);
        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }

        List<GroupOperations> privileges = sharing.getPrivileges();
        setOperations(sharing, dataManager, context, metadata, operationMap, privileges);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);
    }


    @ApiOperation(
        value = "Set sharing settings for one or more records",
        notes = "See record sharing for more details.",
        nickname = "shareRecords")
    @RequestMapping(value = "/sharing",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Report about updated privileges."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    public
    @ResponseBody
    MetadataProcessingReport share(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @ApiParam(value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @ApiParam(
            value = "Privileges",
            required = true
        )
        @RequestBody(
            required = true
        )
            SharingParameter sharing,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext appContext = ApplicationContextHolder.get();
            final DataManager dataMan = appContext.getBean(DataManager.class);
            final AccessManager accessMan = appContext.getBean(AccessManager.class);
            final MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);

            UserSession us = ApiUtils.getUserSession(session);
            boolean isAdmin = Profile.Administrator == us.getProfile();
            boolean isReviewer = Profile.Reviewer == us.getProfile();

            ServiceContext context = ApiUtils.createServiceContext(request);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                Metadata metadata = metadataRepository.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessMan.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    boolean skip = false;
                    if (us.getUserIdAsInt() == metadata.getSourceInfo().getOwner() &&
                        !isAdmin &&
                        !isReviewer) {
                        skip = true;
                    }

                    if (sharing.isClear()) {
                        dataMan.deleteMetadataOper(context,
                            String.valueOf(metadata.getId()), skip);
                    }

                    OperationRepository operationRepository = appContext.getBean(OperationRepository.class);
                    List<Operation> operationList = operationRepository.findAll();
                    Map<String, Integer> operationMap = new HashMap<>(operationList.size());
                    for (Operation o : operationList) {
                        operationMap.put(o.getName(), o.getId());
                    }

                    List<GroupOperations> privileges = sharing.getPrivileges();
                    setOperations(sharing, dataMan, context, metadata, operationMap, privileges);
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

    private void setOperations(
        SharingParameter sharing,
        DataManager dataMan,
        ServiceContext context,
        Metadata metadata,
        Map<String, Integer> operationMap,
        List<GroupOperations> privileges) throws Exception {
        if (privileges != null) {
            SettingManager sm = context.getBean(SettingManager.class);
            DataManager dm = context.getBean(DataManager.class);

            boolean allowPublishInvalidMd = sm.getValueAsBool("metadata/workflow/allowPublishInvalidMd");

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
                        if ((p.getGroup() == ReservedGroup.all.getId()) && (!allowPublishInvalidMd)) {
                            if (!canPublishToAllGroup(context, dm, metadata)) {
                                continue;
                            }
                        }

                        dataMan.setOperation(
                            context, metadata.getId(), p.getGroup(), opId);
                    } else if (!sharing.isClear() && !o.getValue()) {
                        dataMan.unsetOperation(
                            context, metadata.getId(), p.getGroup(), opId);
                    }
                }
            }
        }
    }

    /**
     * For privileges to ALL group, check if it's allowed or not to publish invalid metadata.
     *
     * @param context
     * @param dm
     * @param metadata
     * @return
     * @throws Exception
     */
    private boolean canPublishToAllGroup(ServiceContext context, DataManager dm, Metadata metadata) throws Exception {
        MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);

        boolean hasValidation =
            (metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(metadata.getId())) > 0);

        if (!hasValidation) {
            dm.doValidate(metadata.getDataInfo().getSchemaId(), metadata.getId() + "",
                new Document(metadata.getXmlData(false)), context.getLanguage());
            dm.indexMetadata(metadata.getId() + "", true);
        }

        boolean isInvalid =
            (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

        return !isInvalid;
    }

    @ApiOperation(
        value = "Get record sharing settings",
        notes = "Return current sharing options for a record.",
        nickname = "getRecordSharingSettings")
    @RequestMapping(
        value = "/{metadataUuid}/sharing",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The record sharing settings."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public
    SharingResponse getRecordSharingSettings(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        // TODO: Restrict to user group only in response depending on settings?
        Metadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
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
        AccessManager am = appContext.getBean(AccessManager.class);
        Set<Integer> userGroups = am.getUserGroups(
            userSession,
            context.getIpAddress(), // TODO: Use the request
            false);

        List<Group> elGroup = appContext.getBean(GroupRepository.class).findAll();
        List<Operation> allOperations = appContext.getBean(OperationRepository.class).findAll();
        UserGroupRepository userGroupRepository = appContext.getBean(UserGroupRepository.class);
        OperationAllowedRepository opAllowRepository = appContext.getBean(OperationAllowedRepository.class);

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
                final Specifications<UserGroup> hasUserIdAndGroupId = where(hasGroupId).and(hasUserId);
                List<UserGroup> userGroupEntities = userGroupRepository.findAll(hasUserIdAndGroupId);
                List<Profile> userGroupProfile = new ArrayList<>();
                for (UserGroup ug : userGroupEntities) {
                    userGroupProfile.add(ug.getProfile());
                }
                groupPrivilege.setUserProfile(userGroupProfile);


                //--- get all operations that this group can do on given metadata
                Specifications<OperationAllowed> hasGroupIdAndMetadataId =
                    where(OperationAllowedSpecs.hasGroupId(g.getId()))
                        .and(OperationAllowedSpecs.hasMetadataId(metadata.getId()));
                List<OperationAllowed> operationAllowedForGroup =
                    opAllowRepository.findAll(hasGroupIdAndMetadataId);

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


    @ApiOperation(
        value = "Set record group",
        notes = "A record is related to one group.",
        nickname = "setRecordGroup")
    @RequestMapping(
        value = "/{metadataUuid}/group",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Record group updated."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setRecordGroup(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Group identifier",
            required = true
        )
        @RequestBody(
            required = true
        )
            Integer groupIdentifier,
        HttpServletRequest request
    )
        throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        Group group = appContext.getBean(GroupRepository.class).findOne(groupIdentifier);
        if (group == null) {
            throw new ResourceNotFoundException(String.format(
                "Group with identifier '%s' not found.", groupIdentifier
            ));
        }

        DataManager dataManager = appContext.getBean(DataManager.class);
        MetadataRepository metadataRepository = appContext.getBean(MetadataRepository.class);

        metadata.getSourceInfo().setGroupOwner(groupIdentifier);
        metadataRepository.save(metadata);
        dataManager.indexMetadata(String.valueOf(metadata.getId()), true);
    }


    @ApiOperation(
        value = "Get record sharing settings",
        notes = "",
        nickname = "getSharingSettings")
    @RequestMapping(
        value = "/sharing",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message =
            "Return a default array of group and operations " +
            "that can be used to set record sharing properties."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    @ResponseBody
    public
    SharingResponse getSharingSettings(
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(session);

        SharingResponse sharingResponse = new SharingResponse();
        sharingResponse.setOwner(userSession.getUserId());

        List<Operation> allOperations = appContext.getBean(OperationRepository.class).findAll();

        //--- retrieve groups operations
        AccessManager am = appContext.getBean(AccessManager.class);
        Set<Integer> userGroups = am.getUserGroups(
            context.getUserSession(),
            context.getIpAddress(), false);

        List<Group> elGroup = context.getBean(GroupRepository.class).findAll();
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


    @ApiOperation(
        value = "Set group and owner for one or more records",
        notes = "",
        nickname = "setGroupAndOwner")
    @RequestMapping(value = "/ownership",
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Records group and owner updated"),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    public
    @ResponseBody
    MetadataProcessingReport setGroupAndOwner(
        @ApiParam(value = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false)
            String[] uuids,
        @ApiParam(
            value = "Group identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer groupIdentifier,
        @ApiParam(
            value = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(
            required = false
        )
        String bucket,
        @ApiParam(
            value = "User identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer userIdentifier,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
            report.setTotalRecords(records.size());

            final ApplicationContext context = ApplicationContextHolder.get();
            final DataManager dataManager = context.getBean(DataManager.class);
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
            final AccessManager accessMan = context.getBean(AccessManager.class);
            final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                updateOwnership(groupIdentifier, userIdentifier,
                    report, dataManager, accessMan, metadataRepository,
                    serviceContext, listOfUpdatedRecords, uuid);
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



    @ApiOperation(
        value = "Set record group and owner",
        notes = "",
        nickname = "setRecordOwnership")
    @RequestMapping(
        value = "/{metadataUuid}/ownership",
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Record group and owner updated"),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @PreAuthorize("hasRole('Editor')")
    public
    @ResponseBody
    MetadataProcessingReport setRecordOwnership(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Group identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer groupIdentifier,
        @ApiParam(
            value = "User identifier",
            required = true
        )
        @RequestParam(
            required = true
        )
            Integer userIdentifier,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        try {
            report.setTotalRecords(1);

            final ApplicationContext context = ApplicationContextHolder.get();
            final DataManager dataManager = context.getBean(DataManager.class);
            final AccessManager accessMan = context.getBean(AccessManager.class);
            final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            List<String> listOfUpdatedRecords = new ArrayList<>();
            updateOwnership(groupIdentifier, userIdentifier,
                report, dataManager, accessMan, metadataRepository,
                serviceContext, listOfUpdatedRecords, metadataUuid);
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
                                 List<String> listOfUpdatedRecords, String uuid) throws Exception {
        Metadata metadata = metadataRepository.findOneByUuid(uuid);
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
                report.addMetadataInfos(metadata.getId(), String.format(
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
            // -- set the new owner into the metadata record
            dataManager.updateMetadataOwner(metadata.getId(),
                String.valueOf(userIdentifier),
                String.valueOf(groupIdentifier));
            report.addMetadataId(metadata.getId());
            report.incrementProcessedRecords();
            listOfUpdatedRecords.add(metadata.getId() + "");
        }
    }



    public static Vector<OperationAllowedId> retrievePrivileges(ServiceContext context, String id, Integer userId, Integer groupId) throws Exception {

        OperationAllowedRepository opAllowRepo = context.getBean(OperationAllowedRepository.class);

        int iMetadataId = Integer.parseInt(id);
        Specifications<OperationAllowed> spec =
            where(hasMetadataId(iMetadataId));
        if (groupId != null) {
            spec = spec.and(hasGroupId(groupId));
        }

        List<OperationAllowed> operationsAllowed = opAllowRepo.findAllWithOwner(userId, Optional.of((Specification<OperationAllowed>) spec));

        Vector<OperationAllowedId> result = new Vector<OperationAllowedId>();
        for (OperationAllowed operationAllowed : operationsAllowed) {
            result.add(operationAllowed.getId());
        }

        return result;
    }
}
