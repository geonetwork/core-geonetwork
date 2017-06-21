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

import javax.annotation.Nonnull;

/**
 * The type of metadata.
 * <p/>
 * User: Jesse Date: 9/12/13 Time: 9:21 AM
 */
public enum MetadataType {
    /**
     * Indicates the associated {@link Metadata} entity is a normal metadata.
     */
    METADATA('n'),
    /**
     * Indicates the associated {@link Metadata} entity is a template metadata.
     */
    TEMPLATE('y'),
    /**
     * Indicates the associated {@link Metadata} entity is a sub-template metadata. <p></p> A
     * sub-template is a metadata fragment that can be inserted into another metadata.  It can also
     * be shared as an xlink in multiple metadata to reduce duplication.
     */
    SUB_TEMPLATE('s'),

    /**
     * Indicates the associated {@link Metadata} entity is a template of sub template.
     */
    TEMPLATE_OF_SUB_TEMPLATE('t');

    /**
     * The code (for backwards compatibility) of the metadatatype.
     */
    public final char code;
    /**
     * Same as {@link #code} expect as a string instead of a char.
     */
    public final String codeString;

    private MetadataType(final char code) {
        this.code = code;
        this.codeString = String.valueOf(code);
    }

    @Nonnull
    public static MetadataType lookup(final char code) {
        for (MetadataType type : values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Not a known MetadataType code: " + code);
    }

    /**
     * Look up the MetadataType from a string.
     *
     * @param code the 1 character long string representing the type.
     * @return the metadata type.
     */
    @Nonnull
    public static MetadataType lookup(@Nonnull final String code) {
        final String trimmedCode = code.trim();
        if (trimmedCode.length() == 1) {
            return lookup(trimmedCode.charAt(0));
        } else {
            for (MetadataType type : values()) {
                if (type.toString().equalsIgnoreCase(trimmedCode.toLowerCase())) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException(String.format(
            "'%s' is not a known metadata type code. Values are: %s", trimmedCode, values()));
    }
}
