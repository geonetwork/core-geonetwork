package org.fao.geonet.kernel.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.fao.geonet.index.es.EsRestClient;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

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
    public void getResourcesFromIndexReturnsListWhenFileStoreExists() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> resource1 = new HashMap<>();
        resource1.put("name", "file1.txt");
        resource1.put("size", 1024);
        Map<String, Object> resource2 = new HashMap<>();
        resource2.put("name", "file2.txt");
        resource2.put("size", 2048);

        Map<String, Object> mdIndexFields = new HashMap<>();
        mdIndexFields.put(IndexFields.FILESTORE, Arrays.asList(resource1, resource2));

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenReturn(mdIndexFields);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(2, result.size());
        assertEquals("file1.txt", result.get(0).get("name"));
        assertEquals("file2.txt", result.get(1).get("name"));
    }

    @Test
    public void getResourcesFromIndexReturnsEmptyListWhenDocumentNotFound() throws Exception {
        String metadataUuid = "nonexistent-uuid";

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), eq("nonexistent-uuid"))).thenReturn(null);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(0, result.size());
    }

    @Test
    public void getResourcesFromIndexReturnsEmptyListWhenFileStoreFieldMissing() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> mdIndexFields = new HashMap<>();

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenReturn(mdIndexFields);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(0, result.size());
    }

    @Test
    public void getResourcesFromIndexUsesDraftKeyWhenNotApproved() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> mdIndexFields = new HashMap<>();
        mdIndexFields.put(IndexFields.FILESTORE, new ArrayList<>());

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), eq("test-uuid-draft"))).thenReturn(mdIndexFields);

        manager.getResourcesFromIndex(metadataUuid, false);

        verify(mockClient).getDocument(anyString(), eq("test-uuid-draft"));
    }

    @Test
    public void getResourcesFromIndexFiltersNullElements() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> resource1 = new HashMap<>();
        resource1.put("name", "file1.txt");

        Map<String, Object> mdIndexFields = new HashMap<>();
        mdIndexFields.put(IndexFields.FILESTORE, Arrays.asList(resource1, null));

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenReturn(mdIndexFields);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(1, result.size());
        assertEquals("file1.txt", result.get(0).get("name"));
    }

    @Test
    public void getResourcesFromIndexFiltersNonMapElements() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> resource1 = new HashMap<>();
        resource1.put("name", "file1.txt");

        Map<String, Object> mdIndexFields = new HashMap<>();
        mdIndexFields.put(IndexFields.FILESTORE, Arrays.asList(resource1, "invalid", 123));

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenReturn(mdIndexFields);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(1, result.size());
        assertEquals("file1.txt", result.get(0).get("name"));
    }

    @Test
    public void getResourcesFromIndexReturnsEmptyListOnException() throws Exception {
        String metadataUuid = "test-uuid";

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenThrow(new RuntimeException("Test exception"));

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(0, result.size());
    }

    @Test
    public void getResourcesFromIndexReturnsEmptyListWhenFileStoreIsNotList() throws Exception {
        String metadataUuid = "test-uuid";
        Map<String, Object> mdIndexFields = new HashMap<>();
        mdIndexFields.put(IndexFields.FILESTORE, "not a list");

        EsSearchManager manager = new EsSearchManager();
        EsRestClient mockClient = mock(EsRestClient.class);
        manager.setClient(mockClient);
        when(mockClient.getDocument(anyString(), anyString())).thenReturn(mdIndexFields);

        List<Map<String, Object>> result = manager.getResourcesFromIndex(metadataUuid, true);

        assertEquals(0, result.size());
    }
}
