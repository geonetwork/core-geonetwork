/*
 * Copyright (C) 2001-2018 Food and Agriculture Organization of the
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
package v350;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class UrlComponentsDetectorsRegexMigrationTest {

    private UrlComponentsDetectorsRegexMigration testInstance;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement stmt;
    @Mock
    private ResultSet rs;
    @Mock
    private PreparedStatement updateStmt;

    private ObjectMapper objectMapper;
    private JsonNode expectedJsonNode;

    @Before
    public void before() throws SQLException, IOException {
        String originalSetting = "{\"langDetector\":{\"fromHtmlTag\":false," +
            "\"regexp\":\"^/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/\",\"default\":\"eng\"}," +
            "\"nodeDetector\":{\"regexp\":\"^/[a-zA-Z0-9_-]+/([a-zA-Z0-9_-]+)/[a-z]{3}/\",\"default\":\"srv\"}," +
            "\"mods\":{\"header\":{\"enabled\":true,\"languages\":{\"eng\":\"en\",\"dut\":\"nl\",\"fre\":\"fr\"," +
            "\"ger\":\"ge\",\"kor\":\"ko\",\"spa\":\"es\",\"cze\":\"cz\",\"cat\":\"ca\",\"fin\":\"fi\",\"ice\":\"is\"," +
            "\"rus\":\"ru\",\"chi\":\"zh\"}},\"home\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/home\"},\"search\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/search\",\"hitsperpageValues\":[10,50,100]," +
            "\"paginationInfo\":{\"hitsPerPage\":20},\"defaultSearchString\":\"\",\"facetsSummaryType\":" +
            "\"details\",\"facetConfig\":[],\"facetTabField\":\"\",\"filters\":{},\"sortbyValues\":[{" +
            "\"sortBy\":\"relevance\",\"sortOrder\":\"\"},{\"sortBy\":\"changeDate\",\"sortOrder\":\"\"},{" +
            "\"sortBy\":\"title\",\"sortOrder\":\"reverse\"},{\"sortBy\":\"rating\",\"sortOrder\":\"\"},{" +
            "\"sortBy\":\"popularity\",\"sortOrder\":\"\"},{\"sortBy\":\"denominatorDesc\",\"sortOrder\":\"\"}," +
            "{\"sortBy\":\"denominatorAsc\",\"sortOrder\":\"reverse\"}],\"sortBy\":\"relevance\",\"resultViewTpls\":" +
            "[{\"tplUrl\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"tooltip\":\"Grid\",\"icon\":\"fa-th\"}]," +
            "\"resultTemplate\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"advancedSearchTemplate\":\"../../catalog/views/default/templates/advancedSearchForm/defaultAdvancedSearchForm.html\"," +
            "\"formatter\":{\"list\":[{\"label\":\"full\"," +
            "\"url\":\"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced\"}]},\"grid\":{\"related\":" +
            "[\"parent\",\"children\",\"services\",\"datasets\"]},\"linkTypes\":{\"links\":[\"LINK\",\"kml\"]," +
            "\"downloads\":[\"DOWNLOAD\"],\"layers\":[\"OGC\"],\"maps\":[\"ows\"]}," +
            "\"isFilterTagsDisplayedInSearch\":false},\"defaultSearchString\":\"\",\"map\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/map\",\"is3DModeAllowed\":true,\"isSaveMapInCatalogAllowed" +
            "\":true,\"isExportMapAsImageEnabled\":true,\"bingKey\":\"\",\"storage\":\"sessionStorage\"," +
            "\"map\":\"../../map/config-viewer.xml\",\"listOfServices\":{\"wms\":[],\"wmts\":[]},\"useOSM\":true," +
            "\"context\":\"\",\"layer\":{\"url\":\"http://data.fao.org/maps/wms?\",\"layers\":" +
            "\"COMMON:dark_bluemarble\",\"version\":\"1.1.1\"},\"projection\":\"EPSG:3857\",\"projectionList\":" +
            "[{\"code\":\"EPSG:4326\",\"label\":\"WGS84(EPSG:4326)\"},{\"code\":\"EPSG:3857\",\"label\":" +
            "\"Googlemercator(EPSG:3857)\"}],\"disabledTools\":{\"processes\":false,\"addLayers\":false,\"layers\":" +
            "false,\"filter\":false,\"contexts\":false,\"print\":false,\"mInteraction\":false,\"graticule\":false," +
            "\"syncAllLayers\":false,\"drawVector\":false},\"searchMapLayers\":[],\"viewerMapLayers\":[]}," +
            "\"editor\":{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.edit\",\"geocoder\": " +
            "{\"enabled\": true, \"appUrl\": \"https://secure.geonames.org/searchJSON\"},\"isUserRecordsOnly\":false," +
            "\"isFilterTagsDisplayed\":false," +
            "\"createPageTpl\": \"../../catalog/templates/editor/new-metadata-horizontal.html\"},\"admin\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/admin.console\"},\"signin\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.signin\"}," +
            "\"signout\":{\"appUrl\":\"../../signout\"}}}";

        String updatedSetting = "{\"langDetector\":{\"fromHtmlTag\":false,\"regexp\":\"^(?:/.+)?/.+/([a-z]{2,3})/.+\"," +
            "\"default\":\"eng\"},\"nodeDetector\":{\"regexp\":\"^(?:/.+)?/(.+)/[a-z]{2,3}/.+\",\"default\":\"srv\"}," +
            "\"serviceDetector\":{\"regexp\":\"^(?:/.+)?/.+/[a-z]{2,3}/(.+)\",\"default\":\"catalog.search\"}," +
            "\"baseURLDetector\":{\"regexp\":\"^((?:/.+)?)+/.+/[a-z]{2,3}/.+\",\"default\":\"/geonetwork\"}," +
            "\"mods\":{\"header\":{\"enabled\":true,\"languages\":{\"eng\":\"en\",\"dut\":\"nl\",\"fre\":\"fr\"," +
            "\"ger\":\"ge\",\"kor\":\"ko\",\"spa\":\"es\",\"cze\":\"cz\",\"cat\":\"ca\",\"fin\":\"fi\"," +
            "\"ice\":\"is\",\"rus\":\"ru\",\"chi\":\"zh\"}},\"home\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/home\"},\"search\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/search\",\"hitsperpageValues\":[10,50,100]," +
            "\"paginationInfo\":{\"hitsPerPage\":20},\"defaultSearchString\":\"\",\"facetsSummaryType\":\"details\"," +
            "\"facetConfig\":[],\"facetTabField\":\"\",\"filters\":{},\"sortbyValues\":[{\"sortBy\":\"relevance\"," +
            "\"sortOrder\":\"\"},{\"sortBy\":\"changeDate\",\"sortOrder\":\"\"},{\"sortBy\":\"title\",\"sortOrder\":" +
            "\"reverse\"},{\"sortBy\":\"rating\",\"sortOrder\":\"\"},{\"sortBy\":\"popularity\",\"sortOrder\":\"\"}," +
            "{\"sortBy\":\"denominatorDesc\",\"sortOrder\":\"\"},{\"sortBy\":\"denominatorAsc\"," +
            "\"sortOrder\":\"reverse\"}],\"sortBy\":\"relevance\"," +
            "\"resultViewTpls\":[{\"tplUrl\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"tooltip\":\"Grid\",\"icon\":\"fa-th\"}]," +
            "\"resultTemplate\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"advancedSearchTemplate\":\"../../catalog/views/default/templates/advancedSearchForm/defaultAdvancedSearchForm.html\"," +
            "\"formatter\":{\"list\":[{\"label\":\"full\"," +
            "\"url\":\"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced\"}]},\"grid\":{\"related\":" +
            "[\"parent\",\"children\",\"services\",\"datasets\"]},\"linkTypes\":{\"links\":[\"LINK\",\"kml\"]," +
            "\"downloads\":[\"DOWNLOAD\"],\"layers\":[\"OGC\"],\"maps\":[\"ows\"]}," +
            "\"isFilterTagsDisplayedInSearch\":false},\"defaultSearchString\":\"\",\"map\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/map\",\"is3DModeAllowed\":true," +
            "\"isSaveMapInCatalogAllowed\":true,\"isExportMapAsImageEnabled\":true,\"bingKey\":\"\",\"storage\":" +
            "\"sessionStorage\",\"map\":\"../../map/config-viewer.xml\",\"listOfServices\":{\"wms\":[],\"wmts\":[]}," +
            "\"useOSM\":true,\"context\":\"\",\"layer\":{\"url\":\"http://data.fao.org/maps/wms?\",\"layers\":" +
            "\"COMMON:dark_bluemarble\",\"version\":\"1.1.1\"},\"projection\":\"EPSG:3857\",\"projectionList\":" +
            "[{\"code\":\"EPSG:4326\",\"label\":\"WGS84(EPSG:4326)\"},{\"code\":\"EPSG:3857\",\"label\":" +
            "\"Googlemercator(EPSG:3857)\"}],\"disabledTools\":{\"processes\":false,\"addLayers\":false," +
            "\"layers\":false,\"filter\":false,\"contexts\":false,\"print\":false,\"mInteraction\":false," +
            "\"graticule\":false,\"syncAllLayers\":false,\"drawVector\":false},\"searchMapLayers\":[]," +
            "\"viewerMapLayers\":[]},\"editor\":{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.edit\"," +
            "\"geocoder\": {\"enabled\": true, \"appUrl\": \"https://secure.geonames.org/searchJSON\"}," +
            "\"isUserRecordsOnly\":false,\"isFilterTagsDisplayed\":false," +
            "\"createPageTpl\": \"../../catalog/templates/editor/new-metadata-horizontal.html\"},\"admin\":{" +
            "\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/admin.console\"},\"signin\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.signin\"}," +
            "\"signout\":{\"appUrl\":\"../../signout\"}}}";

        objectMapper = new ObjectMapper();
        testInstance = new UrlComponentsDetectorsRegexMigration();
        when(connection.prepareStatement(contains("UPDATE"))).thenReturn(updateStmt);
        when(connection.prepareStatement(contains("SELECT"))).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("value")).thenReturn(originalSetting);
        expectedJsonNode = objectMapper.readTree(updatedSetting);
    }

    @Test
    public void testUpdate() throws SQLException {
        try {
            testInstance.update(connection);
            ArgumentCaptor<String> newJsonStringArgument = ArgumentCaptor.forClass(String.class);
            Mockito.verify(updateStmt, Mockito.times(1)).setString(eq(1), newJsonStringArgument.capture());
            JsonNode actualSetting = objectMapper.readTree(newJsonStringArgument.getValue());
            Assert.assertEquals("New setting is not the expected", expectedJsonNode, actualSetting);
        } catch (Exception e) {
            Assert.fail("Exception not expected: " + e.getMessage());
        }
    }

}
