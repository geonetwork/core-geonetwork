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

package org.fao.geonet.guiapi.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchApiTest {

    @Test
    public void resultFieldsNeverExposesPrivilegeOrFilteredFields() {
        for (String field : SearchApi.RESULT_FIELDS) {
            Assert.assertFalse("must not request a privilege field: " + field,
                field.startsWith(Geonet.IndexFieldNames.OP_PREFIX));
            Assert.assertNotEquals("must not request online resource links", "link", field);
        }
    }

    @Test
    public void searchCriteriaKeepsRealSearchParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("resourceType", "dataset");
        params.put("fast", "index");
        params.put("from", "11");
        params.put("hitsPerPage", "20");
        params.put("_content_type", "xml");
        Assert.assertEquals(Map.of("resourceType", "dataset"), SearchApi.searchCriteria(params));
    }

    @Test
    public void negotiateResponseFormatPrefersContentTypeParamOverAccept() {
        Assert.assertEquals(SearchApi.ResponseFormat.XML,
            SearchApi.negotiateResponseFormat("xml", MediaType.TEXT_HTML_VALUE));
        Assert.assertEquals(SearchApi.ResponseFormat.JSON,
            SearchApi.negotiateResponseFormat("json", MediaType.TEXT_HTML_VALUE));
        Assert.assertEquals(SearchApi.ResponseFormat.HTML,
            SearchApi.negotiateResponseFormat("html", MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void negotiateResponseFormatFallsBackToAcceptHeader() {
        Assert.assertEquals(SearchApi.ResponseFormat.XML,
            SearchApi.negotiateResponseFormat(null, MediaType.APPLICATION_XML_VALUE));
        Assert.assertEquals(SearchApi.ResponseFormat.JSON,
            SearchApi.negotiateResponseFormat(null, MediaType.APPLICATION_JSON_VALUE));
        Assert.assertEquals(SearchApi.ResponseFormat.HTML,
            SearchApi.negotiateResponseFormat(null, MediaType.TEXT_HTML_VALUE));
    }

    @Test
    public void searchCriteriaDropsAnythingNotOnTheAllowlist() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("op0", "1");
        params.put("op23", "5");
        params.put("lang", "fre");
        params.put("x\") OR (*:*", "y");
        params.put("resourceType", "dataset");
        Assert.assertEquals(Map.of("resourceType", "dataset"), SearchApi.searchCriteria(params));
    }

    @Test
    public void searchCriteriaIgnoresNonSearchAndBlankParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("fast", "index");
        params.put("resultType", "details");
        params.put("topicCat", "");
        Assert.assertTrue(SearchApi.searchCriteria(params).isEmpty());
    }

    @Test
    public void clampDisplayFromDefaultsToOne() {
        Assert.assertEquals(1, SearchApi.clampDisplayFrom(null));
        Assert.assertEquals(1, SearchApi.clampDisplayFrom("not-a-number"));
        Assert.assertEquals(1, SearchApi.clampDisplayFrom("0"));
        Assert.assertEquals(1, SearchApi.clampDisplayFrom("-5"));
    }

    @Test
    public void clampDisplayFromKeepsAnOrdinaryValue() {
        Assert.assertEquals(11, SearchApi.clampDisplayFrom("11"));
    }

    @Test
    public void clampDisplayFromNeverLetsFromPlusHitsPerPageExceedMaxResultWindow() {
        long clamped = SearchApi.clampDisplayFrom("1000000");
        Assert.assertEquals(14_991, clamped);
    }

    @Test
    public void buildMustClausesWithNoCriteriaMatchesEverything() {
        ArrayNode must = SearchApi.buildMustClauses(new LinkedHashMap<>(), new ObjectMapper());
        Assert.assertEquals(1, must.size());
        Assert.assertTrue(must.get(0).has("match_all"));
    }

    @Test
    public void buildMustClausesAndsSearchParamsAsMatchPhraseClauses() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("resourceType", "dataset");
        criteria.put(Geonet.IndexFieldNames.CAT, "maps");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals(2, must.size());
        Assert.assertEquals("dataset", must.get(0).path("match_phrase").path("resourceType").asText());
        Assert.assertEquals("maps", must.get(1).path("match_phrase").path("cat").asText());
    }

    @Test
    public void buildMustClausesRewritesTypeToTheRealResourceTypeField() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("type", "map");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals("map", must.get(0).path("match_phrase").path("resourceType").asText());
    }

    @Test
    public void buildMustClausesRewritesGroupPublishedToTheFieldWithoutUnderscore() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("_groupPublished", "sample");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals("sample", must.get(0).path("match_phrase").path("groupPublished").asText());
    }

    @Test
    public void buildMustClausesSearchesAnyAcrossAnyAndTitleFieldsRegardlessOfLanguage() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("any", "coastal basins");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        JsonNode multiMatch = must.get(0).path("multi_match");
        Assert.assertEquals("coastal basins", multiMatch.path("query").asText());
        Assert.assertEquals("and", multiMatch.path("operator").asText());
        List<String> fields = new ArrayList<>();
        multiMatch.path("fields").forEach(f -> fields.add(f.asText()));
        Assert.assertEquals(List.of("any.*", "resourceTitleObject.*^2"), fields);
    }

    @Test
    public void buildMustClausesRewritesTopicCatToTheRealCodelistField() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("topicCat", "farming");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals("farming", must.get(0).path("match_phrase").path("cl_topic.key").asText());
    }

    @Test
    public void buildMustClausesRewritesSourceToTheRealCatalogueField() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put(Geonet.IndexFieldNames.SOURCE, "abc-123");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals("abc-123", must.get(0).path("match_phrase").path("sourceCatalogue").asText());
    }

    @Test
    public void buildMustClausesNeedsNoValueEscaping() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("title", "a\"b\\c");
        ArrayNode must = SearchApi.buildMustClauses(criteria, new ObjectMapper());
        Assert.assertEquals("a\"b\\c", must.get(0).path("match_phrase").path("title").asText());
    }

    @Test
    public void toResultElementFlattensEsSourceFields() {
        ObjectNode source = new ObjectMapper().createObjectNode();
        source.put(Geonet.IndexFieldNames.UUID, "abc-123");
        source.putObject("resourceTitleObject").put("default", "A title");
        source.putObject("resourceAbstractObject").put("default", "An abstract");
        source.putArray("resourceType").add("dataset").add("series");
        source.putArray(Geonet.IndexFieldNames.CAT).add("_administration").add("environment");
        source.putArray("overview").addObject().put("url", "http://example.org/thumb.png");

        Element metadata = SearchApi.toResultElement(source);

        Assert.assertEquals("abc-123",
            metadata.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid"));
        Assert.assertEquals("A title", metadata.getChildText("title"));
        Assert.assertNull(metadata.getChild("defaultTitle"));
        Assert.assertEquals("An abstract", metadata.getChildText("abstract"));
        Assert.assertEquals("dataset", metadata.getChildText("type"));
        Assert.assertEquals(2, metadata.getChildren("category").size());
        Element image = metadata.getChild("image");
        Assert.assertEquals("A title", image.getAttributeValue("alt"));
        Assert.assertEquals("http://example.org/thumb.png", image.getAttributeValue("url"));
    }

    @Test
    public void toResultElementKeepsAPipeInTheTitleIntact() {
        ObjectNode source = new ObjectMapper().createObjectNode();
        source.put(Geonet.IndexFieldNames.UUID, "abc-123");
        source.putObject("resourceTitleObject").put("default", "Rainfall | Temperature dataset");
        source.putArray("overview").addObject().put("url", "http://example.org/thumb.png");

        Element image = SearchApi.toResultElement(source).getChild("image");

        Assert.assertEquals("Rainfall | Temperature dataset", image.getAttributeValue("alt"));
        Assert.assertEquals("http://example.org/thumb.png", image.getAttributeValue("url"));
    }

    @Test
    public void toResultElementSkipsMissingResourceTypeAndOverview() {
        ObjectNode source = new ObjectMapper().createObjectNode();
        source.put(Geonet.IndexFieldNames.UUID, "abc-123");

        Element metadata = SearchApi.toResultElement(source);

        Assert.assertNull(metadata.getChild("type"));
        Assert.assertNull(metadata.getChild("image"));
        Assert.assertTrue(metadata.getChildren("category").isEmpty());
        Assert.assertEquals("abc-123", metadata.getChildText("defaultTitle"));
    }
}
