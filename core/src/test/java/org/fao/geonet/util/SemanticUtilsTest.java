/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SemanticUtilsTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void extractEmbeddingFromTopLevelResponse() throws Exception {
        JsonNode response = objectMapper.readTree("{\"embedding\":[1.0,2.0,3.0]}");

        JsonNode embedding = SemanticUtils.extractEmbedding(response);

        assertEquals("[1.0,2.0,3.0]", objectMapper.writeValueAsString(embedding));
    }

    @Test
    public void extractEmbeddingFromOpenAiDataResponse() throws Exception {
        JsonNode response = objectMapper.readTree(
            "{\"object\":\"list\",\"data\":[{\"object\":\"embedding\",\"embedding\":[4.0,5.0,6.0],\"index\":0}]}"
        );

        JsonNode embedding = SemanticUtils.extractEmbedding(response);

        assertEquals("[4.0,5.0,6.0]", objectMapper.writeValueAsString(embedding));
    }

    @Test
    public void extractEmbeddingReturnsNullWhenMissing() throws Exception {
        JsonNode response = objectMapper.readTree("{\"data\":[{\"index\":0}]}");

        JsonNode embedding = SemanticUtils.extractEmbedding(response);

        assertNull(embedding);
    }
}
