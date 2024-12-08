/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.*;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.config.IPublicationConfig;
import org.fao.geonet.config.PublicationOption;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordGroupOwnerChangeEvent;
import org.fao.geonet.events.history.RecordOwnerChangeEvent;
import org.fao.geonet.events.history.RecordPrivilegesChangeEvent;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.*;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.util.MetadataPublicationMailNotifier;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL;
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
public class MetadataSharingApi implements ApplicationEventPublisherAware
{
    private static final String DEFAULT_PUBLICATION_TYPE_NAME = "default";

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    FeedbackLanguages feedbackLanguages;

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
    IMetadataStatus metadataStatus;

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

    @Autowired
    MetadataStatusRepository metadataStatusRepository;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    MetadataPublicationMailNotifier metadataPublicationMailNotifier;

    /**
     * What does publish mean?
     */
    @Autowired
    private IPublicationConfig publicationConfig;

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(
        ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public static Vector<OperationAllowedId> retrievePrivileges(ServiceContext context, String id, Integer userId, Integer groupId) {

        OperationAllowedRepository opAllowRepo = context.getBean(OperationAllowedRepository.class);

        int iMetadataId = Integer.parseInt(id);
        Specification<OperationAllowed> spec =
            where(hasMetadataId(iMetadataId));
        if (groupId != null) {
            spec = spec.and(hasGroupId(groupId));
        }

        List<OperationAllowed> operationsAllowed = opAllowRepo.findAllWithOwner(userId, Optional.of(spec));

        Vector<OperationAllowedId> result = new Vector<>();
        for (OperationAllowed operationAllowed : operationsAllowed) {
            result.add(operationAllowed.getId());
        }

        return result;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get publication options.")
    @GetMapping(
        value = "/sharing/options"
    )
    @PreAuthorize("permitAll")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PublicationOption> getPublicationOptions() {
        return publicationConfig.getPublicationOptions();
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set privileges for ALL group to publish the metadata for all users.")
    @RequestMapping(
        value = "/{metadataUuid}/publish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated.", content = {@Content(schema = @Schema(hidden = true))}),
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
        @Parameter(
            description = "Publication type",
            required = false)
        String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        checkUserProfileToPublishMetadata(userSession);

        if (StringUtils.isEmpty(publicationType)) {
            publicationType = DEFAULT_PUBLICATION_TYPE_NAME;
        }

        shareMetadataWithReservedGroup(metadataUuid, true, publicationType, session, request);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationType);
        if (publicationOption.isPresent()) {
            AbstractMetadata metadata = ApiUtils.getRecord(metadataUuid);
            publicationConfig.processMetadata(serviceContext, publicationOption.get(), metadata.getId(), true);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Unsets privileges for ALL group to publish the metadata for all users.")
    @RequestMapping(
        value = "/{metadataUuid}/unpublish",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated.", content = {@Content(schema = @Schema(hidden = true))}),
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
        @Parameter(
            description = "Publication type",
            required = false)
        String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        checkUserProfileToUnpublishMetadata(userSession);

        if (StringUtils.isEmpty(publicationType)) {
            publicationType = DEFAULT_PUBLICATION_TYPE_NAME;
        }

        shareMetadataWithReservedGroup(metadataUuid, false, publicationType, session, request);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationType);
        if (publicationOption.isPresent()) {
            AbstractMetadata metadata = ApiUtils.getRecord(metadataUuid);
            publicationConfig.processMetadata(serviceContext, publicationOption.get(), metadata.getId(), false);
        }
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
            "<a href='https://geonetwork-opensource.org/manuals/trunk/eng/users/user-guide/publishing/managing-privileges.html'>More info</a>")
    @RequestMapping(
        value = "/{metadataUuid}/sharing",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Settings updated.", content = {@Content(schema = @Schema(hidden = true))}),
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
        Locale[] feedbackLocales = feedbackLanguages.getLocales(request.getLocale());

        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them
        boolean skipAllReservedGroup = !accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()));



        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }

        List<GroupOperations> privileges = sharing.getPrivileges();
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
        boolean notifyByEmail = StringUtils.isNoneEmpty(sm.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

        setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
            ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, null, request,
            metadataListToNotifyPublication, notifyByEmail);
        metadataIndexer.indexMetadataPrivileges(metadata.getUuid(), metadata.getId());

        if (notifyByEmail && !metadataListToNotifyPublication.isEmpty()) {
            metadataPublicationMailNotifier.notifyPublication(feedbackLocales,
                metadataListToNotifyPublication);
        }
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
    MetadataProcessingReport publishMultipleRecords(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @Parameter(
            description = "Publication type",
            required = false)
        String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        String publicationTypeToUse =
            StringUtils.isNotEmpty(publicationType) ? publicationType : DEFAULT_PUBLICATION_TYPE_NAME;
        SharingParameter sharing = buildSharingForPublicationConfig(true,
            publicationTypeToUse);
        MetadataProcessingReport metadataProcessingReport = shareSelection(uuids, bucket, sharing, session, request);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationTypeToUse);
        if (publicationOption.isPresent()) {
            Set<Integer> metadataProcessed = metadataProcessingReport.getMetadata();
            for(Integer metadataId: metadataProcessed) {
                publicationConfig.processMetadata(serviceContext, publicationOption.get(),
                    metadataId, true);
            }
        }

        return metadataProcessingReport;
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
    MetadataProcessingReport unpublishMultipleRecords(
        @Parameter(description = ApiParams.API_PARAM_RECORD_UUIDS_OR_SELECTION,
            required = false)
        @RequestParam(required = false) String[] uuids,
        @Parameter(description = ApiParams.API_PARAM_BUCKET_NAME,
            required = false)
        @RequestParam(required = false) String bucket,
        @Parameter(
            description = "Publication type",
            required = false)
        String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        String publicationTypeToUse =
            StringUtils.isNotEmpty(publicationType) ? publicationType : DEFAULT_PUBLICATION_TYPE_NAME;
        SharingParameter sharing = buildSharingForPublicationConfig(false,
            publicationTypeToUse);
        MetadataProcessingReport metadataProcessingReport = shareSelection(uuids, bucket, sharing, session, request);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationTypeToUse);
        if (publicationOption.isPresent()) {
            Set<Integer> metadataProcessed = metadataProcessingReport.getMetadata();
            for(Integer metadataId: metadataProcessed) {
                publicationConfig.processMetadata(serviceContext, publicationOption.get(),
                    metadataId, false);
            }
        }
        return metadataProcessingReport;
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
    MetadataProcessingReport shareMultipleRecords(
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
        HttpServletRequest request,
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication,
        boolean notifyByMail) throws Exception {
        if (privileges != null) {

            ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());

            boolean sharingChanges = false;

            boolean allowPublishInvalidMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_INVALID_MD);
            boolean allowPublishNonApprovedMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_PUBLISH_NON_APPROVED_MD);

            boolean isMdWorkflowEnable = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);

            Integer groupOwnerId = metadata.getSourceInfo().getGroupOwner();

            // Check not trying to publish a retired metadata
            if (isMdWorkflowEnable && (groupOwnerId != null)) {
                java.util.Optional<Group> groupOwner = groupRepository.findById(groupOwnerId);
                boolean isGroupWithEnabledWorkflow = WorkflowUtil.isGroupWithEnabledWorkflow(groupOwner.get().getName());

                if (isGroupWithEnabledWorkflow) {
                    MetadataStatus mdStatus = metadataStatus.getStatus(metadata.getId());
                    if ((mdStatus != null) &&
                        (mdStatus.getStatusValue().getId() == Integer.parseInt(StatusValue.Status.RETIRED))) {
                        List<GroupOperations> allGroupOps =
                            privileges.stream().filter(p -> p.getGroup() == ReservedGroup.all.getId()).collect(Collectors.toList());

                        for (GroupOperations p : allGroupOps) {
                            if (p.getOperations().containsValue(true)) {
                                throw new IllegalStateException(String.format("Retired metadata %s can't be published.",
                                    metadata.getUuid()));
                            }
                        }
                    }
                }
            }

            SharingResponse sharingBefore = getRecordSharingSettings(metadata.getUuid(), request.getSession(), request);

            // Check if the user profile can change the privileges for publication/un-publication of the reserved groups
            checkChangesAllowedToUserProfileForReservedGroups(context.getUserSession(), sharingBefore, privileges, !sharing.isClear());

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

                    if (Boolean.TRUE.equals(o.getValue())) {
                        // For privileges to ALL group, check if it's allowed or not to publish invalid metadata
                        if ((p.getGroup() == ReservedGroup.all.getId())) {
                            try {
                                checkCanPublishToAllGroup(context, messages, metadata,
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
                    } else if (!sharing.isClear() && Boolean.TRUE.equals(!o.getValue())) {
                        dataMan.unsetOperation(
                            context, metadata.getId(), p.getGroup(), opId);
                        sharingChanges = true;
                    }
                }
            }

            java.util.Optional<GroupPrivilege> allGroupPrivsBefore =
                sharingBefore.getPrivileges().stream().filter(p -> p.getGroup() == ReservedGroup.all.getId()).findFirst();
            boolean publishedBefore = allGroupPrivsBefore.get().getOperations().get(ReservedOperation.view.name());

            if (sharing.isClear() && publishedBefore && !metadataUtils.isMetadataPublished(metadata.getId())) {
                // Throw the metadata unpublish event, when removing privileges, are not part of the parameters,
                // not processed in setOperation / unsetOperation that triggers the metadata publish/unpublish events
                eventPublisher.publishEvent(new MetadataUnpublished(metadata));
            }

            if (notifyByMail) {
                java.util.Optional<GroupOperations> allGroupOpsAfter =
                    privileges.stream().filter(p -> p.getGroup() == ReservedGroup.all.getId()).findFirst();

                // If we cannot find it then default to before value so that it will fail the next condition.
                boolean publishedAfter = allGroupOpsAfter.isPresent()?allGroupOpsAfter.get().getOperations().getOrDefault(ReservedOperation.view.name(), publishedBefore):publishedBefore;

                if (publishedBefore != publishedAfter) {
                    MetadataPublicationNotificationInfo metadataNotificationInfo = new MetadataPublicationNotificationInfo();
                    metadataNotificationInfo.setMetadataUuid(metadata.getUuid());
                    metadataNotificationInfo.setMetadataId(metadata.getId());
                    metadataNotificationInfo.setGroupId(metadata.getSourceInfo().getGroupOwner());
                    metadataNotificationInfo.setPublished(publishedAfter);
                    metadataNotificationInfo.setPublicationDateStamp(new ISODate());

                    java.util.Optional<User> publishUser = userRepository.findById(userId);

                    if (publishUser.isPresent()) {
                        metadataNotificationInfo.setPublisherUser(publishUser.get().getUsername());
                    }

                    // If the metadata workflow is enabled retrieve the submitter and reviewer users information
                    if (isMdWorkflowEnable) {
                        String sortField = SortUtils.createPath(MetadataStatus_.changeDate);
                        List<MetadataStatus> statusList = metadataStatusRepository.findAllByMetadataIdAndByType(metadata.getId(),
                            StatusValueType.workflow, Sort.by(Sort.Direction.DESC, sortField));

                        java.util.Optional<MetadataStatus> approvedStatus = statusList.stream().filter(status ->
                            status.getStatusValue().getId() == Integer.parseInt(StatusValue.Status.APPROVED)).findFirst();
                        if (approvedStatus.isPresent()) {
                            java.util.Optional<User> reviewerUser = userRepository.findById(approvedStatus.get().getUserId());
                            reviewerUser.ifPresent(user -> metadataNotificationInfo.setReviewerUser(user.getUsername()));
                        }

                        java.util.Optional<MetadataStatus> submittedStatus = statusList.stream().filter(status ->
                            status.getStatusValue().getId() == Integer.parseInt(StatusValue.Status.SUBMITTED)).findFirst();
                        if (submittedStatus.isPresent()) {
                            java.util.Optional<User> submitterUser = userRepository.findById(submittedStatus.get().getUserId());
                            submitterUser.ifPresent(user -> metadataNotificationInfo.setSubmitterUser(user.getUsername()));
                        }
                    }

                    metadataListToNotifyPublication.add(metadataNotificationInfo);
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
        value = "/{metadataUuid}/sharing",
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
        ServiceContext context = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(session);

        SharingResponse sharingResponse = new SharingResponse();
        sharingResponse.setOwner(userSession.getUserId());
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();
        if (groupOwner != null) {
            sharingResponse.setGroupOwner(String.valueOf(groupOwner));
        }

        String network = sm.getValue(Settings.SYSTEM_INTRANET_NETWORK);
        boolean hasNetworkConfig = StringUtils.isNotEmpty(network);

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
                if (!hasNetworkConfig
                    && g.getId() == ReservedGroup.intranet.getId()) {
                    continue;
                }
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
        value = "/{metadataUuid}/group",
        method = RequestMethod.PUT
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record group updated.", content = {@Content(schema = @Schema(hidden = true))}),
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

        java.util.Optional<Group> group = groupRepository.findById(groupIdentifier);
        if (!group.isPresent()) {
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
        dataManager.indexMetadata(String.valueOf(metadata.getId()), DirectIndexSubmitter.INSTANCE);

        new RecordGroupOwnerChangeEvent(metadata.getId(),
                                        ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                                        ObjectJSONUtils.convertObjectInJsonObject(oldGroup, RecordGroupOwnerChangeEvent.FIELD),
                                        ObjectJSONUtils.convertObjectInJsonObject(group.get(), RecordGroupOwnerChangeEvent.FIELD)).publish(appContext);
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

        String network = sm.getValue(Settings.SYSTEM_INTRANET_NETWORK);
        boolean hasNetworkConfig = StringUtils.isNotEmpty(network);

        for (Group g : elGroup) {
            if (!hasNetworkConfig
                && g.getId() == ReservedGroup.intranet.getId()) {
                continue;
            }
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

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);

            List<String> listOfUpdatedRecords = new ArrayList<>();
            for (String uuid : records) {
                updateOwnership(groupIdentifier, userIdentifier,
                    report, dataManager, accessManager,
                    serviceContext, listOfUpdatedRecords, uuid, session);
            }
            metadataManager.flush();
            metadataIndexer.indexMetadata(listOfUpdatedRecords);

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
        value = "/{metadataUuid}/ownership",
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

        ApiUtils.canEditRecord(metadataUuid, request);
        try {
            report.setTotalRecords(1);

            ServiceContext serviceContext = ApiUtils.createServiceContext(request);
            List<String> listOfUpdatedRecords = new ArrayList<>();
            updateOwnership(groupIdentifier, userIdentifier,
                report, dataManager, accessManager,
                serviceContext, listOfUpdatedRecords, metadataUuid, session);
            metadataManager.flush();
            metadataIndexer.indexMetadata(listOfUpdatedRecords);

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
                                 ServiceContext serviceContext,
                                 List<String> listOfUpdatedRecords, String uuid,
                                 HttpSession session) throws Exception {
        AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
        if (metadata == null) {
            report.incrementNullRecords();
        } else if (!accessMan.canEdit(
            serviceContext, String.valueOf(metadata.getId()))) {
            report.addNotEditableMetadataId(metadata.getId());
        } else {
            // Retrieve the identifiers associated with the metadata uuid.
            // When the workflow is enabled, the metadata can have an approved and a working copy version.
            List<Integer> idList = metadataUtils.findAllIdsBy(MetadataSpecs.hasMetadataUuid(uuid));

            // Increase the total records counter when processing a metadata with approved and working copies
            // as the initial counter doesn't take in account this case
            if (idList.size() > 1) {
                report.setTotalRecords(report.getNumberOfRecords() + 1);
            }

            for (Integer mdId : idList) {
                if (mdId != metadata.getId()) {
                    metadata = metadataUtils.findOne(mdId);
                }

                //-- Get existing owner and privileges for that owner - note that
                //-- owners don't actually have explicit permissions - only their
                //-- group does which is why we have an ownerGroup (parameter groupid)
                Integer sourceUsr = metadata.getSourceInfo().getOwner();
                Integer sourceGrp = metadata.getSourceInfo().getGroupOwner();
                Vector<OperationAllowedId> sourcePriv =
                    retrievePrivileges(serviceContext, String.valueOf(metadata.getId()), sourceUsr, sourceGrp);

                // Let's not reassign to the reserved groups.
                // If the request is to reassign to reserved group then ignore the request and
                // use the source group.
                Integer groupIdentifierUsed = groupIdentifier;
                if (ReservedGroup.isReserved(groupIdentifier)) {
                    groupIdentifierUsed = sourceGrp;
                    report.addMetadataInfos(metadata, String.format(
                        "Reserved group '%s' on metadata '%s' is not allowed. Group owner will not be changed.",
                        groupIdentifier, metadata.getUuid()
                    ));
                }

                // -- Set new privileges for new owner from privileges of the old
                // -- owner, if none then set defaults
                if (sourcePriv.isEmpty()) {
                    dataManager.copyDefaultPrivForGroup(
                        serviceContext,
                        String.valueOf(metadata.getId()),
                        String.valueOf(groupIdentifierUsed),
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
                            groupIdentifierUsed,
                            priv.getOperationId());
                    }
                }

                Long metadataId = Long.valueOf(metadata.getId());
                ApplicationContext context = ApplicationContextHolder.get();
                if (!Objects.equals(groupIdentifierUsed, sourceGrp)) {
                    Group newGroup = groupRepository.findById(groupIdentifierUsed).get();
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
                    String.valueOf(groupIdentifierUsed));
                report.addMetadataId(metadata.getId());
                report.incrementProcessedRecords();
                listOfUpdatedRecords.add(metadata.getId() + "");
            }
        }
    }

    /**
     * For privileges to {@link ReservedGroup#all} group, check if it's allowed or not to publish invalid metadata.
     *
     * @param context
     * @param messages
     * @param metadata
     * @param allowPublishInvalidMd
     * @param allowPublishNonApprovedMd
     * @throws Exception
     */
    private void checkCanPublishToAllGroup(ServiceContext context, ResourceBundle messages, AbstractMetadata metadata,
                                           boolean allowPublishInvalidMd, boolean allowPublishNonApprovedMd) throws Exception {

        if (!allowPublishInvalidMd) {
            boolean hasValidation =
                (metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(metadata.getId())) > 0);

            if (!hasValidation) {
                validator.doValidate(metadata, context.getLanguage());
                metadataIndexer.indexMetadata(metadata.getId() + "", DirectIndexSubmitter.INSTANCE, IndexingMode.full);
            }

            boolean isInvalid =
                (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

            if (isInvalid) {
                throw new Exception(String.format(messages.getString("api.metadata.share.errorMetadataNotValid"), metadata.getUuid()));
            }
        }

        if (!allowPublishNonApprovedMd) {
            MetadataStatus metadataStatusValue = metadataStatus.getStatus(metadata.getId());
            if (metadataStatusValue != null) {
                String statusId = metadataStatusValue.getStatusValue().getId() + "";
                boolean isApproved = statusId.equals(StatusValue.Status.APPROVED);

                if (!isApproved) {
                    throw new Exception(String.format(messages.getString("api.metadata.share.errorMetadataNotApproved"), metadata.getUuid()));
                }
            }
        }

    }


    /**
     * Shares a metadata based on the publicationConfig to publish/unpublish it.
     *
     * @param metadataUuid Metadata uuid.
     * @param publish      Flag to publish/unpublish the metadata.
     * @param session
     * @param request
     * @throws Exception
     */
    private void shareMetadataWithReservedGroup(String metadataUuid, boolean publish, String publicationType,
                                           HttpSession session, HttpServletRequest request) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());
        Locale[] feedbackLocales = feedbackLanguages.getLocales(request.getLocale());

        if (!accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
            throw new Exception(String.format(messages.getString("api.metadata.share.ErrorUserNotAllowedToPublish"),
                metadataUuid, messages.getString(accessManager.getReviewerRule())));

        }

        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }

        SharingParameter sharing = buildSharingForPublicationConfig(publish, publicationType);

        List<GroupOperations> privileges = sharing.getPrivileges();
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
        boolean notifyByEmail = StringUtils.isNoneEmpty(sm.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

        setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
            ApiUtils.getUserSession(session).getUserIdAsInt(), true, null, request,
            metadataListToNotifyPublication, notifyByEmail);
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationType);
        if (publicationOption.isPresent() &&
            publicationOption.get().hasPublicationTo(ReservedGroup.all) &&
            notifyByEmail &&
            !metadataListToNotifyPublication.isEmpty()) {
            metadataPublicationMailNotifier.notifyPublication(feedbackLocales,
                metadataListToNotifyPublication);
        }
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

            ServiceContext context = ApiUtils.createServiceContext(request);
            Locale[] feedbackLocales = feedbackLanguages.getLocales(request.getLocale());

            List<String> listOfUpdatedRecords = new ArrayList<>();
            List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
            boolean notifyByEmail = StringUtils.isNoneEmpty(sm.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

            for (String uuid : records) {
                AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessManager.canEdit(
                    ApiUtils.createServiceContext(request), String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    boolean skipAllReservedGroup = false;
                    if (!accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
                        skipAllReservedGroup = true;
                    }

                    List<Operation> operationList = operationRepository.findAll();
                    Map<String, Integer> operationMap = new HashMap<>(operationList.size());
                    for (Operation o : operationList) {
                        operationMap.put(o.getName(), o.getId());
                    }

                    List<GroupOperations> privileges = sharing.getPrivileges();
                    List<GroupOperations> allGroupPrivileges = new ArrayList<>();

                    try {
                        if (metadata instanceof MetadataDraft) {
                            // If the metadata is a working copy, publish privileges (ALL and INTRANET groups)
                            // should be applied to the approved version.
                            Metadata md = this.metadataRepository.findOneByUuid(metadata.getUuid());

                            if (md != null) {
                                setOperations(sharing, dataManager, context, appContext, md, operationMap, allGroupPrivileges,
                                    ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, report, request,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(md.getId()));
                                report.addMetadataId(metadata.getId());
                            } else {
                                setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
                                    ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, report, request,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                                report.addMetadataId(metadata.getId());
                            }

                        } else {
                            setOperations(sharing, dataManager, context, appContext, metadata, operationMap, privileges,
                                ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, report, request,
                                metadataListToNotifyPublication, notifyByEmail);

                            report.incrementProcessedRecords();
                            listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                            report.addMetadataId(metadata.getId());
                        }
                    } catch (NotAllowedException ex) {
                        report.addMetadataError(metadata, ex.getMessage());
                        report.incrementUnchangedRecords();
                    }
                }
            }

            if (!metadataListToNotifyPublication.isEmpty()) {
                metadataPublicationMailNotifier.notifyPublication(feedbackLocales,
                    metadataListToNotifyPublication);
            }

            metadataManager.flush();
            metadataIndexer.indexMetadata(listOfUpdatedRecords);

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
    private SharingParameter buildSharingForPublicationConfig(boolean publish, String configName) {
        SharingParameter sharing = new SharingParameter();
        sharing.setClear(false);

        List<GroupOperations> privilegesList = new ArrayList<>();

        java.util.Optional<PublicationOption> publicationOptionOptional =
            publicationConfig.getPublicationOptionConfiguration(configName);

        if (publicationOptionOptional.isPresent()) {
            // Process the publication group and operations
            PublicationOption publicationOption = publicationOptionOptional.get();

            Iterator<ReservedOperation> it = publicationOption.getPublicationOperations().iterator();
            GroupOperations privReservedGroup = new GroupOperations();
            privReservedGroup.setGroup(publicationOption.getPublicationGroup().getId());
            Map<String, Boolean> operations = new HashMap<>();

            while (it.hasNext()) {
                ReservedOperation operation = it.next();
                operations.put(operation.toString(), publish);
            }
            privReservedGroup.setOperations(operations);
            privilegesList.add(privReservedGroup);

            // Process the additional publication group(s) and operations
            Iterator<Map.Entry<ReservedGroup, List<ReservedOperation>>> it2 =
                publicationOption.getAdditionalPublications().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<ReservedGroup, List<ReservedOperation>> info = it2.next();

                privReservedGroup = new GroupOperations();
                privReservedGroup.setGroup(info.getKey().getId());

                operations = new HashMap<>();
                for (ReservedOperation operation : info.getValue()) {
                    operations.put(operation.toString(), publish);
                }

                privReservedGroup.setOperations(operations);
                privilegesList.add(privReservedGroup);
            }
        }

        sharing.setPrivileges(privilegesList);
        return sharing;
    }


    /**
     * Verifies if the user profile can make the privileges changes for reserved groups.
     *
     * @param userSession
     * @param originalPrivileges
     * @param newPrivileges
     * @param merge
     */
    private void checkChangesAllowedToUserProfileForReservedGroups(UserSession userSession,
                                                                   SharingResponse originalPrivileges,
                                                                   List<GroupOperations> newPrivileges,
                                                                   boolean merge) {
        if (userSession.getProfile() == Profile.Administrator) {
            return;
        }

        List<PrivilegeStatusChange> privilegeStatusChangesList =
            reservedGroupsPrivilegesStatusChanges(originalPrivileges, newPrivileges, merge);

        if (!privilegeStatusChangesList.isEmpty()) {
            boolean metadataWasPublishedBeforeAndNotAfter = false;
            boolean metadataWasNotPublishedBeforeAndIsAfter = false;

            for (PrivilegeStatusChange status : privilegeStatusChangesList) {
                if (status.isPublishedBefore() && !status.isPublishedAfter()) {
                    metadataWasPublishedBeforeAndNotAfter = true;
                } else if (!status.isPublishedBefore() && status.isPublishedAfter()) {
                    metadataWasNotPublishedBeforeAndIsAfter = true;
                }
            }

            if (metadataWasPublishedBeforeAndNotAfter) {
                // Is the user profile allowed to un-publish the metadata?
                checkUserProfileToUnpublishMetadata(userSession);
            }

            if (metadataWasNotPublishedBeforeAndIsAfter) {
                // Is the user profile allowed to publish the metadata?
                checkUserProfileToPublishMetadata(userSession);
            }
        }
    }

    /**
     * Checks if the user profile is allowed to publish metadata.
     *
     * @param userSession
     */
    private void checkUserProfileToPublishMetadata(UserSession userSession) {
        if (userSession.getProfile() != Profile.Administrator) {
            String allowedUserProfileToPublishMetadata =
                org.apache.commons.lang.StringUtils.defaultIfBlank(sm.getValue(Settings.METADATA_PUBLISH_USERPROFILE), Profile.Reviewer.toString());

            // Is the user profile is higher than the profile allowed to import metadata?
            if (!UserUtil.hasHierarchyRole(allowedUserProfileToPublishMetadata, this.roleHierarchy)) {
                throw new NotAllowedException(String.format(
                    "Publication of metadata is not allowed. User needs to be at least %s to publish record.", allowedUserProfileToPublishMetadata));
            }
        }
    }

    /**
     * Checks if the user profile is allowed to un-publish metadata.
     *
     * @param userSession
     */
    private void checkUserProfileToUnpublishMetadata(UserSession userSession) {
        if (userSession.getProfile() != Profile.Administrator) {
            String allowedUserProfileToUnpublishMetadata =
                org.apache.commons.lang.StringUtils.defaultIfBlank(sm.getValue(Settings.METADATA_UNPUBLISH_USERPROFILE), Profile.Reviewer.toString());

            // Is the user profile is higher than the profile allowed to import metadata?
            if (!UserUtil.hasHierarchyRole(allowedUserProfileToUnpublishMetadata, this.roleHierarchy)) {
                throw new NotAllowedException(String.format(
                    "Unpublication of metadata is not allowed. User needs to be at least %s to unpublish record.", allowedUserProfileToUnpublishMetadata));
            }
        }
    }

    /**
     * Returns the list of privilege changes for the reserved groups.
     *
     * @param sharingBefore Metadata privileges before applying the new privileges.
     * @param newPrivileges New metadata privileges.
     * @param merge         Merge the new privileges or replace them.
     * @return List of privilege changes for the reserved groups.
     */
    public List<PrivilegeStatusChange> reservedGroupsPrivilegesStatusChanges(SharingResponse sharingBefore,
                                                                             List<GroupOperations> newPrivileges,
                                                                             boolean merge) {

        List<PrivilegeStatusChange> privilegeStatuses = new ArrayList<>();
        for (GroupPrivilege g : sharingBefore.getPrivileges()) {
            if (g.isReserved()) {
                ReservedGroup group = Arrays.stream(ReservedGroup.values()).filter(rg -> rg.getId() == g.getGroup()).findFirst().get();

                Map<String, Boolean> operationsAllGroupAfter = new HashMap<>();
                java.util.Optional<GroupOperations> groupPrivilegeAllGroupAfter =
                    newPrivileges.stream().filter(gp -> group.getId() == gp.getGroup().intValue()).findFirst();
                if (groupPrivilegeAllGroupAfter.isPresent()) {
                    operationsAllGroupAfter = groupPrivilegeAllGroupAfter.get().getOperations();
                }

                for (Map.Entry<String, Boolean> op : g.getOperations().entrySet()) {
                    PrivilegeStatusChange privilegeStatus = new PrivilegeStatusChange();
                    privilegeStatus.setGroup(group);
                    privilegeStatus.setPublishedBefore(g.getOperations().getOrDefault(op.getKey(), Boolean.FALSE));
                    // When merging privileges and no value for the new privilege
                    // uses as default the previous value, otherwise false.
                    privilegeStatus.setPublishedAfter(operationsAllGroupAfter.getOrDefault(op.getKey(),
                        merge ? privilegeStatus.publishedBefore : Boolean.FALSE));
                    privilegeStatus.setOperation(op.getKey());
                    privilegeStatuses.add(privilegeStatus);
                }
            }
        }

        return privilegeStatuses.stream().filter(p -> p.publishedBefore != p.publishedAfter).collect(Collectors.toList());
    }

    /**
     * Class to track the privileges status changes on the reserved groups operations.
     */
    private class PrivilegeStatusChange {
        private boolean publishedBefore;
        private boolean publishedAfter;
        private ReservedGroup group;
        private String operation;

        public boolean isPublishedBefore() {
            return publishedBefore;
        }

        public void setPublishedBefore(boolean publishedBefore) {
            this.publishedBefore = publishedBefore;
        }

        public boolean isPublishedAfter() {
            return publishedAfter;
        }

        public void setPublishedAfter(boolean publishedAfter) {
            this.publishedAfter = publishedAfter;
        }

        public ReservedGroup getGroup() {
            return group;
        }

        public void setGroup(ReservedGroup group) {
            this.group = group;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }
    }
}
