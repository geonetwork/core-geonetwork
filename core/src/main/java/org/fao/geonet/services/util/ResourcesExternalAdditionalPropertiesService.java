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

package org.fao.geonet.services.util;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.security.SecurityProviderUtil;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for merging external additional properties into resource properties.
 *
 * <p>This service retrieves additional properties for resources from an external service
 * and merges them into the provided resource properties based on matching identifiers.
 * The external service URL and identifier field name are configurable via application
 * properties.</p>
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li><b>resources.external.additional.properties.url.template</b> - The URL template for the external service.
 *       It should contain placeholders {uuid} and {approved} to be replaced with actual values.</li>
 *   <li><b>resources.external.additional.properties.identifier.field.name</b> - The name of the field in the
 *       external additional properties that contains the resource identifier.</li>
 * </ul>
 */
@Service
public class ResourcesExternalAdditionalPropertiesService {

    /**
     * The SecurityProviderUtil for handling authentication.
     *
     * <p>Responsible for managing service account login and retrieving SSO authentication
     * tokens. This is injected by Spring and may be null if security provider support
     * is not available. When available, it enables authenticated requests to external
     * services without requiring an active user session.</p>
     */
    @Autowired(required = false)
    private SecurityProviderUtil securityProviderUtil;

    public Map<String, Map<String, Object>> getAdditionalPropertiesMap(String uuid, boolean approved, String urlTemplate, String identifierFieldName) {
        String url = resolveUrlTemplate(urlTemplate, uuid, approved);
        List<Map<String, Object>> resourcesExternalAdditionalProperties = fetchAdditionalProperties(url);
        return mapAdditionalPropertiesById(resourcesExternalAdditionalProperties, identifierFieldName);
    }

    /**
     * Retrieves external additional properties for resources from an external service.
     *
     * <p>This method constructs the endpoint URL using the provided UUID and approval status,
     * then makes an authenticated GET request to the external service to fetch the additional
     * properties. If the request is successful and returns a valid response, the method
     * returns the array of additional properties. If the response is not successful or any
     * error occurs during the request, a RuntimeException is thrown with details about the failure.</p>
     *
     * <p><b>Error Handling:</b> Any exceptions during the REST call are wrapped in a RuntimeException
     * with the endpoint URL and detailed error message to aid in troubleshooting. This includes
     * connection errors, timeouts, and invalid responses.</p>
     *
     * <p><b>Authentication:</b> The request is made using an authenticated RestTemplate created
     * by {@link #createAuthenticatedRestTemplate()}, which includes necessary authorization
     * headers for accessing protected external service endpoints.</p>
     *
     * @return an ArrayNode containing the external additional properties; never null
     * @throws RuntimeException if the external service request fails, returns a non-2xx response,
     *                          or any other exception occurs during the request
     */
    @Nonnull
    protected List<Map<String, Object>> fetchAdditionalProperties(String url) {
        try {
            ResponseEntity<List<Map<String, Object>>> responseEntity = createAuthenticatedRestTemplate().exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody();
            }

            throw new RuntimeException("Failed to retrieve external additional resource properties. " +
                "Response code: " + responseEntity.getStatusCodeValue() + ", URL: " + url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve external additional resource properties from: " +
                url, e);
        }
    }

    /**
     * Resolves the URL template by replacing placeholders with actual values.
     *
     * <p>This method performs simple string replacement on the provided URL template,
     * substituting the following placeholders with the provided values:</p>
     * <ul>
     *   <li>{uuid} - replaced with the provided UUID value</li>
     *   <li>{approved} - replaced with the boolean approved status as a string ("true" or "false")</li>
     * </ul>
     *
     * <p><b>Example:</b> Template "http://api.example.com/resources?uuid={uuid}&approved={approved}"
     * with uuid="abc123" and approved=true would resolve to
     * "http://api.example.com/resources?uuid=abc123&approved=true"</p>
     *
     * @param externalAdditionalPropertiesUrlTemplate the URL template containing placeholders; must not be blank
     * @param uuid the UUID to replace the {uuid} placeholder; must not be blank
     * @param approved the approved flag to replace the {approved} placeholder
     * @return the resolved URL with placeholders replaced by actual values
     */
    protected String resolveUrlTemplate(@Nonnull String externalAdditionalPropertiesUrlTemplate, @Nonnull String uuid, boolean approved) {
        return externalAdditionalPropertiesUrlTemplate
                .replace("{uuid}", uuid)
                .replace("{approved}", Boolean.toString(approved));
    }

    /**
     * Indexes external additional properties by their identifier.
     *
     * <p>This method processes an array of external additional properties and creates a map
     * where each entry is keyed by the identifier field specified by
     * {@code externalAdditionalPropertiesIdentifierFieldName}. The identifier field is removed
     * from the individual additional properties objects in the resulting map.</p>
     *
     * <p><b>Important:</b> This method mutates the input array by removing the identifier field
     * from each object. This is intentional to avoid duplication of the identifier in both the
     * map key and the value.</p>
     *
     * <p><b>Validation:</b> The method performs comprehensive validation:</p>
     * <ul>
     *   <li>Skips entries where the identifier field is missing, null, or blank</li>
     *   <li>Skips entries that are not ObjectNode instances</li>
     *   <li>Logs warnings for any skipped entries to aid in debugging</li>
     * </ul>
     *
     * <p><b>Performance:</b> The returned map is initialized with the size of the input array
     * to optimize capacity allocation.</p>
     *
     * @param resourcesExternalAdditionalProperties the array of external additional properties to index;
     *                                               will be mutated as identifier fields are removed
     * @return a map of external additional properties keyed by their identifier; may be smaller than
     *         the input array if some entries fail validation
     */
    protected Map<String, Map<String, Object>> mapAdditionalPropertiesById(List<Map<String, Object>> resourcesExternalAdditionalProperties, String identifierFieldName) {
        Map<String, Map<String, Object>> result = new HashMap<>(resourcesExternalAdditionalProperties.size());

        for (Map<String, Object> resourceExternalAdditionalProperties : resourcesExternalAdditionalProperties) {
            Object id = resourceExternalAdditionalProperties.get(identifierFieldName);

            if (id == null || StringUtils.isBlank(id.toString())) {
                Log.warning(Geonet.INDEX_ENGINE,
                    "mergeResourcesExternalAdditionalProperties: missing or blank external identifier in additional properties: "
                        + resourceExternalAdditionalProperties);
                continue;
            }

            // Remove the identifier field from the additional properties and add to the result map
            resourceExternalAdditionalProperties.remove(identifierFieldName);
            result.put(id.toString(), resourceExternalAdditionalProperties);
        }

        return result;
    }

    /**
     * Creates a RestTemplate with authentication headers set.
     *
     * <p>This method checks if a security provider utility is available and uses it to
     * log in as a service account if supported. It then creates a RestTemplate instance.
     * If a valid authentication token is obtained, an interceptor is added to include
     * the Authorization header for authenticated requests.</p>
     *
     * <p><b>Service Account Authentication:</b> This method attempts to login as a service account
     * if the {@link #securityProviderUtil} is available. This is required because the thread
     * executing this code typically does not have an authenticated user context, particularly
     * when called from background or scheduled tasks.</p>
     *
     * <p><b>Token Handling:</b> If service account login succeeds, an SSO authentication token
     * is retrieved and added via an HTTP interceptor to all requests made by the returned
     * RestTemplate.</p>
     *
     * <p><b>Fallback Behavior:</b> If no security provider is configured or token retrieval
     * results in a blank token, an unauthenticated RestTemplate is returned.</p>
     *
     * <p><b>Error Handling:</b> If service account login is required but fails, a RuntimeException
     * is thrown immediately to prevent unauthenticated requests to protected resources.</p>
     *
     * @return a RestTemplate instance configured with authentication headers if a token is available;
     *         returns an unauthenticated RestTemplate if no security provider is available
     * @throws RuntimeException if service account login is attempted but fails
     */
    protected RestTemplate createAuthenticatedRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        if (securityProviderUtil != null) {

            // If the configuration has support for service account then login as the service account.
            // This is required because the thread executing this code does not have an authenticated user.
            if (!securityProviderUtil.loginServiceAccount()){
                throw new RuntimeException("Failed to login as service account. " +
                    "A service account is required to retrieve external additional resource properties.");
            }

            // Get the authentication header value to be used in the requests.
            String token = securityProviderUtil.getSSOAuthenticationHeaderValue();

            // Add the interceptor to set the Authorization header.
            if (StringUtils.isNotBlank(token)) {
                restTemplate.getInterceptors().add((request, body, execution) -> {
                    request.getHeaders().add("Authorization", token);
                    return execution.execute(request, body);
                });
            }
        }

        return restTemplate;
    }
}
