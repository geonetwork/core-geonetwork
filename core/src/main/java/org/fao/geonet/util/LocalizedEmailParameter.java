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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.search.JSONLocCacheLoader;

import java.util.*;

/**
 * Class representing a parameter used in a localized email.
 * It provides functionality to set and get parameter properties, and parse parameter values.
 */
public class LocalizedEmailParameter {
    private final Object id;
    private final ParameterType parameterType;
    private final Object value; // (Based on Parameter type)
    private final Object metadataUuid;

    /**
     * Enum representing different types of parameters used in a localized email context.
     * <p>
     * This enum defines five types of parameters:
     * <ul>
     * <li>{@link ParameterType#MESSAGE_OR_JSON_KEY MESSAGE_OR_JSON_KEY}: A parameter that tries to retrieve its value using {@link ResourceBundle#getString} or JSON localization files if message key was not found.
     * The value property is set to the (message or json) key to search for.</li>
     * <li>{@link ParameterType#MESSAGE_KEY MESSAGE_KEY}: A parameter that retrieves its value using {@link ResourceBundle#getString}.
     * The value property is set to the message key to search for.</li>
     * <li>{@link ParameterType#JSON_KEY JSON_KEY}: A parameter that retrieves its value by searching the JSON localization files for the specified key.
     * The value property is set to the json key to search for.</li>
     * <li>{@link ParameterType#INDEX_FIELD INDEX_FIELD}: A parameter that retrieves its value using {@link XslUtil#getIndexField}.
     * The value property is set to the field name to search for, and the uuid property is set to the record uuid to search for (required).</li>
     * <li>{@link ParameterType#RAW_VALUE RAW_VALUE}: A parameter with a precomputed value that is simply returned.
     * The value property contains the precomputed value.</li>
     * </ul>
     * </p>
     * These types can be used to categorize parameters and define their intended use in the context of localized email parameterization.
     */
    public enum ParameterType {
        /**
         * A parameter that tries to retrieve its value using {@link ResourceBundle#getString} or JSON localization files if message key was not found.
         * The value property is set to the (message or json) key to search for.
         */
        MESSAGE_OR_JSON_KEY,

        /**
         * A parameter that retrieves its value using {@link ResourceBundle#getString}
         * The value property is set to the message key to search for.
         */
        MESSAGE_KEY,

        /**
         * A parameter that retrieves its value by searching the JSON localization files for the specified key.
         * The value property is set to the json key to search for.
         */
        JSON_KEY,

        /**
         * A parameter that retrieves its value using {@link XslUtil#getIndexField}
         * The value property is set to the field name to search for.
         * The uuid property is set to the record uuid to search for and is required.
         */
        INDEX_FIELD,

        /**
         * A parameter with a precomputed value that is simply returned.
         * The value property contains the precomputed value.
         */
        RAW_VALUE
    }

    /**
     * Constructor with parameters.
     *
     * @param parameterType the type of the parameter
     * @param id the id of the parameter
     * @param value the value of the parameter
     */
    public LocalizedEmailParameter(ParameterType parameterType, Object id, Object value) {
        this.parameterType = parameterType;
        this.id = id;
        this.value = value;
        this.metadataUuid = null;
    }

    /**
     * Constructor with parameters.
     *
     * @param parameterType the type of the parameter
     * @param id the id of the parameter
     * @param value the value of the parameter
     * @param metadataUuid The metadata uuid to use for parsing index field values
     */
    public LocalizedEmailParameter(ParameterType parameterType, Object id, Object value, String metadataUuid) {
        this.parameterType = parameterType;
        this.id = id;
        this.value = value;
        this.metadataUuid = metadataUuid;
    }

    /**
     * @return the id of the parameter
     */
    public Object getId() {
        return id;
    }

    /**
     * Parses the value of the parameter based on its type and the provided locale
     *
     * @param locale the locale to use to parse the value
     * @return the parsed string value
     */
    public String parseValue(Locale locale) {

        if (value == null) {
            return "null";
        }

        switch (parameterType) {
            case MESSAGE_OR_JSON_KEY:
                try {
                    return getResourceBundleString(locale);
                } catch (MissingResourceException missingResourceException) {
                    return getJsonTranslationMapString(locale);
                }
            case MESSAGE_KEY:
                try {
                    return getResourceBundleString(locale);
                } catch (MissingResourceException e) {
                    return value.toString();
                }
            case JSON_KEY:
                return getJsonTranslationMapString(locale);
            case INDEX_FIELD:
                if (metadataUuid == null) throw new IllegalArgumentException("Metadata UUID is required for parameters of type INDEX_FIELD");
                return XslUtil.getIndexField(null, metadataUuid, value, locale);
            case RAW_VALUE:
                return value.toString();
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
        }
    }

    private String getResourceBundleString(Locale locale) {
        return ResourceBundle.getBundle("org.fao.geonet.api.Messages", locale).getString(value.toString());
    }

    private String getJsonTranslationMapString(Locale locale) {
        try {
            Map<String, String> translationMap = new JSONLocCacheLoader(ApplicationContextHolder.get(), locale.getISO3Language()).call();
            return translationMap.getOrDefault(value.toString(), value.toString());
        } catch (Exception exception) {
            return value.toString();
        }
    }
}

