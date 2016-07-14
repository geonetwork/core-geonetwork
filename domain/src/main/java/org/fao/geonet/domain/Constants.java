/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.domain;

/**
 * Constants used internally in for defining the domain objects.
 *
 * @author Jesse
 */
public final class Constants {
    /**
     * The length to use for IP address columns.
     */
    public static final int IP_ADDRESS_COLUMN_LENGTH = 45;
    /**
     * The character used by the JPAWorkaround columns that need a character for boolean false.
     */
    public static final char YN_FALSE = 'n';
    /**
     * The character used by the JPAWorkaround columns that need a character for boolean true.
     */
    public static final char YN_TRUE = 'y';
    /**
     * The module name for logging domain information.
     */
    public static final String DOMAIN_LOG_MODULE = "geonetwork.domain";


    private Constants() {
    }

    /**
     * Convert a boolean to the corresponding character to use for the boolean characters (A
     * workaround for the API). Do a search for JPAWorkaround in domain package.
     *
     * @param enabled the value to convert
     * @return the corresponding char
     *
     * CSOFF: MethodName
     */
    public static char toYN_EnabledChar(final boolean enabled) {
        char enabledChar;
        if (enabled) {
            enabledChar = YN_TRUE;
        } else {
            enabledChar = YN_FALSE;
        }
        return enabledChar;
    }
    // CSON: MethodName

    /**
     * Convert a character from one of the JPAWorkaround columns to the corresponding boolean value.
     * Do a search for JPAWorkaround in domain package.
     *
     * @param enabled the value to convert
     * @return the corresponding boolean value
     */
    public static boolean toBoolean_fromYNChar(final char enabled) {
        return enabled == Constants.YN_TRUE;
    }
}
