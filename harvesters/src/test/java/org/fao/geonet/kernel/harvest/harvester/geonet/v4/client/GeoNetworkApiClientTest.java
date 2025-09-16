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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Source;
import org.fao.geonet.exceptions.BadParameterEx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeoNetworkApiClientTest {
    @Mock
    private GeoNetwork4ApiClient geoNetworkApiClient;

    @Test
    public void testRetrieveGroups() {
        List<Group> groupList = new ArrayList<>();
        Group group1 = new Group();
        group1.setId(1);
        group1.setName("group1");
        groupList.add(group1);

        Group group2 = new Group();
        group1.setId(2);
        group1.setName("group2");
        groupList.add(group2);

        try {
            when(geoNetworkApiClient.retrieveGroups("http://localhost:8080/geonetwork", "", "")).thenReturn(groupList);
            when(geoNetworkApiClient.retrieveGroups("invalidURL", "", "")).thenThrow(BadParameterEx.class);

            List<Group> groupListRetrieved = geoNetworkApiClient.retrieveGroups("http://localhost:8080/geonetwork", "", "");
            assertEquals(groupList, groupListRetrieved);

            assertThrows(BadParameterEx.class, () -> geoNetworkApiClient.retrieveGroups("invalidURL", "", ""));
        } catch (URISyntaxException | IOException ex) {
            fail("Error retrieving groups");
        }
    }


    @Test
    public void testRetrieveSources() {
        Map<String, Source> sourceMap = new HashMap<>();
        Source source1 = new Source();
        source1.setUuid(UUID.randomUUID().toString());
        source1.setName("source1");
        sourceMap.put(source1.getUuid(), source1);

        Source source2 = new Source();
        source2.setUuid(UUID.randomUUID().toString());
        source2.setName("source2");
        sourceMap.put(source2.getUuid(), source2);

        try {
            when(geoNetworkApiClient.retrieveSources("http://localhost:8080/geonetwork", "", "")).thenReturn(sourceMap);
            when(geoNetworkApiClient.retrieveSources("invalidURL", "", "")).thenThrow(BadParameterEx.class);

            Map<String, Source> sourceMapRetrieved = geoNetworkApiClient.retrieveSources("http://localhost:8080/geonetwork", "", "");
            assertEquals(sourceMap, sourceMapRetrieved);

            assertThrows(BadParameterEx.class, () -> geoNetworkApiClient.retrieveSources("invalidURL", "", ""));
        } catch (URISyntaxException | IOException ex) {
            fail("Error retrieving sources");
        }
    }

    @Test
    public void testRetrieveMEF() {
        try {
            Path mefFilePath = Files.createTempFile("temp-", ".dat");

            when(geoNetworkApiClient.retrieveMEF("http://localhost:8080/geonetwork", "aaaa", "", "")).thenReturn(mefFilePath);
            when(geoNetworkApiClient.retrieveMEF("invalidURL", "aaaa", "", "")).thenThrow(BadParameterEx.class);

            Path mefFilePathRetrieved = geoNetworkApiClient.retrieveMEF("http://localhost:8080/geonetwork", "aaaa", "", "");
            assertEquals(mefFilePath, mefFilePathRetrieved);

            assertThrows(BadParameterEx.class, () -> geoNetworkApiClient.retrieveMEF("invalidURL", "aaaa", "", ""));
        } catch (URISyntaxException | IOException ex) {
            fail("Error retrieving sources");
        }
    }

    @Test
    public void testQuery() {
        try {
            String query = String.format("{\n" +
                "    \"from\": %d,\n" +
                "    \"size\": %d,\n" +
                "    \"sort\": [\"_score\"],\n" +
                "    \"query\": {\"bool\": {\"must\": [{\"terms\": {\"isTemplate\": [\"n\"]}},{\"term\": {\"sourceCatalogue\": \"%s\"}}]}},\n" +
                "    \"_source\": {\"includes\": [\n" +
                "        \"uuid\",\n" +
                "        \"id\",\n" +
                "        \"isTemplate\",\n" +
                "        \"sourceCatalogue\",\n" +
                "        \"dateStamp\",\n" +
                "        \"documentStandard\"\n" +
                "    ]},\n" +
                "    \"track_total_hits\": true\n" +
                "}", 1, 30, "fee3d1ae-f32b-4435-865d-36af0a489e3c");

            String queryResult = "{\n" +
                "    \"took\": 41,\n" +
                "    \"timed_out\": false,\n" +
                "    \"_shards\": {\n" +
                "        \"total\": 1,\n" +
                "        \"successful\": 1,\n" +
                "        \"skipped\": 0,\n" +
                "        \"failed\": 0\n" +
                "    },\n" +
                "    \"hits\": {\n" +
                "        \"total\": {\n" +
                "            \"value\": 2,\n" +
                "            \"relation\": \"eq\"\n" +
                "        },\n" +
                "        \"max_score\": 1,\n" +
                "        \"hits\": [\n" +
                "            {\n" +
                "                \"_index\": \"gn-records\",\n" +
                "                \"_type\": \"_doc\",\n" +
                "                \"_id\": \"b5576133-8a6f-4b47-a973-e2b3c80c8a75\",\n" +
                "                \"_score\": 1,\n" +
                "                \"_source\": {\n" +
                "                    \"owner\": \"1\",\n" +
                "                    \"groupOwner\": \"11855\",\n" +
                "                    \"uuid\": \"b5576133-8a6f-4b47-a973-e2b3c80c8a75\",\n" +
                "                    \"documentStandard\": \"iso19139\",\n" +
                "                    \"sourceCatalogue\": \"fee3d1ae-f32b-4435-865d-36af0a489e3c\",\n" +
                "                    \"dateStamp\": \"2021-05-12T09:53:28.000Z\",\n" +
                "                    \"isTemplate\": \"n\",\n" +
                "                    \"id\": \"151659\"\n" +
                "                },\n" +
                "                \"edit\": false,\n" +
                "                \"canReview\": false,\n" +
                "                \"owner\": false,\n" +
                "                \"isPublishedToAll\": true,\n" +
                "                \"view\": true,\n" +
                "                \"notify\": false,\n" +
                "                \"download\": true,\n" +
                "                \"dynamic\": true,\n" +
                "                \"featured\": false,\n" +
                "                \"selected\": false\n" +
                "            },\n" +
                "            {\n" +
                "                \"_index\": \"gn-records\",\n" +
                "                \"_type\": \"_doc\",\n" +
                "                \"_id\": \"5b7008b9-84db-4ae3-9e9d-4de926b00ad9\",\n" +
                "                \"_score\": 1,\n" +
                "                \"_source\": {\n" +
                "                    \"owner\": \"1\",\n" +
                "                    \"groupOwner\": \"11857\",\n" +
                "                    \"uuid\": \"5b7008b9-84db-4ae3-9e9d-4de926b00ad9\",\n" +
                "                    \"documentStandard\": \"iso19139\",\n" +
                "                    \"sourceCatalogue\": \"fee3d1ae-f32b-4435-865d-36af0a489e3c\",\n" +
                "                    \"dateStamp\": \"2021-05-12T09:52:34.000Z\",\n" +
                "                    \"isTemplate\": \"n\",\n" +
                "                    \"id\": \"151601\"\n" +
                "                },\n" +
                "                \"edit\": false,\n" +
                "                \"canReview\": false,\n" +
                "                \"owner\": false,\n" +
                "                \"isPublishedToAll\": true,\n" +
                "                \"view\": true,\n" +
                "                \"notify\": false,\n" +
                "                \"download\": true,\n" +
                "                \"dynamic\": true,\n" +
                "                \"featured\": false,\n" +
                "                \"selected\": false\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module =
                new SimpleModule("CustomSearchResponseDeserializer", new Version(1, 0, 0, null, null, null));
            module.addDeserializer(SearchResponse.class, new SearchResponseDeserializer());
            mapper.registerModule(module);

            SearchResponse searchResponse = mapper.readValue(queryResult, SearchResponse.class);
            when(geoNetworkApiClient.query("http://localhost:8080/geonetwork", query, "", "")).thenReturn(searchResponse);
            when(geoNetworkApiClient.query("invalidURL", query, "", "")).thenThrow(BadParameterEx.class);

            SearchResponse searchResponseRetrieved = geoNetworkApiClient.query("http://localhost:8080/geonetwork", query, "", "");

            assertEquals(searchResponse, searchResponseRetrieved);

            assertThrows(BadParameterEx.class, () -> geoNetworkApiClient.query("invalidURL", query, "", ""));
        } catch (URISyntaxException | IOException ex) {
            fail("Error retrieving sources");
        }
    }
}
