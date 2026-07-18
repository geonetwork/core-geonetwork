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
import org.fao.geonet.api.records.model.MetadataPublicationNotificationInfo;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.config.IPublicationConfig;
import org.fao.geonet.config.PublicationOption;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordGroupOwnerChangeEvent;
import org.fao.geonet.events.history.RecordOwnerChangeEvent;
import org.fao.geonet.events.history.RecordPrivilegesChangeEvent;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.*;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.api.records.model.GroupOperations;
import org.fao.geonet.api.records.model.GroupPrivilege;
import org.fao.geonet.kernel.metadata.MetadataPublicationService;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.util.MetadataPublicationMailNotifier;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.support.ResourceBundleMessageSource;
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
public class MetadataSharingApi {
    private static final String DEFAULT_PUBLICATION_TYPE_NAME = "default";

    /**
     * Language utils used to detect the requested language.
     */
    @Autowired
    LanguageUtils languageUtils;

    /**
     * Message source.
     */
    @Autowired
    @Qualifier("apiMessages")
    ResourceBundleMessageSource messages;

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
    IMetadataManager metadataManager;

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
    RoleHierarchy roleHierarchy;

    @Autowired
    MetadataPublicationMailNotifier metadataPublicationMailNotifier;

    /**
     * What does publish mean?
     */
    @Autowired
    private IPublicationConfig publicationConfig;

    @Autowired
    private MetadataPublicationService metadataPublicationService;

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
        @RequestParam(required = false) String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(request.getSession());
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();
        checkUserProfileToPublishMetadata(groupOwner, userSession);

        if (StringUtils.isEmpty(publicationType)) {
            publicationType = DEFAULT_PUBLICATION_TYPE_NAME;
        }

        metadataPublicationService.shareMetadataWithReservedGroup(serviceContext, metadata, true, publicationType);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationType);
        if (publicationOption.isPresent()) {
            metadata = ApiUtils.getRecord(metadataUuid);
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
        @RequestParam(required = false) String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        UserSession userSession = ApiUtils.getUserSession(request.getSession());

        // Get the id of the group that owns the metadata record identified by the uuid
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();
        checkUserProfileToUnpublishMetadata(groupOwner, userSession);

        if (StringUtils.isEmpty(publicationType)) {
            publicationType = DEFAULT_PUBLICATION_TYPE_NAME;
        }

        metadataPublicationService.shareMetadataWithReservedGroup(serviceContext, metadata, false, publicationType);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationType);
        if (publicationOption.isPresent()) {
            metadata = ApiUtils.getRecord(metadataUuid);
            publicationConfig.processMetadata(serviceContext, publicationOption.get(), metadata.getId(), false);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Set record sharing",
        description = "Privileges are assigned by group. User needs to be able " +
            "to edit a record to set sharing settings. For reserved group " +
            "(ie. Internet, Intranet & Guest), user MUST have the configured " +
            "publication profile in the metadata owner group. " +
            "For other group, if Only set privileges to user's groups is set " +
            "in catalog configuration user MUST be a member of the group.<br/>" +
            "Clear first allows to unset all operations first before setting the new ones." +
            "Clear option does not remove reserved groups operation if user does not " +
            "meet reserved-group publication permissions for the record.<br/>" +
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

        metadataPublicationService.setOperations(sharing, dataManager, context, appContext, metadata, operationMap,
            privileges, ApiUtils.getUserSession(session).getUserIdAsInt(), skipAllReservedGroup, null,
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
        @RequestParam(required = false) String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        String publicationTypeToUse =
            StringUtils.isNotEmpty(publicationType) ? publicationType : DEFAULT_PUBLICATION_TYPE_NAME;

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
        MetadataProcessingReport metadataProcessingReport = metadataPublicationService.shareSelection(serviceContext, records, true, publicationTypeToUse);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationTypeToUse);
        if (publicationOption.isPresent()) {
            Set<Integer> metadataProcessed = metadataProcessingReport.getMetadata();
            for (Integer metadataId : metadataProcessed) {
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
        @RequestParam(required = false) String publicationType,
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);
        String publicationTypeToUse =
            StringUtils.isNotEmpty(publicationType) ? publicationType : DEFAULT_PUBLICATION_TYPE_NAME;
        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
        MetadataProcessingReport metadataProcessingReport = metadataPublicationService.shareSelection(serviceContext, records, false, publicationTypeToUse);

        java.util.Optional<PublicationOption> publicationOption = publicationConfig.getPublicationOptionConfiguration(publicationTypeToUse);
        if (publicationOption.isPresent()) {
            Set<Integer> metadataProcessed = metadataProcessingReport.getMetadata();
            for (Integer metadataId : metadataProcessed) {
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
    ) {
        ServiceContext serviceContext = ApiUtils.createServiceContext(request);

        Set<String> records = ApiUtils.getUuidsParameterOrSelection(uuids, bucket, ApiUtils.getUserSession(session));
        return metadataPublicationService.shareSelection(serviceContext, records, sharing);
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
                if (g.getType() == GroupType.SystemPrivilege) {
                    continue;
                }
                GroupPrivilege groupPrivilege = new GroupPrivilege();
                groupPrivilege.setGroup(g.getId());
                groupPrivilege.setReserved(g.isReserved());
                groupPrivilege.setRecordPrivilege(g.getType() == GroupType.RecordPrivilege);
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

                // Restrict changing privileges for groups with a required profile for setting privileges set
                groupPrivilege.setRestricted(!canUserChangePrivilegesForGroup(context, g));

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
        @Parameter(hidden = true)
        HttpSession session,
        HttpServletRequest request
    )
        throws Exception {

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext appContext = ApplicationContextHolder.get();

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        checkGroupIsWorkspace(groupIdentifier, locale);

        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        java.util.Optional<Group> group = groupRepository.findById(groupIdentifier);
        if (group.isEmpty()) {
            throw new ResourceNotFoundException(String.format(
                "Group with identifier '%s' not found.", groupIdentifier
            ));
        }

        Integer previousGroup = metadata.getSourceInfo().getGroupOwner();
        Group oldGroup = null;
        if (previousGroup != null) {
            oldGroup = groupRepository.findById(previousGroup).get();
        }

        //update group ownership
        // When workflow is enabled, there may be a draft and an approved version.
        // Both need to be updated.
        List<Integer> allMetadataIds = this.metadataUtils.findAllIdsBy(
            MetadataSpecs.hasMetadataUuid(metadata.getUuid()));

        if (!allMetadataIds.isEmpty()) {
            for (Integer mdId : allMetadataIds) {
                AbstractMetadata md = metadataUtils.findOne(mdId);
                if (md != null) {
                    if (!accessManager.canEdit(context, String.valueOf(md.getId()))) {
                        throw new NotAllowedException(
                            "User does not have permission to edit all versions of metadata " + metadataUuid);
                    }
                    md.getSourceInfo().setGroupOwner(groupIdentifier);
                    metadataManager.save(md);
                } else {
                    throw new ResourceNotFoundException(String.format(
                        "Metadata with uuid '%s' and identifier '%d' not found.", metadataUuid, mdId
                    ));
                }
            }
        } else {
            throw new ResourceNotFoundException(String.format(
                "Metadata with uuid '%s' not found.", metadataUuid
            ));
        }

        //need to update permissions.
        // 1. drop ALL permissions for the "old" group
        // 2. ADD these permissions to the new group.

        // get the current set of privileges
        SharingResponse sharingResponse = getRecordSharingSettings(metadataUuid, session, request);

        //copy so we can publish the diff (we are modifying sharingResponse with updated permissions)
        var originalSharingResponse = new SharingResponse();
        originalSharingResponse.setGroupOwner(sharingResponse.getGroupOwner());
        originalSharingResponse.setOwner(sharingResponse.getOwner());
        var copyPrivs = sharingResponse.getPrivileges().stream()
            .map(x -> {
                var result = new GroupPrivilege();
                result.setGroup(x.getGroup());
                result.setReserved(x.isReserved());
                result.setRestricted(x.isRestricted());
                result.setRecordPrivilege(x.isRecordPrivilege());
                result.setUserGroup(x.isUserGroup());
                result.setUserProfile(x.getUserProfiles());
                result.setOperations(new HashMap<>(x.getOperations()));
                return result;
            })
            .collect(Collectors.toList());
        originalSharingResponse.setPrivileges(copyPrivs);


        //old permissions
        Integer sourceUsr = metadata.getSourceInfo().getOwner();
        String metadataId = String.valueOf(metadata.getId());
        Integer oldGroupId = oldGroup != null ? oldGroup.getId() : null;
        Vector<OperationAllowedId> oldPriv = retrievePrivileges(context, metadataId, sourceUsr, oldGroupId);


        //modify the sharingResponse (current permissions from DB) with new privileges (group XFER).
        for (var groupPriv : sharingResponse.getPrivileges()) {
            if (Objects.equals(groupPriv.getGroup(), previousGroup)) {
                //old group: permissions - remove them (set all false)
                var ops = groupPriv.getOperations();
                ops.replaceAll((o, v) -> false);
            } else if (Objects.equals(groupPriv.getGroup(), groupIdentifier)) {
                //new group: permissions - ADD the old groups permissions
                var ops = groupPriv.getOperations();
                for (var addOp : oldPriv) {
                    var opName = ReservedOperation.lookup(addOp.getOperationId());
                    ops.put(opName.toString(), true);
                }
            }
        }

        //convert object for the two apis (getRecordSharingSettings vs setOperations)
        var newPrivileges = sharingResponse.getPrivileges().stream()
            .map(p -> {
                var result = new GroupOperations();
                result.setGroup(p.getGroup());
                result.setOperations(p.getOperations());
                return result;
            })
            .collect(Collectors.toList());


        SharingParameter sharingParameter = new SharingParameter();
        sharingParameter.setClear(true); // remove all permissions then add the new ones
        sharingParameter.setPrivileges(newPrivileges);

        List<Operation> operationList = operationRepository.findAll();
        Map<String, Integer> operationMap = new HashMap<>(operationList.size());
        for (Operation o : operationList) {
            operationMap.put(o.getName(), o.getId());
        }
        List<GroupOperations> privileges = sharingParameter.getPrivileges();

        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them
        boolean skipAllReservedGroup = !accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()));

        MetadataProcessingReport metadataProcessingReport = null;
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
        boolean notifyByEmail = StringUtils.isNoneEmpty(sm.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));
        Locale[] feedbackLocales = feedbackLanguages.getLocales(request.getLocale());

        //actually update DB
        metadataPublicationService.setOperations(sharingParameter,
            this.dataManager,
            context,
            appContext,
            metadata,
            operationMap,
            privileges,
            sourceUsr,
            skipAllReservedGroup,
            metadataProcessingReport,
            metadataListToNotifyPublication,
            notifyByEmail
        );

        if (notifyByEmail && !metadataListToNotifyPublication.isEmpty()) {
            metadataPublicationMailNotifier.notifyPublication(feedbackLocales,
                metadataListToNotifyPublication);
        }


        dataManager.indexMetadata(String.valueOf(metadata.getId()), DirectIndexSubmitter.INSTANCE);

        //publish group change
        new RecordGroupOwnerChangeEvent(metadata.getId(),
            ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
            ObjectJSONUtils.convertObjectInJsonObject(oldGroup, RecordGroupOwnerChangeEvent.FIELD),
            ObjectJSONUtils.convertObjectInJsonObject(group.get(), RecordGroupOwnerChangeEvent.FIELD)).publish(appContext);

        //publish permissions change
        new RecordPrivilegesChangeEvent(metadata.getId(),
            ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
            ObjectJSONUtils.convertObjectInJsonObject(originalSharingResponse.getPrivileges(), RecordPrivilegesChangeEvent.FIELD),
            ObjectJSONUtils.convertObjectInJsonObject(newPrivileges, RecordPrivilegesChangeEvent.FIELD)).publish(appContext);

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

        SharingResponse sharingResponse = new SharingResponse();
        if (session != null) {
            UserSession userSession = ApiUtils.getUserSession(session);
            sharingResponse.setOwner(userSession.getUserId());
        }

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
            if (g.getType() == GroupType.SystemPrivilege) {
                continue;
            }
            GroupPrivilege groupPrivilege = new GroupPrivilege();
            groupPrivilege.setGroup(g.getId());
            groupPrivilege.setReserved(g.isReserved());
            groupPrivilege.setRecordPrivilege(g.getType() == GroupType.RecordPrivilege);
            // Restrict changing privileges for groups with a required profile for setting privileges set
            groupPrivilege.setRestricted(!canUserChangePrivilegesForGroup(context, g));
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

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        // If the group is reserved, there is no need to check if it's a workspace as the group will not be changed.
        // This is required for transferring ownership to an administrator
        if (!ReservedGroup.isReserved(groupIdentifier)) {
            checkGroupIsWorkspace(groupIdentifier, locale);
        }

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

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        // If the group is reserved, there is no need to check if it's a workspace as the group will not be changed.
        // This is required for transferring ownership to an administrator
        if (!ReservedGroup.isReserved(groupIdentifier)) {
            checkGroupIsWorkspace(groupIdentifier, locale);
        }

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

                Long metadataId = (long) metadata.getId();
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
     * Checks if the user profile is allowed to publish metadata.
     *
     * @param groupId the group owner of the metadata to publish
     * @param userSession the user session for authorization checks
     */
    private void checkUserProfileToPublishMetadata(Integer groupId, UserSession userSession) {
        if (userSession.getProfile() == Profile.Administrator) {
            return; // Administrators are always allowed to publish metadata
        }
        if (groupId == null) {
            throw new NotAllowedException("Publication of metadata is not allowed. Metadata without group owner cannot be published.");
        }
        Profile defaultProfileForPublishing = Profile.Reviewer;
        String configuredProfileForPublishing = sm.getValue(Settings.METADATA_PUBLISH_USERPROFILE);
        Profile requiredProfileForPublishing;
        try {
            requiredProfileForPublishing = Profile.valueOf(configuredProfileForPublishing);
        } catch (IllegalArgumentException | NullPointerException e) {
            if (e instanceof IllegalArgumentException) {
                Log.error(Geonet.SETTINGS, "Invalid profile configured for publishing. Using default value: " + defaultProfileForPublishing);
            }
            requiredProfileForPublishing = defaultProfileForPublishing;
        }

        boolean canUserPublishForGroup = accessManager.isProfileOnGroup(userSession, requiredProfileForPublishing, groupId);

        if (!canUserPublishForGroup) {
            throw new NotAllowedException(String.format(
                "Publication of metadata is not allowed. User must have the %s profile in the record owner group.", requiredProfileForPublishing));
        }
    }

    /**
     * Checks if the user profile is allowed to unpublish metadata.
     *
     * @param groupId the group owner of the metadata to unpublish
     * @param userSession the user session for authorization checks
     *
     */
    private void checkUserProfileToUnpublishMetadata(Integer groupId, UserSession userSession) {
        if (userSession.getProfile() == Profile.Administrator) {
            return; // Administrators are always allowed to unpublish metadata
        }
        if (groupId == null) {
            throw new NotAllowedException("Unpublication of metadata is not allowed. Metadata without group owner cannot be unpublished.");
        }
        Profile defaultProfileForUnpublishing = Profile.Reviewer;
        String configuredProfileForUnpublishing = sm.getValue(Settings.METADATA_UNPUBLISH_USERPROFILE);
        Profile requiredProfileForUnpublishing;
        try {
            requiredProfileForUnpublishing = Profile.valueOf(configuredProfileForUnpublishing);
        } catch (IllegalArgumentException | NullPointerException e) {
            if (e instanceof IllegalArgumentException) {
                Log.error(Geonet.SETTINGS, "Invalid profile configured for unpublishing. Using default value: " + defaultProfileForUnpublishing);
            }
            requiredProfileForUnpublishing = defaultProfileForUnpublishing;
        }

        boolean canUserUnpublishForGroup = accessManager.isProfileOnGroup(userSession, requiredProfileForUnpublishing, groupId);

        if (!canUserUnpublishForGroup) {
            throw new NotAllowedException(String.format(
                "Unpublication of metadata is not allowed. User must have the %s profile in the record owner group.", requiredProfileForUnpublishing));
        }
    }

    /**
     * Checks if the user can change the privileges for the group.
     *
     * @param context The {@link ServiceContext} object.
     * @param group   The {@link Group} to change the privileges for.
     * @return True if the user can change the privileges for the group, false otherwise.
     */
    private boolean canUserChangePrivilegesForGroup(final ServiceContext context, Group group) {
        Profile minimumProfileForPrivileges = group.getMinimumProfileForPrivileges();
        if (minimumProfileForPrivileges == null) {
            return true;
        } else {
            return accessManager.isProfileOrMoreOnGroup(context.getUserSession(), minimumProfileForPrivileges, group.getId());
        }
    }

    /**
     * Checks if the given group is of type Workspace.
     *
     * @param groupId the identifier of the group to check
     * @param locale  the locale to use for error messages
     * @throws ResourceNotFoundException if the group is not found
     * @throws IllegalArgumentException  if the group is not of type Workspace
     */
    private void checkGroupIsWorkspace(Integer groupId, Locale locale) throws ResourceNotFoundException {
        if (!groupIsType(groupId, GroupType.Workspace, locale)) {
            throw new IllegalArgumentException(messages.getMessage("api.groups.group_not_workspace", new
                Object[]{groupId}, locale));
        }
    }

    /**
     * Checks if the given group is of the specified type.
     *
     * @param groupId   the identifier of the group to check
     * @param groupType the type to check against
     * @param locale    the locale to use for error messages
     * @return true if the group is of the specified type, false otherwise
     * @throws ResourceNotFoundException if the group is not found
     */
    private boolean groupIsType(Integer groupId, GroupType groupType, Locale locale) throws ResourceNotFoundException {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            throw new ResourceNotFoundException(messages.getMessage("api.groups.group_not_found", new
                Object[]{groupId}, locale));
        }
        return group.getType() == groupType;
    }
}
