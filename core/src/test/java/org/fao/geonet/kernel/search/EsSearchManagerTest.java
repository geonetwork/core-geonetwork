package org.fao.geonet.kernel.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.fao.geonet.index.es.EsRestClient;
import org.mockito.ArgumentCaptor;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class EsSearchManagerTest {

    private EsSearchManager instance;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        this.instance = new EsSearchManager();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    public void documentToJsonSimple() throws JsonProcessingException {
        Element input = new Element("doc");
        Element field1 = new Element("field1").setText("content1");
        Element field2 = new Element("field2").setText("content2");
        input.addContent(field1);
        input.addContent(field2);

        ObjectNode result = instance.documentToJson(input);
        JsonNode expected = objectMapper.readTree(" {\"field1\":\"content1\",\"field2\":\"content2\"}");

        assertEquals(expected, result);
    }

    @Test
    public void documentToJsonIndexingErrorMsg() throws JsonProcessingException {
        Element input = new Element("doc");
        Element field1 = new Element("field1").setText("content1");
        String escapedJson = "{\n" +
            "  \"type\": \"error\",\n" +
            "  \"string\": \"string-reference\",\n" +
            "  \"values\": {\n" +
            "    \"value1\": \"content1\",\n" +
            "    \"value2\": \"content2\"\n" +
            "  }\n" +
            "}";
        Element field2 = new Element(IndexFields.INDEXING_ERROR_MSG).setText(escapedJson);
        Element nestedField1 = new Element("nestedField1").setText("nestedContent1");
        Element nestedField2 = new Element("nestedField2").setText("nestedcontent2");
        field2.addContent(nestedField1);
        field2.addContent(nestedField2);
        field2.setAttribute("type", "object");

        input.addContent(field1);
        input.addContent(field2);

        ObjectNode result = instance.documentToJson(input);
        JsonNode expected = objectMapper.readTree("{\"field1\":\"content1\",\"indexingErrorMsg\":[{\"type\":\"error\",\"string\":\"string-reference\",\"values\":{\"value1\":\"content1\",\"value2\":\"content2\"}}]}");

        assertEquals(expected, result);
    }

    @Test
    public void getTotalSizeOfResourcesApprovedReturnsSumOfApprovedDocuments() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(5120.0);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        // Approved mode filters draft in {"n", "e"}
        assertEquals(5120L, manager.getTotalSizeOfResources(Set.of("uuid1", "uuid2"), true).longValue());
    }

    @Test
    public void getTotalSizeOfResourcesReturnsZeroWhenNoDocumentsExist() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(0.0);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        // Both modes return 0 when ES returns 0
        assertEquals(0L, manager.getTotalSizeOfResources(Set.of("uuid1"), true).longValue());
        assertEquals(0L, manager.getTotalSizeOfResources(Set.of("uuid1"), false).longValue());
    }

    @Test
    public void getTotalSizeOfResourcesHandlesEmptyUuidSet() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(0.0);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        // Works for both modes since it's the same aggregation logic
        assertEquals(0L, manager.getTotalSizeOfResources(Set.of(), true).longValue());
        assertEquals(0L, manager.getTotalSizeOfResources(Set.of(), false).longValue());
    }

    @Test
    public void getTotalSizeOfResourcesHandlesLargeFileSizes() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        // Test with 100GB = 107,374,182,400 bytes
        double largeSize = 107374182400.0;
        SearchResponse<Void> response = setupResponse(largeSize);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        // Works for both modes since it's the same aggregation logic
        assertEquals(107374182400L, manager.getTotalSizeOfResources(Set.of("uuid1"), true).longValue());
    }

    @Test
    public void getTotalSizeOfResourcesPreferDraftReturnsSum() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(8000.0);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        // Prefer-draft mode filters draft in {"y", "n"}
        assertEquals(8000L, manager.getTotalSizeOfResources(Set.of("uuid1", "uuid2"), false).longValue());
    }

    @Test
    public void getTotalSizeOfResourcesThrowsRuntimeExceptionOnIOError() throws IOException {
        Set<String> uuids = Set.of("uuid1", "uuid2");

        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);

        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(any(SearchRequest.class), eq(Void.class)))
            .thenThrow(new IOException("Connection failed"));

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        assertThrows(RuntimeException.class,
            () -> manager.getTotalSizeOfResources(uuids, true));
    }

    @Test
    public void getTotalSizeOfResourcesApprovedUsesCorrectDraftFilter() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(1000.0);

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(requestCaptor.capture(), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        manager.getTotalSizeOfResources(Set.of("uuid1"), true);

        // Verify the query structure
        SearchRequest capturedRequest = requestCaptor.getValue();
        String queryString = capturedRequest.toString();

        // Approved mode should filter draft in {"n", "e"}
        assertTrue("Query should contain draft filter with 'n'", queryString.contains("\"n\""));
        assertTrue("Query should contain draft filter with 'e'", queryString.contains("\"e\""));
    }

    @Test
    public void getTotalSizeOfResourcesPreferDraftUsesCorrectDraftFilter() throws IOException {
        EsRestClient mockEsRestClient = mock(EsRestClient.class);
        ElasticsearchClient mockElasticsearchClient = mock(ElasticsearchClient.class);
        SearchResponse<Void> response = setupResponse(1000.0);

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        when(mockEsRestClient.getClient()).thenReturn(mockElasticsearchClient);
        when(mockElasticsearchClient.search(requestCaptor.capture(), eq(Void.class))).thenReturn(response);

        EsSearchManager manager = new EsSearchManager();
        manager.setClient(mockEsRestClient);

        manager.getTotalSizeOfResources(Set.of("uuid1"), false);

        // Verify the query structure
        SearchRequest capturedRequest = requestCaptor.getValue();
        String queryString = capturedRequest.toString();

        // Prefer-draft mode should filter draft in {"y", "n"}
        assertTrue("Query should contain draft filter with 'y'", queryString.contains("\"y\""));
        assertTrue("Query should contain draft filter with 'n'", queryString.contains("\"n\""));
    }

    private static Aggregate sumAgg(double value) {
        return Aggregate.of(a -> a.sum(s -> s.value(value)));
    }

    private static SearchResponse<Void> setupResponse(double totalSize) {
        @SuppressWarnings("unchecked")
        SearchResponse<Void> response = mock(SearchResponse.class);
        when(response.aggregations()).thenReturn(Map.of(
            "total_resources_size", sumAgg(totalSize)
        ));
        return response;
    }


}
