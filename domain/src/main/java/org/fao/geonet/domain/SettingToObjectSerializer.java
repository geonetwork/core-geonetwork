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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang.StringUtils;
import org.hibernate.jpamodelgen.util.StringUtil;

import java.io.IOException;

/**
 * Convert settings to a key : value object with proper JSON data type
 */
public class SettingToObjectSerializer extends JsonSerializer<Setting> {

    @Override
    public void serialize(Setting s, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider)
        throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", s.getName());
        jsonGenerator.writeStringField("dataType", s.getDataType() == null ? null : s.getDataType().name());
        jsonGenerator.writeNumberField("position", s.getPosition());
        jsonGenerator.writeFieldName("value");
        writeSettingValue(s, jsonGenerator);
        jsonGenerator.writeEndObject();
    }

    public static void writeSettingValue(Setting s, JsonGenerator jsonGenerator) throws IOException {
        try {
            if (StringUtils.isNotEmpty(s.getValue())) {
                if (s.getDataType() == SettingDataType.BOOLEAN) {
                    jsonGenerator.writeBoolean(Boolean.parseBoolean(s.getValue()));
                } else if (s.getDataType() == SettingDataType.INT) {
                    jsonGenerator.writeNumber(Integer.parseInt(s.getValue()));
                } else if (s.getDataType() == SettingDataType.JSON) {
                    ObjectMapper mapper = new ObjectMapper();
                    jsonGenerator.writeTree(mapper.readTree(s.getValue()));
                } else {
                    jsonGenerator.writeString(s.getValue());
                }
            } else {
                jsonGenerator.writeNull();
            }
        } catch (Exception e) {
            jsonGenerator.writeNull();
            jsonGenerator.writeStringField("erroneousValue", s.getValue());
            jsonGenerator.writeStringField("error", e.getMessage());
        }
    }
}
