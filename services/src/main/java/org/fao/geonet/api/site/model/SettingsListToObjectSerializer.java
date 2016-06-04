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

package org.fao.geonet.api.site.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;

import java.io.IOException;

/**
 * Convert settings to a key : value object with proper JSON data type
 */
public class SettingsListToObjectSerializer extends JsonSerializer<SettingsListResponse> {

    @Override
    public void serialize(SettingsListResponse settings, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        for (Setting s : settings.getSettings()) {
            if (s.getDataType() == SettingDataType.BOOLEAN) {
                jsonGenerator.writeBooleanField(
                    s.getName(),
                    s.getValue() == null ? null : Boolean.parseBoolean(s.getValue())
                );
            } else if (s.getDataType() == SettingDataType.INT) {
                jsonGenerator.writeNumberField(
                    s.getName(),
                    s.getValue() == null ? null : Integer.parseInt(s.getValue())
                );
            } else if (s.getDataType() == SettingDataType.JSON) {
                ObjectMapper mapper = new ObjectMapper();
                jsonGenerator.writeFieldName(s.getName());
                jsonGenerator.writeTree(
                    s.getValue() == null ? null : mapper.readTree(s.getValue())
                );
            } else {
                jsonGenerator.writeStringField(
                    s.getName(),
                    s.getValue());
            }
        }
        jsonGenerator.writeEndObject();
    }
}
