//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import com.google.common.base.Joiner;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.GroupSpecs;
import org.fao.geonet.util.LocalizedEmail;
import org.fao.geonet.util.LocalizedEmailParameter;
import org.fao.geonet.util.LocalizedEmailComponent;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_FEEDBACK_EMAIL;
import static org.fao.geonet.util.LocalizedEmailComponent.ComponentType.*;
import static org.fao.geonet.util.LocalizedEmailComponent.KeyType;
import static org.fao.geonet.util.LocalizedEmailComponent.ReplacementType.*;
import static org.fao.geonet.util.LocalizedEmailParameter.ParameterType;

public class DefaultStatusActions implements StatusActions {

    protected ServiceContext context;
    protected String language;
    protected DataManager dm;

    @Autowired
    protected IMetadataUtils metadataUtils;
    protected String siteUrl;
    protected String siteName;
    protected UserSession session;
    protected boolean emailNotes = true;
    private String replyTo;
    private String replyToDescr;
    private StatusValueRepository statusValueRepository;
    protected IMetadataStatus metadataStatusManager;
    private IMetadataUtils metadataRepository;

    /**
     * Constructor.
     */
    public DefaultStatusActions() {
    }

    /**
     * Initializes the StatusActions class with external info from GeoNetwork.
     */
    public void init(ServiceContext context) throws Exception {

        this.context = context;
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        this.statusValueRepository = applicationContext.getBean(StatusValueRepository.class);
        this.metadataUtils = applicationContext.getBean(IMetadataUtils.class);
        this.language = context.getLanguage();

        SettingManager sm = applicationContext.getBean(SettingManager.class);

        siteName = sm.getSiteName();
        String from = sm.getValue(SYSTEM_FEEDBACK_EMAIL);

        if (from == null || from.length() == 0) {
            context.error("Mail feedback address not configured, email notifications won't be sent.");
            emailNotes = false;
        }

        session = context.getUserSession();
        replyTo = session.getEmailAddr();
        if (replyTo != null) {
            replyToDescr = session.getName() + " " + session.getSurname();
        } else {
            replyTo = from;
            replyToDescr = siteName;
        }

        dm = applicationContext.getBean(DataManager.class);
        metadataStatusManager = applicationContext.getBean(IMetadataStatus.class);
        siteUrl = sm.getSiteURL(context);

        metadataRepository = context.getBean(IMetadataUtils.class);
    }

    /**
     * Called when a record is edited to set/reset status.
     *
     * @param id        The metadata id that has been edited.
     * @param minorEdit If true then the edit was a minor edit.
     */
    public void onEdit(int id, boolean minorEdit) throws Exception {
        if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
            Log.trace(Geonet.DATA_MANAGER, "DefaultStatusActions.onEdit(" + id + ", " + minorEdit + ") with status "
                + dm.getCurrentStatus(id));
        }
        if (!minorEdit && dm.getCurrentStatus(id).equals(StatusValue.Status.APPROVED)) {
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
                new Locale(this.language));
            String changeMessage = String.format(messages.getString("status_email_text"), replyToDescr, replyTo, id);
            Log.trace(Geonet.DATA_MANAGER, "Set DRAFT to current record with id " + id);
            dm.setStatus(context, id, Integer.valueOf(StatusValue.Status.DRAFT), new ISODate(), changeMessage);
        }
    }

    /**
     * Called when a record status is added.
     *
     * @param listOfStatus
     * @return
     * @throws Exception
     */
    public Map<Integer, StatusChangeType> onStatusChange(List<MetadataStatus> listOfStatus, boolean updateIndex) throws Exception {

        if (listOfStatus.stream().map(MetadataStatus::getMetadataId).distinct().count() != listOfStatus.size()) {
            throw new IllegalArgumentException("Multiple status update received on the same metadata");
        }

        Map<Integer, StatusChangeType> results = new HashMap<>();

        // process the metadata records to set status
        for (MetadataStatus status : listOfStatus) {
            MetadataStatus currentStatus = dm.getStatus(status.getMetadataId());
            String currentStatusId = (currentStatus != null) ?
                String.valueOf(currentStatus.getStatusValue().getId()) : "";


            String statusId = status.getStatusValue().getId() + "";
            Set<Integer> listOfId = new HashSet<>(1);
            listOfId.add(status.getMetadataId());


            // For the workflow, if the status is already set to value
            // of status then do nothing. This does not apply to task and event.
            if (status.getStatusValue().getType().equals(StatusValueType.workflow) &&
                (statusId).equals(currentStatusId)) {
                if (context.isDebugEnabled())
                    context.debug(String.format("Metadata %s already has status %s ",
                        status.getMetadataId(), status.getStatusValue().getId()));
                results.put(status.getMetadataId(), StatusChangeType.UNCHANGED);
                continue;
            }

            // if not possible to go from one status to the other, don't continue
            AbstractMetadata metadata = metadataRepository.findOne(status.getMetadataId());
            if (!isStatusChangePossible(session.getProfile(), metadata, currentStatusId, statusId)) {
                results.put(status.getMetadataId(), StatusChangeType.UNCHANGED);
                continue;
            }

            // debug output if necessary
            if (context.isDebugEnabled())
                context.debug("Change status of metadata with id " + status.getMetadataId() + " from " + currentStatusId + " to " + statusId);

            // we know we are allowed to do the change, apply any side effects
            boolean deleted = applyStatusChange(status.getMetadataId(), status, statusId, updateIndex);

            if (deleted) {
                results.put(status.getMetadataId(), StatusChangeType.DELETED);
            } else {
                results.put(status.getMetadataId(), StatusChangeType.UPDATED);
            }
            // throw events
            Log.trace(Geonet.DATA_MANAGER, "Throw workflow events.");
            for (Integer mid : listOfId) {
                if (results.get(mid) != StatusChangeType.DELETED) {
                    Log.debug(Geonet.DATA_MANAGER, "  > Status changed for record (" + mid + ") to status " + status);
                    context.getApplicationContext().publishEvent(new MetadataStatusChanged(
                        metadataUtils.findOne(mid),
                        status.getStatusValue(), status.getChangeMessage(),
                        status.getUserId()
                    ));
                }
            }

            // inform content reviewers if the status is submitted
            try {
                notify(getUserToNotify(status), status);
            } catch (Exception e) {
                context.warning(String.format(
                    "Failed to send notification on status change for metadata %s with status %s. Error is: %s",
                    status.getMetadataId(), status.getStatusValue().getId(), e.getMessage()));
            }

        }

        return results;
    }

    /**
     * Placeholder to apply any side effects.
     * eg. if APPROVED, publish a record,
     * if RETIRED, unpublish or delete the record.
     */
    private boolean applyStatusChange(int metadataId, MetadataStatus status, String toStatusId, boolean updateIndex) throws Exception {
        boolean deleted = false;
        if (!deleted) {
            metadataStatusManager.setStatusExt(status, updateIndex);
        }
        return deleted;
    }


    /**
     * Send email to a list of users. The list of users is defined based on the
     * notification level of the status. See {@link StatusValueNotificationLevel}.
     *
     * @param userToNotify
     * @param status
     * @throws Exception
     */
    protected void notify(List<User> userToNotify, MetadataStatus status) throws Exception {
        if ((userToNotify == null) || userToNotify.isEmpty()) {
            return;
        }

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        FeedbackLanguages feedbackLanguages = applicationContext.getBean(FeedbackLanguages.class);

        Locale[] feedbackLocales = feedbackLanguages.getLocales(new Locale(this.language));

        Set<Integer> listOfId = new HashSet<>(1);
        listOfId.add(status.getMetadataId());

        UserRepository userRepository = context.getBean(UserRepository.class);
        User owner = userRepository.findById(status.getOwner()).orElse(null);

        IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        AbstractMetadata metadata = metadataRepository.findOne(status.getMetadataId());

        String subjectTemplateKey = "";
        String textTemplateKey = "";
        boolean failedToFindASpecificSubjectTemplate = false;
        boolean failedToFindASpecificTextTemplate = false;

        for (Locale feedbackLocale: feedbackLocales) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("org.fao.geonet.api.Messages", feedbackLocale);

            if (!failedToFindASpecificSubjectTemplate) {
                try {
                    subjectTemplateKey = "status_change_" + status.getStatusValue().getName() + "_email_subject";
                    resourceBundle.getString(subjectTemplateKey);
                } catch (MissingResourceException e) {
                    failedToFindASpecificSubjectTemplate = true;
                }
            }

            if (!failedToFindASpecificTextTemplate) {
                try {
                    textTemplateKey = "status_change_" + status.getStatusValue().getName() + "_email_text";
                    resourceBundle.getString(textTemplateKey);
                } catch (MissingResourceException e) {
                    failedToFindASpecificTextTemplate = true;
                }
            }
        }

        if (failedToFindASpecificSubjectTemplate) {
            subjectTemplateKey = "status_change_default_email_subject";
        }

        if (failedToFindASpecificTextTemplate) {
            textTemplateKey = "status_change_default_email_text";
        }

        LocalizedEmailComponent emailSubjectComponent = new LocalizedEmailComponent(SUBJECT, subjectTemplateKey, KeyType.MESSAGE_KEY, NUMERIC_FORMAT);
        emailSubjectComponent.enableCompileWithIndexFields(metadata.getUuid());

        LocalizedEmailComponent emailMessageComponent = new LocalizedEmailComponent(MESSAGE, textTemplateKey, KeyType.MESSAGE_KEY, NUMERIC_FORMAT);
        emailMessageComponent.enableCompileWithIndexFields(metadata.getUuid());
        emailMessageComponent.enableReplaceLinks(false);

        LocalizedEmailComponent emailSalutationComponent = new LocalizedEmailComponent(SALUTATION, "{{userName}},\n\n", KeyType.RAW_VALUE, NONE);

        for (Locale feedbackLocale : feedbackLocales) {
            // TODO: Refactor to allow custom messages based on the type of status

            emailSubjectComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, siteName),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 2, getTranslatedStatusName(status.getStatusValue().getId(), feedbackLocale)),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 3, replyToDescr)
            );

            emailMessageComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, replyToDescr),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 2, status.getChangeMessage()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 3, getTranslatedStatusName(status.getStatusValue().getId(), feedbackLocale)),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 4, status.getChangeDate()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 5, status.getDueDate()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 6, status.getCloseDate()),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 7, owner == null ? "" : Joiner.on(" ").skipNulls().join(owner.getName(), owner.getSurname())),
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 8, metadataUtils.getDefaultUrl(metadata.getUuid(), feedbackLocale.getISO3Language()))
            );
        }

        LocalizedEmail localizedEmail = new LocalizedEmail(false);
        localizedEmail.addComponents(emailSubjectComponent, emailMessageComponent, emailSalutationComponent);

        String subject = localizedEmail.getParsedSubject(feedbackLocales);

        for (User user : userToNotify) {
            String userName = Joiner.on(" ").skipNulls().join(user.getName(), user.getSurname());
            //If we have a userName add the salutation
            String message;
            if (StringUtils.isEmpty(userName)) {
                message = localizedEmail.getParsedMessage(feedbackLocales);
            } else {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{{userName}}", userName);
                message = localizedEmail.getParsedMessage(feedbackLocales, replacements);
            }
            sendEmail(user.getEmail(), subject, message);
        }
    }

    /**
     * Based on the status notification level defined in the database collect the
     * list of users to notify.
     *
     * @param status
     * @return
     */
    protected List<User> getUserToNotify(MetadataStatus status) {
        StatusValueNotificationLevel notificationLevel = status.getStatusValue().getNotificationLevel();

        // If new status is DRAFT and previous status is not SUBMITTED (which means a rejection),
        // ignore notifications as the DRAFT status is used also when creating the working copy.
        // We don't want to notify when creating a working copy.
        if (status.getStatusValue().getId() == Integer.parseInt(StatusValue.Status.DRAFT) &&
            ((StringUtils.isEmpty(status.getPreviousState())) ||
                (Integer.parseInt(status.getPreviousState()) != Integer.parseInt(StatusValue.Status.SUBMITTED)))) {
                return new ArrayList<>();
        }

        // TODO: Status does not provide batch update
        // So taking care of one record at a time.
        // Currently the code could notify a mix of reviewers
        // if records are not in the same groups. To be improved.
        Set<Integer> listOfId = new HashSet<>(1);
        listOfId.add(status.getMetadataId());
        return getUserToNotify(notificationLevel, listOfId, status.getOwner());
    }

    public static List<User> getUserToNotify(StatusValueNotificationLevel notificationLevel, Set<Integer> recordIds, Integer ownerId) {
        UserRepository userRepository = ApplicationContextHolder.get().getBean(UserRepository.class);
        List<User> users = new ArrayList<>();

        if (notificationLevel != null) {
            if (notificationLevel == StatusValueNotificationLevel.statusUserOwner) {
                Optional<User> owner = userRepository.findById(ownerId);

                if (owner.isPresent()) {
                    users.add(owner.get());
                }
            } else if (notificationLevel == StatusValueNotificationLevel.recordProfileReviewer) {
                List<Pair<Integer, User>> results = userRepository.findAllByGroupOwnerNameAndProfile(recordIds, Profile.Reviewer);
                Collections.sort(results, Comparator.comparing(s -> s.two().getName()));
                for (Pair<Integer, User> p : results) {
                    users.add(p.two());
                }
            } else if (notificationLevel == StatusValueNotificationLevel.recordUserAuthor) {
                Iterable<Metadata> records = ApplicationContextHolder.get().getBean(MetadataRepository.class).findAllById(recordIds);
                for (Metadata r : records) {
                    Optional<User> owner = userRepository.findById(r.getSourceInfo().getOwner());

                    if (owner.isPresent()) {
                        users.add(owner.get());
                    }
                }

                // Check metadata drafts
                Iterable<MetadataDraft> recordsDraft = ApplicationContextHolder.get().getBean(MetadataDraftRepository.class).findAllById(recordIds);

                for (MetadataDraft r : recordsDraft) {
                    Optional<User> owner = userRepository.findById(r.getSourceInfo().getOwner());

                    if (owner.isPresent()) {
                        users.add(owner.get());
                    }
                }
            } else if (notificationLevel.name().startsWith("catalogueProfile")) {
                String profileId = notificationLevel.name().replace("catalogueProfile", "");
                Profile profile = Profile.findProfileIgnoreCase(profileId);
                users = userRepository.findAllByProfile(profile);
            } else if (notificationLevel == StatusValueNotificationLevel.catalogueAdministrator) {
                SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
                String adminEmail = settingManager.getValue(SYSTEM_FEEDBACK_EMAIL);
                if (StringUtils.isNotEmpty(adminEmail)) {
                    Set<String> emails = new HashSet<>(1);
                    emails.add(adminEmail);
                    User catalogueAdmin = new User().setEmailAddresses(emails);
                    users.add(catalogueAdmin);
                }
            }
        }
        return users;
    }

    public static List<Group> getGroupToNotify(StatusValueNotificationLevel notificationLevel, List<String> groupNames) {
        GroupRepository groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
        List<Group> groups = new ArrayList<>();

        if ((notificationLevel != null) && (notificationLevel == StatusValueNotificationLevel.recordGroupEmail)) {
            groups = groupRepository.findAll(GroupSpecs.inGroupNames(groupNames));
        }

        return groups;
    }


    /**
     * Unset all operations on 'All' Group. Used when status
     * changes from approved to something else.
     *
     * @param mdId The metadata id to unset privileges on
     */
    protected void unsetAllOperations(int mdId) throws Exception {
        Log.trace(Geonet.DATA_MANAGER, "DefaultStatusActions.unsetAllOperations(" + mdId + ")");

        int allGroup = 1;
        for (ReservedOperation op : ReservedOperation.values()) {
            dm.forceUnsetOperation(context, mdId, allGroup, op.getId());
        }
    }

    private String getTranslatedStatusName(int statusValueId, Locale locale) {
        String translatedStatusName = "";
        StatusValue s = statusValueRepository.findOneById(statusValueId);
        if (s == null) {
            translatedStatusName = statusValueId
                + " (Status not found in database translation table. Check the content of the StatusValueDes table.)";
        } else {
            translatedStatusName = s.getLabel(locale.getISO3Language());
        }
        return translatedStatusName;
    }

    /**
     * Send the email message about change of status on a group of metadata records.
     *
     * @param sendTo  The recipient email address
     * @param subject Subject to be used for email notices
     * @param message Text of the mail
     */
    protected void sendEmail(String sendTo, String subject, String message) {

        if (!emailNotes) {
            context.info("Would send email \nTo: " + sendTo + "\nSubject: " + subject + "\n Message:\n" + message);
        } else {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            SettingManager sm = applicationContext.getBean(SettingManager.class);
            // Doesn't make sense go further without any mailserver set...
            if (StringUtils.isNotBlank(sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST))) {
                List<String> to = new ArrayList<>();
                to.add(sendTo);
                MailUtil.sendMail(to, subject, message, null, sm, replyTo, replyToDescr);
            }
        }
    }

    /**
     * Placeholder to test whether a given status change for a given role is allowed or not.
     * <p>
     *
     * @param profile    the role that tries to execute the status change
     * @param fromStatus the status from which we start
     * @param toStatus   the status to which we'd like to change
     * @return whether the change is allowed
     */
    private boolean isStatusChangePossible(Profile profile, AbstractMetadata metadata, String fromStatus, String toStatus) throws Exception {
        return true;
        //  Example:
        //  if (StringUtils.isEmpty(fromStatus) && toStatus.equals(StatusValue.Status.DRAFT))
        //            return true;
        //        // figure out whether we can switch from status to status, depending on the profile
        //        Set<String> toProfiles = new HashSet<>();
        //        switch (profile) {
        //            case Editor:
        //                toProfiles = getEditorFlow().get(fromStatus);
        //                break;
        //            case Administrator:
        //                toProfiles = getAdminFlow().get(fromStatus);
        //                break;
        //            case Reviewer:
        //                toProfiles = getReviewerFlow().get(fromStatus);
        //                break;
        //        }
        //        return toProfiles != null && toProfiles.contains(toStatus);
    }
}
