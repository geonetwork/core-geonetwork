//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Selection;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SelectionRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.UserSavedSelectionRepository;
import org.fao.geonet.util.MailUtil;
import org.fao.geonet.utils.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.*;

import static org.fao.geonet.kernel.metadata.DefaultStatusActions.compileMessageWithIndexFields;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_USER_LASTNOTIFICATIONDATE;

/**
 * Task checking on a regular basis the list of records
 * with changes in user watch list.
 */
public class WatchListNotifier extends QuartzJobBean {

    private String lastNotificationDate;
    private String nextLastNotificationDate;
    private String subject;
    private String message;
    private String recordMessage;
    private String updatedRecordPermalink;
    private String language = "eng";
    private SettingManager settingManager;
    private ApplicationContext appContext;
    private UserSavedSelectionRepository userSavedSelectionRepository;
    private UserRepository userRepository;

    @Value("${usersavedselection.watchlist.searchurl}")
    private String permalinkApp = "catalog.search#/search?_uuid={{filter}}";

    @Value("${usersavedselection.watchlist.recordurl}")
    private String permalinkRecordApp = "api/records/{{index:_uuid}}";

    public String getPermalinkApp() {
        return permalinkApp;
    }

    public void setPermalinkApp(String permalinkApp) {
        this.permalinkApp = permalinkApp;
    }

    public String getPermalinkRecordApp() {
        return permalinkRecordApp;
    }

    public void setPermalinkRecordApp(String permalinkRecordApp) {
        this.permalinkRecordApp = permalinkRecordApp;
    }

    public WatchListNotifier() {
    }

    @Override
    protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
        appContext = ApplicationContextHolder.get();
        settingManager = appContext.getBean(SettingManager.class);

        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
            new Locale(
                language
            ));

        try {
            subject = messages.getString("user_watchlist_subject");
            message = messages.getString("user_watchlist_message");
            recordMessage = messages.getString("user_watchlist_message_record").
                replace("{{link}}",
                    settingManager.getNodeURL() + permalinkRecordApp);
        } catch (Exception e) {
        }

        updatedRecordPermalink = settingManager.getSiteURL(language);

        lastNotificationDate = settingManager.getValue(SYSTEM_USER_LASTNOTIFICATIONDATE);
        nextLastNotificationDate = new ISODate().toString();

        if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
            Log.debug(Geonet.USER_WATCHLIST, String.format(
                "Last notification date for saved selection was %s.",
                lastNotificationDate
            ));
        }

        userSavedSelectionRepository = appContext.getBean(UserSavedSelectionRepository.class);
        userRepository = appContext.getBean(UserRepository.class);

        SelectionRepository selectionRepository = appContext.getBean(SelectionRepository.class);
        final List<Selection> selectionList = selectionRepository.findAll();
        for (Selection selection : selectionList) {
            if (selection.isWatchable()) {
                Integer selectionId = selection.getId();

                final List<Integer> allUsers = userSavedSelectionRepository.findAllUsers(selectionId);

                // Start notification after one notification is made
                if (StringUtils.isNotBlank(lastNotificationDate)) {
                    if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
                        Log.debug(Geonet.USER_WATCHLIST, String.format(
                            "  Notifying %d users about changes since %s in list %d",
                            allUsers.size(),
                            lastNotificationDate,
                            selectionId
                        ));
                    }
                    for (Integer userId : allUsers) {
                        notify(selectionId, userId);
                    }
                } else {
                    if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
                        Log.debug(Geonet.USER_WATCHLIST, String.format(
                            "  Notification of %d users saved selection %d will start on next run. Last notification date was null",
                            allUsers.size(),
                            selectionId
                        ));
                    }
                }
            }
        }

        settingManager.setValue(SYSTEM_USER_LASTNOTIFICATIONDATE, nextLastNotificationDate);

        if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
            Log.debug(Geonet.USER_WATCHLIST, String.format(
                "Next notification date for saved selection is now %s.",
                nextLastNotificationDate
            ));
        }
    }

    private void notify(Integer selectionId, Integer userId) {
        // Get metadata with changes since last notification
        // TODO: Could be relevant to get versionning system info once available
        // and report deleted records too.
        final List<String> updatedRecords =
            userSavedSelectionRepository.findMetadataUpdatedAfter(
                selectionId, userId, lastNotificationDate, nextLastNotificationDate);

        if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
            Log.debug(Geonet.USER_WATCHLIST, String.format(
                "    Notifying user %d about %d changes since %s in list %d",
                userId,
                updatedRecords.size(),
                lastNotificationDate,
                selectionId
            ));
        }

        if (updatedRecords.size() > 0) {
            // Check if user exists and has an email
            // TODO: We should send email depending on user language
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent() && StringUtils.isNotEmpty(user.get().getEmail())) {

                // Build message
                StringBuffer listOfUpdateMessage = new StringBuffer();
                for (String record : updatedRecords) {
                    try {
                        listOfUpdateMessage.append(
                            compileMessageWithIndexFields(recordMessage, record, this.language)
                        );
                    } catch (Exception e) {
                        Log.error(Geonet.USER_WATCHLIST, e.getMessage(), e);
                    }
                }

                String url = updatedRecordPermalink +
                    permalinkApp.replace("{{filter}}", String.join(" or ", updatedRecords));
                String mailSubject = String.format(subject,
                    settingManager.getSiteName(), updatedRecords.size(), lastNotificationDate);
                String htmlMessage = String.format(message,
                    listOfUpdateMessage.toString(),
                    lastNotificationDate,
                    url, url);

                if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
                    Log.debug(Geonet.USER_WATCHLIST, String.format(
                        "    Sending message with subject %s to user %d",
                        mailSubject, userId
                    ));
                }

                // Send email
                MailUtil.sendHtmlMail(
                    Arrays.asList(new String[]{user.get().getEmail()}),
                    mailSubject, htmlMessage, settingManager);
            }
        } else {
            if (Log.isDebugEnabled(Geonet.USER_WATCHLIST)) {
                Log.debug(Geonet.USER_WATCHLIST, String.format(
                    "    No changes for user %d since %s in his/her list %d",
                    userId,
                    lastNotificationDate,
                    selectionId
                ));
            }
        }
    }
}
