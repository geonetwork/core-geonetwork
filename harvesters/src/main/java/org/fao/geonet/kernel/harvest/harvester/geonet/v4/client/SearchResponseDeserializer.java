//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet.v4.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Deserializer for the SearchResponse class, which is used to parse JSON responses.
 */
public class SearchResponseDeserializer extends StdDeserializer<SearchResponse> {
    public SearchResponseDeserializer() {
        this(null);
    }

    public SearchResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SearchResponse deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        long total = node.get("hits").get("total").get("value").asLong();
        Set<SearchResponseHit> searchResponseHits = new HashSet<>();
        node.get("hits").get("hits").forEach(hitNode -> {
            String uuid = hitNode.get("_id").asText();
            String schema = hitNode.get("_source").get("documentStandard").asText();
            String changeDate = hitNode.get("_source").get("dateStamp").asText();
            String source = hitNode.get("_source").get("sourceCatalogue").asText();
            SearchResponseHit searchResponseHit = new SearchResponseHit(uuid, schema, changeDate, source);
            searchResponseHits.add(searchResponseHit);
        });

        return new SearchResponse(total, searchResponseHits);
    }
}
