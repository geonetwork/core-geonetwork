//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SearchResponseDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule(
            "SearchResponseDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(SearchResponse.class, new SearchResponseDeserializer());
        mapper.registerModule(module);
    }

    @Test
    public void deserialize_fullRecord() throws IOException {
        String uuid = UUID.randomUUID().toString();
        String catalogueUuid = UUID.randomUUID().toString();
        String json = "{\n" +
            "  \"hits\": {\n" +
            "    \"total\": {\"value\": 1},\n" +
            "    \"hits\": [{\n" +
            "      \"_id\": \"" + uuid + "\",\n" +
            "      \"_source\": {\n" +
            "        \"documentStandard\": \"iso19139\",\n" +
            "        \"dateStamp\": \"2021-05-12T09:53:28.000Z\",\n" +
            "        \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "      }\n" +
            "    }]\n" +
            "  }\n" +
            "}";

        SearchResponse response = mapper.readValue(json, SearchResponse.class);

        assertEquals(1, response.getTotal());
        assertEquals(1, response.getHits().size());

        SearchResponseHit hit = response.getHits().iterator().next();
        assertEquals(uuid, hit.getUuid());
        assertEquals("iso19139", hit.getSchema());
        assertEquals("2021-05-12T09:53:28.000Z", hit.getChangeDate());
        assertEquals(catalogueUuid, hit.getSource());
    }

    @Test
    public void deserialize_missingDateStamp_changeDateIsNull() throws IOException {
        String uuid = UUID.randomUUID().toString();
        String catalogueUuid = UUID.randomUUID().toString();
        String json = "{\n" +
            "  \"hits\": {\n" +
            "    \"total\": {\"value\": 1},\n" +
            "    \"hits\": [{\n" +
            "      \"_id\": \"" + uuid + "\",\n" +
            "      \"_source\": {\n" +
            "        \"documentStandard\": \"iso19139\",\n" +
            "        \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "      }\n" +
            "    }]\n" +
            "  }\n" +
            "}";

        SearchResponse response = mapper.readValue(json, SearchResponse.class);

        assertEquals(1, response.getTotal());
        SearchResponseHit hit = response.getHits().iterator().next();
        assertEquals(uuid, hit.getUuid());
        assertEquals("iso19139", hit.getSchema());
        assertNull(hit.getChangeDate());
        assertEquals(catalogueUuid, hit.getSource());
    }

    @Test
    public void deserialize_mixedRecords_someWithoutDateStamp() throws IOException {
        String uuidWithDate = UUID.randomUUID().toString();
        String uuidWithoutDate = UUID.randomUUID().toString();
        String catalogueUuid = UUID.randomUUID().toString();
        String json = "{\n" +
            "  \"hits\": {\n" +
            "    \"total\": {\"value\": 2},\n" +
            "    \"hits\": [\n" +
            "      {\n" +
            "        \"_id\": \"" + uuidWithDate + "\",\n" +
            "        \"_source\": {\n" +
            "          \"documentStandard\": \"iso19139\",\n" +
            "          \"dateStamp\": \"2024-01-15T10:00:00.000Z\",\n" +
            "          \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"_id\": \"" + uuidWithoutDate + "\",\n" +
            "        \"_source\": {\n" +
            "          \"documentStandard\": \"iso19139\",\n" +
            "          \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        SearchResponse response = mapper.readValue(json, SearchResponse.class);

        assertEquals(2, response.getTotal());
        assertEquals(2, response.getHits().size());

        SearchResponseHit withDate = response.getHits().stream()
            .filter(h -> h.getUuid().equals(uuidWithDate)).findFirst().orElseThrow(AssertionError::new);
        SearchResponseHit withoutDate = response.getHits().stream()
            .filter(h -> h.getUuid().equals(uuidWithoutDate)).findFirst().orElseThrow(AssertionError::new);

        assertEquals("2024-01-15T10:00:00.000Z", withDate.getChangeDate());
        assertNull(withoutDate.getChangeDate());
    }

    @Test
    public void deserialize_malformedHit_isSkippedAndReported() throws IOException {
        String goodUuid = UUID.randomUUID().toString();
        String catalogueUuid = UUID.randomUUID().toString();
        // The second hit has no "_id" so it cannot be harvested: it must be skipped and reported,
        // not abort parsing of the whole page (which previously aborted the whole harvest).
        String json = "{\n" +
            "  \"hits\": {\n" +
            "    \"total\": {\"value\": 2},\n" +
            "    \"hits\": [\n" +
            "      {\n" +
            "        \"_id\": \"" + goodUuid + "\",\n" +
            "        \"_source\": {\n" +
            "          \"documentStandard\": \"iso19139\",\n" +
            "          \"dateStamp\": \"2024-01-15T10:00:00.000Z\",\n" +
            "          \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"_source\": {\n" +
            "          \"documentStandard\": \"iso19139\",\n" +
            "          \"sourceCatalogue\": \"" + catalogueUuid + "\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

        SearchResponse response = mapper.readValue(json, SearchResponse.class);

        // The total still reflects the remote count, but only the parseable hit is returned.
        assertEquals(2, response.getTotal());
        assertEquals(1, response.getHits().size());
        assertEquals(goodUuid, response.getHits().iterator().next().getUuid());

        // The skipped hit is reported so the harvester can surface it.
        assertEquals(1, response.getFailedHits().size());
        assertEquals(SearchResponseDeserializer.UNKNOWN_ID, response.getFailedHits().get(0));
    }

    @Test
    public void deserialize_emptyHits() throws IOException {
        String json = "{\n" +
            "  \"hits\": {\n" +
            "    \"total\": {\"value\": 0},\n" +
            "    \"hits\": []\n" +
            "  }\n" +
            "}";

        SearchResponse response = mapper.readValue(json, SearchResponse.class);

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getHits().size());
    }
}
