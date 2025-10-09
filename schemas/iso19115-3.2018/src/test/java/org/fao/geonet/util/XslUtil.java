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

public class XslUtil {
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
}
