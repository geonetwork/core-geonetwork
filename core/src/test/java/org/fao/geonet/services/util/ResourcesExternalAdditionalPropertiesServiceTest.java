package org.fao.geonet.services.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesExternalAdditionalPropertiesServiceTest {

    // The names of the fields in ResourcesExternalAdditionalPropertiesService to set via reflection
    private static final String EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME_CLASS_FIELD_NAME = "externalAdditionalPropertiesIdentifierFieldName";
    private static final String EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE_CLASS_FIELD_NAME = "externalAdditionalPropertiesUrlTemplate";
    private static final String SECURITY_PROVIDER_UTIL_CLASS_FIELD_NAME = "securityProviderUtil";

    // The default values of the fields in ResourcesExternalAdditionalPropertiesService to be set via reflection
    private static final String DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME = "externalId";
    private static final String DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE = "http://example.com";

    // The names of fields in the JSON nodes
    private static final String ID_FIELD_NAME = "id";
    private static final String METADATA_RESOURCE_EXTERNAL_MANAGEMENT_PROPERTIES_FIELD_NAME = "metadataResourceExternalManagementProperties";

    @Mock
    SecurityProviderUtil securityProviderUtilMock;

    @Spy
    @InjectMocks
    ResourcesExternalAdditionalPropertiesService resourcesExternalAdditionalPropertiesServiceSpy;

    @Before
    public void setUp() {
        // Common configuration for merge tests
        ReflectionTestUtils.setField(resourcesExternalAdditionalPropertiesServiceSpy,
            EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE_CLASS_FIELD_NAME, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE);
        ReflectionTestUtils.setField(resourcesExternalAdditionalPropertiesServiceSpy,
            EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME_CLASS_FIELD_NAME, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);
    }

    // ============================
    // resolveUrlTemplate tests
    // ============================

    @Test
    public void resolveUrlTemplateReplacesPlaceholdersCorrectly() {
        String template = "http://example.com/resource/{uuid}/status/{approved}";
        String uuid = UUID.randomUUID().toString();
        boolean approved = true;

        String result = resourcesExternalAdditionalPropertiesServiceSpy.resolveUrlTemplate(template, uuid, approved);

        assertEquals("http://example.com/resource/" + uuid + "/status/true", result);
    }

    @Test
    public void resolveUrlTemplateHandlesEmptyTemplate() {
        String template = "";
        boolean approved = true;

        String result = resourcesExternalAdditionalPropertiesServiceSpy.resolveUrlTemplate(template, UUID.randomUUID().toString(), approved);

        assertEquals("", result);
    }

    @Test
    public void resolveUrlTemplateHandlesMissingPlaceholders() {
        String template = "http://example.com/resource";
        boolean approved = true;

        String result = resourcesExternalAdditionalPropertiesServiceSpy.resolveUrlTemplate(template, UUID.randomUUID().toString(), approved);

        assertEquals("http://example.com/resource", result);
    }

    @Test
    public void resolveUrlTemplateHandlesPartialPlaceholders() {
        String template = "http://example.com/resource/{uuid}";
        String uuid = UUID.randomUUID().toString();
        boolean approved = false;

        String result = resourcesExternalAdditionalPropertiesServiceSpy.resolveUrlTemplate(template, uuid, approved);

        assertEquals("http://example.com/resource/" + uuid, result);
    }

    // ============================
    // getResourcesExternalAdditionalProperties tests
    // ============================

    @Test
    public void getResourcesExternalAdditionalPropertiesReturnsDataOnSuccessfulRequest() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        ObjectNode objectNode = arrayNode.addObject();
        objectNode.put(ID_FIELD_NAME, "123");
        objectNode.put("property", "value");

        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.ok(arrayNode);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        ArrayNode result = resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true);

        assertEquals(1, result.size());
        assertEquals("123", result.get(0).get(ID_FIELD_NAME).asText());
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesThrowsExceptionOnNullResponseBody() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.ok(null);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true)
        );
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesThrowsExceptionOnNonSuccessfulStatusCode() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.status(404).body(arrayNode);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true)
        );
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesThrowsExceptionOnException() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class)))
            .thenThrow(new RuntimeException("Connection error"));

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), false)
        );
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesReturnsEmptyArrayWhenResponseIsEmpty() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ArrayNode emptyArray = new ObjectMapper().createArrayNode();
        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.ok(emptyArray);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        ArrayNode result = resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true);

        assertEquals(0, result.size());
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesReturnsMultipleItems() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        arrayNode.addObject().put(ID_FIELD_NAME, "123");
        arrayNode.addObject().put(ID_FIELD_NAME, "456");

        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.ok(arrayNode);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        ArrayNode result = resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true);

        assertEquals(2, result.size());
    }

    @Test
    public void getResourcesExternalAdditionalPropertiesCallsResolveUrlTemplateWithCorrectParameters() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        ResponseEntity<ArrayNode> responseEntity = ResponseEntity.ok(arrayNode);
        when(mockRestTemplate.getForEntity(anyString(), eq(ArrayNode.class))).thenReturn(responseEntity);

        doReturn("http://example.com/api/properties").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(anyString(), anyString(), anyBoolean());
        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        String uuid = UUID.randomUUID().toString();

        resourcesExternalAdditionalPropertiesServiceSpy.getResourcesExternalAdditionalProperties(uuid, false);

        verify(resourcesExternalAdditionalPropertiesServiceSpy).resolveUrlTemplate(anyString(), eq(uuid), eq(false));
    }

    // ============================
    // indexResourcesExternalAdditionalPropertiesById tests
    // ============================

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdReturnsMapWithSingleItem() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        addNewExternalProperty(arrayNode, "ext-123", "property1", "value1");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("ext-123"));
        assertFalse(result.get("ext-123").has(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME));
        assertEquals("value1", result.get("ext-123").get("property1").asText());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdReturnsMapWithMultipleItems() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        addNewExternalProperty(arrayNode, "ext-123", "property1", "value1");
        addNewExternalProperty(arrayNode, "ext-456", "property2", "value2");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("ext-123"));
        assertTrue(result.containsKey("ext-456"));
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdSkipsItemWithMissingIdentifier() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        ObjectNode item = arrayNode.addObject();
        item.put("property1", "value1");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdSkipsItemWithNullIdentifier() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        ObjectNode item = arrayNode.addObject();
        item.putNull(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);
        item.put("property1", "value1");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdSkipsItemWithBlankIdentifier() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        addNewExternalProperty(arrayNode, "", "property1", "value1");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdSkipsNonObjectNodeEntry() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        arrayNode.add("not-an-object");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdReturnsEmptyMapForEmptyArray() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    @Test
    public void indexResourcesExternalAdditionalPropertiesByIdHandlesWhitespaceIdentifier() {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
        addNewExternalProperty(arrayNode, "   ", "property1", "value1");

        Map<String, ObjectNode> result = resourcesExternalAdditionalPropertiesServiceSpy
            .indexResourcesExternalAdditionalPropertiesById(arrayNode);

        assertEquals(0, result.size());
    }

    // ============================
    // createAuthenticatedRestTemplate tests
    // ============================

    @Test
    public void createAuthenticatedRestTemplateReturnsRestTemplateWhenNoSecurityProvider() {
        ReflectionTestUtils.setField(resourcesExternalAdditionalPropertiesServiceSpy, SECURITY_PROVIDER_UTIL_CLASS_FIELD_NAME, null);

        RestTemplate result = resourcesExternalAdditionalPropertiesServiceSpy.createAuthenticatedRestTemplate();

        assertNotNull(result);
        assertEquals(0, result.getInterceptors().size());
    }

    @Test
    public void createAuthenticatedRestTemplateAddsAuthorizationHeaderWhenTokenPresent() {
        when(securityProviderUtilMock.loginServiceAccount()).thenReturn(true);
        when(securityProviderUtilMock.getSSOAuthenticationHeaderValue()).thenReturn("Bearer test-token");

        RestTemplate result = resourcesExternalAdditionalPropertiesServiceSpy.createAuthenticatedRestTemplate();

        assertNotNull(result);
        assertEquals(1, result.getInterceptors().size());
        verify(securityProviderUtilMock).loginServiceAccount();
        verify(securityProviderUtilMock).getSSOAuthenticationHeaderValue();
    }

    @Test
    public void createAuthenticatedRestTemplateDoesNotAddAuthorizationHeaderWhenTokenBlank() {
        when(securityProviderUtilMock.loginServiceAccount()).thenReturn(true);
        when(securityProviderUtilMock.getSSOAuthenticationHeaderValue()).thenReturn("");

        RestTemplate result = resourcesExternalAdditionalPropertiesServiceSpy.createAuthenticatedRestTemplate();

        assertNotNull(result);
        assertEquals(0, result.getInterceptors().size());
    }

    @Test
    public void createAuthenticatedRestTemplateThrowsExceptionWhenServiceAccountLoginFails() {
        when(securityProviderUtilMock.loginServiceAccount()).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.createAuthenticatedRestTemplate()
        );
    }

    @Test
    public void createAuthenticatedRestTemplateInterceptorAddsCorrectAuthorizationHeader() throws Exception {
        when(securityProviderUtilMock.loginServiceAccount()).thenReturn(true);
        when(securityProviderUtilMock.getSSOAuthenticationHeaderValue()).thenReturn("Bearer test-token");

        RestTemplate result = resourcesExternalAdditionalPropertiesServiceSpy.createAuthenticatedRestTemplate();

        // Test the interceptor actually adds the header
        MockClientHttpRequest request = new MockClientHttpRequest();
        result.getInterceptors().get(0).intercept(request, new byte[0], (req, body) -> {
            assertEquals("Bearer test-token", req.getHeaders().getFirst("Authorization"));
            return new MockClientHttpResponse(new byte[0], 200);
        });
    }

    // ============================
    // mergeResourcesExternalAdditionalProperties tests
    // ============================

    @Test
    public void mergeResourcesExternalAdditionalPropertiesSkipsWhenUrlTemplateNotConfigured() {
        ReflectionTestUtils.setField(resourcesExternalAdditionalPropertiesServiceSpy,
            EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE_CLASS_FIELD_NAME, "");

        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        verify(resourcesExternalAdditionalPropertiesServiceSpy, never()).getResourcesExternalAdditionalProperties(anyString(), anyBoolean());
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesSkipsWhenIdentifierFieldNotConfigured() {
        ReflectionTestUtils.setField(resourcesExternalAdditionalPropertiesServiceSpy,
            EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME_CLASS_FIELD_NAME, "");

        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        verify(resourcesExternalAdditionalPropertiesServiceSpy, never()).getResourcesExternalAdditionalProperties(anyString(), anyBoolean());
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesSuccessfullyMergesProperties() {
        // Create base properties
        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        addNewResourceWithId(baseProperties, "123");

        // Create external properties
        ArrayNode externalProperties = new ObjectMapper().createArrayNode();
        addNewExternalProperty(externalProperties, "123", "additionalProp", "value");

        doReturn(externalProperties).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .getResourcesExternalAdditionalProperties(anyString(), anyBoolean());

        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        assertTrue(baseProperties.get(0).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
        assertEquals("value", baseProperties.get(0).get(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME).get("additionalProp").asText());
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesSkipsResourcesWithoutId() {
        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        ObjectNode resource = baseProperties.addObject();
        resource.put("someProp", "value");

        ArrayNode externalProperties = new ObjectMapper().createArrayNode();
        doReturn(externalProperties).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .getResourcesExternalAdditionalProperties(anyString(), anyBoolean());

        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        assertFalse(baseProperties.get(0).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesHandlesNullIdNode() {
        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        addNewResourceWithId(baseProperties, null);

        ArrayNode externalProperties = new ObjectMapper().createArrayNode();
        doReturn(externalProperties).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .getResourcesExternalAdditionalProperties(anyString(), anyBoolean());

        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        assertFalse(baseProperties.get(0).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesHandlesWhitespaceId() {
        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        addNewResourceWithId(baseProperties, "   ");

        ArrayNode externalProperties = new ObjectMapper().createArrayNode();
        doReturn(externalProperties).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .getResourcesExternalAdditionalProperties(anyString(), anyBoolean());

        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        assertFalse(baseProperties.get(0).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
    }

    @Test
    public void mergeResourcesExternalAdditionalPropertiesHandlesPartialMatches() {
        // Create base properties with 3 resources
        ArrayNode baseProperties = new ObjectMapper().createArrayNode();
        addNewResourceWithId(baseProperties, "123");
        addNewResourceWithId(baseProperties, "456");
        addNewResourceWithId(baseProperties, "789");

        // Create external properties with only 2 matching resources
        ArrayNode externalProperties = new ObjectMapper().createArrayNode();
        addNewExternalProperty(externalProperties, "123", "prop1", "value1");
        addNewExternalProperty(externalProperties, "789", "prop3", "value3");

        doReturn(externalProperties).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .getResourcesExternalAdditionalProperties(anyString(), anyBoolean());

        resourcesExternalAdditionalPropertiesServiceSpy.mergeResourcesExternalAdditionalProperties(UUID.randomUUID().toString(), true, baseProperties);

        assertTrue(baseProperties.get(0).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
        assertFalse(baseProperties.get(1).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
        assertTrue(baseProperties.get(2).has(ResourcesExternalAdditionalPropertiesService.EXTERNAL_ADDITIONAL_PROPERTIES_FIELD_NAME));
    }

    private static void addNewExternalProperty(ArrayNode externalProperties, String id, String propName, String propValue) {
        ObjectNode external1 = externalProperties.addObject();
        external1.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, id);
        external1.put(propName, propValue);
    }

    private static void addNewResourceWithId(ArrayNode baseProperties, String number) {
        ObjectNode resource = baseProperties.addObject();
        resource.putObject(METADATA_RESOURCE_EXTERNAL_MANAGEMENT_PROPERTIES_FIELD_NAME).put(ID_FIELD_NAME, number);
    }

}
