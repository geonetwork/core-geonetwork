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
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.api.processing.report.MetadataProcessingReport;
import org.fao.geonet.api.processing.report.SimpleMetadataProcessingReport;
import org.fao.geonet.api.records.model.*;
import org.fao.geonet.config.IPublicationConfig;
import org.fao.geonet.config.PublicationOption;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.domain.utils.ObjectJSONUtils;
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
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.util.MetadataPublicationMailNotifier;
import org.fao.geonet.util.WorkflowUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Service;

import java.util.*;
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

    @Autowired
    @Qualifier("apiMessages")
    private ResourceBundleMessageSource messages;

    /**
     * Shares a metadata based on the publicationConfig to publish/unpublish it.
     *
     * @param context           Service context.
     * @param metadata          Metadata to share.
     * @param publish           Flag to publish/unpublish the metadata.
     * @param publicationType   Publication type, used to retrieve the publication configuration.
     * @throws Exception
     */
    public void shareMetadataWithReservedGroup(ServiceContext context, AbstractMetadata metadata, boolean publish, String publicationType, Locale locale) throws Exception {
        ApplicationContext appContext = ApplicationContextHolder.get();

        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
            locale);

        Locale[] feedbackLocales = feedbackLanguages.getLocales(locale);

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
            context.getUserSession().getUserIdAsInt(), true, null, locale,
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
     * @param serviceContext            Service context.
     * @param records                   Metadata list of uuids to share.
     * @param publish                   Sharing type: publish/unpublish the metadata records.
     * @param publicationTypeToUse      Publication type to use for the sharing, used to retrieve the publication configuration.
     * @param locale                    Locale used to resolve localised messages.
     * @return Report with the results.
     */
    public MetadataProcessingReport shareSelection(ServiceContext serviceContext, Set<String> records, boolean publish, String publicationTypeToUse, Locale locale) {
        SharingParameter sharing = buildSharingForPublicationConfig(publish,
            publicationTypeToUse);

        return shareSelection(serviceContext, records, sharing, locale);
    }

    /**
     * Shares a metadata selection with a list of groups, returning a report with the results.
     *
     * @param serviceContext        Service context.
     * @param records               Metadata list of uuids to share.
     * @param sharing               SharingParameter object containing the privileges to apply.
     * @param locale                Locale used to resolve localised messages.
     * @return
     */
    public MetadataProcessingReport shareSelection(ServiceContext serviceContext, Set<String> records, SharingParameter sharing, Locale locale) {
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
                    boolean skipAllReservedGroup = !accessManager.hasReviewPermission(serviceContext, Integer.toString(metadata.getId()));

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
                                    allGroupPrivileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report, locale,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(md.getId()));
                                report.addMetadataId(metadata.getId());
                            } else {
                                setOperations(sharing, dataManager, serviceContext, appContext, metadata, operationMap,
                                    privileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report, locale,
                                    metadataListToNotifyPublication, notifyByEmail);

                                report.incrementProcessedRecords();
                                listOfUpdatedRecords.add(String.valueOf(metadata.getId()));
                                report.addMetadataId(metadata.getId());
                            }

                        } else {
                            setOperations(sharing, dataManager, serviceContext, appContext, metadata, operationMap,
                                privileges, serviceContext.getUserSession().getUserIdAsInt(), skipAllReservedGroup, report, locale,
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
        Locale locale,
        List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication,
        boolean notifyByMail) throws Exception {
        if (privileges != null) {
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale);

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
            checkChangesAllowedToUserProfileForReservedGroups(context.getUserSession(), metadata, sharingBefore, privileges, !sharing.isClear());

            List<Integer> excludeFromDelete = new ArrayList<>();

            // Exclude deleting privileges for groups in which the user does not have the required profile for privileges
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
                sharingChanges = true;
            }

            for (GroupOperations p : privileges) {
                Integer groupId = p.getGroup();
                for (Map.Entry<String, Boolean> o : p.getOperations().entrySet()) {
                    Integer opId = operationMap.get(o.getKey());
                    // Never set editing for reserved group or any privileges for system groups
                    if (
                        groupIsType(groupId, GroupType.SystemPrivilege, locale) ||
                            (opId == ReservedOperation.editing.getId() && ReservedGroup.isReserved(groupId))
                    ) {
                        continue;
                    }

                    if (Boolean.TRUE.equals(o.getValue())) {
                        // For privileges to ALL group, check if it's allowed or not to publish invalid metadata
                        if ((groupId == ReservedGroup.all.getId())) {
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
                            context, metadata.getId(), groupId, opId);
                        sharingChanges = true;
                    } else if (!sharing.isClear() && Boolean.TRUE.equals(!o.getValue())) {
                        dataMan.unsetOperation(
                            context, metadata.getId(), groupId, opId);
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

                    publishUser.ifPresent(user -> metadataNotificationInfo.setPublisherUser(user.getUsername()));

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
     * <p>The authorization logic is:
     * <ul>
     *   <li>Administrators may always change reserved-group privileges.</li>
     *   <li>For publishing: the user must have exactly the configured publication profile
     *       in the metadata's group owner (e.g. Reviewer means the user must be a Reviewer in that group).</li>
     *   <li>For unpublishing: the user must have exactly the configured unpublication profile
     *       in the metadata's group owner.</li>
     * </ul>
     *
     * @param userSession        the current user session
     * @param metadata           the metadata record whose privileges are being changed
     * @param originalPrivileges the sharing settings before the change
     * @param newPrivileges      the new sharing settings to apply
     * @param merge              whether the new settings are merged with or replace the old ones
     * @throws Exception if the per-group reviewer look-up fails
     */
    private void checkChangesAllowedToUserProfileForReservedGroups(UserSession userSession,
                                                                   AbstractMetadata metadata,
                                                                   SharingResponse originalPrivileges,
                                                                   List<GroupOperations> newPrivileges,
                                                                   boolean merge) {
        if (userSession.getProfile() == Profile.Administrator) {
            return; // Administrators are always allowed to change reserved groups privileges
        }

        // Check if there are any changes to reserved groups privileges
        List<PrivilegeStatusChange> privilegeStatusChangesList =
            reservedGroupsPrivilegesStatusChanges(originalPrivileges, newPrivileges, merge);

        if (privilegeStatusChangesList.isEmpty()) {
            return; // No changes to reserved groups, no authorization check needed
        }

        // Determine if publishing or unpublishing operations are being performed
        boolean isPublishing = false;
        boolean isUnpublishing = false;

            for (PrivilegeStatusChange status : privilegeStatusChangesList) {
            if (!status.isPublishedBefore() && status.isPublishedAfter()) {
                isPublishing = true;
            } else if (status.isPublishedBefore() && !status.isPublishedAfter()) {
                isUnpublishing = true;
                }
            }

        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();

        // Perform authorization checks based on the operation(s) being performed
        if (isPublishing) {
            checkUserProfileToPublishMetadata(groupOwner, userSession);
            }

        if (isUnpublishing) {
            checkUserProfileToUnpublishMetadata(groupOwner, userSession);
        }
    }


    /**
     * Verifies that the user is authorized to publish the given metadata, applying the same
     * authorization rules used for an immediate publication: the user must have review permission
     * on the record and must have the configured publication profile on the record's group owner.
     *
     * <p>This is intended to guard scheduled publication requests: the scheduled publication
     * job performs the publication later while running with administrator privileges, so a user
     * must not be able to schedule a publication that they would not be allowed to perform
     * themselves.</p>
     *
     * @param context  the current service context
     * @param metadata the metadata to be published
     * @throws NotAllowedException if the user is not allowed to publish the metadata
     * @throws Exception           if the review permission look-up fails
     */
    public void checkUserCanPublishMetadata(ServiceContext context, AbstractMetadata metadata) throws Exception {
        // Same review-permission gate enforced for an immediate publication.
        if (!accessManager.hasReviewPermission(context, Integer.toString(metadata.getId()))) {
            throw new NotAllowedException(String.format(
                "Publication of metadata %s is not allowed. User does not have review permission on the record.",
                metadata.getUuid()));
        }
        // Same configured publication-profile gate enforced for an immediate publication.
        checkUserProfileToPublishMetadata(metadata.getSourceInfo().getGroupOwner(), context.getUserSession());
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
    /**
     * Checks if the group is a system privilege group, for which privileges must never be set directly.
     *
     * @param groupId The group identifier to check.
     * @return True if the group exists and is of type {@link GroupType#SystemPrivilege}, false otherwise.
     */
    private boolean isSystemPrivilegeGroup(Integer groupId) {
        return groupRepository.findById(groupId)
            .map(group -> group.getType() == GroupType.SystemPrivilege)
            .orElse(false);
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
     * Checks if the given group is of type Workspace.
     *
     * @param groupId the identifier of the group to check
     * @param locale  the locale to use for error messages
     * @throws ResourceNotFoundException if the group is not found
     * @throws IllegalArgumentException  if the group is not of type Workspace
     */
    public void checkGroupIsWorkspace(Integer groupId, Locale locale) throws ResourceNotFoundException {
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
    public boolean groupIsType(Integer groupId, GroupType groupType, Locale locale) throws ResourceNotFoundException {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            throw new ResourceNotFoundException(messages.getMessage("api.groups.group_not_found", new
                Object[]{groupId}, locale));
        }
        return group.getType() == groupType;
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
