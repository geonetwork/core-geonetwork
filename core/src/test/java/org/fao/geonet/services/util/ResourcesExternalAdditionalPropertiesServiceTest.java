package org.fao.geonet.services.util;

import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesExternalAdditionalPropertiesServiceTest {

    // The name of the field in ResourcesExternalAdditionalPropertiesService to set via reflection
    private static final String SECURITY_PROVIDER_UTIL_CLASS_FIELD_NAME = "securityProviderUtil";

    // Test values
    private static final String DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME = "externalId";
    private static final String DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE = "http://example.com/api/resources?uuid={uuid}&approved={approved}";

    @Mock
    SecurityProviderUtil securityProviderUtilMock;

    @Spy
    @InjectMocks
    ResourcesExternalAdditionalPropertiesService resourcesExternalAdditionalPropertiesServiceSpy;

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
    // fetchAdditionalProperties tests
    // ============================

    @Test
    public void fetchAdditionalPropertiesReturnsDataOnSuccessfulRequest() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        List<Map<String, Object>> responseData = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("id", "123");
        item.put("property", "value");
        responseData.add(item);

        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(responseData);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        List<Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties");

        assertEquals(1, result.size());
        assertEquals("123", result.get(0).get("id"));
    }

    @Test
    public void fetchAdditionalPropertiesThrowsExceptionOnNullResponseBody() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(null);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties")
        );
    }

    @Test
    public void fetchAdditionalPropertiesThrowsExceptionOnNonSuccessfulStatusCode() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        List<Map<String, Object>> responseData = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.status(404).body(responseData);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties")
        );
    }

    @Test
    public void fetchAdditionalPropertiesThrowsExceptionOnException() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("Connection error"));

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        assertThrows(RuntimeException.class, () ->
            resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties")
        );
    }

    @Test
    public void fetchAdditionalPropertiesReturnsEmptyListWhenResponseIsEmpty() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        List<Map<String, Object>> emptyList = new ArrayList<>();
        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(emptyList);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        List<Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties");

        assertEquals(0, result.size());
    }

    @Test
    public void fetchAdditionalPropertiesReturnsMultipleItems() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        List<Map<String, Object>> responseData = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("id", "123");
        responseData.add(item1);
        Map<String, Object> item2 = new HashMap<>();
        item2.put("id", "456");
        responseData.add(item2);

        ResponseEntity<List<Map<String, Object>>> responseEntity = ResponseEntity.ok(responseData);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
            .thenReturn(responseEntity);

        doReturn(mockRestTemplate).when(resourcesExternalAdditionalPropertiesServiceSpy).createAuthenticatedRestTemplate();

        List<Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy.fetchAdditionalProperties("http://example.com/api/properties");

        assertEquals(2, result.size());
    }


    // ============================
    // mapAdditionalPropertiesById tests
    // ============================

    @Test
    public void mapAdditionalPropertiesByIdReturnsMapWithSingleItem() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, "ext-123");
        item.put("property1", "value1");
        resourcesList.add(item);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("ext-123"));
        assertFalse(result.get("ext-123").containsKey(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME));
        assertEquals("value1", result.get("ext-123").get("property1"));
    }

    @Test
    public void mapAdditionalPropertiesByIdReturnsMapWithMultipleItems() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, "ext-123");
        item1.put("property1", "value1");
        resourcesList.add(item1);
        Map<String, Object> item2 = new HashMap<>();
        item2.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, "ext-456");
        item2.put("property2", "value2");
        resourcesList.add(item2);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("ext-123"));
        assertTrue(result.containsKey("ext-456"));
    }

    @Test
    public void mapAdditionalPropertiesByIdSkipsItemWithMissingIdentifier() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("property1", "value1");
        resourcesList.add(item);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(0, result.size());
    }

    @Test
    public void mapAdditionalPropertiesByIdSkipsItemWithNullIdentifier() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, null);
        item.put("property1", "value1");
        resourcesList.add(item);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(0, result.size());
    }

    @Test
    public void mapAdditionalPropertiesByIdSkipsItemWithBlankIdentifier() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, "");
        item.put("property1", "value1");
        resourcesList.add(item);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(0, result.size());
    }

    @Test
    public void mapAdditionalPropertiesByIdReturnsEmptyMapForEmptyList() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

        assertEquals(0, result.size());
    }

    @Test
    public void mapAdditionalPropertiesByIdHandlesWhitespaceIdentifier() {
        List<Map<String, Object>> resourcesList = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put(DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME, "   ");
        item.put("property1", "value1");
        resourcesList.add(item);

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .mapAdditionalPropertiesById(resourcesList, DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME);

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
    // getAdditionalPropertiesMap tests
    // ============================

    @Test
    public void getAdditionalPropertiesMapReturnsMapSuccessfully() {
        String uuid = UUID.randomUUID().toString();
        String urlTemplate = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE;
        String identifierFieldName = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME;

        List<Map<String, Object>> fetchedData = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put(identifierFieldName, "ext-123");
        item1.put("property1", "value1");
        fetchedData.add(item1);

        doReturn("http://example.com/resolved").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(urlTemplate, uuid, true);
        doReturn(fetchedData).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .fetchAdditionalProperties("http://example.com/resolved");

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .getAdditionalPropertiesMap(uuid, true, urlTemplate, identifierFieldName);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("ext-123"));
        assertEquals("value1", result.get("ext-123").get("property1"));
    }

    @Test
    public void getAdditionalPropertiesMapCallsMethodsInCorrectOrder() {
        String uuid = UUID.randomUUID().toString();
        String urlTemplate = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE;
        String identifierFieldName = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME;

        List<Map<String, Object>> fetchedData = new ArrayList<>();
        doReturn("http://example.com/resolved").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(urlTemplate, uuid, false);
        doReturn(fetchedData).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .fetchAdditionalProperties("http://example.com/resolved");

        resourcesExternalAdditionalPropertiesServiceSpy
            .getAdditionalPropertiesMap(uuid, false, urlTemplate, identifierFieldName);

        verify(resourcesExternalAdditionalPropertiesServiceSpy).resolveUrlTemplate(urlTemplate, uuid, false);
        verify(resourcesExternalAdditionalPropertiesServiceSpy).fetchAdditionalProperties("http://example.com/resolved");
        verify(resourcesExternalAdditionalPropertiesServiceSpy).mapAdditionalPropertiesById(fetchedData, identifierFieldName);
    }

    @Test
    public void getAdditionalPropertiesMapHandlesEmptyResponse() {
        String uuid = UUID.randomUUID().toString();
        String urlTemplate = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_URL_TEMPLATE;
        String identifierFieldName = DEFAULT_EXTERNAL_ADDITIONAL_PROPERTIES_IDENTIFIER_FIELD_NAME;

        List<Map<String, Object>> emptyList = new ArrayList<>();
        doReturn("http://example.com/resolved").when(resourcesExternalAdditionalPropertiesServiceSpy)
            .resolveUrlTemplate(urlTemplate, uuid, true);
        doReturn(emptyList).when(resourcesExternalAdditionalPropertiesServiceSpy)
            .fetchAdditionalProperties("http://example.com/resolved");

        Map<String, Map<String, Object>> result = resourcesExternalAdditionalPropertiesServiceSpy
            .getAdditionalPropertiesMap(uuid, true, urlTemplate, identifierFieldName);

        assertEquals(0, result.size());
    }

}
