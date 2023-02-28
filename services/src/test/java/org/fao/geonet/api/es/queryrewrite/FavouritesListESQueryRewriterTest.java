/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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
package org.fao.geonet.api.es.queryrewrite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.fao.geonet.api.selections.FavouriteMetadataListApi;
import org.fao.geonet.api.selections.FavouriteMetadataListApiSupport;
import org.fao.geonet.api.selections.FavouriteMetadataListVM;
import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.domain.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.Cookie;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class FavouritesListESQueryRewriterTest extends FavouriteMetadataListApiSupport {

    @Autowired
    FavouritesListESQueryRewriter favouritesListESQueryRewriter;

    public String nonFavouritesRequest="{\"from\":0,\"size\":30,\"sort\":[\"_score\"],\"query\":{\"function_score\":{\"boost\":\"5\",\"functions\":[{\"filter\":{\"match\":{\"resourceType\":\"series\"}},\"weight\":1.5},{\"filter\":{\"exists\":{\"field\":\"parentUuid\"}},\"weight\":0.3},{\"filter\":{\"match\":{\"cl_status.key\":\"obsolete\"}},\"weight\":0.2},{\"filter\":{\"match\":{\"cl_status.key\":\"superseded\"}},\"weight\":0.3},{\"gauss\":{\"dateStamp\":{\"scale\":\"365d\",\"offset\":\"90d\",\"decay\":0.5}}}],\"score_mode\":\"multiply\",\"query\":{\"bool\":{\"must\":[{\"terms\":{\"isTemplate\":[\"n\"]}}]}}}},\"aggregations\":{\"resourceType\":{\"terms\":{\"field\":\"resourceType\"},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw gn-icon-\"},\"field\":\"resourceType\"}},\"cl_spatialRepresentationType.key\":{\"terms\":{\"field\":\"cl_spatialRepresentationType.key\",\"size\":10},\"meta\":{\"field\":\"cl_spatialRepresentationType.key\"}},\"format\":{\"terms\":{\"field\":\"format\"},\"meta\":{\"collapsed\":true,\"field\":\"format\"}},\"availableInServices\":{\"filters\":{\"filters\":{\"availableInViewService\":{\"query_string\":{\"query\":\"+linkProtocol:/OGC:WMS.*/\"}},\"availableInDownloadService\":{\"query_string\":{\"query\":\"+linkProtocol:/OGC:WFS.*/\"}}}},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw \",\"map\":{\"availableInViewService\":\"fa-globe\",\"availableInDownloadService\":\"fa-download\"}}}},\"th_gemet_tree.default\":{\"terms\":{\"field\":\"th_gemet_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"},\"include\":\"[^^]+^?[^^]+\"},\"meta\":{\"field\":\"th_gemet_tree.default\"}},\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\":{\"terms\":{\"field\":\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"}},\"meta\":{\"field\":\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\"}},\"th_httpinspireeceuropaeutheme-theme_tree.key\":{\"terms\":{\"field\":\"th_httpinspireeceuropaeutheme-theme_tree.key\",\"size\":34},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw gn-icon iti-\",\"expression\":\"http://inspire.ec.europa.eu/theme/(.*)\"},\"field\":\"th_httpinspireeceuropaeutheme-theme_tree.key\"}},\"tag\":{\"terms\":{\"field\":\"tag.default\",\"include\":\".*\",\"size\":10},\"meta\":{\"caseInsensitiveInclude\":true,\"field\":\"tag.default\"}},\"th_regions_tree.default\":{\"terms\":{\"field\":\"th_regions_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"}},\"meta\":{\"field\":\"th_regions_tree.default\"}},\"resolutionScaleDenominator\":{\"histogram\":{\"field\":\"resolutionScaleDenominator\",\"interval\":10000,\"keyed\":true,\"min_doc_count\":1},\"meta\":{\"collapsed\":true}},\"creationYearForResource\":{\"histogram\":{\"field\":\"creationYearForResource\",\"interval\":5,\"keyed\":true,\"min_doc_count\":1},\"meta\":{\"collapsed\":true}},\"OrgForResource\":{\"terms\":{\"field\":\"OrgForResourceObject.default\",\"include\":\".*\",\"size\":20},\"meta\":{\"caseInsensitiveInclude\":true,\"field\":\"OrgForResourceObject.default\"}},\"cl_maintenanceAndUpdateFrequency.key\":{\"terms\":{\"field\":\"cl_maintenanceAndUpdateFrequency.key\",\"size\":10},\"meta\":{\"collapsed\":true,\"field\":\"cl_maintenanceAndUpdateFrequency.key\"}}},\"_source\":{\"includes\":[\"uuid\",\"id\",\"creat*\",\"group*\",\"logo\",\"category\",\"cl_topic*\",\"inspire*\",\"resource*\",\"draft*\",\"overview.*\",\"owner*\",\"link*\",\"image*\",\"status*\",\"rating\",\"tag*\",\"geom\",\"contact*\",\"*Org*\",\"hasBoundingPolygon\",\"isTemplate\",\"valid\",\"isHarvested\",\"dateStamp\",\"documentStandard\",\"standardNameObject.default\",\"cl_status*\",\"mdStatus*\",\"recordLink\"]},\"track_total_hits\":true}";
    public String favouritesRequest="{\"from\":0,\"size\":30,\"sort\":[\"_score\"],\"query\":{\"function_score\":{\"boost\":\"5\",\"functions\":[{\"filter\":{\"match\":{\"resourceType\":\"series\"}},\"weight\":1.5},{\"filter\":{\"exists\":{\"field\":\"parentUuid\"}},\"weight\":0.3},{\"filter\":{\"match\":{\"cl_status.key\":\"obsolete\"}},\"weight\":0.2},{\"filter\":{\"match\":{\"cl_status.key\":\"superseded\"}},\"weight\":0.3},{\"gauss\":{\"dateStamp\":{\"scale\":\"365d\",\"offset\":\"90d\",\"decay\":0.5}}}],\"score_mode\":\"multiply\",\"query\":{\"bool\":{\"must\":[{\"terms\":{\"isTemplate\":[\"n\"]}},{\"terms\":{\"favouritesList\":[FAVOURITESLISTID]}}]}}}},\"aggregations\":{\"resourceType\":{\"terms\":{\"field\":\"resourceType\"},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw gn-icon-\"},\"field\":\"resourceType\"}},\"cl_spatialRepresentationType.key\":{\"terms\":{\"field\":\"cl_spatialRepresentationType.key\",\"size\":10},\"meta\":{\"field\":\"cl_spatialRepresentationType.key\"}},\"format\":{\"terms\":{\"field\":\"format\"},\"meta\":{\"collapsed\":true,\"field\":\"format\"}},\"availableInServices\":{\"filters\":{\"filters\":{\"availableInViewService\":{\"query_string\":{\"query\":\"+linkProtocol:/OGC:WMS.*/\"}},\"availableInDownloadService\":{\"query_string\":{\"query\":\"+linkProtocol:/OGC:WFS.*/\"}}}},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw \",\"map\":{\"availableInViewService\":\"fa-globe\",\"availableInDownloadService\":\"fa-download\"}}}},\"th_gemet_tree.default\":{\"terms\":{\"field\":\"th_gemet_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"},\"include\":\"[^^]+^?[^^]+\"},\"meta\":{\"field\":\"th_gemet_tree.default\"}},\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\":{\"terms\":{\"field\":\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"}},\"meta\":{\"field\":\"th_httpinspireeceuropaeumetadatacodelistPriorityDataset-PriorityDataset_tree.default\"}},\"th_httpinspireeceuropaeutheme-theme_tree.key\":{\"terms\":{\"field\":\"th_httpinspireeceuropaeutheme-theme_tree.key\",\"size\":34},\"meta\":{\"decorator\":{\"type\":\"icon\",\"prefix\":\"fa fa-fw gn-icon iti-\",\"expression\":\"http://inspire.ec.europa.eu/theme/(.*)\"},\"field\":\"th_httpinspireeceuropaeutheme-theme_tree.key\"}},\"tag\":{\"terms\":{\"field\":\"tag.default\",\"include\":\".*\",\"size\":10},\"meta\":{\"caseInsensitiveInclude\":true,\"field\":\"tag.default\"}},\"th_regions_tree.default\":{\"terms\":{\"field\":\"th_regions_tree.default\",\"size\":100,\"order\":{\"_key\":\"asc\"}},\"meta\":{\"field\":\"th_regions_tree.default\"}},\"resolutionScaleDenominator\":{\"histogram\":{\"field\":\"resolutionScaleDenominator\",\"interval\":10000,\"keyed\":true,\"min_doc_count\":1},\"meta\":{\"collapsed\":true}},\"creationYearForResource\":{\"histogram\":{\"field\":\"creationYearForResource\",\"interval\":5,\"keyed\":true,\"min_doc_count\":1},\"meta\":{\"collapsed\":true}},\"OrgForResource\":{\"terms\":{\"field\":\"OrgForResourceObject.default\",\"include\":\".*\",\"size\":20},\"meta\":{\"caseInsensitiveInclude\":true,\"field\":\"OrgForResourceObject.default\"}},\"cl_maintenanceAndUpdateFrequency.key\":{\"terms\":{\"field\":\"cl_maintenanceAndUpdateFrequency.key\",\"size\":10},\"meta\":{\"collapsed\":true,\"field\":\"cl_maintenanceAndUpdateFrequency.key\"}}},\"_source\":{\"includes\":[\"uuid\",\"id\",\"creat*\",\"group*\",\"logo\",\"category\",\"cl_topic*\",\"inspire*\",\"resource*\",\"draft*\",\"overview.*\",\"owner*\",\"link*\",\"image*\",\"status*\",\"rating\",\"tag*\",\"geom\",\"contact*\",\"*Org*\",\"hasBoundingPolygon\",\"isTemplate\",\"valid\",\"isHarvested\",\"dateStamp\",\"documentStandard\",\"standardNameObject.default\",\"cl_status*\",\"mdStatus*\",\"recordLink\"]},\"track_total_hits\":true}";

    @Test
    public void testReplacement() throws Exception {
        MockHttpSession session = loginAsAnonymous();
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        Pair<FavouriteMetadataListVM,String>  createResult= create(session, null,
            "testcase1", FavouriteMetadataList.ListType.WatchList, new String[]{uuid1, uuid2});

        FavouriteMetadataListVM list=createResult.one();
        String returnedSessionId = createResult.two();

        List<String> metadataUuids = list.getSelections();
        Collections.sort(metadataUuids);

        mockHttpServletRequest.setCookies(new Cookie(FavouriteMetadataListApi.SESSION_COOKIE_NAME,returnedSessionId));

        //not a favourites list request -> no change
        String rewrittenQuery = favouritesListESQueryRewriter.rewriteQuery(session,mockHttpServletRequest,nonFavouritesRequest);
        assertEquals(nonFavouritesRequest,rewrittenQuery);

        //bad favourites list request -> no change (error --> no change)
        String query = favouritesRequest.replace("FAVOURITESLISTID","-666");
        rewrittenQuery = favouritesListESQueryRewriter.rewriteQuery(session,mockHttpServletRequest,query);
        assertEquals(query,rewrittenQuery);

        //rewrite
        query = favouritesRequest.replace("FAVOURITESLISTID",Integer.toString(list.getId()));
        rewrittenQuery = favouritesListESQueryRewriter.rewriteQuery(session,mockHttpServletRequest,query);
        assertNotEquals(query,rewrittenQuery);
        assertFalse(rewrittenQuery.contains("favouritesList"));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(rewrittenQuery);
        ArrayNode queryItems = (ArrayNode) root.get("query").get("function_score").get("query").get("bool").get("must");

        assertEquals("_id:("+metadataUuids.get(0)+") OR _id:("+metadataUuids.get(1)+")",
            queryItems.get(1).get("query_string").get("query").textValue());
    }
}
