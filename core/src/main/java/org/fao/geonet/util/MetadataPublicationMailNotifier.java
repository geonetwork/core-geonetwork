/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONGROUPS;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL;
import static org.fao.geonet.util.LocalizedEmailComponent.ComponentType.*;
import static org.fao.geonet.util.LocalizedEmailComponent.KeyType;
import static org.fao.geonet.util.LocalizedEmailComponent.ReplacementType.*;
import static org.fao.geonet.util.LocalizedEmailParameter.ParameterType;

@Component
public class MetadataPublicationMailNotifier {
    @Value("${metadata.publicationmail.format.html:true}")
    private boolean sendHtmlMail;

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
     * @param feedbackLocales                         Array of locales to be used in email.
     * @param metadataListToNotifyPublication List of notifications to send.
     */
    public void notifyPublication(Locale[] feedbackLocales,
                                  List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication) {
        String notificationSetting = settingManager.getValue(SYSTEM_METADATAPRIVS_PUBLICATION_NOTIFICATIONLEVEL);
        if (StringUtils.isNotEmpty(notificationSetting)) {
            StatusValueNotificationLevel notificationLevel =
                StatusValueNotificationLevel.valueOf(notificationSetting);
            if (notificationLevel != null) {

                if (notificationLevel.name().startsWith("recordProfile")) {
                    Map<Integer, List<MetadataPublicationNotificationInfo>> metadataListToNotifyPublicationPerGroup =
                        metadataListToNotifyPublication.stream()
                            .filter(metadata -> metadata.getGroupId() != null)
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

                        sendMailPublicationNotification(feedbackLocales, toAddress1, metadataNotificationInfoList);
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

                    sendMailPublicationNotification(feedbackLocales, toAddress, metadataListToNotifyPublication);
                }
            }
        }
    }

    private void sendMailPublicationNotification(Locale[] feedbackLocales,
                                                 List<String> toAddress,
                                                 List<MetadataPublicationNotificationInfo> metadataListToNotifyPublication) {
        if (toAddress.isEmpty()) {
            return;
        }

        LocalizedEmailComponent emailSubjectComponent = new LocalizedEmailComponent(SUBJECT, "metadata_published_subject", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);
        LocalizedEmailComponent emailMessageComponent = new LocalizedEmailComponent(MESSAGE, "metadata_published_text", KeyType.MESSAGE_KEY, POSITIONAL_FORMAT);

        for (Locale feedbackLocale : feedbackLocales) {
            emailSubjectComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, settingManager.getSiteName())
            );

            StringBuilder listOfProcessedMetadataMessage = new StringBuilder();

            metadataListToNotifyPublication.forEach(metadata -> {
                java.util.Optional<Group> group = groupRepository.findById(metadata.getGroupId());

                String recordMessageKey;

                if (Boolean.TRUE.equals(metadata.getPublished())) {
                    if (!metadata.isReapproval()) {
                        recordMessageKey = "metadata_published_record_text";
                    } else {
                        recordMessageKey = "metadata_approved_published_record_text";
                    }
                } else {
                    recordMessageKey = "metadata_unpublished_record_text";
                }

                LocalizedEmailComponent recordMessageComponent = new LocalizedEmailComponent(NESTED, recordMessageKey, KeyType.MESSAGE_KEY, NAMED_FORMAT);
                recordMessageComponent.enableCompileWithIndexFields(metadata.getMetadataUuid());
                recordMessageComponent.enableReplaceLinks(true);

                recordMessageComponent.addParameters(feedbackLocale, getReplacementParameters(metadata, group));

                listOfProcessedMetadataMessage.append(recordMessageComponent.parseMessage(feedbackLocale));

            });

            emailMessageComponent.addParameters(
                feedbackLocale,
                new LocalizedEmailParameter(ParameterType.RAW_VALUE, 1, listOfProcessedMetadataMessage.toString())
            );
        }

        LocalizedEmail localizedEmail = new LocalizedEmail(sendHtmlMail);
        localizedEmail.addComponents(emailSubjectComponent, emailMessageComponent);

        String subject = localizedEmail.getParsedSubject(feedbackLocales);
        String message = localizedEmail.getParsedMessage(feedbackLocales);

        // Send mail to notify about metadata publication / un-publication
        try {
            if (sendHtmlMail) {
                MailUtil.sendHtmlMail(toAddress, subject, message, settingManager);
            } else {
                MailUtil.sendMail(toAddress, subject, message, settingManager);
            }
        } catch (IllegalArgumentException ex) {
            Log.warning(API.LOG_MODULE_NAME, ex.getMessage(), ex);
        }
    }


    private LocalizedEmailParameter[] getReplacementParameters(MetadataPublicationNotificationInfo metadata, Optional<Group> group) {

        ArrayList<LocalizedEmailParameter> parameters = new ArrayList<>();

        parameters.add(new LocalizedEmailParameter(ParameterType.RAW_VALUE, "{{publisherUser}}", metadata.getPublisherUser()));
        parameters.add(new LocalizedEmailParameter(ParameterType.RAW_VALUE, "{{submitterUser}}", metadata.getSubmitterUser()));
        parameters.add(new LocalizedEmailParameter(ParameterType.RAW_VALUE, "{{reviewerUser}}", metadata.getReviewerUser()));
        parameters.add(new LocalizedEmailParameter(ParameterType.RAW_VALUE, "{{timeStamp}}", metadata.getPublicationDateStamp().getDateAndTime()));

        if (group.isPresent()) {
            parameters.add(new LocalizedEmailParameter(ParameterType.RAW_VALUE, "{{group}}", group.get().getName()));
        }

        return parameters.toArray(new LocalizedEmailParameter[0]);
    }
}
