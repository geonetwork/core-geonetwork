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
 * Created by francois on 25/05/16.
 */
public class ListOnlyClassSerializer extends JsonSerializer<IListOnlyClassToArray> {

    @Override
    public void serialize(IListOnlyClassToArray iListOnlyClassToArray, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartArray();
        for (Object o : iListOnlyClassToArray.getItem()) {
            if (o instanceof RelatedMetadataItem) {
                jsonGenerator.writeObject((RelatedMetadataItem) o);
            } else if (o instanceof RelatedSiblingMetadataItem) {
                jsonGenerator.writeObject((RelatedSiblingMetadataItem) o);
            } else if (o instanceof RelatedLinkItem) {
                jsonGenerator.writeObject((RelatedLinkItem) o);
            } else if (o instanceof RelatedThumbnailItem) {
                jsonGenerator.writeObject((RelatedThumbnailItem) o);
            }
        }
        jsonGenerator.writeEndArray();
    }
}
