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

package org.fao.geonet.api.records.model.related;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Used to encode localized properties in key value layout in JSON eg. in XML <code> &lt;title>
 * &lt;values lang="eng"> Localities in Victoria (VMADMIN.LOCALITY_POLYGON) - Comprehensive Elements
 * &lt;/values> &lt;/title> </code> is in JSON <code> "title": { "eng": "Localities in Victoria
 * (VMADMIN.LOCALITY_POLYGON) - Comprehensive Elements" } </code>
 */
public class LocalizedStringSerializer extends JsonSerializer<ILocalizedStringProperty> {
    @Override
    public void serialize(
        ILocalizedStringProperty localizedStringProperty,
        JsonGenerator jgen,
        SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (LocalizedString l : localizedStringProperty.getValue()) {
            jgen.writeStringField(l.getLang(), l.getValue());
        }
        jgen.writeEndObject();
    }
}
