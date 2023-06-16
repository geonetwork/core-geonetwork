/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.util;

import org.apache.commons.lang3.StringUtils;
import org.fao.geonet.api.API;
import org.fao.geonet.api.records.model.MetadataPublicationNotificationInfo;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.StatusValueNotificationLevel;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.metadata.DefaultStatusActions;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONGROUPS;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL;

@Component
public class MetadataPublicationMailNotifier {
    @Autowired
    SettingManager settingManager;

    @Autowired
    GroupRepository groupRepository;

    /**
     * Notify a metadata publication by mail.
     * <p>
     * Collects the email addresses to be notified based on the metadata publication
     * notification level configured in the settings.
     *
     * @param messages                        Message bundle with the mail texts.
     * @param mailLanguage                    Language to use for the mais.
     * @param metadataListToNotifyPublication List of notifications to send.
     */
    public void notifyPublication(ResourceBundle messages,
                                  String mailLanguage,
                                  List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication) {
        String notificationSetting = settingManager.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL);
        if (StringUtils.isNotEmpty(notificationSetting)) {
            StatusValueNotificationLevel notificationLevel =
                StatusValueNotificationLevel.valueOf(notificationSetting);
            if (notificationLevel != null) {

                if (notificationLevel == StatusValueNotificationLevel.recordProfileReviewer) {
                    Map<Integer, List<MetadataPublicationNotificationInfo>> metadataListToNotifyPublicationPerGroup =
                        metadataListToNotifyPublication.stream()
                            .collect(Collectors.groupingBy(MetadataPublicationNotificationInfo::getGroupId));

                    // Process the metadata published by group owner
                    metadataListToNotifyPublicationPerGroup.forEach((groupId, metadataNotificationInfoList) -> {
                        Set<Integer> metadataIds = metadataNotificationInfoList
                            .stream().map(MetadataPublicationNotificationInfo::getMetadataId).collect(Collectors.toSet());

                        List<User> userToNotify = DefaultStatusActions.getUserToNotify(notificationLevel,
                            metadataIds,
                            null);

                        List<String> toAddress1 = userToNotify.stream()
                            .map(User::getEmail)
                            .filter(StringUtils::isNotEmpty)
                            .collect(Collectors.toList());

                        sendMailPublicationNotification(messages, mailLanguage, toAddress1, metadataNotificationInfoList);
                    });

                } else {
                    List<String> toAddress;

                    if (notificationLevel == StatusValueNotificationLevel.recordGroupEmail) {
                        List<Group> groupToNotify = DefaultStatusActions.getGroupToNotify(notificationLevel,
                            Arrays.asList(settingManager.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONGROUPS).split("\\|")));

                        toAddress = groupToNotify.stream()
                            .map(Group::getEmail)
                            .filter(StringUtils::isNotEmpty)
                            .collect(Collectors.toList());
                    } else {
                        Set<Integer> metadataIds = metadataListToNotifyPublication
                            .stream().map(MetadataPublicationNotificationInfo::getMetadataId).collect(Collectors.toSet());

                        List<User> userToNotify = DefaultStatusActions.getUserToNotify(notificationLevel,
                            metadataIds,
                            null);

                        toAddress = userToNotify.stream()
                            .map(User::getEmail)
                            .filter(StringUtils::isNotEmpty)
                            .collect(Collectors.toList());

                    }

                    sendMailPublicationNotification(messages, mailLanguage, toAddress, metadataListToNotifyPublication);
                }
            }
        }
    }

    private void sendMailPublicationNotification(ResourceBundle messages,
                                                 String mailLanguage,
                                                 List<String> toAddress,
                                                 List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication) {
        if (toAddress.isEmpty()) {
            return;
        }

        String subject = String.format(
            messages.getString("metadata_published_subject"),
            settingManager.getSiteName());
        String message = messages.getString("metadata_published_text");

        String linkRecordTemplate = "{{link}}";
        String linkRecordUrlTemplate = settingManager.getNodeURL() + "api/records/{{index:uuid}}";

        String recordPublishedMessage = messages.getString("metadata_published_record_text")
            .replace(linkRecordTemplate, linkRecordUrlTemplate);
        String recordUnpublishedMessage = messages.getString("metadata_unpublished_record_text")
            .replace(linkRecordTemplate, linkRecordUrlTemplate);
        String recordReapprovedPublishedMessage = messages.getString("metadata_approved_published_record_text")
            .replace(linkRecordTemplate, linkRecordUrlTemplate);


        StringBuilder listOfProcessedMetadataMessage = new StringBuilder();

        metadataListToNotifyPublication.forEach(metadata -> {
            java.util.Optional<Group> group = groupRepository.findById(metadata.getGroupId());

            if (Boolean.TRUE.equals(metadata.getPublished())) {
                String recordPublishedMessageAux;

                if (!metadata.isReapproval()) {
                    recordPublishedMessageAux = replaceMessageValues(recordPublishedMessage, metadata, group);
                } else {
                    recordPublishedMessageAux = replaceMessageValues(recordReapprovedPublishedMessage, metadata, group);
                }

                listOfProcessedMetadataMessage.append(
                    MailUtil.compileMessageWithIndexFields(recordPublishedMessageAux, metadata.getMetadataUuid(), mailLanguage));
            } else {
                String recordUnpublishedMessageAux = replaceMessageValues(recordUnpublishedMessage, metadata, group);

                listOfProcessedMetadataMessage.append(
                    MailUtil.compileMessageWithIndexFields(recordUnpublishedMessageAux, metadata.getMetadataUuid(), mailLanguage));
            }
        });

        String htmlMessage = String.format(message, listOfProcessedMetadataMessage);

        // Send mail to notify about metadata publication / un-publication
        try {
            MailUtil.sendHtmlMail(toAddress, subject, htmlMessage, settingManager);
        } catch (IllegalArgumentException ex) {
            Log.warning(API.LOG_MODULE_NAME, ex.getMessage(), ex);
        }
    }


    private String replaceMessageValues(String message, MetadataPublicationNotificationInfo metadata, Optional<Group> group) {
        String messageAux = message
            .replace("{{publisherUser}}", metadata.getPublisherUser())
            .replace("{{submitterUser}}", metadata.getSubmitterUser())
            .replace("{{reviewerUser}}", metadata.getReviewerUser())
            .replace("{{timeStamp}}", metadata.getPublicationDateStamp().getDateAndTime());

        if (group.isPresent()) {
            messageAux = messageAux.replace("{{group}}", group.get().getName());
        }

        return messageAux;
    }
}
