/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.apache.commons.text.StringEscapeUtils;
import org.owasp.esapi.reference.DefaultEncoder;
import org.w3c.dom.Node;

import java.util.List;

public class XslUtil {
    public static Boolean IS_INSPIRE_ENABLED = false;

    public static String twoCharLangCode(String iso3code) {
        return iso3code.substring(0, 2);
    }
    public static String threeCharLangCode(String iso2code) {
        return "fre";
    }

    public static String getSettingValue(String key) {
        switch (key) {
            case "system/metadata/validation/removeSchemaLocation":
                return "false";
            case "system/inspire/enable":
                return IS_INSPIRE_ENABLED.toString();
            default:
                return "true";
        }
    }

    public static String twoCharLangCode(String iso3code, String defaultValue) {
        switch (iso3code) {
            case "fre":
                return "fr";
            case "ita":
                return "it";
            case "eng":
                return "en";
            case "ger":
                return "de";
            case "roh":
                return "rm";
            default:
                return defaultValue;
        }
    }

    public static String escapeForJson(String value) {
        return StringEscapeUtils.escapeJson(value);
    }

    public static String getIsoLanguageLabel(String code, String language) {
        return "dutch";
    }

    public static String getIndexField(Object appName, Object uuid, Object field, Object lang) {
        return "";
    }

    public static String getDefaultUrl(String uuid, String language) {
        return "www.geonet.org";
    }

    public static String getSiteUrl() {
        return "";
    }

    public static String getCodelistTranslation(Object codelist, Object value, Object langCode) {
        return String.format("%s--%s--%s", codelist, value, langCode);
    }


    public static List<String> getKeywordHierarchy(String keyword, String thesaurusId, String langCode) {
        return List.of();
    }

    public static String getBuildNumber() {
        return "buildNumber-666";
    }


    public static String encodeForJavaScript(String str) {
        return DefaultEncoder.getInstance().encodeForJavaScript(str);
    }

    public static boolean isDisableLoginForm() {
        return false;
    }

    public static boolean isShowLoginAsLink() {
        return false;
    }

    public static boolean isUserProfileUpdateEnabled() {
        return true;
    }

    public static boolean isUserGroupUpdateEnabled() {
        return true;
    }

    public static String getUiConfigurationJsonProperty(String key, String path) {
        return key + "-" + path;
    }

    public static String getWebAnalyticsService() {
        return "";
    }

    public static String getWebAnalyticsJavascriptCode() {
        return "";
    }

    public static String getUiConfiguration(String key) {
        return "{}";
    }

    public static String getSecurityProvider() {
        return "";
    }

    public static boolean isAuthenticated() {
        return false;
    }

    public static String gmlToGeoJson(String gml, Boolean applyPrecisionModel, Integer numberOfDecimals) {
        return "";
    }

    public static Node getUrlContent(String surl) {
        return null;
    }

    public static Node getRecord(String uuid) {
        return null;
    }

    public static String getKeywordUri(String keyword, String thesaurusId, String langCode) {
        return "";
    }

    public static String getThesaurusIdByTitle(String title) {
        return "";
    }
}
