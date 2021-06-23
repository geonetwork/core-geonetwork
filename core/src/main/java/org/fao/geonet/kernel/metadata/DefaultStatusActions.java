//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueNotificationLevel;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.User_;
import org.fao.geonet.events.md.MetadataStatusChanged;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.StatusValueRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_FEEDBACK_EMAIL;

import com.google.common.base.Joiner;

public class DefaultStatusActions implements StatusActions {

    public static final Pattern metadataLuceneField = Pattern.compile("\\{\\{index:([^\\}]+)\\}\\}");
    protected ServiceContext context;
    protected String language;
    protected DataManager dm;

    @Autowired
    protected IMetadataUtils metadataUtils;
    protected String siteUrl;
    protected String siteName;
    protected UserSession session;
    protected boolean emailNotes = true;
    private String from, replyTo, replyToDescr;
    private StatusValueRepository _statusValueRepository;
    private IMetadataStatus metadataStatusManager;

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
        this._statusValueRepository = applicationContext.getBean(StatusValueRepository.class);
        this.metadataUtils = applicationContext.getBean(IMetadataUtils.class);
        this.language = context.getLanguage();

        SettingManager sm = applicationContext.getBean(SettingManager.class);

        siteName = sm.getSiteName();
        from = sm.getValue(SYSTEM_FEEDBACK_EMAIL);

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
        //if (!minorEdit && dm.getCurrentStatus(id).equals(StatusValue.Status.APPROVED)
            //        && (context.getBean(IMetadataManager.class) instanceof DraftMetadataManager)) {
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
    public Set<Integer> onStatusChange(List<MetadataStatus> listOfStatus) throws Exception {

        Set<Integer> unchanged = new HashSet<Integer>();

        // -- process the metadata records to set status
        for (MetadataStatus status : listOfStatus) {
            MetadataStatus currentStatus = dm.getStatus(status.getMetadataId());
            String currentStatusId = (currentStatus != null)?
                String.valueOf(currentStatus.getStatusValue().getId()):"";


            String statusId = status.getStatusValue().getId() + "";
            Set<Integer> listOfId = new HashSet<>(1);
            listOfId.add(status.getMetadataId());


            // --- For the workflow, if the status is already set to value
            // of status then do nothing. This does not apply to task and event.
            if (status.getStatusValue().getType().equals(StatusValueType.workflow) &&
               (statusId).equals(currentStatusId)) {
                if (context.isDebugEnabled())
                    context.debug(String.format("Metadata %s already has status %s ",
                        status.getMetadataId(), status.getStatusValue().getId()));
                unchanged.add(status.getMetadataId());
                continue;
            }

            // --- set status, indexing is assumed to take place later
            metadataStatusManager.setStatusExt(status);

            // --- inform content reviewers if the status is submitted
            try {
                notify(getUserToNotify(status), status);
            } catch (Exception e) {
                context.warning(String.format(
                    "Failed to send notification on status change for metadata %s with status %s. Error is: %s",
                    status.getMetadataId(), status.getStatusValue().getId(), e.getMessage()));
            }

            //Throw events
            Log.trace(Geonet.DATA_MANAGER, "Throw workflow events.");
            for (Integer mid : listOfId) {
                if (!unchanged.contains(mid)) {
                    Log.debug(Geonet.DATA_MANAGER, "  > Status changed for record (" + mid + ") to status " + status);
                    context.getApplicationContext().publishEvent(new MetadataStatusChanged(
                        metadataUtils.findOne(Integer.valueOf(mid)),
                        status.getStatusValue(), status.getChangeMessage(),
                        status.getUserId()));
                }
            }

        }

        return unchanged;
    }

    /**
     * This apply specific rules depending on status change.
     * The default rules are:
     * <ul>
     * <li>DISABLED When approved, the record is automatically published.</li>
     * <li>When draft or rejected, unpublish the record.</li>
     * </ul>
     *
     * @param status
     * @throws Exception
     */
    private void applyRulesForStatusChange(MetadataStatus status) throws Exception {
        String statusId = status.getStatusValue().getId() + "";
        if (statusId.equals(StatusValue.Status.APPROVED)) {
            // setAllOperations(mid); - this is a short cut that could be enabled
            AccessManager accessManager = context.getBean(AccessManager.class);
            if (!accessManager.canReview(context, String.valueOf(status.getMetadataId()))) {
                throw new SecurityException(String.format(
                    "You can't edit record with ID %s",
                    String.valueOf(status.getMetadataId())));
            }
        } else if (statusId.equals(StatusValue.Status.DRAFT)) {
            unsetAllOperations(status.getMetadataId());
        }
    }


    /**
     * Send email to a list of users. The list of users is defined based on the
     * notification level of the status. See {@link StatusValueNotificationLevel}.
     *
     * @param userToNotify
     * @param status
     * @throws Exception
     */
    private void notify(List<User> userToNotify, MetadataStatus status) throws Exception {
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", new Locale(this.language));

        String translatedStatusName = getTranslatedStatusName(status.getStatusValue().getId());
        // TODO: Refactor to allow custom messages based on the type of status
        String subjectTemplate = "";
        try {
            subjectTemplate = messages
                    .getString("status_change_" + status.getStatusValue().getName() + "_email_subject");
        } catch (MissingResourceException e) {
            subjectTemplate = messages.getString("status_change_default_email_subject");
        }
        String subject = MessageFormat.format(subjectTemplate, siteName, translatedStatusName, replyToDescr // Author of
                                                                                                            // the
                                                                                                            // change
        );

        Set<Integer> listOfId = new HashSet<>(1);
        listOfId.add(status.getMetadataId());

        String textTemplate = "";
        try {
            textTemplate = messages.getString("status_change_" + status.getStatusValue().getName() + "_email_text");
        } catch (MissingResourceException e) {
            textTemplate = messages.getString("status_change_default_email_text");
        }

        UserRepository userRepository = context.getBean(UserRepository.class);
        User owner = userRepository.findById(status.getOwner()).orElse(null);

        String message = MessageFormat.format(textTemplate, replyToDescr, // Author of the change
                status.getChangeMessage(), translatedStatusName, status.getChangeDate(), status.getDueDate(),
                status.getCloseDate(),
                owner == null ? "" : Joiner.on(" ").skipNulls().join( owner.getName(), owner.getSurname()),
                siteUrl);

        IMetadataUtils metadataRepository = ApplicationContextHolder.get().getBean(IMetadataUtils.class);
        AbstractMetadata metadata = metadataRepository.findOne(status.getMetadataId());

        subject = compileMessageWithIndexFields(subject, metadata.getUuid(), this.language);
        message = compileMessageWithIndexFields(message, metadata.getUuid(), this.language);
        for (User user : userToNotify) {
            String salutation = Joiner.on(" ").skipNulls().join( user.getName(), user.getSurname());
            //If we have a salutation then end it with a ","
            if (StringUtils.isEmpty(salutation)) {
                salutation = "";
            } else {
                salutation += ",\n\n";
            }
            sendEmail(user.getEmail(), subject, salutation + message);
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
        UserRepository userRepository = context.getBean(UserRepository.class);
        List<User> users = new ArrayList<>();

        // TODO: Status does not provide batch update
        // So taking care of one record at a time.
        // Currently the code could notify a mix of reviewers
        // if records are not in the same groups. To be improved.
        Set<Integer> listOfId = new HashSet<>(1);
        listOfId.add(status.getMetadataId());

        if (notificationLevel != null) {
            if (notificationLevel == StatusValueNotificationLevel.statusUserOwner) {
                Optional<User> owner = userRepository.findById(status.getOwner());

                if (owner.isPresent()) {
                    users.add(owner.get());
                }
            } else if (notificationLevel == StatusValueNotificationLevel.recordProfileReviewer) {
                List<Pair<Integer, User>> results = userRepository.findAllByGroupOwnerNameAndProfile(listOfId,
                        Profile.Reviewer, SortUtils.createSort(User_.name));
                for (Pair<Integer, User> p : results) {
                    users.add(p.two());
                }
            } else if (notificationLevel == StatusValueNotificationLevel.recordUserAuthor) {
                Iterable<Metadata> records = this.context.getBean(MetadataRepository.class).findAllById(listOfId);
                for (Metadata r : records) {
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

    /**
     * Unset all operations on 'All' Group. Used when status
     * changes from approved to something else.
     *
     * @param mdId The metadata id to unset privileges on
     */
    private void unsetAllOperations(int mdId) throws Exception {
        Log.trace(Geonet.DATA_MANAGER, "DefaultStatusActions.unsetAllOperations(" + mdId + ")");

        int allGroup = 1;
        for (ReservedOperation op : ReservedOperation.values()) {
            dm.forceUnsetOperation(context, mdId, allGroup, op.getId());
        }
    }

    /**
     *
     * @param message  The message to work on
     * @param uuid     The record UUID
     * @param language The language (define the index to look into)
     * @return The message with field substituted by values
     */
    public static String compileMessageWithIndexFields(String message, String uuid, String language) {
        // Search lucene field to replace
        Matcher m = metadataLuceneField.matcher(message);
        ArrayList<String> fields = new ArrayList<String>();
        while (m.find()) {
            fields.add(m.group(1));
        }

        // First substitution for variables not stored in the index
        for (String f : fields) {
            String mdf = XslUtil.getIndexField(null, uuid, f, language);
            message = message.replace("{{index:" + f + "}}", mdf);
        }
        return message;
    }

    private String getTranslatedStatusName(int statusValueId) {
        String translatedStatusName = "";
        StatusValue s = _statusValueRepository.findOneById(statusValueId);
        if (s == null) {
            translatedStatusName = statusValueId
                    + " (Status not found in database translation table. Check the content of the StatusValueDes table.)";
        } else {
            translatedStatusName = s.getLabel(this.language);
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
    protected void sendEmail(String sendTo, String subject, String message) throws Exception {

        if (!emailNotes) {
            context.info("Would send email \nTo: " + sendTo + "\nSubject: " + subject + "\n Message:\n" + message);
        } else {
            ApplicationContext applicationContext = ApplicationContextHolder.get();
            SettingManager sm = applicationContext.getBean(SettingManager.class);
            // Doesn't make sense go further without any mailserver set...
            if(StringUtils.isNotBlank(sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST))) {
                List<String> to = new ArrayList<>();
                to.add(sendTo);
                MailUtil.sendMail(to, subject, message, null, sm, replyTo, replyToDescr);
            }
        }
    }
}
