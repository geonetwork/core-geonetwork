package v3110;

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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class UpdateUISettingsMigrationTest {
    private UpdateUISettingsMigration testInstance;
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
        String originalSetting = "{\"langDetector\":{\"fromHtmlTag\":false,\"regexp\":\"^/[a-z" +
            "A-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/\",\"default\":\"eng\"},\"nodeDetector\":{\"regexp\":\"^/[a-zA-Z0-9_-]" +
            "+/([a-zA-Z0-9_-]+)/[a-z]{3}/\",\"default\":\"srv\"},\"mods\":{\"header\":{\"enabled\":true,\"languages\":" +
            "{\"eng\":\"en\",\"dut\":\"nl\",\"fre\":\"fr\",\"ger\":\"ge\",\"kor\":\"ko\",\"spa\":\"es\",\"cze\":\"cz\"," +
            "\"cat\":\"ca\",\"fin\":\"fi\",\"ice\":\"is\", \"rus\": \"ru\", \"chi\": \"zh\"}},\"home\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/home\"},\"search\":{\"enabled\":true,\"appUrl\":" +
            "\"../../srv/{{lang}}/catalog.search#/search\",\"hitsperpageValues\":[10,50,100],\"paginationInfo\":" +
            "{\"hitsPerPage\":20},\"facetsSummaryType\":\"details\",\"facetConfig\":[],\"facetTabField\":\"\"," +
            "\"filters\":{},\"sortbyValues\":[{\"sortBy\":\"relevance\",\"sortOrder\":\"\"},{\"sortBy\":\"changeDate\"," +
            "\"sortOrder\":\"\"},{\"sortBy\":\"title\",\"sortOrder\":\"reverse\"},{\"sortBy\":\"rating\",\"sortOrder\":" +
            "\"\"},{\"sortBy\":\"popularity\",\"sortOrder\":\"\"},{\"sortBy\":\"denominatorDesc\",\"sortOrder\":\"\"}," +
            "{\"sortBy\":\"denominatorAsc\",\"sortOrder\":\"reverse\"}],\"sortBy\":\"relevance\",\"resultViewTpls\":" +
            "[{\"tplUrl\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\",\"tooltip\":" +
            "\"Grid\",\"icon\":\"fa-th\"}]," +
            "\"resultTemplate\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"formatter\":{\"list\":[{\"label\":\"full\",\"url\":\"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced\"}]}," +
            "\"grid\":{\"related\":[\"parent\",\"children\",\"services\",\"datasets\"]},\"linkTypes\":{\"links\":[\"LINK\",\"kml\"]," +
            "\"downloads\":[\"DOWNLOAD\"],\"layers\":[\"OGC\"],\"maps\":[\"ows\"]},\"isFilterTagsDisplayedInSearch\":false},\"map\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.search#/map\",\"is3DModeAllowed\":true," +
            "\"isSaveMapInCatalogAllowed\":true,\"isExportMapAsImageEnabled\":true," +
            "\"bingKey\":\"AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D\",\"storage\":" +
            "\"sessionStorage\",\"map\":\"../../map/config-viewer.xml\",\"listOfServices\":{\"wms\":[],\"wmts\"" +
            ":[]},\"useOSM\":true,\"context\":\"\",\"layer\":{\"url\":\"http://www2.demis.nl/mapserver/wms.asp?\"," +
            "\"layers\":\"Countries\",\"version\":\"1.1.1\"},\"projection\":\"EPSG:3857\",\"projectionList\":" +
            "[{\"code\":\"EPSG:4326\",\"label\":\"WGS84(EPSG:4326)\"},{\"code\":\"EPSG:3857\",\"label\":" +
            "\"Googlemercator(EPSG:3857)\"}],\"disabledTools\":{\"processes\":false,\"addLayers\":false," +
            "\"layers\":false,\"filter\":false,\"contexts\":false,\"print\":false,\"mInteraction\":false," +
            "\"graticule\":false,\"syncAllLayers\":false,\"drawVector\":false},\"searchMapLayers\":[]," +
            "\"viewerMapLayers\":[]},\"editor\":{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.edit\"," +
            "\"isUserRecordsOnly\":false,\"isFilterTagsDisplayed\":false," +
            "\"createPageTpl\": \"../../catalog/templates/editor/new-metadata-horizontal.html\"},\"admin\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/admin.console\"},\"signin\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.signin\"},\"signout\":{\"appUrl\":\"../../signout\"}}}";

        String updatedSetting = "{\"langDetector\":{\"fromHtmlTag\":false,\"regexp\":\"^/[a-z" +
            "A-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/\",\"default\":\"eng\"},\"nodeDetector\":{\"regexp\":\"^/[a-zA-Z0-9_-]" +
            "+/([a-zA-Z0-9_-]+)/[a-z]{3}/\",\"default\":\"srv\"},\"mods\":{\"header\":{\"enabled\":true,\"languages\":" +
            "{\"eng\":\"en\",\"dut\":\"nl\",\"fre\":\"fr\",\"ger\":\"ge\",\"kor\":\"ko\",\"spa\":\"es\",\"cze\":\"cz\"," +
            "\"cat\":\"ca\",\"fin\":\"fi\",\"ice\":\"is\", \"rus\": \"ru\", \"chi\": \"zh\"}, \"isMenubarAccessible\":true},\"home\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.search#/home\"},\"search\":{\"enabled\":true,\"appUrl\":" +
            "\"../../srv/{{lang}}/catalog.search#/search\",\"hitsperpageValues\":[10,50,100],\"paginationInfo\":" +
            "{\"hitsPerPage\":20},\"facetsSummaryType\":\"details\",\"facetConfig\":[],\"facetTabField\":\"\"," +
            "\"filters\":{},\"sortbyValues\":[{\"sortBy\":\"relevance\",\"sortOrder\":\"\"},{\"sortBy\":\"changeDate\"," +
            "\"sortOrder\":\"\"},{\"sortBy\":\"title\",\"sortOrder\":\"reverse\"},{\"sortBy\":\"rating\",\"sortOrder\":" +
            "\"\"},{\"sortBy\":\"popularity\",\"sortOrder\":\"\"},{\"sortBy\":\"denominatorDesc\",\"sortOrder\":\"\"}," +
            "{\"sortBy\":\"denominatorAsc\",\"sortOrder\":\"reverse\"}],\"sortBy\":\"relevance\",\"resultViewTpls\":" +
            "[{\"tplUrl\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\",\"tooltip\":" +
            "\"Grid\",\"icon\":\"fa-th\"}]," +
            "\"resultTemplate\":\"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html\"," +
            "\"formatter\":{\"list\":[{\"label\":\"full\",\"url\":\"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced\"}]}," +
            "\"grid\":{\"related\":[\"parent\",\"children\",\"services\",\"datasets\"]},\"linkTypes\":{\"links\":[\"LINK\",\"kml\"]," +
            "\"downloads\":[\"DOWNLOAD\"],\"layers\":[\"OGC\"],\"maps\":[\"ows\"]},\"isFilterTagsDisplayedInSearch\":false},\"map\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.search#/map\",\"is3DModeAllowed\":true," +
            "\"isSaveMapInCatalogAllowed\":true,\"isExportMapAsImageEnabled\":true," +
            "\"bingKey\":\"AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D\",\"storage\":" +
            "\"sessionStorage\",\"map\":\"../../map/config-viewer.xml\",\"listOfServices\":{\"wms\":[],\"wmts\"" +
            ":[]},\"useOSM\":true,\"context\":\"\",\"layer\":{\"url\":\"http://www2.demis.nl/mapserver/wms.asp?\"," +
            "\"layers\":\"Countries\",\"version\":\"1.1.1\"},\"projection\":\"EPSG:3857\",\"projectionList\":" +
            "[{\"code\":\"EPSG:4326\",\"label\":\"WGS84(EPSG:4326)\"},{\"code\":\"EPSG:3857\",\"label\":" +
            "\"Googlemercator(EPSG:3857)\"}],\"disabledTools\":{\"processes\":false,\"addLayers\":false," +
            "\"layers\":false,\"filter\":false,\"contexts\":false,\"print\":false,\"mInteraction\":false," +
            "\"graticule\":false,\"syncAllLayers\":false,\"drawVector\":false},\"searchMapLayers\":[]," +
            "\"viewerMapLayers\":[]},\"editor\":{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/catalog.edit\"," +
            "\"isUserRecordsOnly\":false,\"isFilterTagsDisplayed\":false," +
            "\"createPageTpl\": \"../../catalog/templates/editor/new-metadata-horizontal.html\"},\"admin\":" +
            "{\"enabled\":true,\"appUrl\":\"../../srv/{{lang}}/admin.console\"},\"signin\":{\"enabled\":true," +
            "\"appUrl\":\"../../srv/{{lang}}/catalog.signin\"},\"signout\":{\"appUrl\":\"../../signout\"}}}";
        objectMapper = new ObjectMapper();
        testInstance = new UpdateUISettingsMigration();
        when(connection.prepareStatement(contains("UPDATE"))).thenReturn(updateStmt);
        when(connection.prepareStatement(contains("SELECT"))).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("configuration")).thenReturn(originalSetting);
        expectedJsonNode = objectMapper.readTree(updatedSetting);
    }

    @Test
    public void testNewLeaf() throws IOException {
        String originalJsonString = "{\"a\": {\"b\":{\"c\":\"cVal\"}}}";
        Map<String, String> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("/a/b/d", "\"dVal\"");
        String newJsonString = testInstance.insertOrUpdateField(originalJsonString, fieldsToUpdate);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJsonNode = mapper.readTree("{\"a\": {\"b\":{\"c\":\"cVal\", \"d\":\"dVal\"}}}");
        JsonNode actualJsonNode = mapper.readTree(newJsonString);
        Assert.assertEquals(expectedJsonNode, actualJsonNode);
    }

    @Test
    public void testNewPath() throws IOException {
        String originalJsonString = "{\"a\": {\"b\":{}}}";
        Map<String, String> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("/a/b/c/d", "\"dVal\"");
        String newJsonString = testInstance.insertOrUpdateField(originalJsonString, fieldsToUpdate);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJsonNode = mapper.readTree("{\"a\": {\"b\":{\"c\":{ \"d\":\"dVal\"}}}}");
        JsonNode actualJsonNode = mapper.readTree(newJsonString);
        Assert.assertEquals(expectedJsonNode, actualJsonNode);
    }

    @Test
    public void testReplacePath() throws IOException {
        String originalJsonString = "{\"a\": {\"b\":{\"c\":\"cVal\"}}}";
        Map<String, String> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("/a/b/c", "\"cNewVal\"");
        String newJsonString = testInstance.insertOrUpdateField(originalJsonString, fieldsToUpdate);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJsonNode = mapper.readTree("{\"a\": {\"b\":{\"c\":\"cNewVal\"}}}");
        JsonNode actualJsonNode = mapper.readTree(newJsonString);
        Assert.assertEquals(expectedJsonNode, actualJsonNode);
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
