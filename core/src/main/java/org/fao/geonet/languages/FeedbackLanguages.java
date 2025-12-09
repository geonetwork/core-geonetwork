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

package org.fao.geonet.languages;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Represents a utility class for managing supported locales and translation follows text for feedback.
 */
public class FeedbackLanguages {
    private Locale[] supportedLocales;
    private String translationFollowsText;

    @Autowired
    SettingManager settingManager;

    /**
     * Initializes the supported locales and translation follows text after bean creation.
     */
    @PostConstruct
    public void init() {
        updateSupportedLocales();
        updateTranslationFollowsText();
    }

    /**
     * Updates the supported locales based on the system feedback languages setting.
     */
    public void updateSupportedLocales() {
        String systemFeedbackLanguages = getSettingsValue(Settings.SYSTEM_FEEDBACK_LANGUAGES);

        if (StringUtils.isBlank(systemFeedbackLanguages)) {
            supportedLocales = null;
            return;
        }

        supportedLocales = Arrays.stream(systemFeedbackLanguages.split(","))
            .map(String::trim)
            .map(Locale::new)
            .filter(this::isValidLocale)
            .toArray(Locale[]::new);
    }

    /**
     * Updates the translation follows text based on the system feedback translation text setting.
     */
    public void updateTranslationFollowsText() {
        translationFollowsText = getSettingsValue(Settings.SYSTEM_FEEDBACK_TRANSLATION_FOLLOWS_TEXT);
    }

    /**
     * Retrieves the supported locales. If no supported locales are found, returns a fallback locale.
     * @param fallbackLocale The fallback locale to be returned if no supported locales are available.
     * @return An array of supported locales or a single fallback locale if none are available.
     */
    public Locale[] getLocales(Locale fallbackLocale) {
        if (supportedLocales == null || supportedLocales.length < 1) {
            return new Locale[] { fallbackLocale };
        }

        return supportedLocales;
    }

    /**
     * Retrieves the translation follows text.
     * @return The translation follows text.
     */
    public String getTranslationFollowsText() {
        return translationFollowsText;
    }

    /**
     * Checks if the provided locale is valid by attempting to load a ResourceBundle.
     * @param locale The locale to validate.
     * @return True if the locale is valid, false otherwise.
     */
    private boolean isValidLocale(Locale locale) {
        Boolean isValid;
        try {
            isValid = locale.getLanguage().equals(Geonet.DEFAULT_LANGUAGE)
                || ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale).getLocale().getLanguage().equals(locale.getLanguage());
        } catch (MissingResourceException e) {
            isValid = false;
        }
        if (!isValid) {
            String localeLanguage;
            try {
                localeLanguage = locale.getISO3Language();
            } catch (MissingResourceException e) {
                localeLanguage = locale.getLanguage();
            }
            Log.warning(Log.GEONETWORK_MODULE + ".feedbacklanguages", "Locale '" + localeLanguage + "'  is invalid or missing message bundles. Ensure feedback locales are correct.");
        }
        return isValid;
    }

    private String getSettingsValue(String settingName) {
        return settingManager.getValue(settingName);
    }
}
