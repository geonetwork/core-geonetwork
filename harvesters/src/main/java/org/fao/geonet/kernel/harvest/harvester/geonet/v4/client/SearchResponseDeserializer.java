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
 * A custom deserializer for parsing JSON responses into instances of {@link SearchResponse}.
 * This deserializer is used to process search results, specifically responses from
 * systems such as Elasticsearch.
 *
 * <p>
 * The JSON structure expected by this deserializer includes:
 * <ul>
 *   <li>A {@code "hits"} object containing:
 *     <ul>
 *       <li>A {@code "total"} object with a {@code "value"} field that specifies the total number of matching records.</li>
 *       <li>A {@code "hits"} array, where each entry represents a search result item and contains:
 *         <ul>
 *           <li>An {@code "_id"} field representing the unique identifier of the item.</li>
 *           <li>A {@code "_source"} object with fields:
 *             <ul>
 *               <li>{@code "documentStandard"}</li>
 *               <li>{@code "dateStamp"}</li>
 *               <li>{@code "sourceCatalogue"}</li>
 *             </ul>
 *           </li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>
 *
 * <p>
 * The deserializer extracts this information to create a {@link SearchResponse} instance,
 * which includes the total hit count and a set of {@link SearchResponseHit} objects.
 */
public class SearchResponseDeserializer extends StdDeserializer<SearchResponse> {

    public static final String HITS = "hits";
    public static final String SOURCE = "_source";
    public static final String TOTAL = "total";
    public static final String VALUE = "value";
    public static final String DOCUMENT_STANDARD = "documentStandard";
    public static final String DATE_STAMP = "dateStamp";
    public static final String SOURCE_CATALOGUE = "sourceCatalogue";
    public static final String ID = "_id";

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

        long total = node.get(HITS).get(TOTAL).get(VALUE).asLong();

        Set<SearchResponseHit> searchResponseHits = new HashSet<>();
        node.get(HITS).get(HITS).forEach(hitNode -> {
            String uuid = hitNode.get(ID).asText();
            String schema = hitNode.get(SOURCE).get(DOCUMENT_STANDARD).asText();
            String changeDate = hitNode.get(SOURCE).get(DATE_STAMP).asText();
            String source = hitNode.get(SOURCE).get(SOURCE_CATALOGUE).asText();
            SearchResponseHit searchResponseHit = new SearchResponseHit(uuid, schema, changeDate, source);
            searchResponseHits.add(searchResponseHit);
        });

        return new SearchResponse(total, searchResponseHits);
    }
}
