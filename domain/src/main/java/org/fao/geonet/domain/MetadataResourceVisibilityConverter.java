/*
 * =============================================================================
 * ===	Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.domain;

import javax.persistence.AttributeConverter;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;

/**
 * Created by francois on 31/12/15.
 */
public class MetadataResourceVisibilityConverter
    extends PropertyEditorSupport
    implements AttributeConverter<MetadataResourceVisibility, String> {
    @Override
    public void setAsText(final String visibility) throws IllegalArgumentException {
        MetadataResourceVisibility value = MetadataResourceVisibility.parse(visibility.trim());
        if (value != null) {
            setValue(value);
        } else {
            throw new IllegalArgumentException(
                String.format("Unsupported value '%s'. Values are %s.",
                    visibility,
                    Arrays.toString(MetadataResourceVisibility.values())));
        }
    }

    @Override
    public String convertToDatabaseColumn(MetadataResourceVisibility visibility) {
        switch (visibility) {
            case PRIVATE:
                return "P";
            case PUBLIC:
                return "A";
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported value '%s'. Values are %s.",
                        visibility,
                        Arrays.toString(MetadataResourceVisibility.values())));
        }
    }

    @Override
    public MetadataResourceVisibility convertToEntityAttribute(String s) {
        switch (s) {
            case "P":
                return MetadataResourceVisibility.PRIVATE;
            case "A":
                return MetadataResourceVisibility.PUBLIC;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported value '%s'. Values are %s.",
                        s,
                        Arrays.toString(MetadataResourceVisibility.values())));
        }
    }
}
