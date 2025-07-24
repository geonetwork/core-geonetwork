//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.metadata;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.GroupOperations;
import org.fao.geonet.api.records.model.GroupPrivilege;
import org.fao.geonet.api.records.model.MetadataPublicationNotificationInfo;
import org.fao.geonet.api.records.model.SharingParameter;
import org.fao.geonet.api.records.model.SharingResponse;
import org.fao.geonet.config.IPublicationConfig;
import org.fao.geonet.config.PublicationOption;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
import org.fao.geonet.events.history.RecordPrivilegesChangeEvent;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.util.MetadataPublicationMailNotifier;
import org.fao.geonet.util.UserUtil;
import org.fao.geonet.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.springframework.data.jpa.domain.Specification.where;

@Service
public class MetadataPublicationService {

    @Autowired
    private AccessManager accessManager;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private SettingManager sm;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetadataPublicationMailNotifier metadataPublicationMailNotifier;

    @Autowired
    private IPublicationConfig publicationConfig;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private MetadataStatusRepository metadataStatusRepository;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    IMetadataStatus metadataStatus;

    @Autowired
    IMetadataManager metadataManager;

    @Autowired
    private FeedbackLanguages feedbackLanguages;

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    @Autowired
    private IMetadataOperations metadataOperations;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    IMetadataValidator validator;

    @Autowired
    MetadataValidationRepository metadataValidationRepository;

    /**
     * Shares a metadata based on the publicationConfig to publish/unpublish it.
     *
     * @param context           Service context.
     * @param metadata          Metadata to share.
     * @param publish           Flag to publish/unpublish the metadata.
     * @param publicationType   Publication type, used to retrieve the publication configuration.
     * @throws Exception
     */
    public void shareMetadataWithReservedGroup(ServiceContext context, AbstractMetadata metadata, boolean publish, String publicationType) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();

        String lang = context.getLanguage();
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
            new Locale(lang));

        Locale[] feedbackLocales = feedbackLanguages.getLocales(new Locale(lang));

        if (!accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
            throw new Exception(String.format(messages.getString("api.metadata.share.ErrorUserNotAllowedToPublish"),
                metadata.getUuid(), messages.getString(accessManager.getReviewerRule())));

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
            context.getUserSession().getUserIdAsInt(), true, null,
            metadataListToNotifyPublication, notifyByEmail);
        metadataIndexer.indexMetadata(String.valueOf(metadata.getId()), true, IndexingMode.full);

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
     * @param serviceContext            Service context.
     * @param records                   Metadata list of uuids to share.
     * @param publish                   Sharing type: publish/unpublish the metadata records.
     * @param publicationTypeToUse      Publication type to use for the sharing, used to retrieve the publication configuration.
     * @return Report with the results.
     */
    public MetadataProcessingReport shareSelection(ServiceContext serviceContext, Set<String> records, boolean publish, String publicationTypeToUse) {
        SharingParameter sharing = buildSharingForPublicationConfig(publish,
            publicationTypeToUse);

        return shareSelection(serviceContext, records, sharing);
    }

    /**
     * Shares a metadata selection with a list of groups, returning a report with the results.
     *
     * @param serviceContext        Service context.
     * @param records               Metadata list of uuids to share.
     * @param sharing               SharingParameter object containing the privileges to apply.
     * @return
     */
    public MetadataProcessingReport shareSelection(ServiceContext serviceContext, Set<String> records, SharingParameter sharing) {
        MetadataProcessingReport report = new SimpleMetadataProcessingReport();

        try {
            report.setTotalRecords(records.size());

            final ApplicationContext appContext = ApplicationContextHolder.get();

            String lang = serviceContext.getLanguage();
            Locale[] feedbackLocales = feedbackLanguages.getLocales(new Locale(lang));

            List<String> listOfUpdatedRecords = new ArrayList<>();
            List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication = new ArrayList<>();
            boolean notifyByEmail = StringUtils.isNoneEmpty(sm.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL));

            for (String uuid : records) {
                AbstractMetadata metadata = metadataUtils.findOneByUuid(uuid);
                if (metadata == null) {
                    report.incrementNullRecords();
                } else if (!accessManager.canEdit(
                    serviceContext, String.valueOf(metadata.getId()))) {
                    report.addNotEditableMetadataId(metadata.getId());
                } else {
                    boolean skipAllReservedGroup = false;
                    if (!accessManager.hasReviewPermission(serviceContext, Integer.toString(metadata.getId()))) {
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
                            Metadata md = metadataRepository.findOneByUuid(metadata.getUuid());

                            if (md != null) {
                               setOperations(sharing, dataManager, serviceContext, appContext, md, operationMap,
                                    privileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(md.getId()));
                                report.addMetadataId(metadata.getId());
                            } else {
                                setOperations(sharing, dataManager, serviceContext, appContext, metadata, operationMap,
                                    privileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                                report.addMetadataId(metadata.getId());
                            }

                        } else {
                            setOperations(sharing, dataManager, serviceContext, appContext, metadata, operationMap,
                                privileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report,
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

    public void setOperations(
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
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication,
        boolean notifyByMail) throws Exception {
        if (privileges != null) {

            //ResourceBundle messages = ApiUtils.getMessagesResourceBundle(request.getLocales());
            String lang = context.getLanguage();
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
                new Locale(lang));

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

            SharingResponse sharingBefore = getRecordSharingSettings(context, metadata);

            // Check if the user profile can change the privileges for publication/un-publication of the reserved groups
            checkChangesAllowedToUserProfileForReservedGroups(context.getUserSession(), sharingBefore, privileges, !sharing.isClear());

            List<Integer> excludeFromDelete = new ArrayList<Integer>();

            // Exclude deleting privileges for groups in which the user does not have the minimum profile for privileges
            for (Group group: groupRepository.findByMinimumProfileForPrivilegesNotNull()) {
                if (!canUserChangePrivilegesForGroup(context, group)) {
                    excludeFromDelete.add(group.getId());
                }
            }

            // Exclude deleting privileges for reserved groups if the skipAllReservedGroup flag is set
            if (skipAllReservedGroup) {
                excludeFromDelete.add(ReservedGroup.all.getId());
                excludeFromDelete.add(ReservedGroup.intranet.getId());
                excludeFromDelete.add(ReservedGroup.guest.getId());
            }

            if (sharing.isClear()) {
                metadataOperations.deleteMetadataOper(String.valueOf(metadata.getId()), excludeFromDelete);
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

    private SharingResponse getRecordSharingSettings(ServiceContext context, AbstractMetadata metadata) throws Exception {
        UserSession userSession = context.getUserSession();

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

                // Restrict changing privileges for groups with a minimum profile for setting privileges set
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

    /**
     * Creates a ref {@link SharingParameter} object with privileges to publih/un-publish
     * metadata in {@link ReservedGroup#all} group.
     *
     * @param publish Flag to add/remove sharing privileges.
     * @return
     */
    public SharingParameter buildSharingForPublicationConfig(boolean publish, String configName) {
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
            return accessManager.isProfileOrMoreOnGroup(context, minimumProfileForPrivileges, group.getId());
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
                metadataIndexer.indexMetadata(metadata.getId() + "", true, IndexingMode.full);
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
