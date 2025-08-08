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
import org.fao.geonet.kernel.search.JSONLocCacheLoader;
import org.fao.geonet.kernel.setting.SettingManager;

import java.text.MessageFormat;
import java.util.*;

import static org.fao.geonet.util.LocalizedEmailComponent.ReplacementType.*;

/**
 * This class is used to handle email parameters used to format localized email messages
 */
public class LocalizedEmailComponent {

    private final ComponentType componentType;
    private final String keyOrRawValue;
    private final KeyType keyType;
    private final ReplacementType replacementType;
    private final Map<Locale, ArrayList<LocalizedEmailParameter>> parameters;
    private Boolean compileWithIndexFields;
    private String metadataUuid;
    private Boolean replaceLinks;
    private Boolean replaceLinksWithHtmlFormat = false;

    /**
     * Enum representing the types of components in an email.
     * <p>
     * This enum defines four types of components:
     * <ul>
     * <li>{@link ComponentType#SUBJECT SUBJECT}: The email subject field.</li>
     * <li>{@link ComponentType#MESSAGE MESSAGE}: The email body.</li>
     * <li>{@link ComponentType#SALUTATION SALUTATION}: The salutation to prepend each localized message with. (Ex. 'Hello John')</li>
     * <li>{@link ComponentType#NESTED NESTED}: A component of insignificant type that is used to generate other components.</li>
     * </ul>
     */
    public enum ComponentType {
        /**
         * The email subject field.
         */
        SUBJECT,

        /**
         * The email body.
         */
        MESSAGE,

        /**
         * The salutation to prepend each localized message with. (Ex. 'Hello John').
         */
        SALUTATION,

        /**
         * A component of insignificant type that is used to generate other components.
         */
        NESTED
    }

    /**
     * Enum representing the types of keys used to parse a components message.
     * <p>
     * This enum defines four types of keys:
     * <ul>
     * <li>{@link KeyType#MESSAGE_OR_JSON_KEY MESSAGE_OR_JSON_KEY}: Represents a component that tries to retrieve its value using {@link ResourceBundle#getString} or JSON localization files if message key was not found.</li>
     * <li>{@link KeyType#MESSAGE_KEY MESSAGE_KEY}: Represents a component that retrieves its value using {@link ResourceBundle#getString}.</li>
     * <li>{@link KeyType#JSON_KEY JSON_KEY}: Represents a component that retrieves its value by searching the JSON localization files for the specified key.</li>
     * <li>{@link KeyType#RAW_VALUE RAW_VALUE}: Represents a component in which keys are not required. The raw value from keyOrRawValue is used.</li>
     * </ul>
     * </p>
     */
    public enum KeyType {
        /**
         * Represents a component that tries to retrieve its value using {@link ResourceBundle#getString} or JSON localization files if message key was not found.
         */
        MESSAGE_OR_JSON_KEY,

        /**
         * Represents a component that retrieves its value using {@link ResourceBundle#getString}.
         */
        MESSAGE_KEY,

        /**
         * Represents a component that retrieves its value by searching the JSON localization files for the specified key.
         */
        JSON_KEY,

        /**
         * Represents a component in which keys are not required. The raw value from keyOrRawValue is used.
         */
        RAW_VALUE
    }

    /**
     * Enum representing the types of replacements performed on the email component.
     * <p>
     * This enum defines four types of replacement:
     * <ul>
     * <li>{@link ReplacementType#POSITIONAL_FORMAT POSITIONAL_FORMAT}: A parameter that retrieves its value using {@link ResourceBundle#getString}.
     * The value property is set to the message key to search for.</li>
     * <li>{@link ReplacementType#NUMERIC_FORMAT NUMERIC_FORMAT}: A parameter that retrieves its value by searching the JSON localization files for the specified key.
     * The value property is set to the json key to search for.</li>
     * <li>{@link ReplacementType#NAMED_FORMAT NAMED_FORMAT}: A parameter that retrieves its value using {@link XslUtil#getIndexField}.
     * The value property is set to the field name to search for, and the uuid property is set to the record uuid to search for (required).</li>
     * <li>{@link ReplacementType#NONE NONE}: For components that require no replacement to compute their values.</li>
     * </ul>
     * </p>
     */
    public enum ReplacementType {
        /**
         * For {@link String#format}, where parameters are replaced based on their position (Ex. %s).
         * The parameter id stores an integer representing the order of the parameters.
         */
        POSITIONAL_FORMAT,

        /**
         * For {@link MessageFormat#format}, where parameters are replaced based on position (Ex. {0}).
         * The parameter id stores an integer representing the order of the parameters.
         */
        NUMERIC_FORMAT,

        /**
         * For {@link String#replace}, where parameters are replaced based on their names ({{title}}).
         * The parameter id stores the string to replace.
         */
        NAMED_FORMAT,

        /**
         * For components that require no replacement to compute their values.
         */
        NONE
    }

    /**
     * Constructor for LocalizedEmailParameters.
     *
     * @param replacementType the type of template variable
     */
    public LocalizedEmailComponent(ComponentType componentType, String keyOrRawValue, KeyType keyType, ReplacementType replacementType) {
        this.componentType = componentType;
        this.keyOrRawValue = keyOrRawValue;
        this.keyType = keyType;
        this.replacementType = replacementType;
        this.parameters = new HashMap<>();
        this.compileWithIndexFields = false;
        this.metadataUuid = null;
        this.replaceLinks = false;
    }

    /**
     * Adds parameters to the email parameters list.
     *
     * @param newParameters the parameters to add
     * @throws IllegalArgumentException if a null parameter is passed or if a duplicate parameter id is found
     */
    public void addParameters(Locale locale, LocalizedEmailParameter... newParameters) {
        // If the map does not have the locale as a key add it
        if (!parameters.containsKey(locale)) {
            parameters.put(locale, new ArrayList<>());
        }

        for (LocalizedEmailParameter newParameter : newParameters) {

            if (newParameter == null) {
                throw new IllegalArgumentException("Null parameter not allowed");
            }

            // If the parameter id is already in the list
            if (parameters.get(locale).stream().anyMatch(existingParameter -> newParameter.getId().equals(existingParameter.getId()))) {
                throw new IllegalArgumentException("Duplicate parameter id: " + newParameter.getId());
            }

            // If the type of parameters are positional and the new parameters id is not an integer
            if ((replacementType.equals(POSITIONAL_FORMAT) || replacementType.equals(NUMERIC_FORMAT)) && !(newParameter.getId() instanceof Integer)) {
                throw new IllegalArgumentException("Positional parameter id must be an integer");
            }

            parameters.get(locale).add(newParameter);
        }
    }

    /**
     * @return the map of locales to lists of email parameters
     */
    public Map<Locale, ArrayList<LocalizedEmailParameter>> getParameters() {
        return parameters;
    }

    /**
     * Enables the compilation with index fields and sets the metadata UUID.
     *
     * @param metadataUuid the metadata UUID
     */
    public void enableCompileWithIndexFields(String metadataUuid) {
        this.compileWithIndexFields = true;
        this.metadataUuid = metadataUuid;
    }

    /**
     * Sets the replace links flag and format.
     *
     * @param useHtmlFormat replace links using the HTML format instead of the text format.
     */
    public void enableReplaceLinks(Boolean useHtmlFormat) {
        this.replaceLinks = true;
        this.replaceLinksWithHtmlFormat = useHtmlFormat;
    }

    /**
     * @return The type of the component.
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * Parses the message based on the provided key or template and locale.
     *
     * @param locale the locale
     * @return the parsed message
     * @throws RuntimeException if an unsupported template variable type is encountered
     */
    public String parseMessage(Locale locale) {

        ArrayList<LocalizedEmailParameter> parametersForLocale = parameters.getOrDefault(locale, new ArrayList<>());

        String parsedMessage;
        switch (keyType) {
            case MESSAGE_OR_JSON_KEY:
                try {
                    parsedMessage = getResourceBundleString(locale);
                } catch (MissingResourceException missingResourceException) {
                    parsedMessage = getTranslationMapString(locale);
                }
                break;
            case MESSAGE_KEY:
                try {
                    parsedMessage = getResourceBundleString(locale);
                } catch (MissingResourceException e) {
                    parsedMessage = keyOrRawValue;
                }
                break;
            case JSON_KEY:
                parsedMessage = getTranslationMapString(locale);
                break;
            case RAW_VALUE:
                parsedMessage = keyOrRawValue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported key type: " + keyType);
        }

        // Handle replacements
        if (replacementType == POSITIONAL_FORMAT || replacementType == NUMERIC_FORMAT) {

            Object[] parsedLocaleEmailParameters = parametersForLocale.stream()
                .sorted(Comparator.comparing(parameter -> (Integer) parameter.getId()))
                .map(parameter -> parameter.parseValue(locale))
                .toArray();

            if (replacementType == POSITIONAL_FORMAT) {
                parsedMessage = String.format(parsedMessage, parsedLocaleEmailParameters);
            } else {
                // Replace the link placeholders with index field placeholder so that it isn't interpreted as a MessageFormat arg
                if (replaceLinks) {
                    parsedMessage = replaceLinks(parsedMessage);
                }
                parsedMessage = MessageFormat.format(parsedMessage, parsedLocaleEmailParameters);
            }

        } else if (replacementType == NAMED_FORMAT) {

            for (LocalizedEmailParameter parameter : parametersForLocale) {
                parsedMessage = parsedMessage.replace(parameter.getId().toString(), parameter.parseValue(locale));
            }

        }

        // Replace link placeholders
        if (replaceLinks) {
            parsedMessage = replaceLinks(parsedMessage);
        }

        // Replace index field placeholders
        if (compileWithIndexFields && metadataUuid != null) {
            parsedMessage = MailUtil.compileMessageWithIndexFields(parsedMessage, metadataUuid, locale.getLanguage());
        }

        return parsedMessage;
    }

    /**
     * Returns a map of locales to parsed messages for the provided array of locales.
     *
     * @param feedbackLocales the array of locales
     * @return the map of locales to parsed messages
     */
    public LinkedHashMap<Locale, String> getParsedMessagesMap(Locale[] feedbackLocales) {
        return getParsedMessagesMap(feedbackLocales, false);
    }

    /**
     * Returns a map of locales to parsed messages for the provided array of locales.
     * If flagged only distinct values are returned.
     *
     * @param feedbackLocales the array of locales
     * @param distinct flag to only return messages with distinct values
     * @return the map of locales to parsed messages
     */
    public LinkedHashMap<Locale, String> getParsedMessagesMap(Locale[] feedbackLocales, Boolean distinct) {

        LinkedHashMap<Locale, String> parsedMessages = new LinkedHashMap<>();

        for (Locale locale : feedbackLocales) {
            String parsedMessage = parseMessage(locale);
            if (!distinct || !parsedMessages.containsValue(parsedMessage)) {
                parsedMessages.put(locale, parsedMessage);
            }
        }

        return parsedMessages;
    }

    private String getResourceBundleString(Locale locale) {
        return ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale).getString(keyOrRawValue);
    }

    private String getTranslationMapString(Locale locale) {
        try {
            Map<String, String> translationMap = new JSONLocCacheLoader(ApplicationContextHolder.get(), locale.getISO3Language()).call();
            return translationMap.getOrDefault(keyOrRawValue, keyOrRawValue);
        } catch (Exception exception) {
            return keyOrRawValue;
        }
    }

    private String replaceLinks(String message) {

        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);

        // Get the emailRecordViewFormatter from the UI configuration
        String emailRecordViewFormatter = XslUtil.getUiConfigurationJsonProperty(null, "mods.search.emailRecordViewFormatter");

        // Set the recordViewFormatter parameter if emailRecordViewFormatter is present
        String emailRecordViewFormatterParameter = StringUtils.isNotBlank(emailRecordViewFormatter) ? "?recordViewFormatter=" + emailRecordViewFormatter : "";

        String newPlaceholder;
        if (replaceLinksWithHtmlFormat) {
            newPlaceholder = "{{index:uuid}}";
        } else {
            newPlaceholder = "'{{'index:uuid'}}'";
        }
        return message.replace("{{link}}", settingManager.getNodeURL() + "api/records/" + newPlaceholder + emailRecordViewFormatterParameter);
    }
}
