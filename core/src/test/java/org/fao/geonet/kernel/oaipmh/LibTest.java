//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.oaipmh;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fao.geonet.schema.iso19139.ISO19139SchemaPlugin;
import org.jdom.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LibTest {

    @Test
    public void testCreateSearchQueryMetadataPrefix() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryMetadataPrefixAndSet() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));
        params.addContent(new Element("category").setText("maps"));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        }, {" +
                "          \"term\": {" +
                "            \"cat\": {" +
                "              \"value\": \"maps\"" +
                "            }" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryMetadataPrefixAndTemporalExtentFrom() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));
        params.addContent(new Element("extFrom").setText("2023-09-01"));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        },{\"range\": {\"resourceTemporalDateRange\": {" +
                "                \"gte\": \"2023-09-01\"," +
                "                \"relation\": \"intersects\"" +
                "            }}}" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryMetadataPrefixAndTemporalExtentTo() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));
        params.addContent(new Element("extTo").setText("2023-12-29"));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        },{\"range\": {\"resourceTemporalDateRange\": {" +
                "                \"lte\": \"2023-12-29\"," +
                "                \"relation\": \"intersects\"" +
                "            }}}" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryMetadataPrefixAndTemporalExtent() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));
        params.addContent(new Element("extFrom").setText("2023-09-01"));
        params.addContent(new Element("extTo").setText("2023-12-29"));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        },{\"range\": {\"resourceTemporalDateRange\": {" +
                "                \"gte\": \"2023-09-01\"," +
                "                \"lte\": \"2023-12-29\"," +
                "                \"relation\": \"intersects\"" +
                "            }}}" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryAllParams() {
        Element params = new Element("params");
        params.addContent(new Element("_schema").setText(ISO19139SchemaPlugin.IDENTIFIER));
        params.addContent(new Element("category").setText("maps"));
        params.addContent(new Element("extFrom").setText("2023-09-01"));
        params.addContent(new Element("extTo").setText("2023-12-29"));

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        }, {" +
                "          \"term\": {" +
                "            \"cat\": {" +
                "              \"value\": \"maps\"" +
                "            }" +
                "          }" +
                "        },{\"range\": {\"resourceTemporalDateRange\": {" +
                "                \"gte\": \"2023-09-01\"," +
                "                \"lte\": \"2023-12-29\"," +
                "                \"relation\": \"intersects\"" +
                "            }}}" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }

    @Test
    public void testCreateSearchQueryEmptyParams() {
        Element params = new Element("params");

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String queryExpected = "{" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"terms\": {" +
                "            \"isTemplate\": [\"n\"]" +
                "          }" +
                "        }, " +
                "        {" +
                "          \"term\": {" +
                "            \"documentStandard\": {" +
                "              \"value\": \"iso19139\"" +
                "            }" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "}";

            JsonNode searchQueryExpected = objectMapper.readTree(queryExpected);
            JsonNode searchQuery = Lib.createSearchQuery(params);
            assertEquals(searchQueryExpected, searchQuery);
        } catch (Exception ex) {
            fail("Error creating OAIMPH search query");
        }
    }
}
