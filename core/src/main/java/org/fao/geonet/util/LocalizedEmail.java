//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.languages.FeedbackLanguages;
import org.fao.geonet.utils.Log;

import static org.fao.geonet.util.LocalizedEmailComponent.ComponentType.*;
import static org.fao.geonet.util.LocalizedEmailComponent.ComponentType;

import java.util.*;

/**
 * Class representing a localized email.
 */
public class LocalizedEmail {
    private final Boolean isHtml;
    private final Map<ComponentType, LocalizedEmailComponent> components;
    private final String translationFollowsText;

    private static final String SUBJECT_DELIMITER = " | ";
    private static final String HTML_MESSAGE_DELIMITER = "<hr>";
    private static final String HTML_LINE_BREAK = "<br><br>";
    private static final String TEXT_MESSAGE_DELIMITER = "\n\n--------------------------------------------------------\n\n";
    private static final String TEXT_LINE_BREAK = "\n\n";

    public LocalizedEmail(Boolean isHtml) {
        this.isHtml = isHtml;

        FeedbackLanguages feedbackLanguages = ApplicationContextHolder.get().getBean(FeedbackLanguages.class);
        this.translationFollowsText = feedbackLanguages.getTranslationFollowsText();

        this.components = new HashMap<>();
    }

    /**
     * Add one or more components to the email object. Existing components are replaced.
     *
     * @param newComponents The components to add to the email.
     */
    public void addComponents(LocalizedEmailComponent... newComponents) {

        for (LocalizedEmailComponent newComponent : newComponents) {

            if (newComponent == null) {
                throw new IllegalArgumentException("Null parameter not allowed");
            }

            components.put(newComponent.getComponentType(), newComponent);
        }
    }

    public String getParsedSubject(Locale[] feedbackLocales) {
        LinkedHashMap<Locale, String> subjects = components.get(SUBJECT).getParsedMessagesMap(feedbackLocales);
        return String.join(SUBJECT_DELIMITER, subjects.values());
    }

    public String getParsedMessage(Locale[] feedbackLocales) {
        return getParsedMessage(feedbackLocales, null);
    }

    public String getParsedMessage(Locale[] feedbackLocales, Map<String, String> replacements) {
        LinkedHashMap<Locale, String> messages = components.get(MESSAGE).getParsedMessagesMap(feedbackLocales, true);

        // Prepend the message with a salutation placeholder if the salutation component is present
        if (components.containsKey(SALUTATION) && components.get(SALUTATION) != null) {

            LinkedHashMap<Locale, String> salutations = components.get(SALUTATION).getParsedMessagesMap(feedbackLocales);
            LinkedHashMap<Locale, String> messagesWithSalutations = new LinkedHashMap<>();

            for (Map.Entry<Locale, String> entry : messages.entrySet()) {
                //Skip messages that have no matching salutation
                if (!salutations.containsKey(entry.getKey())) {
                    continue;
                }

                String message = entry.getValue();
                String salutation = salutations.get(entry.getKey());

                if (replacements != null && !replacements.isEmpty()) {
                    for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                        salutation = salutation.replace(replacement.getKey(), replacement.getValue());
                    }
                }

                messagesWithSalutations.put(entry.getKey(), salutation + message);
            }

            messages = messagesWithSalutations;

        }

        String messageDelimiter;
        String lineBreak;

        // Set the delimiter and break string to use based on email type
        if (isHtml) {
            messageDelimiter = HTML_MESSAGE_DELIMITER;
            lineBreak = HTML_LINE_BREAK;
            // Wrap each message in a div with a lang attribute for accessibility
            messages.replaceAll((locale, message) -> "<div lang=\"" + XslUtil.twoCharLangCode(locale.getISO3Language()) + "\">" + message + "</div>");
        } else {
            messageDelimiter = TEXT_MESSAGE_DELIMITER;
            lineBreak = TEXT_LINE_BREAK;
        }

        String emailMessage = String.join(messageDelimiter, messages.values());

        // Prepend the message with the translation follows label if there is more than one language specified
        if (messages.size() > 1 && !StringUtils.isBlank(translationFollowsText)) {
            emailMessage = translationFollowsText + lineBreak + emailMessage;
        }

        // If the email is html wrap the content in html and body tags
        if (isHtml) {
            if (emailMessage.contains("<html>") || emailMessage.contains("<body>")) {
                Log.warning(Log.GEONETWORK_MODULE + ".localizedemail","Multilingual emails are unsupported for HTML emails with messages containing <html> or <body> tags. Reverting to first specified locale.");
                return messages.get(feedbackLocales[0]);
            }
            emailMessage = "<html><body>" + emailMessage + "</body></html>";
        }

        return emailMessage;
    }
}

